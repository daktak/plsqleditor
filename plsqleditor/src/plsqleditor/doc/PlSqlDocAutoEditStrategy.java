/**
 * 
 */
package plsqleditor.doc;

import java.io.IOException;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

import plsqleditor.parsers.ContentOutlineParser;
import plsqleditor.parsers.Segment;

/**
 * @author Toby Zines
 * 
 */
public class PlSqlDocAutoEditStrategy extends DefaultIndentLineAutoEditStrategy
{
    private String               newLine   = System.getProperty("line.separator");
    private ContentOutlineParser theParser = new ContentOutlineParser();

    /**
     * 
     */
    public PlSqlDocAutoEditStrategy()
    {
        super();
    }

    /**
     * This method indicates whether the supplied <code>txt</code> ends with a valid line delimiter for the supplied
     * document <code>d</code>.
     * 
     * @param d
     *            The document from which to obtain valid line delimiters.
     * 
     * @param txt
     *            The text whose end is being checked for end of line
     * 
     * delimiters.
     * 
     * @return <code>true</code> if <code>txt</code> ends with a line delimiter.
     */
    private boolean endsWithDelimiter(IDocument d, String txt)
    {
        String delimiters[] = d.getLegalLineDelimiters();
        if (delimiters != null)
        {
            return TextUtilities.endsWith(delimiters, txt) > -1;
        }
        else
        {
            return false;
        }
    }

    public void customizeDocumentCommand(IDocument d, DocumentCommand c)
    {
        if (c.length == 0 && c.text != null && endsWithDelimiter(d, c.text))
        {
            javaDocAfterNewLine(d, c);
        }
    }

    private boolean javaDocAfterNewLine(IDocument document, DocumentCommand command)
    {
        int textLength = 3;
        int endTextLength = 2;
        int docLength = document.getLength();
        if (command.offset == -1 || docLength == 0 || command.offset < textLength)
        {
            return false;
        }
        try
        {
            String prevText = document.get(command.offset - textLength, textLength);
            if (prevText.equals("/**"))
            {
                int foundIndex = -1;
                for (int searchOffset = command.offset; searchOffset < docLength - endTextLength; searchOffset++)
                {
                    String toTest = document.get(searchOffset, endTextLength);
                    if (toTest.equals("*/"))
                    {
                        foundIndex = searchOffset;
                        break;
                    }
                }
                Segment foundSegment = null;
                // TODO should figure out whether this is a package body, package header or plain sql
                List<Segment> segments = theParser.parseBodySection(ContentOutlineParser.Type.Package_Body,
                                                                    document,
                                                                    command.offset,
                                                                    foundIndex == -1
                                                                            ? docLength - (command.offset + 1)
                                                                            : foundIndex - command.offset,
                                                                    "packageName");
                for (Segment s : segments)
                {
                    if (s.getPosition().getOffset() > command.offset)
                    {
                        foundSegment = s;
                        break;
                    }
                }
                StringBuffer toAppend = new StringBuffer();
                toAppend.append(" *");
                if (foundSegment != null && (foundIndex == -1 || foundSegment.getPosition().getOffset() < foundIndex))
                {
                    // TODO should get the correct indent from the previous line
                    for (Segment parameter : foundSegment.getParameterList())
                    {
                        toAppend.append(" @param ");
                        toAppend.append(parameter.getName());
                        toAppend.append(newLine);
                        toAppend.append(" *");
                    }
                    String returnType = foundSegment.getReturnType();
                    if (returnType.trim().length() > 0)
                    {
                        toAppend.append(" @return ");
                        toAppend.append(returnType);
                        toAppend.append(newLine);
                    }
                    toAppend.append(" */");
                }
                command.text += toAppend.toString();
                return true;
            }
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
