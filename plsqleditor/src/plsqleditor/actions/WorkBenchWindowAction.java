/**
 * 
 */
package plsqleditor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * @author Toby Zines
 *
 */
public class WorkBenchWindowAction extends AbstractMenuAction
     implements IWorkbenchWindowActionDelegate
 {

     public WorkBenchWindowAction()
     {
         //
     }

     public void dispose()
     {
         myEditor = null;
     }

     public void init(IWorkbenchWindow window)
     {
         setActiveEditor(window);
     }

     public void run(IAction action)
     {
         runEditorAction(action);
     }

     public void selectionChanged(IAction action, ISelection selection)
     {
         IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
         setActiveEditor(window);
     }
 }
