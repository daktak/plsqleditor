package plsqleditor.actions;

import java.sql.SQLException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.LoadPackageManager;
import plsqleditor.db.ResultSetWrapper;
import plsqleditor.editors.PlSqlEditor;
import plsqleditor.parsers.StringLocationMap;
import plsqleditor.util.Util;
import plsqleditor.views.DbmsOutputView;
import plsqleditor.views.ScratchPadView;
import plsqleditor.views.SqlOutputContentProvider;

/**
 * This class represents the action that executes individual sql calls.
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 15/03/2005
 */
public class ExecuteScratchSqlAction extends SelectedTextAction
{
    protected LoadPackageManager theLoadPackageManager = LoadPackageManager.instance();

    public ExecuteScratchSqlAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    @SuppressWarnings("unused")
	public void operateOn(IDocument doc, ITextSelection selection)
    {
        Shell shell = new Shell();

        String schema = PlsqleditorPlugin.getDefault().getCurrentSchema();
        IProject project = PlsqleditorPlugin.getDefault().getProject();

        String toLoad = null;
        try
        {
            if (selection.getLength() == 0)
            {
                toLoad = Util.grabSurroundingSqlBlock(doc, selection.getOffset());
            }
            else
            {
                toLoad = doc.get(selection.getOffset(), selection.getLength());
            }
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }

        toLoad = StringLocationMap.replaceNewLines(toLoad).trim();
        ResultSetWrapper rsw = null;
        try
        {
            rsw = theLoadPackageManager.loadCode(((PlSqlEditor)getTextEditor()).getFile(), project, schema, toLoad);
            DbmsOutputView.getInstance().addMessage(schema);
            if (rsw != null)
            {
                // open the viewer, reset the input.
                try
                {
                    SqlOutputContentProvider.getInstance().updateContent(rsw, toLoad);
                    IWorkbenchPage page = getTextEditor().getSite().getPage();
                    /*IViewPart scratchOutput = */
                    page.showView(ScratchPadView.theId);
                }
                catch (PartInitException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        catch (SQLException e)
        {
            MessageDialog.openInformation(shell, "Toby's PL SQL Editor", e.getMessage());
            if (rsw != null)
            {
                rsw.close();
            }
        }
    }
}
