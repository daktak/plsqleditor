package plsqleditor.popup.actions;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.LoadPackageManager;
import plsqleditor.db.SQLErrorDetail;
import plsqleditor.parsers.StringLocationMap;
import plsqleditor.stores.PackageStore;

public class LoadAction implements IObjectActionDelegate
{

    private IWorkbenchPart     myActivePart;
    protected LoadPackageManager myLoadPackageManager = new LoadPackageManager();

    /**
     * Constructor for Action1.
     */
    public LoadAction()
    {
        super();
    }

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
        myActivePart = targetPart;
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action)
    {
        Shell shell = new Shell();
        if (myActivePart != null)
        {
            try
            {
                IWorkbenchPartSite site = myActivePart.getSite();
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
                                        /* epart = */myActivePart.getSite().getPage()
                                                .openEditor(input,
                                                            "plsqleditor.editors.PlSqlEditor");
                                    }
                                    catch (PartInitException e)
                                    {
                                        e.printStackTrace();
                                        // TODO Auto-generated catch block
                                    }

                                    String name = file.getName();
                                    IDocument doc = PackageStore.getDoc(file);
                                    String toLoad = doc.get();
                                    String packageName =PlsqleditorPlugin.getDefault().getSegments(name, doc).get(0).getName();
                                    String separator = System.getProperty("line.separator");

                                    StringBuffer spacesBuffer = new StringBuffer();
                                    for (int i = 0; i < separator.length(); i++)
                                    {
                                        spacesBuffer.append(' ');
                                    }
                                    String spaces = spacesBuffer.toString();
                                    toLoad = StringLocationMap.replacePlSqlSingleLineComments(toLoad);
                                    toLoad = toLoad.replaceAll(separator, spaces);
                                    String terminator = "[Ee][Nn][Dd] +" + packageName + ";";
                                    int end = toLoad.length();
                                    Pattern p = Pattern.compile(terminator);
                                    Matcher m = p.matcher(toLoad);
                                    while (m.find())
                                    {
                                        end = m.end();
                                    }
                                    toLoad = toLoad.substring(0, end);
                                    
                                    LoadPackageManager.PackageType type = name.contains(".pkb")
                                            ? LoadPackageManager.PackageType.Package_Body
                                            : LoadPackageManager.PackageType.Package;
                                    String schema = PlsqleditorPlugin.getDefault()
                                            .getCurrentSchema();

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
                                            .loadFile(schema, packageName, toLoad, type);
                                    if (details != null && details.length > 0)
                                    {
                                        for (SQLErrorDetail detail : details)
                                        {
                                            addError(file, doc, ("Create or Replace " + type.toString() + packageName + " AS").length() + 1, detail);
                                        }
                                        MessageDialog
                                        .openInformation(shell,
                                                         "Toby's PL SQL Editor",
                                                         "There are errors in the file.");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (RuntimeException e)
            {
                MessageDialog.openInformation(shell, "Toby's PL SQL Editor", "Errors Occurred : "
                        + e);
            }
        }
    }

    protected void addError(IFile file, IDocument doc, int docOffset, SQLErrorDetail detail)
    {
        try
        {
            IMarker marker = file.createMarker(IMarker.PROBLEM);
            if (marker.exists())
            {
                int offset = detail.getColumn() + docOffset;
                int line = doc.getLineOfOffset(offset);
                // add 1 because it works. Can't figure out why
                line += 1;
//                System.out.println(offset
//                        - doc.getLineOffset(line));
                marker.setAttribute(IMarker.MESSAGE, detail
                        .getText());
                marker.setAttribute(IMarker.PRIORITY,
                                    IMarker.PRIORITY_HIGH);
                marker.setAttribute(IMarker.LINE_NUMBER, line);
                int severity = IMarker.SEVERITY_ERROR;
                if (detail.getText().contains("Statement ignored"))
                {
                    severity = IMarker.SEVERITY_INFO;
                }
                marker.setAttribute(IMarker.SEVERITY,
                                    severity);
                // marker.setAttribute(IMarker.CHAR_START,
                // offset);
                // marker.setAttribute(IMarker.CHAR_END, offset
                // + 1);
            }
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
            // TODO Auto-generated catch block
        }
    }

    /**
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        //
    }

    public IWorkbenchPart getActivePart()
    {
        return myActivePart;
    }
}
