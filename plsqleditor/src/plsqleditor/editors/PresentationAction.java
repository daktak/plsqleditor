/*
 * Created on 22/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 22/02/2005
 */

public class PresentationAction extends TextEditorAction
{

    public PresentationAction()
    {
        super(PlSqlEditorMessages.getResourceBundle(), "TogglePresentation.", null);
        update();
    }

    public void run()
    {
        ITextEditor editor = getTextEditor();
        editor.resetHighlightRange();
        boolean show = editor.showsHighlightRangeOnly();
        setChecked(!show);
        editor.showHighlightRangeOnly(!show);
    }

    public void update()
    {
        setChecked(getTextEditor() != null && getTextEditor().showsHighlightRangeOnly());
        setEnabled(true);
    }
}
