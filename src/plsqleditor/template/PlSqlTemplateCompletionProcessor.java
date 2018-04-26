package plsqleditor.template;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;

import plsqleditor.PlsqleditorPlugin;



/**
 * A completion processor for plsql templates.
 */
public class PlSqlTemplateCompletionProcessor extends TemplateCompletionProcessor
{
    private static final String DEFAULT_IMAGE = "icons/at.png";

    /**
     * We watch for angular brackets since those are often part of XML templates.
     */
    protected String extractPrefix(ITextViewer viewer, int offset)
    {
        IDocument document = viewer.getDocument();
        int i = offset;
        if (i > document.getLength())
        {
            return "";
        }

        try
        {
            while (i > 0)
            {
                char ch = document.getChar(i - 1);
                if (!Character.isJavaIdentifierPart(ch))
                {
                    break;
                }
                i--;
            }

            return document.get(i, offset - i);
        }
        catch (BadLocationException e)
        {
            return "";
        }
    }

    /**
     * 
     */
    protected int getRelevance(Template template, String prefix)
    {
        if (template.getName().toUpperCase().startsWith(prefix.toUpperCase()))
        {
            return 90;
        }
        return 0;
    }

    /**
     * Simply return all templates.
     */
    protected Template[] getTemplates(String contextTypeId)
    {
        return TemplateEditorUI.getDefault().getTemplateStore().getTemplates();
    }

    /**
     * Return the XML context type that is supported by this plugin.
     */
    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region)
    {
        return TemplateEditorUI.getDefault().getContextTypeRegistry()
                .getContextType(PlSqlContextType.PLSQL_CONTEXT_TYPE);
    }

    /**
     * Always return the default image.
     */
    protected Image getImage(Template template)
    {
        ImageRegistry registry = PlsqleditorPlugin.getDefault().getImageRegistry();
        Image image = registry.get(DEFAULT_IMAGE);
        if (image == null)
        {
            ImageDescriptor desc = PlsqleditorPlugin
                    .imageDescriptorFromPlugin(PlsqleditorPlugin.theId, DEFAULT_IMAGE); //$NON-NLS-1$
            registry.put(DEFAULT_IMAGE, desc);
            image = registry.get(DEFAULT_IMAGE);
        }
        return image;
    }

}
