package plsqleditor.template;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * Looks up existing plsql exceptions and proposes them. The proposals are sorted by their
 * prefix-likeness with the variable type.
 */
public class PlSqlExceptionResolver extends TemplateVariableResolver
{
    /*
     * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolveAll(org.eclipse.jface.text.templates.TemplateContext)
     */
    protected String[] resolveAll(TemplateContext context)
    {
        String[] proposals = new String[]{"NO_DATA_FOUND", "TOO_MANY_ROWS", "OTHERS"};

        Arrays.sort(proposals, new Comparator()
        {

            public int compare(Object o1, Object o2)
            {
                return getCommonPrefixLength(getType(), (String) o2)
                        - getCommonPrefixLength(getType(), (String) o1);
            }

            private int getCommonPrefixLength(String type, String var)
            {
                int i = 0;
                while (i < type.length() && i < var.length())
                {
                    if (Character.toLowerCase(type.charAt(i)) == Character.toLowerCase(var
                            .charAt(i)))
                    {
                        i++;
                    }
                    else
                    {
                        break;
                    }
                }
                return i;
            }
        });

        return proposals;
    }
}
