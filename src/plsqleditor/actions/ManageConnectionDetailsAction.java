package plsqleditor.actions;

import java.util.ResourceBundle;

import org.boomsticks.plsqleditor.dialogs.ManageConnectionDetailsDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.editors.PlSqlEditor;

public class ManageConnectionDetailsAction extends SelectedTextAction
{

    public ManageConnectionDetailsAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    public void operateOn(IDocument doc, ITextSelection selection)
    {
        ITextEditor part = getTextEditor();
        if (part instanceof PlSqlEditor)
        {
            Shell shell = new Shell();

            try
            {
                ManageConnectionDetailsDialog dialog = new ManageConnectionDetailsDialog(shell,
                        "Manage Connection Details", "Add, Modify and Delete Connection Details",null);
                dialog.open();
            }
            catch (RuntimeException e)
            {
                e.printStackTrace();
                MessageDialog.openInformation(shell, "Toby's PL SQL Editor", "Failed to process : " + e.getMessage());
            }
        }
    }
}
