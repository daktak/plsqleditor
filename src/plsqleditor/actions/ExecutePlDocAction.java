package plsqleditor.actions;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import plsqleditor.process.PlDocProcessExecutor;

public class ExecutePlDocAction extends TextEditorAction
{
    /**
     * Constructor for the ExecutePlDocAction.
     */
    public ExecutePlDocAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(IAction)
     */
    public void runWithEvent(Event event)
    {
        IFile file = null;
        ITextEditor editor = getTextEditor();
        IFileEditorInput input = (IFileEditorInput) editor.getEditorInput(); 
        file = input.getFile();
        
        PlDocProcessExecutor pldocProc = new PlDocProcessExecutor();
        StringBuffer successBuffer = new StringBuffer();
        StringBuffer failureBuffer = new StringBuffer();
        try
        {
            pldocProc.executePlDoc(new IFile[] {file});
            successBuffer.append(file.getName());
        }
        catch (RuntimeException e)
        {
            failureBuffer.append("\nFailed to generate pldoc for " + file.getName() +
                                 " because of error " + e.getMessage());
        }
        Shell shell = new Shell();
        MessageDialog.openInformation(shell,
                                      "PlDoc",
                                      "PlDoc for the file\n" + successBuffer + 
                                      "\nwas generated." + (failureBuffer.length() > 0 ? 
                                              "There were failures:\n" + failureBuffer.toString() : ""));
    }
}
