package plsqleditor.actions;

import java.sql.SQLException;
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.LoadPackageManager;
import plsqleditor.parsers.StringLocationMap;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id: ExecuteScratchSqlAction.java,v 1.1 2005/03/24 02:12:55 tobyz
 *          Exp $
 * 
 * Created on 15/03/2005
 * 
 */
public class ExecuteScratchSqlAction extends SelectedTextAction
{
    protected LoadPackageManager theLoadPackageManager = LoadPackageManager.instance();

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
        // SQLErrorDetail [] errors = theLoadPackageManager.loadCode(schema,
        // "scratch", toLoad, LoadPackageManager.PackageType.Sql);
        // if (errors != null)
        // {
        // MessageDialog.openInformation(shell,
        // "Toby's PL SQL Editor",
        // errors[0].getText());
        // }
        try
        {
            theLoadPackageManager.loadCode(schema, toLoad);
        }
        catch (SQLException e)
        {
            MessageDialog.openInformation(shell, "Toby's PL SQL Editor", e.getMessage());
        }
    }
}
