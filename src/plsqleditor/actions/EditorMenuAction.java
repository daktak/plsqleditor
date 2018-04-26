/**
 * 
 */
package plsqleditor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 15/03/2005
 * 
 */
public class EditorMenuAction extends AbstractMenuAction implements IEditorActionDelegate
{

    public EditorMenuAction()
    {
        //
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
        if (targetEditor instanceof ITextEditor)
        {
            myEditor = (ITextEditor) targetEditor;
        }
        else
        {
            myEditor = null;
        }
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null)
        {
            IWorkbenchPage page = window.getActivePage();
            if (page != null)
            {
                IEditorPart editor = page.getActiveEditor();
                if (editor != null && (editor instanceof ITextEditor))
                {
                    setActiveEditor(action, editor);
                }
            }
        }
    }

    public void run(IAction action)
    {
        runEditorAction(action);
    }
}
