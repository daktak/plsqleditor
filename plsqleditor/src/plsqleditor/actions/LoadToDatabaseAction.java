package plsqleditor.actions;

import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import plsqleditor.parsers.StringLocationMap;

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
        String separator = System.getProperty("line.separator");

        toLoad = modifyCodeToLoad(toLoad, packageName, separator);

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

        SQLErrorDetail[] details = execute(toLoad, packageName, type, schema);
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

    protected SQLErrorDetail[] execute(String toLoad, String packageName, LoadPackageManager.PackageType type, String schema)
    {
        return myLoadPackageManager.loadFile(schema, packageName, toLoad, type);
    }

    protected String modifyCodeToLoad(String toLoad, String packageName, String separator)
    {
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
        return toLoad;
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
