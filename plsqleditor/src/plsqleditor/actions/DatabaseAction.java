package plsqleditor.actions;

import java.sql.SQLException;
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;

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
public class DatabaseAction extends TextEditorAction
{
    public static class CommitAction extends DatabaseAction
    {

        public CommitAction(ResourceBundle bundle, String prefix, ITextEditor editor)
        {
            super(bundle, prefix, editor);
        }

        protected void operateOn(String schema) throws SQLException
        {
            DbUtility.commit(schema);
        }
    }

    public static class RollbackAction extends DatabaseAction
    {

        public RollbackAction(ResourceBundle bundle, String prefix, ITextEditor editor)
        {
            super(bundle, prefix, editor);
        }

        protected void operateOn(String schema) throws SQLException
        {
            DbUtility.rollback(schema);
        }
    }

    public DatabaseAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    public void run()
    {
        String schema = PlsqleditorPlugin.getDefault().getCurrentSchema();
        try
        {
            operateOn(schema);
        }
        catch (SQLException e)
        {
            Shell shell = new Shell();
            MessageDialog.openInformation(shell,
                                          "Toby's PL SQL Editor",
                                          "Failed to execute requested functionality: "
                                                  + e.getMessage());
        }
    }

    protected void operateOn(String schema) throws SQLException
    {
        // by default do nothing
    }
}
