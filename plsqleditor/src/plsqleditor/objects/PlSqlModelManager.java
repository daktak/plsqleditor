package plsqleditor.objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import plsqleditor.PlsqleditorPlugin;

public class PlSqlModelManager implements ISaveParticipant
{
    protected static final String DELTA_LISTENER_PERF = null;
    /**
     * Holds the state used for delta processing.
     */
    public DeltaProcessingState deltaState = new DeltaProcessingState();

    public IPlSqlModel getPlSqlModel()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public static PlSqlModelManager getPlSqlModelManager()
    {
        // TODO Auto-generated method stub
        return null;
    }

    private void startup()
    {
        try
        {
            final IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace
                    .addResourceChangeListener(PlsqleditorPlugin.getDefault()
                                                       .getPlSqlModelManager().deltaState,
                                               IResourceChangeEvent.PRE_BUILD
                                                       | IResourceChangeEvent.POST_BUILD
                                                       | IResourceChangeEvent.POST_CHANGE
                                                       | IResourceChangeEvent.PRE_DELETE
                                                       | IResourceChangeEvent.PRE_CLOSE);
            // process deltas since last activated in indexer thread so that
            // indexes
            // are up-to-date.
            // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658
            Job processSavedState = new Job("savedState_jobName")
            {
                protected IStatus run(IProgressMonitor monitor)
                {
                    try
                    {
                        // add save participant and process delta atomically
                        // see
                        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=59937
                        workspace.run(new IWorkspaceRunnable()
                        {
                            public void run(IProgressMonitor progress)
                                    throws CoreException
                            {
                                ISavedState savedState = workspace
                                        .addSaveParticipant(PlsqleditorPlugin
                                                                    .getDefault(),
                                                            PlSqlModelManager.this);
                                if (savedState != null)
                                {
                                    // the event type coming from the saved
                                    // state is
                                    // always POST_AUTO_BUILD
                                    // force it to be POST_CHANGE so that the
                                    // delta
                                    // processor can handle it
                                    PlSqlModelManager.this.deltaState
                                            .getDeltaProcessor().overridenEventType = IResourceChangeEvent.POST_CHANGE;
                                    savedState
                                            .processResourceChangeEvents(PlSqlModelManager.this.deltaState);
                                }
                            }
                        },
                                      monitor);
                    }
                    catch (CoreException e)
                    {
                        return e.getStatus();
                    }
                    return Status.OK_STATUS;
                }
            };
            processSavedState.setSystem(true);
            processSavedState.setPriority(Job.SHORT); // process asap
            processSavedState.schedule();
        }
        catch (RuntimeException e)
        {
            shutdown();
            throw e;
        }
    }

    public void shutdown()
    {
        PlsqleditorPlugin plsqlCore = PlsqleditorPlugin.getDefault();
        plsqlCore.savePluginPreferences();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.removeResourceChangeListener(this.deltaState);
        workspace.removeSaveParticipant(plsqlCore);
        // wait for the initialization job to finish
        try
        {
            Platform.getJobManager().join(PlsqleditorPlugin.theId, null);
        }
        catch (InterruptedException e)
        {
            // ignore
        }
    }

    /**
     * @see ISaveParticipant
     */
    public void saving(ISaveContext context) throws CoreException
    {
        if (context.getKind() == ISaveContext.FULL_SAVE)
        {
            context.needDelta();
        }
        IProject savedProject = context.getProject();
        if (savedProject != null)
        {
            // ??
            return;
        }
    }

    /**
     * @see ISaveParticipant
     */
    public void doneSaving(ISaveContext context)
    {
        // nothing to do for jdt.core
    }

    /**
     * @see ISaveParticipant
     */
    public void prepareToSave(ISaveContext context) /* throws CoreException */
    {
        // nothing to do
    }

    /**
     * @see ISaveParticipant
     */
    public void rollback(ISaveContext context)
    {
        // nothing to do
    }
}
