package plsqleditor.actions;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.LoadPackageManager;
import plsqleditor.db.SQLErrorDetail;

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
public class LoadToDatabaseAction extends TextEditorAction
{
    protected LoadPackageManager myLoadPackageManager = new LoadPackageManager();

    public LoadToDatabaseAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    public void run()
    {
        Shell shell = new Shell();
        ITextEditor editor = getTextEditor();
        IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
        IFile file = input.getFile();
        String name = file.getName();

        IDocument doc = editor.getDocumentProvider().getDocument(input);
        String toLoad = doc.get();
        String packageName = PlsqleditorPlugin.getDefault().getSegments(name, doc).get(0).getName();
        LoadPackageManager.PackageType type = name.contains(".pkb")
                ? LoadPackageManager.PackageType.Package_Body
                : LoadPackageManager.PackageType.Package;
        String schema = PlsqleditorPlugin.getDefault().getCurrentSchema();

        deleteMarkers(file);
        SQLErrorDetail[] details = myLoadPackageManager.execute(schema, packageName, toLoad, type);

        if (details != null && details.length > 0)
        {
            for (SQLErrorDetail detail : details)
            {
                addError(file, doc, ("Create or Replace " + type.toString() + packageName + " AS")
                        .length() + 1, detail);
            }
            MessageDialog.openInformation(shell,
                                          "Toby's PL SQL Editor",
                                          "There are errors in the file.");
        }
    }

    private void deleteMarkers(IFile file)
    {
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
    }

    /**
     * This method adds an error marker to the supplied <code>file</code> based on the information
     * in the supplied <code>detail</code>.
     * 
     * @param file The file to which the error marker will be added.
     * 
     * @param doc The document backing the file.
     * 
     * @param docOffset The additional raw offset to add to the offset found in the
     *        <code>detail</code> if the detail's row is 0 or 1. Otherwise the detail's
     *        column will be used directly.
     *        
     * @param detail The location and mesage of the error.
     */
    protected void addError(IFile file, IDocument doc, int docOffset, SQLErrorDetail detail)
    {
        try
        {
            IMarker marker = file.createMarker(IMarker.PROBLEM);
            if (marker.exists())
            {
                int line = detail.getRow();
                if (line <= 1)
                {
                    int offset = detail.getColumn() + docOffset;
                    line = doc.getLineOfOffset(offset);
                }
                marker.setAttribute(IMarker.MESSAGE, detail.getText());
                marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
                marker.setAttribute(IMarker.LINE_NUMBER, line);
                int severity = IMarker.SEVERITY_ERROR;
                if (detail.getText().contains("Statement ignored"))
                {
                    severity = IMarker.SEVERITY_INFO;
                }
                marker.setAttribute(IMarker.SEVERITY, severity);
            }
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }
}
