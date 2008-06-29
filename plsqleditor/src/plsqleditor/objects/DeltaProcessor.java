package plsqleditor.objects;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.runtime.Platform;
import plsqleditor.Utils;

/**
 * This class is used by <code>JavaModelManager</code> to convert
 * <code>IResourceDelta</code>s into <code>IPlSqlElementDelta</code>s. It
 * also does some processing on the <code>JavaElement</code>s involved (e.g.
 * closing them or updating classpaths).
 */
public class DeltaProcessor
{
    public static final int DEFAULT_CHANGE_EVENT = 0; // must not collide with ElementChangedEvent event masks

    static class RootInfo
    {
        char[][]             inclusionPatterns;
        char[][]             exclusionPatterns;
        IPlSqlProject          project;
        IPath                rootPath;
        int                  entryKind;

        RootInfo(IPlSqlProject project,
                 IPath rootPath,
                 char[][] inclusionPatterns,
                 char[][] exclusionPatterns,
                 int entryKind)
        {
            this.project = project;
            this.rootPath = rootPath;
            this.inclusionPatterns = inclusionPatterns;
            this.exclusionPatterns = exclusionPatterns;
            this.entryKind = entryKind;
        }

        boolean isRootOfProject(IPath path)
        {
            return this.rootPath.equals(path)
                    && this.project.getProject().getFullPath().isPrefixOf(path);
        }

        public String toString()
        {
            StringBuffer buffer = new StringBuffer("project="); //$NON-NLS-1$
            if (this.project == null)
            {
                buffer.append("null"); //$NON-NLS-1$
            }
            else
            {
                buffer.append(this.project.getElementName());
            }
            buffer.append("\npath="); //$NON-NLS-1$
            if (this.rootPath == null)
            {
                buffer.append("null"); //$NON-NLS-1$
            }
            else
            {
                buffer.append(this.rootPath.toString());
            }
            buffer.append("\nincluding="); //$NON-NLS-1$
            if (this.inclusionPatterns == null)
            {
                buffer.append("null"); //$NON-NLS-1$
            }
            else
            {
                for (int i = 0, length = this.inclusionPatterns.length; i < length; i++)
                {
                    buffer.append(new String(this.inclusionPatterns[i]));
                    if (i < length - 1)
                    {
                        buffer.append("|"); //$NON-NLS-1$
                    }
                }
            }
            buffer.append("\nexcluding="); //$NON-NLS-1$
            if (this.exclusionPatterns == null)
            {
                buffer.append("null"); //$NON-NLS-1$
            }
            else
            {
                for (int i = 0, length = this.exclusionPatterns.length; i < length; i++)
                {
                    buffer.append(new String(this.exclusionPatterns[i]));
                    if (i < length - 1)
                    {
                        buffer.append("|"); //$NON-NLS-1$
                    }
                }
            }
            return buffer.toString();
        }
    }
    public static boolean    DEBUG                = false;
    public static boolean    VERBOSE              = false;
    public static boolean    PERF                 = false;

    // with
    // ElementChangedEvent
    // event masks
    /*
     * Answer a combination of the lastModified stamp and the size. Used for
     * detecting external JAR changes
     */
    public static long getTimeStamp(File file)
    {
        return file.lastModified() + file.length();
    }
    /*
     * The global state of delta processing.
     */
    private DeltaProcessingState state;
    /*
     * The Java model manager
     */
    PlSqlModelManager             manager;
    /*
     * The <code>JavaElementDelta</code> corresponding to the <code>IResourceDelta</code>
     * being translated.
     */
    private PlSqlElementDelta     currentDelta;
    
    /*
     * Queue of deltas created explicily by the Java Model that have yet to be
     * fired.
     */
    public ArrayList             myPlSqlModelDeltas      = new ArrayList();

    /*
     * Turns delta firing on/off. By default it is on.
     */
    private boolean              isFiring             = true;
    /*
     * Used to update the JavaModel for <code>IPlSqlElementDelta</code>s.
     */
    private final ModelUpdater   modelUpdater         = new ModelUpdater();

    /*
     * A table from IPlSqlProject to an array of IPackageFragmentRoot. This table
     * contains the pkg fragment roots of the project that are being deleted.
     */
    public Map                   removedRoots;
    /*
     * Type of event that should be processed no matter what the real event type
     * is.
     */
    public int                   overridenEventType   = -1;

    public DeltaProcessor(DeltaProcessingState state, PlSqlModelManager manager)
    {
        this.state = state;
        this.manager = manager;
    }

    private PlSqlElementDelta currentDelta()
    {
        if (this.currentDelta == null)
        {
            this.currentDelta = new PlSqlElementDelta(this.manager
                    .getPlSqlModel());
        }
        return this.currentDelta;
    }

    /*
     * Flushes all deltas without firing them.
     */
    public void flush()
    {
        this.myPlSqlModelDeltas = new ArrayList();
    }

    /*
     * Returns the list of Java projects in the workspace.
     * 
     */
    IPlSqlProject[] getPlSqlProjects()
    {
        try
        {
            return this.manager.getPlSqlModel().getPlSqlProjects();
        }
        catch (PlSqlModelException e)
        {
            // java model doesn't exist
            return new IPlSqlProject[0];
        }
    }

    /*
     * Fire Java Model delta, flushing them after the fact after post_change
     * notification. If the firing mode has been turned off, this has no effect.
     */
    public void fire(IPlSqlElementDelta customDelta, int eventType)
    {
        if (!this.isFiring) return;
        if (DEBUG)
        {
            System.out
                    .println("-----------------------------------------------------------------------------------------------------------------------");//$NON-NLS-1$
        }
        IPlSqlElementDelta deltaToNotify = customDelta;
        if (deltaToNotify == null)
        {
            return;
        }

        // Notification
        // Important: if any listener reacts to notification by updating the
        // listeners list or mask, these lists will
        // be duplicated, so it is necessary to remember original lists in a
        // variable (since field values may change under us)
        IElementChangedListener[] listeners = this.state.elementChangedListeners;
        int[] listenerMask = this.state.elementChangedListenerMasks;
        int listenerCount = this.state.elementChangedListenerCount;
        switch (eventType)
        {
            case DEFAULT_CHANGE_EVENT :
                firePostChangeDelta(deltaToNotify,
                                    listeners,
                                    listenerMask,
                                    listenerCount);
                break;
            case ElementChangedEvent.POST_CHANGE :
                firePostChangeDelta(deltaToNotify,
                                    listeners,
                                    listenerMask,
                                    listenerCount);
                break;
        }
    }

    private void firePostChangeDelta(IPlSqlElementDelta deltaToNotify,
                                     IElementChangedListener[] listeners,
                                     int[] listenerMask,
                                     int listenerCount)
    {
        // post change deltas
        if (DEBUG)
        {
            System.out
                    .println("FIRING POST_CHANGE Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
            System.out.println(deltaToNotify == null
                    ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
        }
        if (deltaToNotify != null)
        {
            // flush now so as to keep listener reactions to post their own
            // deltas for subsequent iteration
            this.flush();
            notifyListeners(deltaToNotify,
                            ElementChangedEvent.POST_CHANGE,
                            listeners,
                            listenerMask,
                            listenerCount);
        }
    }

    private void notifyListeners(IPlSqlElementDelta deltaToNotify,
                                 int eventType,
                                 IElementChangedListener[] listeners,
                                 int[] listenerMask,
                                 int listenerCount)
    {
        final ElementChangedEvent extraEvent = new ElementChangedEvent(
                deltaToNotify, eventType);
        for (int i = 0; i < listenerCount; i++)
        {
            if ((listenerMask[i] & eventType) != 0)
            {
                final IElementChangedListener listener = listeners[i];
                long start = -1;
                if (VERBOSE)
                {
                    System.out
                            .print("Listener #" + (i + 1) + "=" + listener.toString());//$NON-NLS-1$//$NON-NLS-2$
                    start = System.currentTimeMillis();
                }
                // wrap callbacks with Safe runnable for subsequent listeners to
                // be called when some are causing grief
                Platform.run(new ISafeRunnable()
                {
                    public void handleException(Throwable exception)
                    {
                        Utils
                                .log(exception,
                                     "Exception occurred in listener of Java element change notification"); //$NON-NLS-1$
                    }

                    public void run() throws Exception
                    {
                        PerformanceStats stats = null;
                        if (PERF)
                        {
                            stats = PerformanceStats
                                    .getStats(PlSqlModelManager.DELTA_LISTENER_PERF,
                                              listener);
                            stats.startRun();
                        }
                        listener.elementChanged(extraEvent);
                        if (PERF)
                        {
                            stats.endRun();
                        }
                    }
                });
                if (VERBOSE)
                {
                    System.out
                            .println(" -> " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
    }

    /*
     * Registers the given delta with this delta processor.
     */
    public void registerJavaModelDelta(IPlSqlElementDelta delta)
    {
        this.myPlSqlModelDeltas.add(delta);
    }

    /*
     * Notification that some resource changes have happened on the platform,
     * and that the Java Model should update any required internal structures
     * such that its elements remain consistent. Translates <code>IResourceDeltas</code>
     * into <code>IPlSqlElementDeltas</code>.
     * 
     * @see IResourceDelta
     * @see IResource
     */
    public void resourceChanged(IResourceChangeEvent event)
    {
        if (event.getSource() instanceof IWorkspace)
        {
            int eventType = this.overridenEventType == -1
                    ? event.getType()
                    : this.overridenEventType;
            IResource resource = event.getResource();
            IResourceDelta delta = event.getDelta();
            switch (eventType)
            {
                case IResourceChangeEvent.PRE_DELETE :
                    return;
                case IResourceChangeEvent.POST_CHANGE :
                    return;
                case IResourceChangeEvent.PRE_BUILD :
                    return;
                case IResourceChangeEvent.POST_BUILD :
                    return;
            }
        }
    }

    /*
     * Update PlSql Model given some delta
     */
    public void updatePlSqlModel(IPlSqlElementDelta customDelta)
    {
        if (customDelta == null)
        {
            for (int i = 0, length = this.myPlSqlModelDeltas.size(); i < length; i++)
            {
                IPlSqlElementDelta delta = (IPlSqlElementDelta) this.myPlSqlModelDeltas
                        .get(i);
                this.modelUpdater.processPlSqlDelta(delta);
            }
        }
        else
        {
            this.modelUpdater.processPlSqlDelta(customDelta);
        }
    }

}
