package plsqleditor.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

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
public class SelectedTextAction extends TextEditorAction
{
    public SelectedTextAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    public void run()
    {
        ITextEditor editor = getTextEditor();
        IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
        ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
        IDocument doc = editor.getDocumentProvider().getDocument(input);
        operateOn(doc, selection);
    }

    protected void operateOn(IDocument doc, ITextSelection selection)
    {
        // by default do nothing
    }

}
