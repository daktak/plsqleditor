package plsqleditor.template;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * This class represents a template context designed to group pldoc editor templates together.
 * 
 * @author Toby Zines
 */
public class PlDocEditContext extends DocumentTemplateContext
{
    /**
     * This constructor creates a new pldoc edit context with a type and a location in a document.
     * 
     * @param type
     *            The type of the context. At the moment it must be {@link PlSqlContextType}.
     * 
     * @param document
     *            The document that the context relates to.
     * 
     * @param completionOffset
     *            The offset of the context into the <code>document</code>.
     * 
     * @param completionLength
     *            The length of the context from the offset in the <code>document</code>.
     */
    protected PlDocEditContext(TemplateContextType type,
                               IDocument document,
                               int completionOffset,
                               int completionLength)
    {
        super(type, document, completionOffset, completionLength);
    }
}
