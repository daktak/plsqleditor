package plsqleditor.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.DbUtility;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage </samp>, we can use the field support built into
 * JFace that allows us to create a page that is small and knows how to save, restore and apply
 * itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 */

public class DbSetupPreferencePage extends FieldEditorPreferencePage
        implements
            IWorkbenchPreferencePage
{

    public DbSetupPreferencePage()
    {
        super(GRID);
        setPreferenceStore(PlsqleditorPlugin.getDefault().getPreferenceStore());
        setDescription("PL/SQL Database Connectivity and DBA User Preference page");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    public void createFieldEditors()
    {
        addField(new StringFieldEditor(PreferenceConstants.P_DRIVER, "Driver class",
                getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.P_URL, "Ur&l", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.P_USER, "&User Name",
                getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.P_PASSWORD, "&Password",
                getFieldEditorParent()));
        addField(new IntegerFieldEditor(PreferenceConstants.P_INIT_CONNS, "&Initial Connections",
                getFieldEditorParent()));
        addField(new IntegerFieldEditor(PreferenceConstants.P_MAX_CONNS, "&Max Connections",
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_USE_LOCAL_CLIENT,
                "Use Local Oracle Client", getFieldEditorParent()));
        addField(new FileFieldEditor(PreferenceConstants.P_SQLPLUS_EXECUTABLE,
                "SQLPlus Client executable", true, getFieldEditorParent()));
    }

    /**
     * This is a hook for sublcasses to do special things when the ok button is pressed. For example
     * reimplement this method if you want to save the page's data into the preference bundle.
     */
    public boolean performOk()
    {
        super.performOk();
        DbUtility.reinit();
        return true;
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
