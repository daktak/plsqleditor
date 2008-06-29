package plsqleditor.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class represents an action that either comments or uncomments a block of code.
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 15/03/2005
 * 
 */
public class CommentBlockAction extends SelectedTextAction
{
    private boolean myIsCommenting;

    public CommentBlockAction(ResourceBundle bundle,
                              String prefix,
                              ITextEditor editor,
                              boolean comment)
    {
        super(bundle, prefix, editor);
        myIsCommenting = comment;
    }

    public void operateOn(IDocument doc, ITextSelection selection)
    {
        try
        {
            int start = selection.getStartLine();
            int end = selection.getEndLine();
            
            for (int i = start; i <= end; i++)
            {
                int offset = doc.getLineOffset(i);
                int length = doc.getLineLength(i);
                String toReplace = doc.get(offset, length);
                boolean startsWithComment = toReplace.trim().startsWith("--");
                if (myIsCommenting && !startsWithComment)
                {
                    doc.replace(offset, length, "--" + toReplace);
                }
                else if (!myIsCommenting && startsWithComment)
                {
                    toReplace = toReplace.replaceFirst("--", "");
                    doc.replace(offset, length, toReplace);
                }
            }
            getTextEditor().getSelectionProvider().setSelection(selection);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }
}
