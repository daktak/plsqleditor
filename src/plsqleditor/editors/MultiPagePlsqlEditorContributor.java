package plsqleditor.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

/**
 * Manages the installation/deinstallation of global actions for multi-page
 * editors. Responsible for the redirection of global actions to the active
 * editor. Multi-page contributor replaces the contributors for the individual
 * editors in the multi-page editor.
 */
public class MultiPagePlsqlEditorContributor extends
		MultiPageEditorActionBarContributor
{
	private static final int DEFAULT_WIDTH_IN_CHARS = 14;

	private IEditorPart activeEditorPart;

	private Map<StatusFieldDef, StatusLineContributionItem> fStatusFields;

	// private Action runPlDocAction;

	// from plsq action contrib
	protected RetargetTextEditorAction fContentAssistProposal;
	protected RetargetTextEditorAction fContentAssistTip;

	private RetargetTextEditorAction fFindNext;

	private IAction fFindPrevious;

	private RetargetTextEditorAction fIncrementalFind;

	private RetargetTextEditorAction fIncrementalFindReverse;

	private RetargetTextEditorAction fGotoLine;

	// end from plsql action contrib

	/**
	 * Status field definition.
	 * 
	 * @since 3.0
	 */
	private static class StatusFieldDef
	{
		private String category;
		private String actionId;
		private boolean visible;
		private int widthInChars;

		private StatusFieldDef(String category, String actionId,
				boolean visible, int widthInChars)
		{
			assert category != null;
			this.category = category;
			this.actionId = actionId;
			this.visible = visible;
			this.widthInChars = widthInChars;
		}
	}

	/**
	 * The status fields to be set to the editor
	 * 
	 * @since 3.0
	 */
	private final static StatusFieldDef[] STATUS_FIELD_DEFS = {
			new StatusFieldDef(
					ITextEditorActionConstants.STATUS_CATEGORY_INPUT_MODE,
					ITextEditorActionDefinitionIds.TOGGLE_OVERWRITE, true,
					DEFAULT_WIDTH_IN_CHARS),
			new StatusFieldDef(
					ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION,
					ITextEditorActionConstants.GOTO_LINE, true,
					DEFAULT_WIDTH_IN_CHARS) };

	/**
	 * Creates a multi-page contributor.
	 */
	public MultiPagePlsqlEditorContributor()
	{
		super();

		fContentAssistProposal = new RetargetTextEditorAction(
				PlSqlEditorMessages.getResourceBundle(),
				"ContentAssistProposal.");
		fContentAssistProposal
				.setActionDefinitionId("org.eclipse.ui.edit.text.contentAssist.proposals");
		fContentAssistTip = new RetargetTextEditorAction(PlSqlEditorMessages
				.getResourceBundle(), "ContentAssistTip.");
		fContentAssistTip
				.setActionDefinitionId("org.eclipse.ui.edit.text.contentAssist.contextInformation");
		// end from plsq action contrib

		fFindNext = new RetargetTextEditorAction(PlSqlEditorMessages
				.getResourceBundle(), "Editor.FindNext."); //$NON-NLS-1$
		fFindNext
				.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_NEXT);
		fFindPrevious = new RetargetTextEditorAction(PlSqlEditorMessages
				.getResourceBundle(), "Editor.FindPrevious."); //$NON-NLS-1$
		fFindPrevious
				.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_PREVIOUS);
		fIncrementalFind = new RetargetTextEditorAction(PlSqlEditorMessages
				.getResourceBundle(), "Editor.FindIncremental."); //$NON-NLS-1$
		fIncrementalFind
				.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_INCREMENTAL);
		fIncrementalFindReverse = new RetargetTextEditorAction(
				PlSqlEditorMessages.getResourceBundle(),
				"Editor.FindIncrementalReverse."); //$NON-NLS-1$
		fIncrementalFindReverse
				.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_INCREMENTAL_REVERSE);
		fGotoLine = new RetargetTextEditorAction(PlSqlEditorMessages
				.getResourceBundle(), "Editor.GotoLine."); //$NON-NLS-1$
		fGotoLine
				.setActionDefinitionId(ITextEditorActionDefinitionIds.LINE_GOTO);

		fStatusFields = new HashMap<StatusFieldDef, StatusLineContributionItem>(
				3);
		for (int i = 0; i < STATUS_FIELD_DEFS.length; i++)
		{
			StatusFieldDef fieldDef = STATUS_FIELD_DEFS[i];
			fStatusFields
					.put(fieldDef, new StatusLineContributionItem(
							fieldDef.category, fieldDef.visible,
							fieldDef.widthInChars));
		}
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
		// IToolBarManager toolBarManager = bars.getToolBarManager();
		// if (toolBarManager != null)
		// {
		// toolBarManager.add(new Separator());
		// toolBarManager.add(stuff here);
		// }
	}

	/**
	 * Returns the action registed with the given text editor.
	 * 
	 * @return IAction or null if editor is null.
	 */
	protected IAction getAction(ITextEditor editor, String actionID)
	{
		return (editor == null ? null : editor.getAction(actionID));
	}

	/*
	 * (non-JavaDoc) Method declared in
	 * AbstractMultiPageEditorActionBarContributor.
	 */

	public void setActivePage(IEditorPart part)
	{
		if (activeEditorPart == part) return;

		activeEditorPart = part;

		ITextEditor editor = (part instanceof ITextEditor) ? (ITextEditor) part
				: null;

		if (editor != null)
		{
			fContentAssistProposal.setAction(getAction(editor,
					"ContentAssistProposal"));
			fContentAssistTip.setAction(getAction(editor, "ContentAssistTip"));
			// fTogglePresentation.setEditor(editor);
			// fTogglePresentation.update();

			if (activeEditorPart instanceof ITextEditorExtension)
			{
				ITextEditorExtension extension = (ITextEditorExtension) activeEditorPart;
				for (int i = 0; i < STATUS_FIELD_DEFS.length; i++)
					extension.setStatusField(null,
							STATUS_FIELD_DEFS[i].category);
			}
		}

		IActionBars actionBars = getActionBars();
		if (actionBars != null)
		{
			actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
					getAction(editor, ITextEditorActionConstants.DELETE));
			actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
					getAction(editor, ITextEditorActionConstants.UNDO));
			actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
					getAction(editor, ITextEditorActionConstants.REDO));
			actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
					getAction(editor, ITextEditorActionConstants.CUT));
			actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
					getAction(editor, ITextEditorActionConstants.COPY));
			actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
					getAction(editor, ITextEditorActionConstants.PASTE));
			actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
					getAction(editor, ITextEditorActionConstants.SELECT_ALL));
			actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
					getAction(editor, ITextEditorActionConstants.FIND));
			actionBars.setGlobalActionHandler(
					IDEActionFactory.BOOKMARK.getId(), getAction(editor,
							IDEActionFactory.BOOKMARK.getId()));
			actionBars.updateActionBars();
		}


		for (int i = 0; i < STATUS_FIELD_DEFS.length; i++)
		{
			if (activeEditorPart instanceof ITextEditorExtension)
			{
				StatusLineContributionItem statusField = (StatusLineContributionItem) fStatusFields
						.get(STATUS_FIELD_DEFS[i]);
				statusField.setActionHandler(getAction(editor,
						STATUS_FIELD_DEFS[i].actionId));
				ITextEditorExtension extension = (ITextEditorExtension) activeEditorPart;
				extension.setStatusField(statusField,
						STATUS_FIELD_DEFS[i].category);
			}
		}
	}

	/*
	 * @see
	 * EditorActionBarContributor#contributeToStatusLine(org.eclipse.jface.action
	 * .IStatusLineManager)
	 * 
	 * @since 2.0
	 */
	public void contributeToStatusLine(IStatusLineManager statusLineManager)
	{
		super.contributeToStatusLine(statusLineManager);
		for (int i = 0; i < STATUS_FIELD_DEFS.length; i++)
			statusLineManager.add((IContributionItem) fStatusFields
					.get(STATUS_FIELD_DEFS[i]));
	}

}
