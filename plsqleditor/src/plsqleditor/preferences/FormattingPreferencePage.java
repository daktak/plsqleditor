package plsqleditor.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import plsqleditor.PlsqleditorPlugin;

/**
 * This class represents 
 */
public class FormattingPreferencePage extends FieldEditorPreferencePage
        implements
            IWorkbenchPreferencePage
{

    public FormattingPreferencePage()
    {
        super(GRID);
        setPreferenceStore(PlsqleditorPlugin.getDefault().getPreferenceStore());
        setDescription("PL/SQL Formatting Preferences");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    public void createFieldEditors()
    {
        addField(new BooleanFieldEditor(PreferenceConstants.P_METHOD_SEMI_COLON_AT_END, "Put &Semi colon after proc/func calls",
                                       getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_METHOD_ALIGN_ARROWS, "&Align the assignment arrows in proc/func calls",
                                       getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_METHOD_USE_PARAM_NAMES_NOT_TYPES,
                                        "For procedure and function auto completion, use parameter &Names instead of types", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_FIRST_PARAMETER_ON_NEWLINE,
                                        "For procedure and function auto completion, put the &First parameter on a new line", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_PARAMETERS_ON_NEWLINE,
                                        "For procedure and function auto completion, put &Parameters on a new line", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_COMMA_ON_NEWLINE,
                                        "For procedure and function auto completion, put &Commas on a new line", getFieldEditorParent()));
        // TODO add feature support id 
        addField(new BooleanFieldEditor(PreferenceConstants.P_CONTENT_ASSIST_AUTO_INSERT,
                                        "Auto complete content assist if a &Single option is present", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_CONTENT_ASSIST_AUTO_ACTIVATION,
                                        "Pop up auto complete proposals without prompting by the user (after a delayed time)", getFieldEditorParent()));
        
        addField(new BooleanFieldEditor(PreferenceConstants.P_LOWERCASE_KEYWORDS,
                "Format all keywords to lower case, otherwise all keywords will be upper cased", getFieldEditorParent()));
    }

    /**
     * This is a hook for sublcasses to do special things when the ok button is pressed. For example
     * reimplement this method if you want to save the page's data into the preference bundle.
     */
    public boolean performOk()
    {
        return super.performOk();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
        // do nothing
    }

}
