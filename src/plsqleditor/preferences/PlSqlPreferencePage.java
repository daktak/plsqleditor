package plsqleditor.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import plsqleditor.PlsqleditorPlugin;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage </samp>, we can use the field support built into JFace that allows us to create a
 * page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */

public class PlSqlPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

    public PlSqlPreferencePage()
    {
        super(GRID);
        setPreferenceStore(PlsqleditorPlugin.getDefault().getPreferenceStore());
        setDescription("PL/SQL Plugin preference page");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
     * types of preferences. Each field editor knows how to save and restore itself.
     */
    public void createFieldEditors()
    {
        //addField(new IntegerFieldEditor(PreferenceConstants.P_EDITOR_TAB_WIDTH, "Tab Width", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.P_SCHEMA_PACKAGE_DELIMITER, "Schema to Package Delimiter", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_IS_SHOWING_PARAMETER_LIST,
                "Show Parameter &List in Outline", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_IS_SHOWING_PARAMETER_NAME,
                "Show Parameter &Name in Parameter List", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_IS_SHOWING_PARAMETER_IN_OUT,
                "Show Parameter &In Out Value in Parameter List", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_IS_SHOWING_PARAMETER_TYPE,
                "Show Parameter T&ype in Parameter List", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_IS_SHOWING_RETURN_TYPE,
                "Show &Return Type in Parameter List", getFieldEditorParent()));

        addField(new RadioGroupFieldEditor(PreferenceConstants.P_HDR_GENERATION_USE, "Header Generation Options", 1,
                new String[][]{
                        {"Don't ask to save on header generation", PreferenceConstants.C_HDR_GENERATION_DONT_CHECK},
                        {"Always Save before header generation", PreferenceConstants.C_HDR_GENERATION_ALWAYS_SAVE},
                        {"Prompt for Save before header generation",
                                PreferenceConstants.C_HDR_GENERATION_PROMPT_FOR_SAVE}}, getFieldEditorParent(), true));

        addField(new RadioGroupFieldEditor(
                PreferenceConstants.P_LOAD_TO_DB_USE,
                "Load to Database Options",
                1,
                new String[][]{
                        {"Don't ask to save on loading to Database", PreferenceConstants.C_LOAD_TO_DB_DONT_CHECK},
                        {"Always Save before loading to Database", PreferenceConstants.C_LOAD_TO_DB_ALWAYS_SAVE},
                        {"Prompt for Save before loading to Database", PreferenceConstants.C_LOAD_TO_DB_PROMPT_FOR_SAVE}},
                getFieldEditorParent(), true));

//        addField(new ColorFieldEditor(PreferenceConstants.P_BACKGROUND_COLOUR, "Colour of &Background",
//                getFieldEditorParent()));
//        addField(new ColorFieldEditor(PreferenceConstants.P_KEYWORD_COLOUR, "Colour of &Keywords",
//                getFieldEditorParent()));
//        addField(new ColorFieldEditor(PreferenceConstants.P_OPERATOR_COLOUR, "Colour of &Operators",
//                getFieldEditorParent()));
//        addField(new ColorFieldEditor(PreferenceConstants.P_TYPE_COLOUR, "Colour of &Types", getFieldEditorParent()));
//        addField(new ColorFieldEditor(PreferenceConstants.P_CONSTANT_COLOUR, "Colour of Con&stants",
//                getFieldEditorParent()));
//        addField(new ColorFieldEditor(PreferenceConstants.P_COMMENT_COLOUR, "Colour of Co&mments",
//                getFieldEditorParent()));
//        addField(new ColorFieldEditor(PreferenceConstants.P_JAVADOC_COLOUR, "Colour of Pl&doc", getFieldEditorParent()));
//        addField(new ColorFieldEditor(PreferenceConstants.P_STRING_COLOUR, "Colour of Strin&gs", getFieldEditorParent()));
//        addField(new ColorFieldEditor(PreferenceConstants.P_BUILT_IN_FUNCS_COLOUR, "Colour of Built In &Functions",
//                getFieldEditorParent()));
//        addField(new ColorFieldEditor(PreferenceConstants.P_DEFAULT_CODE_COLOUR, "Colour of Basic &Code Foreground",
//                getFieldEditorParent()));
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
