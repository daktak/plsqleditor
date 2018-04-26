package plsqleditor.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import plsqleditor.PlsqleditorPlugin;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage </samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class PlDocPreferencePage extends FieldEditorPreferencePage
        implements
            IWorkbenchPreferencePage
{

    public PlDocPreferencePage()
    {
        super(GRID);
        setPreferenceStore(PlsqleditorPlugin.getDefault().getPreferenceStore());
        setDescription("PlDoc Preference Page");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    public void createFieldEditors()
    {
        addField(new DirectoryFieldEditor(PreferenceConstants.P_PLDOC_PATH,
                "Path to PlDoc &Executable", getFieldEditorParent()));
        addField(new FileFieldEditor(PreferenceConstants.P_PLDOC_OVERVIEW, "Overview &Html File",
                getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.P_PLDOC_DOCTITLE, "&Title",
                getFieldEditorParent()));
        addField(new RadioGroupFieldEditor(
                PreferenceConstants.P_PLDOC_OUTPUT_DIR_USE,
                "Use of the Output Directory",
                1,
                new String[][]{
                        {"Absolute (all files under specified directory)",
                                PreferenceConstants.C_OUTPUTDIR_ABSOLUTE},
                        {"Local File System Relative", PreferenceConstants.C_OUTPUTDIR_FS_RELATIVE},
                        {"Project Relative", PreferenceConstants.C_OUTPUTDIR_PROJECT_RELATIVE}},
                getFieldEditorParent(), true));
        addField(new StringFieldEditor(PreferenceConstants.P_PLDOC_OUTPUT_DIRECTORY,
                "Output Directory", getFieldEditorParent()));
        addField(new RadioGroupFieldEditor(PreferenceConstants.P_PLDOC_NAMECASE,
                "Upper or Lower Case", 3, new String[][]{
                        {"Neither", PreferenceConstants.C_NAMECASE_NEITHER},
                        {"Names Uppercase", PreferenceConstants.C_NAMECASE_UPPER},
                        {"Names Lowercase", PreferenceConstants.C_NAMECASE_LOWER}},
                getFieldEditorParent(), true));
        addField(new FileFieldEditor(PreferenceConstants.P_PLDOC_DEFINESFILE, "&Defines File",
                getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.P_PLDOC_EXTRA_PARAMS,
                "E&xtra Parameters", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_PLDOC_EXITONERROR, "Exit on Error",
                getFieldEditorParent()));
    }

    /**
     * This is a hook for sublcasses to do special things when the ok button is
     * pressed. For example reimplement this method if you want to save the
     * page's data into the preference bundle.
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
