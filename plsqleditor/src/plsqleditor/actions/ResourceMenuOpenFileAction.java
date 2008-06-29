/**
 * 
 */
package plsqleditor.actions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.editors.PlSqlEditorMessages;

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
public class ResourceMenuOpenFileAction extends AbstractMenuAction implements IObjectActionDelegate
{
    protected List             fSelectedFiles;
    private IWorkbenchPartSite mySite;

    public ResourceMenuOpenFileAction()
    {
        //
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
        if (targetPart != null)
        {
            mySite = targetPart.getSite();
        }
    }

    public void run(IAction action)
    {
        if (fSelectedFiles != null)
        {
            Object [] objs = fSelectedFiles.toArray();
            for (int i = 0;  i < objs.length; i++)
            {
                Object selectedFile = objs[i];
                if (selectedFile != null)
                {
                    if (selectedFile instanceof IFile)
                    {
                        IFile file = (IFile) selectedFile;
                        FileEditorInput input = new FileEditorInput(file);
                        ITextEditor editor = null;
                        editor = openEditor(input);
                        executeOperation(file, action.getId(), editor);
                    }
                }
                else
                {
                    MessageDialog.openError(myShell, "Error", PlSqlEditorMessages
                            .format("error.selectedfile", new String[]{selectedFile.getClass()
                                    .getName()}));
                }
            }

        }
    }

    private ITextEditor openEditor(FileEditorInput input)
    {
        ITextEditor editor = null;
        try
        {
            editor = (ITextEditor) mySite.getPage().openEditor(input, "plsqleditor.editors.PlSqlEditor");
        }
        catch (PartInitException e)
        {
            e.printStackTrace();
        }
        return editor;
    }
    
    public void selectionChanged(IAction iaction, ISelection iselection)
    {
        if (iselection != null && (iselection instanceof IStructuredSelection))
        {
            IStructuredSelection istructuredselection = (IStructuredSelection) iselection;
            if (!istructuredselection.isEmpty())
            {
                fSelectedFiles = istructuredselection.toList();
            }
        }
    }
}
