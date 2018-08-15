package plsqleditor.template;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import plsqleditor.editors.PlSqlEditorMessages;



/**
 * A very simple context type.
 */
public class PlDocContextType extends TemplateContextType
{
    /** This context's id */
    public static final String PLDOC_CONTEXT_TYPE = "plsqleditor.template.pldoc";

    /**
     * Creates a new PL.SQL context type.
     */
    public PlDocContextType()
    {
        addGlobalResolvers();
    }

    private void addGlobalResolvers()
    {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
        addResolver(new PlSqlContextType.CodeTemplateVariableResolver(PlSqlContextType.FILE_NAME, PlSqlEditorMessages
                .getString("PlSqlContextType.variable.description.filename")));
        addResolver(new PlSqlContextType.CurrentPackageResolver());
        addResolver(new PlSqlContextType.CurrentSchemaResolver());
    }

    /**
     * @param document
     * @param offset
     * @param length
     * @return and PlSqlEditContext
     */
    public PlSqlEditContext createContext(IDocument document, int offset, int length)
    {
        return new PlSqlEditContext(this, document, offset, length);
    }

}
