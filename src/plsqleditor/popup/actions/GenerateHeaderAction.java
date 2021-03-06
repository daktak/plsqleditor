package plsqleditor.popup.actions;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

public class GenerateHeaderAction implements IObjectActionDelegate
{
    private IWorkbenchPart myWorkBenchPart;

    /**
     * Constructor for Action1.
     */
    public GenerateHeaderAction()
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
                        for (Iterator<?> it = structuredSelection.iterator();it.hasNext();)
                        {
                            Object o = it.next();
                            if (o instanceof IFile)
                            {
                                IFile file = (IFile) o;
                                
                                try
                                {
                                    successBuffer.append(plsqleditor.actions.GenerateHeaderAction.generateHeader(file, myWorkBenchPart.getSite())).append("\n");
                                }
                                catch (IOException e)
                                {
                                    failureBuffer.append("\nFailed to generate header for " + file.getName() +
                                                         " because of i/o error " + e.getMessage());
                                }
                                catch (CoreException e)
                                {
                                    failureBuffer.append("\nFailed to generate header for " + file.getName() +
                                                         " because of core error " + e.getMessage());
                                }
                            }
                        }
                    }
                    Shell shell = new Shell();
                    MessageDialog.openInformation(shell,
                                                  "Header Generation",
                                                  "Headers for the files \n" + successBuffer + 
                                                  "were generated." + (failureBuffer.length() > 0 ? 
                                                          "There were failures:\n" + failureBuffer.toString() : ""));
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
