package plsqleditor.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import plsqleditor.PlsqleditorPlugin;

/**
 * This class is used to create a preference page for marking occurrences.
 * Several buttons determine, if generally occurrences of a text will be marked,
 * which types of a text should be marked and if the marks will be kept if the
 * selection changed.
 * 
 * @author Katrin Dust
 * 
 */
public class MarkOccurrencesPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage
{

	/*
	 * Boolean field to determine if occurrences should be highlighted
	 */
	private BooleanFieldEditor markOcc;

	/*
	 * Boolean field to determine if occurrences of comments will be marked
	 */
	private BooleanFieldEditor comment;

	/*
	 * Boolean field to determine if occurrences of literals will be marked
	 */
	private BooleanFieldEditor literal;

	/*
	 * Boolean field to determine if occurrences of documentation will be marked
	 */
	private BooleanFieldEditor documentation;

	/*
	 * Boolean field to determine if occurrences should be kept when selection
	 * changed
	 */
	private BooleanFieldEditor keepMarks;

	/*
	 * Boolean field to determine if occurrences should be marked when there is
	 * no conscious selection.
	 */
	private BooleanFieldEditor selectWordAroundCursor;

	/**
	 * Constructor sets layout (GRID) and the preference store
	 * 
	 */
	public MarkOccurrencesPreferencePage()
	{
		super(GRID);
		setPreferenceStore(PlsqleditorPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * The method creates and adds the fields
	 */
	protected void createFieldEditors()
	{
		// create fields
		this.markOcc = new BooleanFieldEditor(
				MarkOccurrencesPreferences.MARK_OCCURRENCES,
				"Mark occurrences of the selected element in the current file",
				getFieldEditorParent());
		this.comment = new BooleanFieldEditor(
				MarkOccurrencesPreferences.COMMENT, "Comment",
				getFieldEditorParent());
		this.literal = new BooleanFieldEditor(
				MarkOccurrencesPreferences.LITERAL, "String Literal",
				getFieldEditorParent());
		this.documentation = new BooleanFieldEditor(
				MarkOccurrencesPreferences.PL_DOC, "Documentation",
				getFieldEditorParent());
		// this.variable = new BooleanFieldEditor(
		// MarkOccurrencesPreferences.VARIABLE, "Variable",
		// getFieldEditorParent());
		// this.number = new
		// BooleanFieldEditor(MarkOccurrencesPreferences.NUMBER,
		// "Number", getFieldEditorParent());
		// this.operator = new BooleanFieldEditor(
		// MarkOccurrencesPreferences.OPERATOR, "Operator",
		// getFieldEditorParent());
		// this.keyword = new BooleanFieldEditor(
		// MarkOccurrencesPreferences.KEYWORD, "Keyword",
		// getFieldEditorParent());
		this.keepMarks = new BooleanFieldEditor(
				MarkOccurrencesPreferences.KEEP_MARKS,
				"Keep marks when the selection changes", getFieldEditorParent());
		this.selectWordAroundCursor = new BooleanFieldEditor(
				MarkOccurrencesPreferences.SELECT_WORD_AROUND_CURSOR,
				"Automatically select a word around the cursor location",
				getFieldEditorParent());
		addField(markOcc);
		addField(comment);
		addField(literal);
		addField(documentation);
		// addField(variable);
		// addField(number);
		// addField(operator);
		// addField(keyword);
		addField(keepMarks);
		addField(selectWordAroundCursor);
		boolean defaultMarkOcc = PlsqleditorPlugin.getDefault()
				.getPreferenceStore().getBoolean(
						MarkOccurrencesPreferences.MARK_OCCURRENCES);
		this.setButtonsEnabled(defaultMarkOcc);
	}

	/**
	 * Calls the super method to restore default values. Sets buttons enabled or
	 * disabled depending on the default value of the mark occurrence button.
	 */
	protected void performDefaults()
	{
		super.performDefaults();
		boolean defaultMarkOcc = PlsqleditorPlugin.getDefault()
				.getPreferenceStore().getBoolean(
						MarkOccurrencesPreferences.MARK_OCCURRENCES);
		this.setButtonsEnabled(defaultMarkOcc);
	}

	/**
	 * nothing to do
	 */
	public void init(IWorkbench workbench)
	{
	}

	/**
	 * If the property of the mark occurrence changed to false, the other fields
	 * will be disabled (otherwise enabled).
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getSource() != this.markOcc)
		{
			return;
		}
		boolean isMarkOcc = Boolean.valueOf(event.getNewValue().toString())
				.booleanValue();
		setButtonsEnabled(isMarkOcc);
	}

	/**
	 * Enables or disables buttons.
	 * 
	 * @param enabled
	 *            true to enable, false to disable buttons
	 */
	private void setButtonsEnabled(boolean enabled)
	{
		comment.setEnabled(enabled, getFieldEditorParent());
		literal.setEnabled(enabled, getFieldEditorParent());
		documentation.setEnabled(enabled, getFieldEditorParent());
		// variable.setEnabled(enabled, getFieldEditorParent());
		// number.setEnabled(enabled, getFieldEditorParent());
		// operator.setEnabled(enabled, getFieldEditorParent());
		// keyword.setEnabled(enabled, getFieldEditorParent());
		keepMarks.setEnabled(enabled, getFieldEditorParent());
	}
}
