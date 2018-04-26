package plsqleditor.template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariable;

import au.com.zinescom.util.UsefulOperations;

/**
 * This class represents a template context designed to group plsql editor templates together.
 * 
 * @author Toby Zines
 */
public class PlSqlEditContext extends DocumentTemplateContext
{
    /**
     * This constructor creates a new edit context with a type and a location in a document.
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
    protected PlSqlEditContext(TemplateContextType type,
                               IDocument document,
                               int completionOffset,
                               int completionLength)
    {
        super(type, document, completionOffset, completionLength);
    }

    public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException
    {
        //TemplateBuffer buffer = super.evaluate(template);
        if (!canEvaluate(template))
        {
            throw new TemplateException(PlSqlTemplateMessages.Context_error_cannot_evaluate); 
        }
        
        TemplateTranslator translator= new TemplateTranslator() {
            /*
             * @see org.eclipse.jface.text.templates.TemplateTranslator#createVariable(java.lang.String, java.lang.String, int[])
             */
            protected TemplateVariable createVariable(String type, String name, int[] offsets) {
                return new MultiVariable(type, name, offsets);
            }
        };
        TemplateBuffer buffer= translator.translate(template);

        getContextType().resolve(buffer, this);

        String string = buffer.getString();
        // TODO incorporate the formatter in here
        int location = getStart();
        IDocument doc = getDocument();
        int line = doc.getLineOfOffset(location);
        int lineOffset = doc.getLineOffset(line);
        int numSpaces = location - lineOffset;
        String spaces = UsefulOperations.pad("", " ", numSpaces).toString();
        StringBuffer buf = new StringBuffer();
       
        UsefulOperations.insertDataString(buf, spaces, string, false, false); 
        String adjustedString = buf.toString();
        int newlineIndex = adjustedString.indexOf('\n');
        TemplateVariable [] variables = (TemplateVariable[]) buffer.getVariables().clone();
        if (newlineIndex != -1)
        {
            for (int i = 0; i < variables.length; i++)
            {
                TemplateVariable var = variables[i];
                int [] offsets = (int[]) var.getOffsets().clone();
                for (int j = 0; j < offsets.length; j++)
                {
                    if (offsets[j] > newlineIndex)
                    {
                        offsets[j] += numSpaces * countNewlines(string, offsets[j]);
                    }
                }
                var.setOffsets(offsets);
            }
            buffer.setContent(adjustedString, variables);
        }
        return buffer;
    }

    private int countNewlines(String toCount, int index)
    {
        Pattern p = Pattern.compile("\n");
        Matcher m = p.matcher(toCount);
        int location = 0;
        int count = 0;
        while (m.find(location))
        {
            if (m.start() < index)
            {
                count ++;
            }
            location = m.end();
        }
        return count;
    }
}
