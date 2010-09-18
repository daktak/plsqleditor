/*
 * Created on 22/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import org.eclipse.jface.action.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.*;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 22/02/2005
 */
public class PlSqlActionContributor extends TextEditorActionContributor
{

    protected RetargetTextEditorAction fContentAssistProposal;
    protected RetargetTextEditorAction fContentAssistTip;

    public PlSqlActionContributor()
    {
        fContentAssistProposal = new RetargetTextEditorAction(PlSqlEditorMessages
                .getResourceBundle(), "ContentAssistProposal.");
        fContentAssistProposal
                .setActionDefinitionId("org.eclipse.ui.edit.text.contentAssist.proposals");
        fContentAssistTip = new RetargetTextEditorAction(PlSqlEditorMessages.getResourceBundle(),
                "ContentAssistTip.");
        fContentAssistTip
                .setActionDefinitionId("org.eclipse.ui.edit.text.contentAssist.contextInformation");
    }

    public void init(IActionBars bars)
    {
        super.init(bars);
        IMenuManager menuManager = bars.getMenuManager();
        IMenuManager editMenu = menuManager.findMenuUsingPath("edit");
        if (editMenu != null)
        {
            editMenu.add(new Separator());
            editMenu.add(fContentAssistProposal);
            editMenu.add(fContentAssistTip);
        }
    }

    private void doSetActiveEditor(IEditorPart part)
    {
        super.setActiveEditor(part);
        ITextEditor editor = null;
        if (part instanceof ITextEditor)
        {
            editor = (ITextEditor) part;
        }
        fContentAssistProposal.setAction(getAction(editor, "ContentAssistProposal"));
        fContentAssistTip.setAction(getAction(editor, "ContentAssistTip"));
    }

    public void setActiveEditor(IEditorPart part)
    {
        super.setActiveEditor(part);
        doSetActiveEditor(part);
    }

    public void dispose()
    {
        doSetActiveEditor(null);
        super.dispose();
    }
}
