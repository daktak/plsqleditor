package plsqleditor.template;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import plsqleditor.PlsqleditorPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class TemplateEditorUI {
    /** Key to store custom templates. */
    private static final String CUSTOM_TEMPLATES_KEY = "plsqleditor.template.customtemplates";

    /** The shared instance. */
    private static TemplateEditorUI theInstance;

    /** The template store. */
    private TemplateStore myTemplateStore;
    /** The context type registry. */
    private ContributionContextTypeRegistry myRegistry;

    private TemplateEditorUI() {
	//
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static TemplateEditorUI getDefault() {
	if (theInstance == null) {
	    theInstance = new TemplateEditorUI();
	}
	return theInstance;
    }

    /**
     * Returns this plug-in's template store.
     * 
     * @return the template store of this plug-in instance
     */
    public TemplateStore getTemplateStore() {
	if (myTemplateStore == null) {
	    myTemplateStore = new ContributionTemplateStore(getContextTypeRegistry(),
		    PlsqleditorPlugin.getDefault().getPreferenceStore(), CUSTOM_TEMPLATES_KEY);
	    try {
		myTemplateStore.load();
	    } catch (IOException e) {
		PlsqleditorPlugin.getDefault().getLog()
			.log(new Status(IStatus.ERROR, "plsqleditor.editors.PlSqlEditor", IStatus.OK, "", e));
	    }
	}
	return myTemplateStore;
    }

    /**
     * Returns this plug-in's context type registry.
     * 
     * @return the context type registry for this plug-in instance
     */
    public ContextTypeRegistry getContextTypeRegistry() {
	if (myRegistry == null) {
	    // create an configure the contexts available in the template editor
	    myRegistry = new ContributionContextTypeRegistry();
	    myRegistry.addContextType(PlSqlContextType.PLSQL_CONTEXT_TYPE);
	    myRegistry.addContextType(PlDocContextType.PLDOC_CONTEXT_TYPE);
	}
	return myRegistry;
    }

    /**
     * Creates and returns a new image descriptor for an image file located within
     * the specified plug-in.
     * 
     * @param pluginId      the id of the plug-in containing the image file;
     *                      <code>null</code> is returned if the plug-in does not
     *                      exist
     * 
     * @param imageFilePath the relative path of the image file, relative to the
     *                      root of the plug-in; the path must be legal
     * 
     * @return an image descriptor, or <code>null</code> if no image could be found
     * 
     * @see AbstractUIPlugin#imageDescriptorFromPlugin(java.lang.String,
     *      java.lang.String)
     */
    public static ImageDescriptor imageDescriptorFromPlugin(String pluginId, String imageFilePath) {
	return AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imageFilePath);
    }

    public IPreferenceStore getPreferenceStore() {
	return PlsqleditorPlugin.getDefault().getPreferenceStore();
    }

    @SuppressWarnings("deprecation")
    public void savePluginPreferences() {
	PlsqleditorPlugin.getDefault().savePluginPreferences();
	// InstanceScope.getNode(PlsqleditorPlugin.getDefault().toString()).flush();
    }
}
