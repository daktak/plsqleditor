/*
 * Created on 26/02/2005
 *
 * @version $Id$
 */
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
        return "\\W*[Cc][Rr][Ee][Aa][Tt][Ee] [Oo][Rr] [Rr][Ee][Pp][Ll][Aa][Cc][Ee] [Pp][Aa][Cc][Kk][Aa][Gg][Ee] ";
    }

    protected String getParametersTerminator()
    {
        return ".*;.*";
    }

    /**
     * This method parses a section of the myOutputText from the
     * <code>document</code> starting from the next line out of the supplied
     * <code>file</code>. It assumes that the next line read from here is the
     * <code>currentLineOffset + 1</code>. It adds the discovered segments to
     * the list of <code>segments</code>. The section of myOutputText being
     * parsed is a body section. This method assumes that the body part may
     * contain documentation and implementation of procedures, functions, types
     * and fields.
     * 
     * @param currentLineOffset The index of the line from the beginning of the
     *            <code>document</code> that the last line of myOutputText
     *            obtained from the <code>file</code> corresponds to. The next
     *            call to {@link BufferedReader#readLine()} will read the line
     *            located in the <code>document</code> at
     *            <code>currentLineOffset + 1</code>.
     * 
     * @param document The document from which the <code>file</code> was
     *            obtained.
     * 
     * @param file The buffered reader from which we are parsing the
     *            myOutputText.
     * 
     * @param segments The list of segments to which the next parsed tokens will
     *            be added.
     * 
     * @param packageSegment The segment holding the name of the package.
     */
    protected int parseBody(int currentLineOffset,
                            IDocument document,
                            BufferedReader file,
                            List segments,
                            Segment packageSegment) throws IOException, BadLocationException
    {
        StringBuffer comments = new StringBuffer();
        String line = null;
        String tmpLine = null;
        boolean isGrabbingParameters = false;
        boolean isGrabbingPragmaParameters = false;
        boolean isGrabbingTypes = false;
        boolean isInComment = false;
        Segment currentSegment = null;
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

        while ((line = file.readLine()) != null)
        {
            currentLineOffset++;
            int offset = document.getLineOffset(currentLineOffset - 1);
            if (isGrabbingTypes)
            {
                if (!(isGrabbingTypes = grabType(line, currentSegment, segmentsToAddTo)))
                {
                    currentSegment = null;
                }
            }
            else if (isGrabbingParameters)
            {
                if (!(isGrabbingParameters = grabParameters(line, currentSegment, offset)))
                {
                    currentSegment = null;
                }
            }
            else if (isGrabbingPragmaParameters)
            {
                if (!(isGrabbingPragmaParameters = grabPragmaRestrictReferenceParameters(line,
                                                                                         currentSegment,
                                                                                         offset)))
                {
                    currentSegment = null;
                }
            }
            else if (isInComment)
            {
                comments.append(line).append(newLine);
                if (isEndingComment(line))
                {
                    isInComment = false;
                }
            }
            else if (!line.equals(tmpLine = line.replaceFirst(".*(" + PROCEDURE + "|" + FUNCTION
                    + ")( +)([^( ]+).*", "$1$2$3")))
            {
                offset += line.indexOf(tmpLine);
                currentSegment = new Segment(tmpLine, new Position(offset, tmpLine.length()));
                currentSegment.setDocumentation(comments.toString());
                segmentsToAddTo.add(currentSegment);
                isGrabbingParameters = grabParameters(line, currentSegment, offset);
            }
            else if (!line.equals(tmpLine = line.replaceFirst(".*(" + PROCEDURE + "|" + FUNCTION
                    + ")( +\\w+) *;.*", "$1$2")))
            {
                offset += line.indexOf(tmpLine);
                Segment segment = new Segment(tmpLine, new Position(offset, tmpLine.length()));
                segment.setDocumentation(comments.toString());
                segmentsToAddTo.add(segment);
            }
            else if (line.matches(".*(" + PROCEDURE + "|" + FUNCTION + ")( +\\w+) *"))
            {
                offset += line.indexOf(line);
                currentSegment = new Segment(line, new Position(offset, line.length()));
                currentSegment.setDocumentation(comments.toString());
                segmentsToAddTo.add(currentSegment);
                isGrabbingParameters = true;
            }
            else if (!line.equals(tmpLine = line.replaceFirst(".*(" + PRAGMA + ")( +)"
                    + RESTRICT_REFERENCES + "([^( ]*).*", "$1$2$3")))
            {
                offset += line.indexOf(tmpLine);
                currentSegment = new Segment(tmpLine, new Position(offset, tmpLine.length()),
                        SegmentType.Pragma);
                currentSegment.setDocumentation(comments.toString());
                segmentsToAddTo.add(currentSegment);
                currentSegment.setName(line.replaceFirst(".*?" + PRAGMA + " +"
                                            + RESTRICT_REFERENCES + "\\(\\s*([\\w_]+).*", "$1"));

                tmpLine = line.replaceFirst(".*(" + PRAGMA + ")( +)"
                                            + RESTRICT_REFERENCES + "(.*)", "$3");
                isGrabbingPragmaParameters = grabPragmaRestrictReferenceParameters(tmpLine,
                                                                                   currentSegment,
                                                                                   offset);
            }
            else if (line.matches(".*--.*"))
            {
                addCode(segmentsToAddTo, currentLineOffset, line);
            }
            else if (isStartingComment(line))
            {
                comments = new StringBuffer(line);
                comments.append(newLine);
                isInComment = true;
            }
            else if (line.matches(".*[Ee][Nn][Dd] " + packageName + ".*"))
            {
                addCode(segmentsToAddTo, currentLineOffset, line);
                break;
            }
            else if (line.trim().length() > 0)
            {
                currentSegment = new Segment(line, new Position(offset, line.length()));
                currentSegment.setDocumentation(comments.toString());
                if (!(isGrabbingTypes = grabType(line, currentSegment, segmentsToAddTo)))
                {
                    currentSegment = null;
                }
            }
        }
        resolvePragmas(segmentsToAddTo);
        return currentLineOffset;
    }

    private void resolvePragmas(List segmentsToAddTo)
    {
        for (int i = segmentsToAddTo.size() - 1; i >= 0; i--)
        {
            Segment pragma = (Segment) segmentsToAddTo.get(i);
            if (pragma.getType() == SegmentType.Pragma)
            {
                segmentsToAddTo.remove(i);
                for (int j = i - 1; j > 0; j--)
                {
                    Segment method = (Segment) segmentsToAddTo.get(j);
                    if (method.getName().equals(pragma.getName()))
                    {
                        method.setDocumentation(method.getDocumentation() + "\n" + pragma.toString());
                        // TODO in the future this information may be useful to display in some way
                        break;
                    }
                }
            }
                                                     
        }
    }


    protected PackageSegment createPackageSegment(String pkgName, int offset)
    {
        return new PackageSegment(pkgName, new Position(offset, pkgName.length()), true);
    }

    /**
     * This method pulls out more of the parameters of a pragma restrict
     * references definition from a line.
     * 
     * @param line The line being parsed for segment information.
     * 
     * @param currentSegment The segment (function/procedure) to add the details
     *            to.
     * 
     * @return <code>false</code> if the parameters have all been grabbed and
     *         <code>true</code> otherwise.
     */
    private boolean grabPragmaRestrictReferenceParameters(String line,
                                                          Segment currentSegment,
                                                          int lineOffset)
    {
        final Pattern pragmaParamPattern = Pattern.compile("([\\w\\.\\%\\d_]+)()([^,)]*)()\\W*");
        // FIX for bug 1387877
        // remove comments from auto completion
        line = line.replaceFirst("^(.*)--.*$", "$1");
        Matcher m = pragmaParamPattern.matcher(line);
        int currentFindLoc = 0;
        while (m.find(currentFindLoc))
        {
            currentFindLoc = grabParam(currentSegment, m, lineOffset);
        }
        if (UsefulOperations.matchesWord(line, getParametersTerminator()))
        {
            return false;
        }
        return true;
    }
}
