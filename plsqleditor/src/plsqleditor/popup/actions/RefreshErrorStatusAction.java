package plsqleditor.popup.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.LoadPackageManager;
import plsqleditor.db.SQLErrorDetail;
import plsqleditor.stores.PackageStore;

public class RefreshErrorStatusAction extends LoadAction implements IObjectActionDelegate
{
    /**
     * Constructor for Action1.
     */
    public RefreshErrorStatusAction()
    {
        super();
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(IAction)
     */
    public void run(IAction action)
    {
        Shell shell = new Shell();
        System.out.println("In Run");
        IWorkbenchPart part = getActivePart();
        if (part != null)
        {
            IWorkbenchPartSite site = part.getSite();
            if (site != null)
            {
                ISelectionProvider sp = site.getSelectionProvider();
                if (sp != null)
                {
                    ISelection selection = sp.getSelection();

                    if (selection instanceof IStructuredSelection)
                    {
                        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                        for (Iterator it = structuredSelection.iterator(); it.hasNext();)
                        {
                            Object o = it.next();
                            if (o instanceof IFile)
                            {
                                IFile file = (IFile) o;
                                FileEditorInput input = new FileEditorInput(file);
                                try
                                {
                                    /* epart = */part.getSite().getPage()
                                            .openEditor(input, "plsqleditor.editors.PlSqlEditor");
                                }
                                catch (PartInitException e)
                                {
                                    e.printStackTrace();
                                }

                                String name = file.getName();
                                IDocument doc = PackageStore.getDoc(file);
                                String packageName = PlsqleditorPlugin.getDefault()
                                        .getSegments(name, doc).get(0).getName();

                                LoadPackageManager.PackageType type = name.contains(".pkb")
                                        ? LoadPackageManager.PackageType.Package_Body
                                        : LoadPackageManager.PackageType.Package;
                                String schema = PlsqleditorPlugin.getDefault().getCurrentSchema();

                                // delete all markers in the file
                                int depth = IResource.DEPTH_INFINITE;
                                try
                                {
                                    file.deleteMarkers(IMarker.PROBLEM, true, depth);
                                }
                                catch (CoreException e)
                                {
                                    e.printStackTrace();
                                    // something went wrong
                                }

                                SQLErrorDetail[] details = myLoadPackageManager
                                        .getErrorDetails(schema, packageName, type);
                                if (details != null && details.length > 0)
                                {
                                    for (SQLErrorDetail detail : details)
                                    {
                                        addError(file, doc, 0, detail);
                                    }
                                    MessageDialog.openInformation(shell,
                                                                  "Toby's PL SQL Editor",
                                                                  "There are errors in the file.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
