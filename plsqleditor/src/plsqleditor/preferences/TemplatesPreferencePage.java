package plsqleditor.preferences;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.template.TemplateEditorUI;

/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class TemplatesPreferencePage extends TemplatePreferencePage
        implements
            IWorkbenchPreferencePage
{

    public TemplatesPreferencePage()
    {
        PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
        setPreferenceStore(plugin.getPreferenceStore());
        setTemplateStore(TemplateEditorUI.getDefault().getTemplateStore());
        setContextTypeRegistry(TemplateEditorUI.getDefault().getContextTypeRegistry());
    }

    protected boolean isShowFormatterSetting()
    {
        return false;
    }


    public boolean performOk()
    {
        boolean ok = super.performOk();

        TemplateEditorUI.getDefault().savePluginPreferences();

        return ok;
    }
}
