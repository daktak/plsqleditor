package plsqleditor.objects;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Keep the global states used during Java element delta processing.
 */
public class DeltaProcessingState implements IResourceChangeListener {
    
    /*
     * Collection of listeners for Java element deltas
     */
    public IElementChangedListener[] elementChangedListeners = new IElementChangedListener[5];
    public int[] elementChangedListenerMasks = new int[5];
    public int elementChangedListenerCount = 0;
    
    /*
     * Collection of pre Java resource change listeners
     */
    public IResourceChangeListener[] preResourceChangeListeners = new IResourceChangeListener[1];
    public int preResourceChangeListenerCount = 0;

    /*
     * The delta processor for the current thread.
     */
    private ThreadLocal deltaProcessors = new ThreadLocal();

    /*
     * Need to clone defensively the listener information, in case some listener is reacting to some notification iteration by adding/changing/removing
     * any of the other (for example, if it deregisters itself).
     */
    public void addElementChangedListener(IElementChangedListener listener, int eventMask) {
        for (int i = 0; i < this.elementChangedListenerCount; i++){
            if (this.elementChangedListeners[i].equals(listener)){
                
                // only clone the masks, since we could be in the middle of notifications and one listener decide to change
                // any event mask of another listeners (yet not notified).
                int cloneLength = this.elementChangedListenerMasks.length;
                System.arraycopy(this.elementChangedListenerMasks, 0, this.elementChangedListenerMasks = new int[cloneLength], 0, cloneLength);
                this.elementChangedListenerMasks[i] = eventMask; // could be different
                return;
            }
        }
        // may need to grow, no need to clone, since iterators will have cached original arrays and max boundary and we only add to the end.
        int length;
        if ((length = this.elementChangedListeners.length) == this.elementChangedListenerCount){
            System.arraycopy(this.elementChangedListeners, 0, this.elementChangedListeners = new IElementChangedListener[length*2], 0, length);
            System.arraycopy(this.elementChangedListenerMasks, 0, this.elementChangedListenerMasks = new int[length*2], 0, length);
        }
        this.elementChangedListeners[this.elementChangedListenerCount] = listener;
        this.elementChangedListenerMasks[this.elementChangedListenerCount] = eventMask;
        this.elementChangedListenerCount++;
    }

    public void addPreResourceChangedListener(IResourceChangeListener listener) {
        for (int i = 0; i < this.preResourceChangeListenerCount; i++){
            if (this.preResourceChangeListeners[i].equals(listener)) {
                return;
            }
        }
        // may need to grow, no need to clone, since iterators will have cached original arrays and max boundary and we only add to the end.
        int length;
        if ((length = this.preResourceChangeListeners.length) == this.preResourceChangeListenerCount){
            System.arraycopy(this.preResourceChangeListeners, 0, this.preResourceChangeListeners = new IResourceChangeListener[length*2], 0, length);
        }
        this.preResourceChangeListeners[this.preResourceChangeListenerCount] = listener;
        this.preResourceChangeListenerCount++;
    }

    public DeltaProcessor getDeltaProcessor() {
        DeltaProcessor deltaProcessor = (DeltaProcessor)this.deltaProcessors.get();
        if (deltaProcessor != null) return deltaProcessor;
        deltaProcessor = new DeltaProcessor(this, PlSqlModelManager.getPlSqlModelManager());
        this.deltaProcessors.set(deltaProcessor);
        return deltaProcessor;
    }

    public void removeElementChangedListener(IElementChangedListener listener) {
        
        for (int i = 0; i < this.elementChangedListenerCount; i++){
            
            if (this.elementChangedListeners[i].equals(listener)){
                
                // need to clone defensively since we might be in the middle of listener notifications (#fire)
                int length = this.elementChangedListeners.length;
                IElementChangedListener[] newListeners = new IElementChangedListener[length];
                System.arraycopy(this.elementChangedListeners, 0, newListeners, 0, i);
                int[] newMasks = new int[length];
                System.arraycopy(this.elementChangedListenerMasks, 0, newMasks, 0, i);
                
                // copy trailing listeners
                int trailingLength = this.elementChangedListenerCount - i - 1;
                if (trailingLength > 0){
                    System.arraycopy(this.elementChangedListeners, i+1, newListeners, i, trailingLength);
                    System.arraycopy(this.elementChangedListenerMasks, i+1, newMasks, i, trailingLength);
                }
                
                // update manager listener state (#fire need to iterate over original listeners through a local variable to hold onto
                // the original ones)
                this.elementChangedListeners = newListeners;
                this.elementChangedListenerMasks = newMasks;
                this.elementChangedListenerCount--;
                return;
            }
        }
    }

    public void removePreResourceChangedListener(IResourceChangeListener listener) {
        
        for (int i = 0; i < this.preResourceChangeListenerCount; i++){
            
            if (this.preResourceChangeListeners[i].equals(listener)){
                
                // need to clone defensively since we might be in the middle of listener notifications (#fire)
                int length = this.preResourceChangeListeners.length;
                IResourceChangeListener[] newListeners = new IResourceChangeListener[length];
                System.arraycopy(this.preResourceChangeListeners, 0, newListeners, 0, i);
                
                // copy trailing listeners
                int trailingLength = this.preResourceChangeListenerCount - i - 1;
                if (trailingLength > 0){
                    System.arraycopy(this.preResourceChangeListeners, i+1, newListeners, i, trailingLength);
                }
                
                // update manager listener state (#fire need to iterate over original listeners through a local variable to hold onto
                // the original ones)
                this.preResourceChangeListeners = newListeners;
                this.preResourceChangeListenerCount--;
                return;
            }
        }
    }

    public void resourceChanged(final IResourceChangeEvent event) {
        boolean isPostChange = event.getType() == IResourceChangeEvent.POST_CHANGE;
        if (isPostChange) {
            for (int i = 0; i < this.preResourceChangeListenerCount; i++) {
                // wrap callbacks with Safe runnable for subsequent listeners to be called when some are causing grief
                final IResourceChangeListener listener = this.preResourceChangeListeners[i];
                Platform.run(new ISafeRunnable() {
                    public void handleException(Throwable exception) {
                        log(exception, "Exception occurred in listener of pre PlSql resource change notification"); //$NON-NLS-1$
                    }
                    public void run() throws Exception {
                        listener.resourceChanged(event);
                    }
                });
            }
        }
        try {
            getDeltaProcessor().resourceChanged(event);
        } finally {
            // TODO (jerome) see 47631, may want to get rid of following so as to reuse delta processor ? 
            if (isPostChange) {
                this.deltaProcessors.set(null);
            }
        }

    }

    protected void log(Throwable exception, String string)
    {
        // TODO Auto-generated method stub
        
    }
}
