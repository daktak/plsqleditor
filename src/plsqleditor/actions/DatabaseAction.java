package plsqleditor.actions;

import java.sql.SQLException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
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
 */
public class DatabaseAction extends TextEditorAction
{
    public static class CommitAction extends DatabaseAction
    {
        /**
         * This is a commit action that will execute a commit on a particular open schema
         * connection.
         * 
         * @param bundle
         *            The resource bundle to use to obtain message strings.
         * 
         * @param prefix
         *            dunno
         * 
         * @param editor
         */
        public CommitAction(ResourceBundle bundle, String prefix, ITextEditor editor)
        {
            super(bundle, prefix, editor);
        }

        /**
         * This method just executes the commit.
         * 
         * @param project The project in which the function is currently being executed.
         * 
         * @param schema
         *            The schema to commit.
         */
        protected void execFunc(IProject project, String schema) throws SQLException
        {
            DbUtility.commit(project, schema);
        }

        /**
         * @see DatabaseAction#confirm(Shell, String)
         */
        protected boolean confirm(Shell shell, String schema)
        {
            return MessageDialog
                    .openConfirm(shell,
                                 "Committing " + schema,
                                 "Are you sure you want to commit the schema " + schema);
        }
    }

    public static class RollbackAction extends DatabaseAction
    {

        /**
         * This is a rollback action that will execute a commit on a particular open schema
         * connection.
         * 
         * @param bundle
         *            The resource bundle to use to obtain message strings.
         * 
         * @param prefix
         *            dunno
         * 
         * @param editor
         *            The editor from which this action might be called.
         */
        public RollbackAction(ResourceBundle bundle, String prefix, ITextEditor editor)
        {
            super(bundle, prefix, editor);
        }

        /**
         * This method just executes the rollback.
         * 
         * @param schema
         *            The schema to rollback.
         */
        protected void execFunc(IProject project, String schema) throws SQLException
        {
            DbUtility.rollback(project, schema);
        }

        /**
         * @see DatabaseAction#confirm(Shell, String)
         */
        protected boolean confirm(Shell shell, String schema)
        {
            return MessageDialog.openConfirm(shell,
                                             "Rolling Back " + schema,
                                             "Are you sure you want to Roll Back the schema "
                                                     + schema);
        }
    }

    /**
     * This class represents a basic database action that will perform a simple task on a connection
     * within the database.
     * 
     * @param bundle
     *            The resource bundle to use to obtain message strings.
     * 
     * @param prefix
     *            dunno
     * 
     * @param editor
     *            The editor from which this action might be called.
     */
    public DatabaseAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    /**
     * This is the method that is executed to perform the 'action'.
     */
    public void run()
    {
        String schema = PlsqleditorPlugin.getDefault().getCurrentSchema();
        IProject project = PlsqleditorPlugin.getDefault().getProject();
        try
        {
            operateOn(project, schema);
        }
        catch (SQLException e)
        {
            Shell shell = new Shell();
            MessageDialog.openInformation(shell,
                                          "Toby's PL SQL Editor",
                                          "Failed to execute requested functionality: "
                                                  + e.getMessage());
            shell.dispose();
        }
    }

    /**
     * This method is the base part of the functionality. It causes the operation of the
     * functionality by checking whether the schema is non null, confirming the request, and then
     * executing it.
     * 
     * @param schema
     *            The name of the schema on which the functionality is being executed.
     * 
     * @throws SQLException
     *             When the functionality fails to execute because of database issues.
     */
    protected void operateOn(IProject project, String schema) throws SQLException
    {
        Shell shell = new Shell();
        try
        {
            if (schema != null)
            {
                if (confirm(shell, schema))
                {
                    execFunc(project, schema);
                }
            }
        }
        finally
        {
            shell.dispose();
        }
    }

    /**
     * This method executes the particular functionality of the give database action. By default no
     * functionality is executed.
     * 
     * @param project The project in which the function is being currently executed.
     * 
     * @param schema
     *            The name of the schema on which the functionality is being executed.
     */
    protected void execFunc(IProject project, String schema) throws SQLException
    {
        // do nothing by default
    }

    /**
     * This method should pop up a confirm dialog to ensure that the functionality being requested
     * is definitely desired. By default nothing is done in this super class.
     * 
     * @param shell
     *            The shell to use to execute the popup.
     * 
     * @param schema
     *            The name of the schema on which the functionality is being executed.
     * 
     * @return whether to go ahead
     */
    protected boolean confirm(Shell shell, String schema)
    {
        // do nothing by default
        return false;
    }
}
