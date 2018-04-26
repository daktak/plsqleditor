package plsqleditor.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.popup.actions.PlsqlHeaderGenerator;
import plsqleditor.preferences.PreferenceConstants;

public class GenerateHeaderAction extends TextEditorAction
{
    private static PlsqlHeaderGenerator theHeaderGenerator = new PlsqlHeaderGenerator();

    /**
     * Constructor for Generate Header Action.
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
        ITextEditor editor = getTextEditor();
        IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
        IFile file = input.getFile();
        StringBuffer successBuffer = new StringBuffer();
        StringBuffer failureBuffer = new StringBuffer();
        try
        {
            successBuffer.append(generateHeader(file, editor)).append("\n");
        }
        catch (IOException e)
        {
            failureBuffer.append("\nFailed to generate header for " + file.getName()
                    + " because of i/o error " + e.getMessage());
        }
        catch (CoreException e)
        {
            failureBuffer.append("\nFailed to generate header for " + file.getName()
                    + " because of core error " + e.getMessage());
        }
        Shell shell = new Shell();
        MessageDialog.openInformation(shell, "Header Generation", "Headers for the file \n"
                + successBuffer
                + "was generated."
                + (failureBuffer.length() > 0
                        ? "There were failures:\n" + failureBuffer.toString()
                        : ""));
        shell.dispose();
    }

    /**
     * This method
     * 
     * @param file
     */
    public static String generateHeader(IFile file, ITextEditor editor) throws IOException,
            CoreException
    {
        /*
         * Support for Feature 1430732 - Save before Load to database
         */ 
        String saveType = PlsqleditorPlugin.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.P_HDR_GENERATION_USE);

        if (editor != null)
        {
            if (saveType.equals(PreferenceConstants.C_HDR_GENERATION_ALWAYS_SAVE))
            {
                if (editor.isDirty())
                {
                    editor.doSave(null);
                }
            }
            else if (saveType.equals(PreferenceConstants.C_HDR_GENERATION_PROMPT_FOR_SAVE))
            {
                if (editor.isDirty())
                {
                    Shell shell = new Shell();
                    if (MessageDialog
                            .openQuestion(shell,
                                          "Save before Generating?",
                                          "This file has been modified since last saving - do you wish to save before generating the header?"))
                    {
                        editor.doSave(null);
                    }
                    shell.dispose();
                }
            }
        }
        String filename = generateHeader(file);
        return filename;
    }

	static String generateHeader(IFile file) throws IOException,
			CoreException {
		// fix for bug 1455136 - header generation fails at folder level
        IContainer parent = file.getParent();
        String filename = file.getName();
        int extensionIndex = filename.lastIndexOf(".");
        //String filename = file.getName().replaceFirst("\\.pkb", ".pkh");
        filename = filename.substring(0,extensionIndex) + ".pkh";
        // fix for bug 1455136 - header generation fails at folder level
        IFile pkhFile = parent.getFile(new Path(filename));
        String result = theHeaderGenerator.parseBodyFile(file.getContents());
        InputStream resultingInputStream = new ByteArrayInputStream(result.getBytes());
        if (pkhFile.exists())
        {
            // changed for bug 1387073 - header generation
            pkhFile.setContents(resultingInputStream, true, true, null);
        }
        else
        {
            pkhFile.create(resultingInputStream, true, null);
        }
		return filename;
	}

    public static String generateHeader(IFile file, IWorkbenchSite site) throws IOException,
            CoreException
    {
        ITextEditor editor = findEditor(file, site);
        return generateHeader(file, editor);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPage#findEditor(org.eclipse.ui.IEditorInput)
     * @param file
     * @return
     */
    private static ITextEditor findEditor(IFile file, IWorkbenchSite site)
    {
        ITextEditor editor = null;
        try
        {
            FileEditorInput input = new FileEditorInput(file);
            editor = (ITextEditor) site.getPage().findEditor(input);
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }
        return editor;
    }
}
