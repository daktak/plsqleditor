/*
 * Created on 26/02/2005
 *
 * @version $Id$
 */
package plsqleditor.parsers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 26/02/2005
 */
public abstract class AbstractPlSqlParser
{
    protected static final Pattern inOutPattern = Pattern
                                                        .compile("(\\w+)\\W+(IN OUT)\\W+([\\w\\.]+)([^,]*)\\W*");
    protected static final Pattern inPattern    = Pattern
                                                        .compile("(\\w+)\\W+(IN)\\W+([\\w\\.]+)([^,]*)\\W*");
    protected static final Pattern outPattern   = Pattern
                                                        .compile("(\\w+)\\W+(OUT)\\W+([\\w\\.]+)([^,]*)\\W*");
    protected static final Pattern recPattern   = Pattern
                                                        .compile(".*TYPE +([_\\w]+) +IS +(RECORD[^)]+\\)).*;.*");
    protected static final Pattern tablePattern = Pattern
                                                        .compile(".*TYPE +([_\\w]+) +IS +(TABLE +OF[^;]+);.*");
    protected static final Pattern constPattern = Pattern
                                                        .compile(".*\\s+([_\\w]+)\\s+CONSTANT\\s+([()\\w_\\.\\%\\d]+).*;.*");
    protected static final Pattern fldPattern   = Pattern
                                                        .compile("^[^:]+?([_\\w]+)\\s+([()\\w_\\.\\%\\d]+).*;.*");


    public AbstractPlSqlParser()
    {
        //
    }

    /**
     * This method grabs the details out of the "header details" section of the header pldoc
     * comment. This information is generated into the package header and as such should be
     * available to the body.
     * 
     * @param currentLineOffset The current line location in the document.
     * 
     * @param document The document being parsed.
     * 
     * @param file The reader reading the document. The actual parsing stream.
     * 
     * @param details The list of segments to add to as they are discovered.
     */
    protected int grabHeaderDetails(int currentLineOffset,
                                    IDocument document,
                                    BufferedReader file,
                                    List<Segment> details) throws IOException, BadLocationException
    {
        String line = null;
        boolean isCommenting = false;
        boolean isGrabbingTypes = false;
        Segment currentSegment = null;

        // this represents the internal comments
        // to be converted to javadoc comments
        String internalCommentsId = "--";
        // this represents the last line's starting spaces
        // parse header details here
        while ((line = file.readLine()) != null)
        {
            currentLineOffset++;
            if (line.matches(".*end header details.*"))
            {
                break;
            }

            String detailsLine = line;
            if (isGrabbingTypes)
            {
                if (!(isGrabbingTypes = grabType(line, currentSegment, details)))
                {
                    currentSegment = null;
                }
            }
            else if (isCommenting)
            {
                // find the middle of a comment for a particular file
                if (!(detailsLine.replaceFirst("(\\W*)" + internalCommentsId + "(.*)$", "$1 \\*$2"))
                        .equals(detailsLine))
                {
                    // do nothing, just a comment. detailsLine = tmpLine;
                }
                else
                {
                    // we have found the end of the comment for a particular file
                    // detailsLine = lastSpace + "*/\n" + detailsLine;
                    isCommenting = false;
                    int offset = document.getLineOffset(currentLineOffset - 1);
                    int lineLength = document.getLineLength(currentLineOffset - 1);

                    currentSegment = new Segment(detailsLine, new Position(offset, lineLength));
                    if (!(isGrabbingTypes = grabType(line, currentSegment, details)))
                    {
                        currentSegment = null;
                    }
                }
            }
            else
            {
                // find the start of the commenting for a particular file
                if (!(detailsLine.replaceFirst("(\\W*)" + internalCommentsId + "(.*)$",
                                               "$1/\\*\\*\n$1 \\*$2")).equals(detailsLine))
                {
                    isCommenting = true;
                }
                else
                {
                    if (detailsLine.trim().length() > 0)
                    {
                        int offset = document.getLineOffset(currentLineOffset - 1);
                        int lineLength = document.getLineLength(currentLineOffset - 1);

                        currentSegment = new Segment(detailsLine, new Position(offset, lineLength));
                        if (!(isGrabbingTypes = grabType(line, currentSegment, details)))
                        {
                            currentSegment = null;
                        }
                    }
                }
            }
        }
        return currentLineOffset;
    }

    protected int parseHeader(int currentLineOffset,
                              IDocument document,
                              BufferedReader file,
                              List<Segment> details,
                              String[] packageName) throws IOException, BadLocationException
    {
        boolean isInHeader = false;
        String line = null;
        String tmpLine = null;

        while ((line = file.readLine()) != null)
        {
            currentLineOffset++;
            String prefix = getStartOfFilePrefix();
            if (!(tmpLine = line.replaceFirst(prefix + "(\\w+).*", "$1")).equals(line))
            {
                packageName[0] = tmpLine;
                int offset = document.getLineOffset(currentLineOffset - 1);
                offset += line.indexOf(prefix) + prefix.length();
                details.add(new Segment(tmpLine, new Position(offset, tmpLine.length()),
                        Segment.SegmentType.Package));
            }
            else if (line.matches(".*header details.*"))
            {
                currentLineOffset = grabHeaderDetails(currentLineOffset, document, file, details);
            }
            else if (isInHeader)
            {
                if (isEndingComment(line))
                {
                    break;
                }
            }
            else if (line.matches(".*/\\*.*"))
            {
                isInHeader = true;
            }
        }
        return currentLineOffset;
    }

    protected abstract String getStartOfFilePrefix();

    protected abstract int parseBody(int currentLineOffset,
                                     IDocument document,
                                     BufferedReader file,
                                     List<Segment> segments,
                                     String packageName) throws IOException, BadLocationException;

    public static boolean isEndingComment(String line)
    {
        return line.matches(".*\\*/.*");
    }

    public static boolean isStartingComment(String line)
    {
        return line.matches(".*/\\*.*") && !line.matches(".*\\*/.*");
    }

    /**
     * This method pulls out more of the type details of a function or procedure from a line. format =
     * <p>
     * The format of these fields is:
     * <ul>
     * <li>name [CONSTANT] typewithdots[%type_qualifier] [DEFAULT|:=] [']value['];</li>
     * <li>name type;</li>
     * <li>TYPE name IS RECORD(blah1 type1, blah2 type2);</li>
     * <li>TYPE name IS TABLE OF type INDEX BY type;</li>
     * </ul>
     * </p>
     * 
     * @param line The line being parsed for segment information.
     * 
     * @param currentSegment The segment (type/field/constant) to add the details to.
     * 
     * @param details The details to add to when the (type) segment is completed.
     * 
     * @return <code>false</code> if the details have all been grabbed and <code>true</code>
     *         otherwise.
     */
    boolean grabType(String line, Segment currentSegment, List<Segment> details)
    {
        currentSegment.setReturnType(currentSegment.getReturnType() + " " + line.trim());
        if (line.matches(".*;.*"))
        {
            String fullType = currentSegment.getReturnType();

            Matcher m = null;
            if ((m = recPattern.matcher(fullType)).matches()
                    || (m = tablePattern.matcher(fullType)).matches())
            {
                currentSegment = new Segment(m.group(1), currentSegment.getPosition(),
                        Segment.SegmentType.Type);
                currentSegment.setReturnType(m.group(2));
            }
            else if ((m = constPattern.matcher(fullType)).matches()
                    || (m = fldPattern.matcher(fullType)).matches())
            {
                currentSegment = new Segment(m.group(1), currentSegment.getPosition(),
                        Segment.SegmentType.Field);
                currentSegment.setReturnType(m.group(2));
            }
            details.add(currentSegment);
            return false;
        }
        return true;
    }

    /**
     * This method pulls out more of the parameters of a function or procedure from a line.
     * 
     * @param line The line being parsed for segment information.
     * 
     * @param currentSegment The segment (function/procedure) to add the details to.
     * 
     * @return <code>false</code> if the parameters have all been grabbed and <code>true</code>
     *         otherwise.
     */
    boolean grabParameters(String line, Segment currentSegment)
    {
        Matcher m = inPattern.matcher(line);
        int currentFindLoc = 0;
        m = inOutPattern.matcher(line);
        currentFindLoc = 0;
        while (m.find(currentFindLoc))
        {
            currentFindLoc = grabParam(currentSegment, m);
        }
        m = inPattern.matcher(line);
        currentFindLoc = 0;
        while (m.find(currentFindLoc))
        {
            currentFindLoc = grabParam(currentSegment, m);
        }
        m = outPattern.matcher(line);
        currentFindLoc = 0;
        while (m.find(currentFindLoc))
        {
            currentFindLoc = grabParam(currentSegment, m);
        }

        String tmpLine = null;
        if (!line.equals(tmpLine = line.replaceFirst(".*RETURN\\s+([\\w\\._]+).*", "$1"))
                && currentSegment.getType() == Segment.SegmentType.Function)
        {
            currentSegment.setReturnType(tmpLine);
        }
        if (line.matches(getParametersTerminator()))
        {
            currentSegment = null;
            return false;
        }
        return true;
    }

    protected String getParametersTerminator()
    {
        return ".*[AI]S.*";
    }

    /**
     * This method
     * 
     * @param currentSegment
     * @param m
     */
    int grabParam(Segment currentSegment, Matcher m)
    {
        String paramName = m.group(1);
        String paramInOut = m.group(2);
        String paramType = m.group(3);
        currentSegment.addParameter(paramName, paramInOut, paramType);
        return m.end();
    }

    /**
     * This method
     * 
     * @param br
     * @return The string representation of the whole package header.
     * @throws IOException
     */
    List<Segment> parseBodyReader(IDocument document, BufferedReader br, String[] packageName)
            throws IOException
    {
        List<Segment> segments = new ArrayList<Segment>();
        int currentLineOffset = 0;
        try
        {
            currentLineOffset = parseHeader(currentLineOffset, document, br, segments, packageName);
            currentLineOffset = parseBody(currentLineOffset, document, br, segments, packageName[0]);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        br.close();
        return segments;
    }

    public List<Segment> parseBodyFile(IDocument document, String[] packageName) throws IOException
    {
        String wholeText = document.get();
        ByteArrayInputStream input = new ByteArrayInputStream(wholeText.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        return parseBodyReader(document, br, packageName);
    }
}
