package plsqleditor.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

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
public class ExecuteScratchSqlAction extends SelectedTextAction
{
    protected LoadPackageManager myLoadPackageManager = new LoadPackageManager();

    public ExecuteScratchSqlAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    public void operateOn(IDocument doc, ITextSelection selection)
    {
        Shell shell = new Shell();

        String schema = PlsqleditorPlugin.getDefault().getCurrentSchema();

        String toLoad = null;
        try
        {
            toLoad = doc.get(selection.getOffset(), selection.getLength());
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        toLoad = StringLocationMap.replacePlSqlSingleLineComments(toLoad);
        toLoad = StringLocationMap.replaceNewLines(toLoad);
        SQLErrorDetail [] errors = myLoadPackageManager.loadCode(schema, "scratch", toLoad, LoadPackageManager.PackageType.Sql);
        if (errors != null)
        {
            MessageDialog.openInformation(shell,
                                          "Toby's PL SQL Editor",
                                          errors[0].getText());
        }
    }
}
