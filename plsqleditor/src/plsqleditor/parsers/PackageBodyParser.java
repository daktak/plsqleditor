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
public class PackageBodyParser extends AbstractPlSqlParser
{
    private Segment myCurrentSegment = null;
    
    public PackageBodyParser()
    {
        //
    }

    protected String getStartOfFilePrefix()
    {
        return "[Cc][Rr][Ee][Aa][Tt][Ee] [Oo][Rr] [Rr][Ee][Pp][Ll][Aa][Cc][Ee] [Pp][Aa][Cc][Kk][Aa][Gg][Ee] [Bb][Oo][Dd][Yy] ";
    }

    protected String getParametersTerminator()
    {
        return ".*[AI]S.*";
    }

    protected int parseBody(int currentLineOffset,
                            IDocument document,
                            BufferedReader file,
                            List<Segment> segments,
                            String packageName) throws IOException, BadLocationException
    {
        myCurrentSegment = null;
        String line = null;
        String tmpLine = null;
        boolean isInMethod = false;
        boolean isGrabbingParameters = false;
        boolean isGrabbingTypes = false;
        boolean isGrabbingLocals = false;
        boolean isInComment = false;
        int beginCount = 0;
        boolean isInMethodInComment = false;

        while ((line = file.readLine()) != null)
        {
            currentLineOffset++;
            if (isGrabbingTypes)
            {
                if (!(isGrabbingTypes = grabType(line, myCurrentSegment, segments)))
                {
                    myCurrentSegment = null;
                }
            }
            else if (isGrabbingParameters)
            {
                if (!(isGrabbingParameters = grabParameters(line, myCurrentSegment)))
                {
                    myCurrentSegment = null;
                    isGrabbingLocals = !line.matches(".*BEGIN.*");
                }
            }
            else if (isInMethod)
            {
                if (isStartingComment(line))
                {
                    isInMethodInComment = true;
                }
                else
                {
                    if (isInMethodInComment)
                    {
                        if (isEndingComment(line))
                        {
                            isInMethodInComment = false;
                        }
                    }
                    else
                    {
                        if (isGrabbingLocals)
                        {
                            if (myCurrentSegment == null)
                            {
                                if (line.matches(".*BEGIN.*"))
                                {
                                    isGrabbingLocals = false;
                                    beginCount++;
                                }
                                else if (line.trim().length() > 0)
                                {
                                    int offset = document.getLineOffset(currentLineOffset - 1);
                                    myCurrentSegment = new Segment(line, new Position(offset, line
                                            .length()));
                                    if (!(isGrabbingLocals = grabLocal(line,
                                                                       segments)))
                                    {
                                        myCurrentSegment = null;
                                        beginCount++;
                                    }
                                }
                            }
                            else if (!(isGrabbingLocals = grabLocal(line, segments)))
                            {
                                myCurrentSegment = null;
                                beginCount++;
                            }
                        }
                        else if ((line.matches(".*\\WEND.*") || line.matches("END.*"))
                                && !isEndInComments(line))
                        {
                            beginCount--;
                            if (beginCount <= 0)
                            {
                                isInMethod = false;
                                setLastPosition(currentLineOffset, document, segments);
                            }
                        }
                        else if (line.matches(".*BEGIN.*") || line.matches(".*FOR.*")
                                || line.matches(".*WHILE.*") || line.matches(".*\\WIF.*")
                                || line.matches(".*LOOP.*") || line.matches(".*CASE.*"))
                        {
                            beginCount++;
                        }
                        if (line.matches(".*LANGUAGE JAVA NAME.*"))
                        {
                            isInMethod = false;
                            isGrabbingLocals = false;
                            beginCount = 0;
                            setLastPosition(currentLineOffset, document, segments);
                        }
                    }
                }
            }
            else if (isInComment)
            {
                if (isEndingComment(line))
                {
                    isInComment = false;
                }
            }
            else if (!line.equals(tmpLine = line
                    .replaceFirst(".*(PROCEDURE|FUNCTION)( +)([^( ]+).*", "$1$2$3")))
            {
                int offset = document.getLineOffset(currentLineOffset - 1);
                offset += line.indexOf(tmpLine);
                myCurrentSegment = new Segment(tmpLine, new Position(offset, tmpLine.length()));
                segments.add(myCurrentSegment);
                isInMethod = true;
                isGrabbingParameters = grabParameters(line, myCurrentSegment);
            }
            else if (!line.equals(tmpLine = line
                    .replaceFirst(".*(PROCEDURE|FUNCTION)( +\\w+) +[IA]S.*", "$1$2")))
            {
                int offset = document.getLineOffset(currentLineOffset - 1);
                offset += line.indexOf(tmpLine);
                segments.add(new Segment(tmpLine, new Position(offset, tmpLine.length())));
                isInMethod = true;
            }
            else if (line.matches(".*(PROCEDURE|FUNCTION)( +\\w+) *"))
            {
                int offset = document.getLineOffset(currentLineOffset - 1);
                offset += line.indexOf(line);
                myCurrentSegment = new Segment(line, new Position(offset, line.length()));
                segments.add(myCurrentSegment);
                isGrabbingParameters = true;
                isInMethod = true;
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
                myCurrentSegment = new Segment(line, new Position(offset, line.length()));
                if (!(isGrabbingTypes = grabType(line, myCurrentSegment, segments)))
                {
                    myCurrentSegment = null;
                }
            }
        }
        return currentLineOffset;
    }

    /**
     * This method indicates whether the supplied <code>line</code> contains an END and that END
     * is actually embedded in a comment. If there is no END in the line, it will return true.
     * 
     * @param line The line that may contain an "END" statement, and may have that END statement in
     *            comments.
     * 
     * @return <code>false</code> if there is an END in the line that is not in comments, and
     *         <code>true</code> otherwise.
     */
    private boolean isEndInComments(String line)
    {
        int endIndex = line.indexOf("END");
        if (endIndex == -1)
        {
            return true;
        }
        int commentIndex = line.indexOf("/*");
        if (commentIndex == -1)
        {
            return false;
        }
        if (endIndex < commentIndex)
        {
            return false;
        }
        int endCommentIndex = line.indexOf("*/");
        if (endCommentIndex == -1)
        {
            return true;
        }
        else if (endIndex < endCommentIndex)
        {
            return true;
        }
        else
        {
            return isEndInComments(line.substring(endCommentIndex));
        }
    }

    /**
     * @param line
     * @param myCurrentSegment
     * @param segments
     * @return
     */
    private boolean grabLocal(String line, List<Segment> segments)
    {
        if (line.matches(".*BEGIN.*"))
        {
            return false;
        }
        boolean stillGrabbingType = grabType(line, myCurrentSegment, segments);
        if (!stillGrabbingType)
        {
            Segment local = segments.remove(segments.size() - 1);
            segments.get(segments.size() - 1).addLocalField(local);
            myCurrentSegment = null;
        }
        return true;
    }

    private void setLastPosition(int currentLineOffset, IDocument document, List<Segment> segments)
            throws BadLocationException
    {
        segments.get(segments.size() - 1).setLastPosition(new Position(document
                .getLineOffset(currentLineOffset - 1)));
    }
}
