package plsqleditor.actions;

import java.util.List;
import java.util.ResourceBundle;

import org.boomsticks.plsqleditor.dialogs.ChangeSchemaForPackageDialog;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlEditor;
import plsqleditor.parsers.PlSqlParserManager;
import plsqleditor.parsers.Segment;
import plsqleditor.stores.PackageStore;

public class ChangeSchemaForPackageAction extends SelectedTextAction
{

    public ChangeSchemaForPackageAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    public void operateOn(IDocument doc, ITextSelection selection)
    {
        ITextEditor part = getTextEditor();
        if (part instanceof PlSqlEditor)
        {
            FileEditorInput input = (FileEditorInput) part.getEditorInput();
            IFile file = input.getFile();
            Shell shell = new Shell();

            PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
            PackageStore store = plugin.getPackageStore(plugin.getProject());
            List segments = plugin.getCurrentSegments(doc);
            
            String packageName = "";
            if (segments != null && segments.size() > 0)
            {
                packageName = ((Segment) segments.get(0)).getName();
            }
            else
            {
            	packageName = PlSqlParserManager.getPackageName(file);
            	if (packageName == null)
            	{
	                PlsqleditorPlugin.log(("Failed to find packagename for file [" + plugin.getCurrentFileName() + 
	                        "]. Assuming a package name of 'scratch'"), null);
	                packageName = "scratch";
            	}
            }
                
            String schemaName = plugin.getCurrentSchema();
            try
            {
                ChangeSchemaForPackageDialog dialog = new ChangeSchemaForPackageDialog(shell,
                        "Change Schema", "Which schema would you like this to be changed to",
                        packageName, null);
                if (dialog.open() != Window.CANCEL)
                {
                    schemaName = dialog.getSchemaName();
                    packageName = dialog.getPackageName();
                    store.changeSchemaFor(file, doc, schemaName, packageName);
                }
            }
            catch (RuntimeException e)
            {
                e.printStackTrace();
                MessageDialog.openInformation(shell, "Toby's PL SQL Editor", "Failed to process : " + e.getMessage());
            }
        }
    }
}
