package plsqleditor.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import plsqleditor.popup.actions.PlsqlHeaderGenerator;

public class GenerateHeaderAction extends TextEditorAction
{
    private static PlsqlHeaderGenerator theHeaderGenerator = new PlsqlHeaderGenerator();

    /**
     * Constructor for Action1.
     */
    public GenerateHeaderAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(IAction)
     */
    public void runWithEvent(Event event)
    {
        IFile file = null;
        if (event instanceof FileHoldingEvent)
        {
            FileHoldingEvent fhe = (FileHoldingEvent) event;
            file = fhe.getFile();
            try
            {
                fhe.setHeaderName(generateHeader(file));
            }
            catch (IOException e)
            {
                throw new FileHoldingEvent.CatchableRuntimeException(e);
            }
            catch (CoreException e)
            {
                throw new FileHoldingEvent.CatchableRuntimeException(e);
            }
        }
        else
        {
            ITextEditor editor = getTextEditor();
            IFileEditorInput input = (IFileEditorInput) editor.getEditorInput(); 
            file = input.getFile();
            StringBuffer successBuffer = new StringBuffer();
            StringBuffer failureBuffer = new StringBuffer();
            try
            {
                successBuffer.append(generateHeader(file)).append("\n");
            }
            catch (IOException e)
            {
                failureBuffer.append("\nFailed to generate header for " + file.getName() +
                                     " because of i/o error " + e.getMessage());
            }
            catch (CoreException e)
            {
                failureBuffer.append("\nFailed to generate header for " + file.getName() +
                                     " because of core error " + e.getMessage());
            }
            Shell shell = new Shell();
            MessageDialog.openInformation(shell,
                                          "Header Generation",
                                          "Headers for the file \n" + successBuffer + 
                                          "was generated." + (failureBuffer.length() > 0 ? 
                                                  "There were failures:\n" + failureBuffer.toString() : ""));
        }

    }

    /**
     * This method 
     * 
     * @param file
     */
    public static String generateHeader(IFile file) throws IOException, CoreException
    {
        IFolder parent = (IFolder) file.getParent();
        String filename = file.getName().replaceFirst("\\.pkb", ".pkh");
        IFile pkhFile = parent.getFile(filename);
        String result = theHeaderGenerator.parseBodyFile(file.getContents());
        InputStream resultingInputStream = new ByteArrayInputStream(result.getBytes());
        if (pkhFile.exists())
        {
            pkhFile.delete(true, true, null);
        }
        pkhFile.create(resultingInputStream, true, null);
        return filename;
    }
}
