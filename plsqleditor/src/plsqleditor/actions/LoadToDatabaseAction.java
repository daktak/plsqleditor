package plsqleditor.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.TextEditorAction;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.LoadPackageManager;
import plsqleditor.db.PackageType;
import plsqleditor.db.SQLErrorDetail;
import plsqleditor.editors.PlSqlEditor;
import plsqleditor.parsers.ParseType;
import plsqleditor.parsers.PlSqlParserManager;
import plsqleditor.parsers.Segment;
import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.stores.PlSqlType;

/**
 * This class manages the loading of a whole file to a database.
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 15/03/2005
 */
public class LoadToDatabaseAction extends TextEditorAction
{
    protected LoadPackageManager theLoadPackageManager = LoadPackageManager.instance();

    public LoadToDatabaseAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    public void run()
    {
        Shell shell = new Shell();
        ITextEditor part = getTextEditor();
        if (part instanceof PlSqlEditor)
        {
            try
            {
                PlSqlEditor editor = (PlSqlEditor) part;
                FileEditorInput input = (FileEditorInput) part.getEditorInput();
                IFile file = input.getFile();

                checkSave(editor);

                IDocument doc = editor.getDocumentProvider().getDocument(input);
                String toLoad = doc.get();
                String packageName = ((Segment) PlsqleditorPlugin.getDefault().getSegments(file, doc, false).get(0))
                        .getName();
                ParseType type = PlSqlParserManager.getType(file);
                String schema = PlsqleditorPlugin.getDefault().getCurrentSchema();

                deleteMarkers(file);
                SQLErrorDetail[] details = execute(file, toLoad, packageName, type, schema);

                if (details != null && details.length > 0)
                {
                    for (int i = 0; i < details.length; i++)
                    {
                        SQLErrorDetail detail = details[i];
                        addError(file,
                                 doc,
                                 ("Create or Replace " + type.toString() + packageName + " AS").length() + 1,
                                 detail);
                    }
                    MessageDialog.openError(shell, "Toby's PL SQL Editor", "There are errors in the file.");
                }
                else
                {
                    // Fix for bug 1419192 - Feedback after successful "Load to
                    // Database"
                    // fix for bug 1436209 - the "refresh error status" prints wrong msg
                    MessageDialog.openInformation(shell, "Toby's PL SQL Editor", getNoErrorMsg());
                }
            }
            catch (RuntimeException e)
            {
                // fix for bug 1436209 - the "refresh error status" prints wrong msg
                MessageDialog.openError(shell, "Toby's PL SQL Editor", getRuntimeErrorMsg(e));
            }
        }
    }

    protected String getRuntimeErrorMsg(RuntimeException e)
    {
        return "There was a failure while trying to compile: " + e;
    }

    protected String getNoErrorMsg()
    {
        return "The file has been successfully compiled.";
    }

    /**
     * @param editor
     */
    private void checkSave(PlSqlEditor editor)
    {
        /*
         * Support for Feature 1430732 - Save before Load to database
         */
        String saveType = PlsqleditorPlugin.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.P_LOAD_TO_DB_USE);

        if (editor != null)
        {
            if (saveType.equals(PreferenceConstants.C_LOAD_TO_DB_ALWAYS_SAVE))
            {
                if (editor.isDirty())
                {
                    editor.doSave(null);
                }
            }
            else if (saveType.equals(PreferenceConstants.C_LOAD_TO_DB_PROMPT_FOR_SAVE))
            {
                if (editor.isDirty())
                {
                    Shell shell = new Shell();
                    if (MessageDialog
                            .openQuestion(shell,
                                          "Save before Loading?",
                                          "This file has been modified since last saving - do you wish to save before loading to the database?"))
                    {
                        editor.doSave(null);
                    }
                    shell.dispose();
                }
            }
        }
    }

    /**
     * @param toLoad
     * @param packageName
     * @param type
     * @param schema
     * @return The error details from the call.
     */
    protected SQLErrorDetail[] execute(IFile file, String toLoad, String packageName, ParseType type, String schema)
    {
        IProject project = PlsqleditorPlugin.getDefault().getProject();
        
        if (type == ParseType.SqlScript)
        {
            
            return new SQLErrorDetail[0];
        }
        else
        {
            PackageType packageType = null;
            if (type == ParseType.Package_Body)
        	{
            	packageType = PackageType.Package_Body;
        	}
            else if (type == ParseType.Package)
            {
            	packageType = PackageType.Package;
            }
            else if (type == ParseType.Package_Header_And_Body)
            {
            	packageType = PackageType.Package_Header_And_Body;
            }
            else
            {
            	packageType = PackageType.Sql;
            }
    
            SQLErrorDetail[] details = theLoadPackageManager.execute(file, project, schema, packageName, toLoad, packageType);
            return details;
        }
    }

    /**
     * This method deletes the markers from the supplied <code>file</code>.
     * 
     * @param file The file whose markers will all be deleted.
     */
    private void deleteMarkers(IFile file)
    {
        // delete ALL markers in the file
        int depth = IResource.DEPTH_INFINITE;
        try
        {
            file.deleteMarkers(IMarker.PROBLEM, true, depth);
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method adds an error marker to the supplied <code>file</code> based on the information in the supplied
     * <code>detail</code>.
     * 
     * @param file The file to which the error marker will be added.
     * 
     * @param doc The document backing the file.
     * 
     * @param docOffset The additional raw offset to add to the offset found in the <code>detail</code> if the
     *            detail's row is 0 or 1. Otherwise the detail's column will be used directly.
     * 
     * @param detail The location and mesage of the error.
     */
    protected void addError(IFile file, IDocument doc, int docOffset, SQLErrorDetail detail)
    {
        try
        {
            int line = detail.getRow();
            if (line <= 1)
            {
                int offset = detail.getColumn() + docOffset;
                line = doc.getLineOfOffset(offset);
            }
            Map attributes = new HashMap();

            MarkerUtilities.setLineNumber(attributes, line);
            attributes.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_HIGH));
            MarkerUtilities.setMessage(attributes, detail.getText());
            int severity = IMarker.SEVERITY_ERROR;
            if (detail.getText().indexOf("Statement ignored") != -1)
            {
                severity = IMarker.SEVERITY_INFO;
            }
            attributes.put(IMarker.SEVERITY, new Integer(severity));
            MarkerUtilities.createMarker(file, attributes, IMarker.PROBLEM);
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
