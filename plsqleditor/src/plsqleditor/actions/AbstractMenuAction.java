package plsqleditor.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
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
public abstract class AbstractMenuAction implements IActionDelegate
{
    protected Shell       myShell;
    protected ITextEditor myEditor;

    public AbstractMenuAction()
    {
        myShell = new Shell();
    }

    protected void runEditorAction(IAction action)
    {
        if (myEditor == null)
        {
            return;
        }
        IEditorInput editorInput = myEditor.getEditorInput();
        if (editorInput instanceof IFileEditorInput)
        {
            IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
            IFile ifile = fileEditorInput.getFile();

            executeOperation(ifile, action.getId(), myEditor);
        }
    }

    protected String executeOperation(IFile file, String actionId, ITextEditor textEditor)
    {
        IAction action = textEditor.getAction(actionId);
        if (action != null)
        {
            action.run();
        }
        return "";
    }

    protected void setActiveEditor(IWorkbenchWindow window)
    {
        if (window != null)
        {
            IWorkbenchPage page = window.getActivePage();
            if (page != null)
            {
                IEditorPart editor = page.getActiveEditor();
                if (editor != null && (editor instanceof ITextEditor))
                {
                    if (editor instanceof ITextEditor)
                    {
                        myEditor = (ITextEditor) editor;
                    }
                }
                else
                {
                    myEditor = null;
                }
            }
        }
    }
}
