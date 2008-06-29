package plsqleditor.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import au.com.zinescom.util.UsefulOperations;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 *          Created on 26/02/2005
 */
public class PackageBodyParser extends AbstractPlSqlParser
{
    final String beginStr = "[Bb][Ee][Gg][Ii][Nn]";
    final String forStr   = "[Ff][Oo][Rr]";
    final String whileStr = "[Ww][Hh][Ii][Ll][Ee]";
    final String ifStr    = "[Ii][Ff]";
    final String loopStr  = "[Ll][Oo][Oo][Pp]";
    final String caseStr  = "[Cc][Aa][Ss][Ee]";
    final String endStr   = "[Ee][Nn][Dd]";

    public PackageBodyParser()
    {
        //
    }

    protected String getStartOfFilePrefix()
    {
        return "\\W*[Cc][Rr][Ee][Aa][Tt][Ee] [Oo][Rr] [Rr][Ee][Pp][Ll][Aa][Cc][Ee] [Pp][Aa][Cc][Kk][Aa][Gg][Ee] [Bb][Oo][Dd][Yy] ";
    }

    protected String getParametersTerminator()
    {
        return "([AaIi][Ss])";
    }


    protected int parseBody(final int currentLineOffset,
                            IDocument document,
                            BufferedReader file,
                            List segments,
                            Segment packageSegment) throws IOException, BadLocationException
    {
        StringBuffer comments = new StringBuffer();
        boolean isInComment = false;
        boolean isGrabbingTypes = false;

        String packageName;
        if (packageSegment != null)
        {
            packageName = packageSegment.getName();
        }
        else
        {
            packageName = "unknownPackage";
        }

        List segmentsToAddTo = segments;
        if (packageSegment != null)
        {
            segmentsToAddTo = packageSegment.getContainedSegments();
        }

        String line = null;
        // this is the segment we are currently working on
        Segment[] currentSegments = new Segment[1];
        currentSegments[0] = null;
        // this is the current line offset
        int[] currentLineOffsets = new int[]{currentLineOffset};

        while ((line = file.readLine()) != null)
        {
            currentLineOffsets[0]++;
            int offset = document.getLineOffset(currentLineOffsets[0] - 1);

            if (isGrabbingTypes)
            {
                // looking for types inside a local, or inside a field or type,
                // or procedure or function
                isGrabbingTypes = grabType(line, currentSegments[0], segmentsToAddTo);
            }
            else if (isInComment)
            {
                comments.append(line).append(newLine);
                if (isEndingComment(line))
                {
                    isInComment = false;
                }
            }
            else if (checkForMethod(document,
                                    file,
                                    line,
                                    offset,
                                    currentLineOffsets,
                                    comments,
                                    currentSegments,
                                    segmentsToAddTo,
                                    null))
            {
                // all done internally
            }
            else if (line.matches(".*--.*"))
            {
                addCode(segmentsToAddTo, currentLineOffsets[0], line);
            }
            else if (isSingleLineClosedComment(line)) // should check for
            // quoted --
            {
                addCommentsToList(segmentsToAddTo, comments, offset);
                comments = new StringBuffer(line);
                comments.append(newLine);
            }
            else if (isStartingComment(line))
            {
                addCommentsToList(segmentsToAddTo, comments, offset);
                comments = new StringBuffer(line);
                comments.append(newLine);
                isInComment = true;
            }
            // fix for 1489860 - not happy about this one, but oh well 
            else if (line.toUpperCase().matches(".*" + endStr + "\\s+" + packageName.toUpperCase() + ".*"))
            {
                addCode(segmentsToAddTo, offset, line);
                break;
            }
            else if (UsefulOperations.matchesWord(line, beginStr))
            {
                Segment seg = parseInitialiser(document,
                                               file,
                                               line,
                                               offset,
                                               currentLineOffsets,
                                               comments);
                if (seg != null)
                {
                    segmentsToAddTo.add(seg);
                }
            }
            else if (line.trim().length() > 0)
            {
                currentSegments[0] = new Segment(line, new Position(offset, line.length()));
                currentSegments[0].setDocumentation(comments.toString());
                comments = new StringBuffer();
                isGrabbingTypes = grabType(line, currentSegments[0], segmentsToAddTo);
            }
            else
            {
                addCode(segmentsToAddTo, currentLineOffsets[0], line);
            }
        }
        return currentLineOffsets[0];
    }

    /**
     * This method parses an initialiser block in the code. This is a block that
     * just has a BEGIN to start and an END to finish, with no declaration.
     * There should be only one of these in any body file.
     * 
     * added to fix bug 1438918 - Package initialization code block is parsed
     * incorrectly
     * 
     * @param document The document being read (used for line and offset
     *            information).
     * @param file The buffered reader that is being used to retrieve the lines
     *            of the <code>document</code>.
     * @param line The currently parsed line.
     * @param offset The current offset (in the <code>document</code>) of the
     *            currently parsed line
     * @param currentLineOffsets The offset (array of length one) that each
     *            recursive call updates so that the current line number is
     *            known at all levels of recursion.
     * @param comments The current comments pertaining to the next constructed
     *            segment.
     * 
     * @return The initialiser segment
     * 
     * @throws BadLocationException
     * @throws IOException
     */
    private Segment parseInitialiser(IDocument document,
                                     BufferedReader file,
                                     String line,
                                     int offset,
                                     int[] currentLineOffsets,
                                     StringBuffer comments) throws BadLocationException,
            IOException
    {
        int beginCount = 0;
        boolean isInMethodInComment = false;
        int[] hasForOrWhileOnPreviousLine = new int[]{0};

        String tmpLine = line.replaceAll(".*(" + beginStr + ").*", "$1");
        offset += line.indexOf(tmpLine);
        Segment currentMethod = new Segment("FUNCTION Initialiser", new Position(offset, tmpLine
                .length()));
        currentMethod.setDocumentation(comments.toString());
        currentMethod.addLine(line, new Position(offset, line.length()));
        currentMethod.setReturnType("Initialisation Block");

        // inside the IS/AS ... END procName; section
        while ((line = file.readLine()) != null)
        {
            currentLineOffsets[0]++;
            offset = document.getLineOffset(currentLineOffsets[0] - 1);

            if (isStartingComment(line) && !isStringInQuotes(line, ".*(/\\*).*"))
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
                    currentMethod.addLine(line, new Position(offset, line.length()));
                    if (UsefulOperations.matchesWord(line, endStr) && !isEndInComments(line)
                            && !isEndInQuotes(line))
                    {
                        if (!isStartPriorToEndPresentInLine(line))
                        {
                            beginCount--;
                            hasForOrWhileOnPreviousLine[0] = 0;
                            if (beginCount <= 0)
                            {
                                setLastPosition(currentLineOffsets[0], document, currentMethod);
                                break;
                            }
                        }
                    }
                    else if (lineContainsBlockOpener(line, hasForOrWhileOnPreviousLine))
                    {
                        beginCount++;
                    }
                }
            }
        }
        return currentMethod;
    }

    /**
     * This method returns true when there is a single line start and end block,
     * such as CASE WHEN blah THEN blah END
     * 
     * @param line The line to check
     * 
     * @return <code>true</code> when there is a start and end block on the
     *         same line, and the start occurs before the end, and
     *         <code>false</code> otherwise.
     */
    private boolean isStartPriorToEndPresentInLine(String line)
    {
        if (UsefulOperations.matchesWord(line, caseStr))
        {
            return (line.matches(".*?" + caseStr + "\\W.*?[Ww][Hh][Ee][Nn]\\W.*" + endStr + "\\W.*") && !line
                    .matches(".*\\W" + endStr + "\\W.*" + caseStr + "\\W.*"));
        }
        
        if (UsefulOperations.matchesWord(line, ifStr))
        {
            return (line.matches(".*" + ifStr + "\\W.*" + endStr + "\\W+" + ifStr + "\\W+.*"));
        }
        return false;
    }

    /**
     * This method checks whether the current line is the first in a series that
     * will lead to a function or procedure declaration, and if it is, then
     * processes that method declaration until completion, returning the latest
     * segment in the <code>currentSegments</code> array.
     * 
     * @param document The document being read (used for line and offset
     *            information).
     * @param file The buffered reader that is being used to retrieve the lines
     *            of the <code>document</code>.
     * @param line The currently parsed line.
     * @param offset The current offset (in the <code>document</code>) of the
     *            currently parsed line
     * @param currentLineOffsets The offset (array of length one) that each
     *            recursive call updates so that the current line number is
     *            known at all levels of recursion.
     * @param comments The current comments pertaining to the next constructed
     *            segment.
     * @param currentSegments An array of length one containing a returnable
     *            current segment.
     * @param segmentsToAddTo The list of segments to add any new additions to.
     * 
     * @return <code>true</code> if the line was the beginning of a method,
     *         and <code>false</code> otherwise.
     * 
     * @throws BadLocationException
     * @throws IOException
     */
    private boolean checkForMethod(IDocument document,
                                   BufferedReader file,
                                   String line,
                                   int offset,
                                   int[] currentLineOffsets,
                                   StringBuffer comments,
                                   Segment[] currentSegments,
                                   List segmentsToAddTo,
                                   Segment containingSegment) throws BadLocationException,
            IOException
    {
        String tmpLine = null;

        if (!line.equals(tmpLine = line.replaceFirst("^\\s*(" + PROCEDURE + "|" + FUNCTION
                + ")( +[\\w_]+) +[IiAa][Ss]\\W*", "$1$2")))
        {
            processMethod(true,
                          false,
                          false,
                          document,
                          file,
                          line,
                          tmpLine,
                          offset,
                          currentLineOffsets,
                          comments,
                          currentSegments,
                          segmentsToAddTo,
                          containingSegment);
            return true;
        }
        else if (!line.equals(tmpLine = line.replaceFirst("^\\s*(" + PROCEDURE + "|" + FUNCTION
                + ")( +)([^( ]+).*", "$1$2$3")))
        {
            currentSegments[0] = null;
            processMethod(false,
                          true,
                          true,
                          document,
                          file,
                          line,
                          tmpLine,
                          offset,
                          currentLineOffsets,
                          comments,
                          currentSegments,
                          segmentsToAddTo,
                          containingSegment);
            return true;
        }
        else if (line.matches("^\\s*(" + PROCEDURE + "|" + FUNCTION + ")( +[\\w_]+) *"))
        {
            currentSegments[0] = null;
            processMethod(false,
                          true,
                          false,
                          document,
                          file,
                          line,
                          tmpLine,
                          offset,
                          currentLineOffsets,
                          comments,
                          currentSegments,
                          segmentsToAddTo,
                          containingSegment);
            return true;
        }
        return false;
    }

    /**
     * This method processes a function or procedure, storing it appropriately.
     * 
     * @param isGrabbingParameters
     * @param grabExtraParameters
     * @param document The document being read (used for line and offset
     *            information).
     * @param file The buffered reader that is being used to retrieve the lines
     *            of the <code>document</code>.
     * @param line The currently parsed line.
     * @param tmpLine The line containing the name of the method and pertinent
     *            details obtained from <b>within</b> the full
     *            <code>line</code>.
     * @param offset The current offset (in the <code>document</code>) of the
     *            currently parsed line
     * @param currentLineOffsets The offset (array of length one) that each
     *            recursive call updates so that the current line number is
     *            known at all levels of recursion.
     * @param comments The current comments pertaining to the next constructed
     *            segment.
     * @param currentSegments An array of length one containing a returnable
     *            current segment.
     * @param segmentsToAddTo The list of segments to add any new additions to.
     * 
     * @param containingSegment The segment that contains the segmentsToAddTo.
     *            When this is not null, any discovered segments are added as
     *            local fields to this.
     * 
     * @throws BadLocationException
     * @throws IOException
     */
    private void processMethod(boolean isGrabbingLocals,
                               boolean isGrabbingParameters,
                               boolean grabExtraParameters,
                               IDocument document,
                               BufferedReader file,
                               String line,
                               String tmpLine,
                               int offset,
                               int[] currentLineOffsets,
                               StringBuffer comments,
                               Segment[] currentSegments,
                               List segmentsToAddTo,
                               Segment containingSegment) throws BadLocationException, IOException
    {
        boolean isGrabbingTypes = false;
        int beginCount = 0;
        boolean isInMethodInComment = false;
        boolean isReadyForNextLocal[] = new boolean[]{true};

        offset += line.indexOf(tmpLine);
        Segment currentMethod = new Segment(tmpLine, new Position(offset, tmpLine.length()));
        if (currentSegments[0] == null)
        {
            currentSegments[0] = currentMethod;
        }
        if (containingSegment == null)
        {
            segmentsToAddTo.add(currentMethod);
        }
        else
        {
            containingSegment.addLocalField(currentMethod);
        }
        currentMethod.setDocumentation(comments.toString());
        comments.delete(0, comments.length());

        if (isGrabbingParameters)
        {
            isGrabbingParameters = grabParameters(line, currentSegments[0], offset);
            if (!isGrabbingParameters)
            {
                isGrabbingLocals = true;
            }
        }

        // address FOR \nLOOP and WHILE \nLOOP syntax
        // Bug Id: 1358534, 1415118
        int[] hasForOrWhileOnPreviousLine = new int[]{0};

        while ((line = file.readLine()) != null)
        {
            offset = document.getLineOffset(currentLineOffsets[0]);
            currentLineOffsets[0]++;
            if (isGrabbingTypes)
            {
                // looking for types inside a local, or inside a field or type,
                // or procedure or function
                isGrabbingTypes = grabType(line, currentSegments[0], segmentsToAddTo);
            }
            else if (isGrabbingParameters)
            {
                // looking for parameters inside a function/procedure
                if (!(isGrabbingParameters = grabParameters(line, currentSegments[0], offset)))
                {
                    // finished parameters, now onto locals
                    isGrabbingLocals = !UsefulOperations.matchesWord(line, beginStr);
                    if (!isGrabbingLocals)
                    {
                        int beginIndex = line.toUpperCase().indexOf("BEGIN");
                        String toAdd = line.substring(beginIndex);
                        currentMethod.addLine(toAdd, new Position(offset + beginIndex, toAdd
                                .length()));
                    }
                }
            }
            else if (checkForMethod(document,
                                    file,
                                    line,
                                    offset,
                                    currentLineOffsets,
                                    comments,
                                    currentSegments,
                                    segmentsToAddTo,
                                    currentMethod))
            {
                // all done internally
            }
            else
            {
                currentMethod.addLine(line, new Position(offset, line.length()));
                // inside the IS/AS ... END procName; section
                if (isStartingComment(line) && !isStringInQuotes(line, ".*(/\\*).*"))
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
                            // looking for locals in the method
                            if (isReadyForNextLocal[0])
                            {
                                // last local is complete and stored
                                if (UsefulOperations.matchesWord(line, beginStr))
                                {
                                    // finished local grabbing
                                    isGrabbingLocals = false;
                                    beginCount++;
                                }
                                else if (line.trim().length() > 0)
                                {
                                    currentSegments[0] = new Segment(line, new Position(offset,
                                            line.length()));
                                    if (!(isGrabbingLocals = grabLocal(line,
                                                                       segmentsToAddTo,
                                                                       currentSegments[0],
                                                                       isReadyForNextLocal)))
                                    {
                                        beginCount++;
                                    }
                                }
                            }
                            else if (!(isGrabbingLocals = grabLocal(line,
                                                                    segmentsToAddTo,
                                                                    currentSegments[0],
                                                                    isReadyForNextLocal)))
                            {
                                beginCount++;
                            }
                        }
                        else if (UsefulOperations.matchesWord(line, endStr)
                                && !isEndInComments(line) && !isEndInQuotes(line))
                        {
                            if (!isStartPriorToEndPresentInLine(line))
                            {
                                beginCount--;
                                // reset "FOR \nLOOP" checker (Bug Id: 1358534,
                                // 1415118)
                                hasForOrWhileOnPreviousLine[0] = 0;
                                if (beginCount <= 0)
                                {
                                    setLastPosition(currentLineOffsets[0], document, currentMethod);
                                    currentMethod = null;
                                    return;
                                }
                            }
                        }
                        else if (lineContainsBlockOpener(line, hasForOrWhileOnPreviousLine))
                        {
                            beginCount++;
                        }
                        if (line
                                .matches(".*[Ll][Aa][Nn][Gg][Uu][Aa][Gg][Ee] +[Jj][Aa][Vv][Aa] +[Nn][Aa][Mm][Ee].*"))
                        {
                            isGrabbingLocals = false;
                            beginCount = 0;
                            setLastPosition(currentLineOffsets[0], document, currentMethod);
                            currentMethod = null;
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * This method indicates whether the supplied <code>line</code> contains
     * the start of a new code block.
     * 
     * @param line The line we are searching for a block open.
     * 
     * @param hasForOrWhileOnPreviousLine a length 1 array with a value of 0 at
     *            index 0 if there is no line immediately before this with a FOR
     *            in it, and 1 (at index 0) otherwise. This addresses Bug Id:
     *            1358534, 1415118.
     * 
     * @return <code>true</code> if the supplied <code>line</code> contains
     *         a string that represents the opening of a block that is not in a
     *         quote or comment.
     */
    private boolean lineContainsBlockOpener(String line, int[] hasForOrWhileOnPreviousLine)
    {
        final String forMatch = ".*?(" + forStr + ").*";
        final String whileMatch = ".*?(" + whileStr + ").*";
        final String loopMatch = ".*?(" + loopStr + ").*";
        final String caseMatch = ".*?(" + caseStr + ").*";

        // fix for bug 1436329 - if statement parser problem
        String[] toMatch = new String[]{".*?(" + beginStr + ").*", forMatch, whileMatch,
                ".*?(" + ifStr + ").*", loopMatch, caseMatch};
        String[] toMatchWord = new String[]{beginStr, forStr, whileStr, ifStr, loopStr, caseStr};
        for (int i = 0; i < toMatch.length; i++)
        {
            String matched = toMatch[i];
            // if (line.matches(matched))
            // FIX for BUG 1387900 - upper and lower case
            if (UsefulOperations.matchesWord(line, toMatchWord[i]))
            {
                if (!isStringInComments(line, matched) && !isStringInQuotes(line, matched))
                {
                    if (matched.equals(forMatch))
                    {
                        // checking OPEN x FOR syntax
                        // Bug Id: 1210580
                        if (line.matches(".*?[Oo][Pp][Ee][Nn]\\W.*" + forStr + ".*"))
                        {
                            return false;
                        }
                        // address FOR update syntax
                        // Bug Id: 1436129
                        else if (line.matches(".*" + forStr + "\\W.*?[Uu][Pp][Dd][Aa][Tt][Ee].*"))
                        {
                            return false;
                        }
                        // address FOR \nLOOP syntax
                        // Bug Id: 1358534
                        else if (!line.matches(".*?" + forStr + "\\W.*" + loopStr + ".*"))
                        {
                            // if "FOR \nLOOP" checker is set, reset it
                            // 16/12/2005 updated this logic because it was back
                            // to front
                            hasForOrWhileOnPreviousLine[0] = 1;
                        }
                        else
                        {
                            hasForOrWhileOnPreviousLine[0] = 0;
                        }
                    }
                    // FIX for bug 1415118 - WHILE \nLOOOP syntax
                    else if (matched.equals(whileMatch))
                    {
                        // address WHILE \nLOOP syntax
                        // Bug Id: 1415118
                        if (!line.matches(".*?" + whileStr + "\\W.*?" + loopStr + ".*"))
                        {
                            // if "WHILE \nLOOP" checker is set, reset it
                            // 16/12/2005 updated this logic because it was back
                            // to front
                            hasForOrWhileOnPreviousLine[0] = 1;
                        }
                        else
                        {
                            hasForOrWhileOnPreviousLine[0] = 0;
                        }
                    }
                    else if (matched.equals(loopMatch))
                    {
                        // address FOR \nLOOP syntax
                        // Bug Id: 1358534
                        boolean containsBlockOpener = (hasForOrWhileOnPreviousLine[0] == 0);
                        hasForOrWhileOnPreviousLine[0] = 0;
                        return containsBlockOpener;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method indicates whether the supplied <code>line</code> contains
     * an END and that END is actually embedded in quotes. If there is no END in
     * the line, it will return true.
     * 
     * @param line The line that may contain an "END" statement, and may have
     *            that END statement in quotes.
     * 
     * @return <code>false</code> if there is an END in the line that is not
     *         in quotes, and <code>true</code> otherwise.
     */
    private boolean isEndInQuotes(String line)
    {
        return isStringInQuotes(line, "(" + endStr + ")");
    }

    /**
     * This method indicates whether the supplied <code>line</code> contains a
     * string <code>toSearchFor</code> and that string
     * <code>toSearchFor</code> is actually embedded in quotes. If there is no
     * <code>toSearchFor</code> in the line, it will return true.
     * 
     * @param line The line that may contain a <code>toSearchFor</code>
     *            statement, and may have that END statement in quotes.
     * 
     * @param toSearchFor The regex containing an identified group (1) to search
     *            for in the supplied <code>line</code>.
     * 
     * @return <code>false</code> if there is a <code>toSearchFor</code> in
     *         the line that is not in quotes, and <code>true</code>
     *         otherwise.
     */
    public static boolean isStringInQuotes(String line, String toSearchFor)
    {
        Pattern p = Pattern.compile(toSearchFor);
        Matcher m = p.matcher(line);
        boolean isRealMatch = false;
        int toSearchForIndex = 0;
        while (!isRealMatch)
        {
            if (!m.find())
            {
                // toSearchFor not in the line, same as being in comments
                return true;
            }
            isRealMatch = true;
            toSearchForIndex = m.start(1);
            if (toSearchForIndex != 0)
            {
                char ch = line.charAt(toSearchForIndex - 1);
                if (Character.isJavaIdentifierPart(ch))
                {
                    isRealMatch = false;
                    continue;
                }
            }
            int endPt = m.end(1);
            if (endPt < line.length())
            {
                char ch = line.charAt(endPt);
                if (Character.isJavaIdentifierPart(ch))
                {
                    isRealMatch = false;
                    continue;
                }
            }
        }
        int quoteIndex = line.indexOf("'");
        if (quoteIndex == -1)
        {
            return false;
        }
        if (toSearchForIndex < quoteIndex)
        {
            return false;
        }
        int endCommentIndex = 0;
        while (true)
        {
            endCommentIndex = line.indexOf("'", quoteIndex + 1);
            if (endCommentIndex == -1)
            {
                return true;
            }
            if (endCommentIndex + 1 < line.length())
            {
                if (line.charAt(endCommentIndex + 1) == '\'')
                {
                    quoteIndex = endCommentIndex + 1;
                }
                else
                {
                    break;
                }
            }
            else if (endCommentIndex + 1 == line.length())
            {
                break;
            }
        }
        if (toSearchForIndex < endCommentIndex)
        {
            return true;
        }
        else
        {
            // fixes [ 1925733 ] CASE statement on newline causes parsing to bail (+1)
            return isStringInQuotes(line.substring(endCommentIndex + 1), toSearchFor);
        }
    }

    /**
     * This method indicates whether the supplied <code>line</code> contains
     * an END and that END is actually embedded in a comment. If there is no END
     * in the line, it will return true.
     * 
     * @param line The line that may contain an "END" statement, and may have
     *            that END statement in comments.
     * 
     * @return <code>false</code> if there is an END in the line that is not
     *         in comments, and <code>true</code> otherwise.
     */
    private boolean isEndInComments(String line)
    {
        return isStringInComments(line, "(" + endStr + ")");
    }

    /**
     * @param line
     * @param segments
     * @return <code>true</code> if we are still grabbing locals and
     *         <code>false</code> otherwise.
     */
    private boolean grabLocal(String line,
                              List segments,
                              Segment currentLocal,
                              boolean[] isReadyForNextLocal)
    {
        // fix bug 1387900 - upper and lower case
        if (UsefulOperations.matchesWord(line, beginStr))
        {
            return false;
        }
        boolean stillGrabbingType = grabType(line, currentLocal, segments);
        if (!stillGrabbingType)
        {
            Segment local = (Segment) segments.remove(segments.size() - 1);
            ((Segment) segments.get(segments.size() - 1)).addLocalField(local);
        }
        isReadyForNextLocal[0] = !stillGrabbingType;
        return true;
    }

    private void setLastPosition(int currentLineOffset, IDocument document, Segment currentMethod)
            throws BadLocationException
    {
        currentMethod.setLastPosition(new Position(document.getLineOffset(currentLineOffset - 1)));
    }

    protected PackageSegment createPackageSegment(String pkgName, int offset)
    {
        return new PackageSegment(pkgName, new Position(offset, pkgName.length()), false);
    }
}
