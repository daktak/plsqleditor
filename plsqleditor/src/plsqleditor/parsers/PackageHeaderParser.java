/*
 * Created on 26/02/2005
 *
 * @version $Id$
 */
package plsqleditor.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

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
public class PackageHeaderParser extends AbstractPlSqlParser
{
    public PackageHeaderParser()
    {
        //
    }

    protected String getStartOfFilePrefix()
    {
        return "[Cc][Rr][Ee][Aa][Tt][Ee] [Oo][Rr] [Rr][Ee][Pp][Ll][Aa][Cc][Ee] [Pp][Aa][Cc][Kk][Aa][Gg][Ee] ";
    }

    protected String getParametersTerminator()
    {
        return ".*;.*";
    }

    /**
     * This method parses a section of the text from the <code>document</code> starting from the next line out of the
     * supplied <code>file</code>. It assumes that the next line read from here is the
     * <code>currentLineOffset + 1</code>. It adds the discovered segments to the list of <code>segments</code>.
     * The section of text being parsed is a body section. This method assumes that the body part may contain
     * documentation and implementation of procedures, functions, types and fields.
     * 
     * @param currentLineOffset
     *            The index of the line from the beginning of the <code>document</code> that the last line of text
     *            obtained from the <code>file</code> corresponds to. The next call to
     *            {@link BufferedReader#readLine()} will read the line located in the <code>document</code> at
     *            <code>currentLineOffset + 1</code>.
     * 
     * @param document
     *            The document from which the <code>file</code> was obtained.
     * 
     * @param file
     *            The buffered reader from which we are parsing the text.
     * 
     * @param segments
     *            The list of segments to which the next parsed tokens will be added.
     * 
     * @param packageName
     *            The name of the package.
     */
    protected int parseBody(int currentLineOffset,
                            IDocument document,
                            BufferedReader file,
                            List<Segment> segments,
                            String packageName) throws IOException, BadLocationException
    {
        String line = null;
        String tmpLine = null;
        boolean isGrabbingParameters = false;
        boolean isGrabbingTypes = false;
        boolean isInComment = false;
        Segment currentSegment = null;

        while ((line = file.readLine()) != null)
        {
            currentLineOffset++;
            if (isGrabbingTypes)
            {
                if (!(isGrabbingTypes = grabType(line, currentSegment, segments)))
                {
                    currentSegment = null;
                }
            }
            else if (isGrabbingParameters)
            {
                if (!(isGrabbingParameters = grabParameters(line, currentSegment)))
                {
                    currentSegment = null;
                }
            }
            else if (isInComment)
            {
                if (isEndingComment(line))
                {
                    isInComment = false;
                }
            }
            else if (!line.equals(tmpLine = line.replaceFirst(".*(PROCEDURE|FUNCTION)( +)([^( ]+).*", "$1$2$3")))
            {
                int offset = document.getLineOffset(currentLineOffset - 1);
                offset += line.indexOf(tmpLine);
                currentSegment = new Segment(tmpLine, new Position(offset, tmpLine.length()));
                segments.add(currentSegment);
                isGrabbingParameters = grabParameters(line, currentSegment);
            }
            else if (!line.equals(tmpLine = line.replaceFirst(".*(PROCEDURE|FUNCTION)( +\\w+) *;.*", "$1$2")))
            {
                int offset = document.getLineOffset(currentLineOffset - 1);
                offset += line.indexOf(tmpLine);
                segments.add(new Segment(tmpLine, new Position(offset, tmpLine.length())));
            }
            else if (line.matches(".*(PROCEDURE|FUNCTION)( +\\w+) *"))
            {
                int offset = document.getLineOffset(currentLineOffset - 1);
                offset += line.indexOf(line);
                currentSegment = new Segment(line, new Position(offset, line.length()));
                segments.add(currentSegment);
                isGrabbingParameters = true;
            }
            else if (line.matches(".*--.*"))
            {
                //
            }
            else if (isStartingComment(line))
            {
                isInComment = true;
            }
            else if (line.matches(".*END " + packageName + ".*"))
            {
                break;
            }
            else if (line.trim().length() > 0)
            {
                int offset = document.getLineOffset(currentLineOffset - 1);
                currentSegment = new Segment(line, new Position(offset, line.length()));
                if (!(isGrabbingTypes = grabType(line, currentSegment, segments)))
                {
                    currentSegment = null;
                }
            }
        }
        return currentLineOffset;
    }
}
