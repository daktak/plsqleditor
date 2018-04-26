package plsqleditor.template;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlEditorMessages;
import plsqleditor.parsers.Segment;



/**
 * A very simple context type.
 */
public class PlSqlContextType extends TemplateContextType
{

    public static class CodeTemplateVariableResolver extends TemplateVariableResolver
    {

        protected String resolve(TemplateContext context)
        {
            return context.getVariable(getType());
        }

        public CodeTemplateVariableResolver(String type, String description)
        {
            super(type, description);
        }
    }

    public static class CurrentSchemaResolver extends TemplateVariableResolver
    {
        public CurrentSchemaResolver()
        {
            super("currentschema", PlSqlEditorMessages.getString("plsql.resolvers.currentschema.description"));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateContext)
         */
        protected String resolve(TemplateContext context)
        {
            return PlsqleditorPlugin.getDefault().getCurrentSchema();
        }
    }

    public static class CurrentPackageResolver extends TemplateVariableResolver
    {
        public CurrentPackageResolver()
        {
            super("currentpackage", PlSqlEditorMessages.getString("plsql.resolvers.currentpackage.description"));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateContext)
         */
        protected String resolve(TemplateContext context)
        {
            List list = PlsqleditorPlugin.getDefault().getCurrentSegments(null);
            if (list != null && list.size() > 0)
            {
                Segment s = (Segment) list.get(0);
                return s.getName();
            }
            return "unknown_package";
        }
    }


    /** This context's id */
    public static final String PLSQL_CONTEXT_TYPE = "plsqleditor.template.plsql";
    public static final String FILE_NAME          = "file_name";

    /**
     * Creates a new PL.SQL context type.
     */
    public PlSqlContextType()
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
        addResolver(new CodeTemplateVariableResolver(FILE_NAME, PlSqlEditorMessages
                .getString("PlSqlContextType.variable.description.filename")));
        addResolver(new CurrentPackageResolver());
        addResolver(new CurrentSchemaResolver());
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
