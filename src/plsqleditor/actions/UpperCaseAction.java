package plsqleditor.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;

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
public class UpperCaseAction extends SelectedTextAction
{
    public UpperCaseAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    public void operateOn(IDocument doc, ITextSelection selection)
    {
        String toLowerCase = null;
        try
        {
            toLowerCase = doc.get(selection.getOffset(), selection.getLength());
            doc.replace(selection.getOffset(), selection.getLength(), toLowerCase.toUpperCase());
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }
}
