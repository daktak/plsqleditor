/**
 * 
 */
package plsqleditor.doc;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

import plsqleditor.parsers.ContentOutlineParser;
import plsqleditor.parsers.ParseType;
import plsqleditor.parsers.PlSqlParserManager;
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
     * This method indicates whether the supplied <code>txt</code> ends with a valid line
     * delimiter for the supplied document <code>d</code>.
     * 
     * @param d
     *            The document from which to obtain valid line delimiters.
     * 
     * @param txt
     *            The myOutputText whose end is being checked for end of line
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
        try
        {
//            if (c.length == 0 && c.offset > 3 && c.text != null && c.text.equals("/"))
//            {
//                String prevText = d.get(c.offset - 2, 2);
//                if (prevText.equals("* "))
//                {
//                    c.offset -= 2;
//                    c.length = 3;
//                    c.text = "*/";
//                }
//            }
//            else 
                if (c.length == 0 && c.text != null && endsWithDelimiter(d, c.text))
            {
                int line = d.getLineOfOffset(c.offset);
                int start = d.getLineOffset(line);
                int length = c.offset - start;
                String str = d.get(start, length);

                int starIndex = str.indexOf('*');
                StringBuffer sb = new StringBuffer();
                appendSpaces(starIndex, sb);
                String preSpaces = sb.toString();

                if (!javaDocAfterNewLine(d, c, preSpaces))
                {
                    addCommentString(d, c, preSpaces);
                }
            }
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @param d
     * @param c
     */
    private void addCommentString(IDocument d, DocumentCommand c, String preSpaces)
    {
        c.text += preSpaces + "* ";
    }

    private boolean javaDocAfterNewLine(IDocument document,
                                        DocumentCommand command,
                                        String preSpaces)
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
                // TODO should figure out whether this is a package body, package header or plain
                // sql
                List<Segment> segments = theParser.parseBodySection(ParseType.Package_Body,
                                                           document,
                                                           command.offset,
                                                           foundIndex == -1
                                                                   ? docLength
                                                                           - (command.offset + 1)
                                                                   : foundIndex - command.offset,
                                                           null);
                foundSegment = PlSqlParserManager.instance().findNextMethod(segments, command.offset);
                StringBuffer toAppend = new StringBuffer();
                if (foundSegment != null)
                {
                    if ((foundIndex == -1 || foundSegment.getPosition().getOffset() < foundIndex))
                    {
                        toAppend.append(preSpaces).append("*");
                        for (Iterator<?> it = foundSegment.getParameterList().iterator(); it.hasNext();)
                        {
                            Segment parameter = (Segment) it.next();
                            toAppend.append(" @param ");
                            toAppend.append(parameter.getName());
                            toAppend.append(newLine);
                            toAppend.append(preSpaces).append("*");
                        }
                        String returnType = foundSegment.getReturnType();
                        if (returnType.trim().length() > 0)
                        {
                            toAppend.append(" @return ");
                            toAppend.append(returnType);
                            toAppend.append(newLine);
                            toAppend.append(preSpaces).append("*/");
                        }
                        else
                        {
                            toAppend.append("/");
                        }
                    }
                    command.text += toAppend.toString();
                    return toAppend.length() > 0;
                }
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

    private void appendSpaces(int numSpaces, StringBuffer buf)
    {
        for (int i = 0; i < numSpaces; i++)
        {
            buf.append(' ');
        }
    }
}
