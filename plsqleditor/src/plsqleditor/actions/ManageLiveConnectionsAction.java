package plsqleditor.actions;

import org.boomsticks.plsqleditor.dialogs.ManageConnectionDetailsDialog;
import org.boomsticks.plsqleditor.dialogs.ManageOpenConnectionsDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ManageLiveConnectionsAction implements IWorkbenchWindowActionDelegate
{
    private IWorkbenchWindow window;

    public ManageLiveConnectionsAction()
    {
        // do nothing
    }

    /**
     * Selection in the workbench has been changed. We can change the state of
     * the 'real' action here if we want, but this can only happen after the
     * delegate has been created.
     * 
     * @see IWorkbenchWindowActionDelegate#selectionChanged
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        // do nothing
    }

    /**
     * We can use this method to dispose of any system resources we previously
     * allocated.
     * 
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose()
    {
        // do nothing
    }

    /**
     * We will cache window object in order to be able to provide parent shell
     * for the message dialog.
     * 
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow window)
    {
        this.window = window;
    }

    public void run(IAction action)
    {
        Shell shell = window.getShell();
        try
        {
            ManageOpenConnectionsDialog dialog = new ManageOpenConnectionsDialog(shell,
                    "Manage Live Connections", "View and Disconnect Live Connections", null);
            dialog.open();
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            MessageDialog.openInformation(shell, "Toby's PL SQL Editor", "Failed to process : "
                    + e.getMessage());
        }
    }
}
