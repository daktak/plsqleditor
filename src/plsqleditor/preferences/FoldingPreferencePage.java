package plsqleditor.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import plsqleditor.PlsqleditorPlugin;


/**
 *Folding preferences page
 */
public class FoldingPreferencePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage
{
    //~ Constructors

    public FoldingPreferencePage()
    {
        super(GRID);
        setPreferenceStore(PlsqleditorPlugin.getDefault().getPreferenceStore());
    }

    //~ Methods

    /*
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
        // empty impl
    }

    /*
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors()
    {
        addField(new BooleanFieldEditor(PreferenceConstants.SOURCE_FOLDING,
                "Source Folding", getFieldEditorParent()));

//        addField(new BooleanFieldEditor(PreferenceConstants.PLDOC_FOLDING,
//                "Initially Fold PL Documentation", getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.METHOD_FOLDING,
                "Initially Fold Procedures and Functions", getFieldEditorParent()));
    }
}
