package plsqleditor.popup.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

import plsqleditor.process.PlDocProcessExecutor;

public class ExecutePlDocAction implements IObjectActionDelegate
{
    private IWorkbenchPart myWorkBenchPart;

    /**
     * Constructor for ExecutePlDocAction.
     */
    public ExecutePlDocAction()
    {
        super();
    }

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
        myWorkBenchPart = targetPart;
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(IAction)
     */
    public void run(IAction action)
    {
        if (myWorkBenchPart != null)
        {
            IWorkbenchPartSite site = myWorkBenchPart.getSite();
            if (site != null)
            {
                ISelectionProvider sp = site.getSelectionProvider();
                if (sp != null)
                {
                    ISelection selection = sp.getSelection();

                    StringBuffer successBuffer = new StringBuffer();
                    StringBuffer failureBuffer = new StringBuffer();
                    if (selection instanceof IStructuredSelection)
                    {
                        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                        List<IFile> filelist = new ArrayList<IFile>();
                        for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();)
                        {
                            Object o = it.next();
                            if (o instanceof IFile)
                            {
                                filelist.add((IFile)o);
                            }
                        }
                        IFile[] files = (IFile[]) filelist.toArray(new IFile[filelist.size()]);
                        try
                        {
                            PlDocProcessExecutor pldocProc = new PlDocProcessExecutor();
                            pldocProc.executePlDoc(files);
                        }
                        catch (RuntimeException e)
                        {
                            failureBuffer.append("\nFailed to generate pldoc for " + files
                                    + " because of error " + e.getMessage());
                        }
                        Shell shell = new Shell();
                        MessageDialog.openInformation(shell, "PlDoc", "PlDoc for the file \n"
                                + successBuffer
                                + "was generated."
                                + (failureBuffer.length() > 0 ? "There were failures:\n"
                                        + failureBuffer.toString() : ""));
                    }
                }
            }
        }
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        // do nothing for the moment
    }
}
