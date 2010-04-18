package plsqleditor.actions;

import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;
import plsqleditor.db.LoadPackageManager;

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
public class ShowDebugInfoAction extends SelectedTextAction
{
    protected LoadPackageManager theLoadPackageManager = LoadPackageManager.instance();

    public ShowDebugInfoAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    public void operateOn(IDocument doc, ITextSelection selection)
    {
        Shell shell = new Shell();

        PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
        
        String schema = plugin.getCurrentSchema();
        String currentFile = plugin.getCurrentFileName();
        List<String> openConnections = DbUtility.getCurrentConnectionPoolList();
        
        StringBuffer openConnectionsSb = new StringBuffer();
        for (String openConn : openConnections)
        {
            openConnectionsSb.append("\n    ");
            openConnectionsSb.append(openConn);
        }
        
        String details = "Debug Information:" +
            "\nCurrent Schema   = " + schema +
            "\nCurrent File     = " + currentFile +
            "\nConnection Pools = " + openConnectionsSb;
            
        MessageDialog.openInformation(shell, "Toby's PL SQL Editor", details);
    }
}
