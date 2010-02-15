package plsqleditor.parsers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import plsqleditor.stores.PlSqlTypeManager;

import au.com.zinescom.util.UsefulOperations;

/**
 * This class represents the common parts of a plsql parser.
 * 
 * @author Toby Zines
 * 
 * Created on 26/02/2005
 */
public abstract class AbstractPlSqlParser implements PlSqlParser
{
    public static final String     FUNCTION            = "[Ff][Uu][Nn][Cc][Tt][Ii][Oo][Nn]";
    public static final String     PROCEDURE           = "[Pp][Rr][Oo][Cc][Ee][Dd][Uu][Rr][Ee]";
    public static final String     RECORD              = "[Rr][Ee][Cc][Oo][Rr][Dd]";
    public static final String     TABLE_OF            = "[Tt][Aa][Bb][Ll][Ee]\\s+[Oo][Ff]";
    protected static final String  PRAGMA              = "[Pp][Rr][Aa][Gg][Mm][Aa]";
    protected static final String  RESTRICT_REFERENCES = "[Rr][eE][Ss][Tt][Rr][Ii][Cc][Tt]_[Rr][Ee][Ff][Ee][Rr][Ee][Nn][Cc][Ee][Ss]";
    protected static final Pattern inOutPattern        = Pattern
                                                               .compile("(\\w+)\\W+([Ii][Nn] [Oo][Uu][Tt])\\W+([\\w\\.\\%\\d_]+)([^,)]*)\\W*");
    protected static final Pattern inPattern           = Pattern
                                                               .compile("(\\w+)\\W+([Ii][Nn])\\W+([\\w\\.\\%\\d_]+)([^,)]*)\\W*");
    protected static final Pattern outPattern          = Pattern
                                                               .compile("(\\w+)\\W+([Oo][Uu][Tt])\\W+([\\w\\.\\%\\d_]+)([^,)]*)\\W*");
    protected static final Pattern missingTypePattern  = Pattern
                                                               .compile("(\\w+)()\\W+([\\w\\.\\%\\d_]+)([^,()]*)\\W*");
    protected static final Pattern recPattern          = Pattern
                                                               .compile(".*[Tt][Yy][Pp][Ee] +([_\\w]+) +[Ii][Ss] +("
                                                                       + RECORD + ".+\\)).*;.*");
    protected static final Pattern subTypePattern      = Pattern
                                                               .compile(".*[Ss][Uu][Bb][Tt][Yy][Pp][Ee] +([_\\w]+) +[Ii][Ss] +(.*);.*");
    protected static final Pattern tablePattern        = Pattern
                                                               .compile(".*[Tt][Yy][Pp][Ee] +([_\\w]+) +[Ii][Ss] +("
                                                                       + TABLE_OF + "[^;]+);.*");
    protected static final Pattern constPattern        = Pattern
                                                               .compile(".*?\\s*([_\\w]+)\\s+[Cc][Oo][Nn][Ss][Tt][Aa][Nn][Tt]\\s+([()\\w_\\.\\%\\d]+)\\W*(DEFAULT|:=)\\s*('[^']*'|[-()\\w_\\.\\%\\d]+).*;.*");
    protected static final Pattern fldPattern          = Pattern
                                                               .compile("^[^:]*?([_\\w]+)\\s+([()\\w_\\.\\%\\d]+)(.*);.*");

    protected static final Pattern cursorPattern       = Pattern
                                                               .compile(".*[Cc][Uu][Rr][Ss][Oo][Rr] +([_\\w]+\\(?[^)]*\\)?) +IS +([^;]+);.*");
    protected static final Pattern exInitPattern       = Pattern
                                                               .compile(".*"
                                                                       + PRAGMA
                                                                       + "\\s+[Ee][Xx][Cc][Ee][Pp][Tt][Ii][Oo][Nn]_[Ii][Nn][Ii][Tt][^\\w]+(.*)");
    protected static String        newLine             = System.getProperty("line.separator");
    private String myPackageName;

    public AbstractPlSqlParser()
    {
        //
    }

    /**
     * This method grabs the details out of the "header details" section of the
     * header pldoc comment. This information is generated into the package
     * header and as such should be available to the body.
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
                                    List details) throws IOException, BadLocationException
    {
        String line = null;
        boolean isCommenting = false;
        boolean isGrabbingTypes = false;
        Segment currentSegment = null;
        StringBuffer comments = new StringBuffer();

        // this represents the internal comments
        // to be converted to javadoc comments
        String internalCommentsId = "--";
        // this represents the last line's starting spaces
        // parse header details here
        while ((line = file.readLine()) != null)
        {
            currentLineOffset++;
            int offset = document.getLineOffset(currentLineOffset - 1);
            int lineLength = document.getLineLength(currentLineOffset - 1);

            if (line.matches(".*end header details.*"))
            {
                addCode(details, offset, line);
                break;
            }

            String detailsLine = line;
            if (isGrabbingTypes)
            {
                isGrabbingTypes = grabType(line, currentSegment, details);
            }
            else if (isCommenting)
            {
                // find the middle of a comment for a particular file
                if (!(detailsLine.replaceFirst("(\\W*)" + internalCommentsId + "(.*)$", "$1 \\*$2"))
                        .equals(detailsLine))
                {
                    comments.append(detailsLine).append(newLine);
                }
                else
                {
                    // we have found the end of the comment for a particular
                    // file
                    isCommenting = false;
                    currentSegment = new Segment(detailsLine, new Position(offset, lineLength));
                    currentSegment.setDocumentation(comments.toString());
                    comments = new StringBuffer();
                    isGrabbingTypes = grabType(line, currentSegment, details);
                }
            }
            else
            {
                // find the start of the commenting for a particular file
                if (!(detailsLine.replaceFirst("(\\W*)" + internalCommentsId + "(.*)$",
                                               "$1/\\*\\*\n$1 \\*$2")).equals(detailsLine))
                {
                    isCommenting = true;
                    addCommentsToList(details, comments, offset);
                    comments = new StringBuffer(detailsLine);
                    comments.append(newLine);
                }
                else
                {
                    if (detailsLine.trim().length() > 0)
                    {
                        currentSegment = new Segment(detailsLine, new Position(offset, lineLength));
                        isGrabbingTypes = grabType(line, currentSegment, details);
                    }
                    else
                    {
                        // add the empty line
                        addCode(details, offset, line);
                    }
                }
            }
        }
        for (Iterator it = details.iterator(); it.hasNext();)
        {
            Segment element = (Segment) it.next();
            element.setPublic(true);
        }
        return currentLineOffset;
    }

    /**
     * @param segments
     * @param comments
     * @param offset
     */
    protected void addCommentsToList(List segments, StringBuffer comments, int offset)
    {
        if (comments.length() > 0)
        {
            String commentString = comments.toString();
            addCode(segments, offset, commentString);
        }
    }

    /**
     * This method parses the header of the whole document. This is the header
     * of the file, not the header of a particular method. The parsing of all
     * the methods (headers and bodies) is handled by the call to
     * {@link #parseBody(int, IDocument, BufferedReader, List, String)}.
     * 
     * @param currentLineOffset
     * 
     * @param document
     * 
     * @param file
     * 
     * @param details
     * 
     * @param packageName
     */
    protected int parseHeader(int currentLineOffset,
                              IDocument document,
                              BufferedReader file,
                              List details,
                              String[] packageName) throws IOException, BadLocationException
    {
        boolean isInHeader = false;

        String line = null;
        String tmpLine = null;
        String prefix = getStartOfFilePrefix();
        String fullPrefixRegex = prefix + "\\W*(\\w+).*";
        PackageSegment packageSegment = null;

        /*
         * this field indicates that we have opened the package body, and should
         * start looking for plsql statements/declarations etc
         */
        boolean processedStartOfFile = false;

        /*
         * this field is to support comments after the CREATE .. statement but
         * before the name of the package
         */
        boolean isCommentBeforePackageName = false;

        while ((line = file.readLine()) != null)
        {
            currentLineOffset++;
            int offset = document.getLineOffset(currentLineOffset - 1);
            if (!processedStartOfFile
                    && !(tmpLine = line.replaceFirst(fullPrefixRegex, "$1")).equals(line))
            {
                // have hit the declaration of the package/package body
                packageName[0] = tmpLine;
                setPackageName(tmpLine);
                Pattern p = Pattern.compile(fullPrefixRegex);
                Matcher m = p.matcher(line);
                m.find();
                offset += m.start();
                //offset += line.indexOf(prefix);// + prefix.length();
                packageSegment = createPackageSegment(tmpLine, offset);
                details.add(packageSegment);
                processedStartOfFile = true;
            }
            else if (!processedStartOfFile
                    && !(tmpLine = line.replaceFirst(prefix + "(.*)", "$1")).equals(line))
            {
                // TODO sort out correct behaviour here
                offset += line.indexOf(prefix) + prefix.length();
                packageSegment = createPackageSegment("TempName", offset);
                details.add(packageSegment);
                addCode(details, offset, tmpLine);
                isCommentBeforePackageName = true;
                processedStartOfFile = true;
            }
            else if (line.matches(".*header details.*"))
            {
                addCode(details, offset, line);
                // this grabs the whole 'header details' --> 'end header
                // details' section
                currentLineOffset = grabHeaderDetails(currentLineOffset, document, file, details);
            }
            else if (isInHeader)
            {
            	if (isCommentBeforePackageName)
            	{
            		String tmp = line.replaceFirst("(.*\\*/).*", "$1");
            		addCode(details, offset, tmp);
            	}
            	else
            	{
// TODO put this into the next release (properly)
//            		if (!(tmpLine = line.replaceFirst(".*@schema\\W+([\\w_\\d]+).*", "$1")).equals(line))
//            		{
//            			packageSegment.setSchemaName(tmpLine);
//            		}
            		addCode(details, offset, line);
            	}
                if (isEndingComment(line))
                {
                    if (isCommentBeforePackageName)
                    {
                        currentLineOffset = processUntilPackageName(line,
                        		                                    file,
                                                                    document,
                                                                    currentLineOffset,
                                                                    packageName,
                                                                    packageSegment);
                    }
                    break;
                }
            }
            else if (line.matches(".*/\\*.*"))
            {
                addCode(details, offset, line);
                isInHeader = true;
            }
            else
            {
                addCode(details, offset, line);
            }
        }
        return currentLineOffset;
    }

    /**
     * 
     * @param file
     * @param offset
     * @param currentLineOffset
     * @param string
     * @param packageSegment
     * @return
     * @throws BadLocationException
     * @throws IOException
     */
    private int processUntilPackageName(String currentLine,
    		                            BufferedReader file,
                                        IDocument document,
                                        int currentLineOffset,
                                        String[] packageName,
                                        Segment packageSegment) throws BadLocationException,
            IOException
    {
        String line = null;
        String tmpLine = null;
        boolean processedPackageName = false;
        boolean isCommenting = false;

        final String isAsString = "([IiAa][Ss])";

        // check that the previous line has the package name
		String tmp = currentLine.replaceFirst(".*\\*/\\W*(\\w+).*", "$1");
		if (!tmp.equals(currentLine))
		{
			packageName[0] = tmp;
			packageSegment.setName(packageName[0]);
			processedPackageName = true;
			// check if the IS or AS is also on the previous line
			if (UsefulOperations.matchesWord(currentLine, isAsString)
	                && !isStringInComments(currentLine, isAsString))
	        {
	            return currentLineOffset;
	        }
		}
		// start looking at the next lines
        while ((line = file.readLine()) != null)
        {
            currentLineOffset++;
            tmpLine = line.replaceFirst("[^\\w]*(\\w+).*", "$1").trim();
            if (!processedPackageName && tmpLine.length() != 0)
            {
                packageName[0] = tmpLine;
                packageSegment.setName(packageName[0]);
                processedPackageName = true;
            }

			// check if the IS or AS is on the line
            if (!isCommenting && UsefulOperations.matchesWord(line, isAsString)
                    && !isStringInComments(line, isAsString))
            {
                break;
            }
            else if (isCommenting)
            {
                if (isEndingComment(line))
                {
                    if (UsefulOperations.matchesWord(line, isAsString)
                            && !isStringInComments(line, isAsString))
                    {
                        break;
                    }
                    else
                    {
                        isCommenting = false;
                    }
                }
            }
            else
            {
                if (isStartingComment(line))
                {
                    isCommenting = true;
                }
            }
        }
        return currentLineOffset;
    }

    protected abstract PackageSegment createPackageSegment(String pkgName, int offset);

    /**
     * This method adds a code segment to the last segment if it is a code
     * segment, or it creates a new code segment and adds the line to that.
     * 
     * @param line The line of text being added as a code segment to the segment
     *            <code>toAddTo</code>.
     * 
     * @param offset The location of the beginning of the supplied
     *            <code>line</code> in the document.
     */
    protected void addCode(List segments, int offset, String line)
    {
        Segment lastSegment = null;
        if (!segments.isEmpty())
        {
            lastSegment = (Segment) segments.get(segments.size() - 1);
        }
        if (lastSegment != null && lastSegment.getType() == SegmentType.Code)
        {
            lastSegment.addLine(line, new Position(offset, line.length()));
        }
        else
        {
            Segment codeSegment = new Segment(line, new Position(offset, line.length()),
                    SegmentType.Code);
            segments.add(codeSegment);
        }
    }

    /**
     * This method gets the start of file prefix, which allows us to determine a
     * correct starting point for parsing.
     */
    protected abstract String getStartOfFilePrefix();

    /**
     * This method indicates whether a <code>line</code> contains a comment
     * end. This looks like <code>*\/</code>.
     * 
     * @param line The line we are checking for the containment of a comment
     *            end.
     */
    public static boolean isEndingComment(String line)
    {
        return line.matches(".*\\*/.*");
    }

    /**
     * This method indicates whether a <code>line</code> contains a starting
     * comment in it, without and ending comment.
     * 
     * @param line The line we are checking for a comment start with no comment
     *            end.
     */
    public static boolean isStartingComment(String line)
    {
        return line.matches(".*/\\*.*") && !line.matches(".*\\*/.*");
    }

    /**
     * This method indicates whether the supplied <code>line</code> has a
     * starting and ending comment in it at the same time.
     * 
     * @param line The line we are checking for the following contents:
     *            <code>[anything]/*[anything]*\/[anything]</code>.
     * 
     * @return <code>true</code> if the line starts with a comment and ends
     *         with one
     */
    public static boolean isSingleLineClosedComment(String line)
    {
        return line.matches(".*/\\*.*") && line.matches(".*\\*/.*");
    }

    /**
     * This method pulls out more of the type details of a function or procedure
     * from a line.
     * <p>
     * The format of these fields is:
     * <ul>
     * <li>name [CONSTANT] typewithdots[%type_qualifier] [DEFAULT|:=]
     * [']value['];</li>
     * <li>name type;</li>
     * <li>TYPE name IS RECORD(blah1 type1, blah2 type2);</li>
     * <li>SUBTYPE name IS blah;</li>
     * <li>TYPE name IS TABLE OF type INDEX BY type;</li>
     * </ul>
     * </p>
     * 
     * @param line The line being parsed for segment information.
     * 
     * @param currentSegment The segment (type/field/constant) to add the
     *            details to.
     * 
     * @param details The list of segments to add to when the (type) segment is
     *            completed.
     * 
     * @return <code>false</code> if the details have all been grabbed and
     *         <code>true</code> otherwise.
     */
    boolean grabType(String line, Segment currentSegment, List details)
    {
        String documentation = currentSegment.getDocumentation();
        int commentIndex = line.indexOf("--");
        // strip single line comments
        if (commentIndex != -1)
        {
            Position position = currentSegment.getPosition();
            position.offset = position.offset + line.length() + 1;
            documentation = documentation + "\n" + line.substring(commentIndex);
            currentSegment.setDocumentation(documentation);
            line = line.substring(0, commentIndex);
        }
        String tmpLine = null;
        final String separator = " ::: ";
        // strip multi line comments that are on one line
        while (!(tmpLine = line.replaceFirst("^(.*?)/\\*(.*?)\\*/(.*?)$", "$1 $3" + separator
                + "$2")).equals(line))
        {
            int separatorIndex = tmpLine.indexOf(separator);
            String newDoc = "/*" + tmpLine.substring(separatorIndex + separator.length()) + "*/";
            documentation += "\n" + newDoc;
            currentSegment.setDocumentation(documentation);
            Position position = currentSegment.getPosition();
            position.offset = position.offset + line.length() + 1;
            line = tmpLine.substring(0, separatorIndex);
        }

        // Fix for bug 1387877.
        // Return types incorrect in auto completion list.

        currentSegment.setTmpReturnType(currentSegment.getTmpReturnType() + " " + line.trim());

        Segment typeSegment = null;
        if (line.matches(".*;.*"))
        {
            boolean isTakenCareOf = false;
            String fullType = currentSegment.getTmpReturnType().trim();

            Matcher m = null;
            if ((m = recPattern.matcher(fullType)).matches()
                    || (m = tablePattern.matcher(fullType)).matches())
            {
                typeSegment = makeType(currentSegment, m, SegmentType.Type);
            }
            else if ((m = exInitPattern.matcher(fullType)).matches())
            {
                isTakenCareOf = true;
                addCode(details, currentSegment.getPosition().offset, fullType);
            }
            else if ((m = cursorPattern.matcher(fullType)).matches())
            {
                typeSegment = makeType(currentSegment, m, SegmentType.Cursor);
            }
            else if ((m = subTypePattern.matcher(fullType)).matches())
            {
                typeSegment = makeType(currentSegment, m, SegmentType.SubType);
            }
            else if ((m = constPattern.matcher(fullType)).matches())
            {
                typeSegment = makeConstant(currentSegment, m);
            }
            else if ((m = fldPattern.matcher(fullType)).matches())
            {
                typeSegment = makeField(currentSegment, m, SegmentType.Field);
            }
            if (typeSegment != null)
            {
                typeSegment.setDocumentation(documentation);
                details.add(typeSegment);
            }
            else
            {
                if (!isTakenCareOf)
                {
                    // TODO log a warning that we didn't detect the type.
                    details.add(currentSegment);
                }
            }
            currentSegment.setTmpReturnType("");
            return false;
        }
        return true;
    }

    /**
     * @param currentSegment
     * @param m
     * @param type
     * @return The newly constructed field definition of the supplied
     *         <code>type</code>.
     */
    private Segment makeField(Segment currentSegment, Matcher m, SegmentType type)
    {
        Segment typeSegment;
        Position pos = currentSegment.getPosition();
        typeSegment = new Segment(m.group(1), pos, type);
        typeSegment.setReturnType(m.group(2));
        String line = m.group(3);
        typeSegment.addLine(line, new Position(pos.offset + m.start(3), line.length()));
        return typeSegment;
    }

    /**
     * @param currentSegment
     * @param m
     * @param type
     * @return The newly constructed field definition of the supplied
     *         <code>type</code>.
     */
    private Segment makeConstant(Segment currentSegment, Matcher m)
    {
        Segment constSegment;
        Position pos = currentSegment.getPosition();
        constSegment = new Segment(m.group(1), pos, SegmentType.Constant);
        constSegment.setReturnType(m.group(2));
        constSegment.setReferredData(m.group(4));
        // String line = m.group(3);
        // constSegment.addLine(line, new Position(pos.offset + m.start(3),
        // line.length()));
        return constSegment;
    }

    /**
     * @param currentSegment
     * @param m
     * @param type
     * @return The newly constructed type definition of the supplied
     *         <code>type</code>.
     */
    private Segment makeType(Segment currentSegment, Matcher m, SegmentType type)
    {
        Segment typeSegment;
        Position p = currentSegment.getPosition();
        // p.offset = p.offset + m.start(1);
        typeSegment = new Segment(m.group(1), p, type);
        if (type == SegmentType.Cursor)
        {
            typeSegment = makeCursor(m, type, typeSegment, p);
        }
        else
        {
            typeSegment.setReturnType(m.group(2));
        }
        PlSqlTypeManager.getTypeManager(null).storeType(getPackageName(), typeSegment);
        return typeSegment;
    }

    public String getPackageName()
    {
        return myPackageName;
    }

    private void setPackageName(String packageName)
    {
        myPackageName = packageName;
    }

    private Segment makeCursor(Matcher m, SegmentType type, Segment typeSegment, Position p)
    {
        String cursorName = m.group(1);
        int bracketIndex = cursorName.indexOf('(');
        if (bracketIndex != -1)
        {
            // get the parameters of the cursor
            typeSegment = new Segment(cursorName.substring(0, bracketIndex), p, type);

            // fix here for bug 1430510 - Code Completion for Cursors
            // TODO - fix this code to work with parameter types that contain (), such as VARCHAR2(20)
            final Pattern cursorParamsPattern = Pattern
                    .compile(".*?\\([^_\\w]*([_\\w]+)[^_\\w,]*([Ii][Nn])?[^\\w\\.\\%\\d_,]+([\\w\\.\\%\\d_]+)([^\\w\\.\\%\\d_,])*(,[^)]*)?\\)");
            // final Pattern cursorParamsPattern =
            // Pattern.compile("([_\\w]+)\\([^_\\w]*([_\\w]+)[^_\\w,]*(,[^)]*)?\\)");
            Matcher paramsMatcher = cursorParamsPattern.matcher(cursorName);
            if (paramsMatcher.matches())
            {
                grabParam(typeSegment, paramsMatcher, p.getOffset());
                String toSplit = paramsMatcher.group(5);
                int addition = paramsMatcher.start(5) + p.getOffset();

                if (toSplit != null)
                {
                    // final Pattern individualParams =
                    // Pattern.compile(",[^_\\w]*([_\\w]+)([^_\\w,]*).*");
                    final Pattern individualParams = Pattern
                            .compile(",[^_\\w]*([_\\w]+)[^_\\w,]+([Ii][Nn])?[^\\w\\.\\%\\d_,]*([\\w\\.\\%\\d_]+)([^\\w\\.\\%\\d_,)])?.*");

                    Matcher individualParamsMatcher = individualParams.matcher(toSplit);
                    int findLocation = 0;
                    while (individualParamsMatcher.find(findLocation))
                    {
                        findLocation = grabParam(typeSegment, individualParamsMatcher, addition, 3);
                    }
                }
            }
        }
        String cursorDefinition = m.group(2);
        final Pattern cursorSelectPattern = Pattern
                .compile(".*?SELECT +([_\\w]+)[^_\\w,]*(.+)FROM +.*");
        Matcher memberMatcher = cursorSelectPattern.matcher(cursorDefinition);
        if (memberMatcher.matches())
        {
            // get the members of the cursor
            Segment local = new Segment(memberMatcher.group(1),
                    new Position(memberMatcher.start(1)));
            local.setReturnType("");
            typeSegment.addLocalField(local);
            String nextMatches = m.group(2);
            int addition = memberMatcher.start(2);
            if (nextMatches != null)
            {
                final Pattern nextParametersPattern = Pattern
                        .compile(",[^_\\w]*([_\\w]+)([^_\\w,]*).*");
                Matcher nextMatcher = nextParametersPattern.matcher(nextMatches);
                int findLocation = 0;
                while (nextMatcher.find(findLocation))
                {
                    local = new Segment(nextMatcher.group(1), new Position(nextMatcher.start(1)
                            + addition));
                    local.setReturnType("");
                    typeSegment.addLocalField(local);
                    findLocation = nextMatcher.end(2);
                }
            }
        }
        typeSegment.setReturnType("");
        return typeSegment;
    }

    /**
     * This method pulls out more of the parameters of a function or procedure
     * from a line.
     * 
     * @param line The line being parsed for segment information.
     * 
     * @param currentSegment The segment (function/procedure) to add the details
     *            to.
     * 
     * @return <code>false</code> if the parameters have all been grabbed and
     *         <code>true</code> otherwise.
     */
    boolean grabParameters(String line, Segment currentSegment, int lineOffset)
    {
        // FIX for bug 1387877
        // remove comments from auto completion
        line = line.replaceFirst("^(.*)--.*$", "$1");
        Matcher m = inPattern.matcher(line);
        int currentFindLoc = 0;
        m = inOutPattern.matcher(line);
        currentFindLoc = 0;
        while (m.find(currentFindLoc))
        {
            currentFindLoc = grabParam(currentSegment, m, lineOffset);
        }
        m = inPattern.matcher(line);
        currentFindLoc = 0;
        while (m.find(currentFindLoc))
        {
            currentFindLoc = grabParam(currentSegment, m, lineOffset);
        }
        m = outPattern.matcher(line);
        currentFindLoc = 0;
        while (m.find(currentFindLoc))
        {
            String name = m.group(1).toUpperCase();
            if (name.equals("IN"))
            {
                currentFindLoc = m.end();
                continue;
            }
            currentFindLoc = grabParam(currentSegment, m, lineOffset);
        }
        m = missingTypePattern.matcher(line);
        currentFindLoc = 0;
        while (m.find(currentFindLoc))
        {
            String name = m.group(1).toUpperCase();
            String paramType = m.group(3).toUpperCase();
            if (name.equals("FUNCTION") || name.equals("PROCEDURE") || name.equals("RETURN")
                    || paramType.equals("IN") || paramType.equals("OUT") || name.equals("IN"))
            {
                currentFindLoc = m.end();
                continue;
            }
            currentFindLoc = grabParam(currentSegment, m, lineOffset);
        }

        String tmpLine = null;
        if (!line.equals(tmpLine = line
                .replaceFirst(".*[Rr][Ee][Tt][Uu][Rr][Nn]\\s+([\\w\\._\\%\\d]+).*", "$1"))
                && currentSegment.getType() == SegmentType.Function)
        {
            currentSegment.setReturnType(tmpLine);
        }
        if (UsefulOperations.matchesWord(line, getParametersTerminator()))
        {
            return false;
        }
        return true;
    }

    protected String getParametersTerminator()
    {
        return "([AaIi][Ss])";
    }

    /**
     * This method grabs a parameter from the contained matcher <code>m</code>
     * and pulls out 4 groups.
     * <p>
     * <ul>
     * <li> Group 1 is the name of the parameter </li>
     * <li> Group 2 is the in out value </li>
     * <li> Group 3 is the type of the parameter </li>
     * <li> Group 4 is the extra details </li>
     * </ul>
     * 
     * @param currentSegment The segment to add the parameter to.
     * 
     * @param m The matcher to obtain the 4 groups from
     * 
     * @param lineOffset the offset of the current line
     * 
     * @param groupEnd If this is -1, the return value will be the result of a
     *            call to <code>m.end()</code>. Otherwise, it will be a call
     *            to <code>m.end(groupEnd)</code>.
     * @return Either the end offset of the matcher obtained by
     *         <code>m.end()</code> or the result of
     *         <code>m.end(groupEnd)</code>, depending on the value of
     *         <code>groupEnd</code> that is passed in. If the group selected
     *         is invalid (returns -1) then the result of <code>m.end()</code>
     *         will once again be used.
     */
    int grabParam(Segment currentSegment, Matcher m, int lineOffset, int groupEnd)
    {
        String paramName = m.group(1);
        String paramInOut = m.group(2);
        String paramType = m.group(3);
        String extraParamDetails = m.group(4);
        if (extraParamDetails != null)
        {
            extraParamDetails = extraParamDetails.trim();
        }
        else
        {
            extraParamDetails = "";
        }
        if (!currentSegment.containsParameter(paramName))
        {
            currentSegment.addParameter(paramName,
                                        paramInOut,
                                        paramType,
                                        extraParamDetails,
                                        lineOffset + m.start(1));
        }
        if (groupEnd != -1)
        {
            int end = m.end(groupEnd);
            if (end == -1)
            {
                end = m.end();
            }
            return end;
        }
        return m.end();
    }

    /**
     * This method grabs a parameter from the contained matcher <code>m</code>
     * and pulls out 4 groups.
     * <p>
     * <ul>
     * <li> Group 1 is the name of the parameter </li>
     * <li> Group 2 is the in out value </li>
     * <li> Group 3 is the type of the parameter </li>
     * <li> Group 4 is the extra details </li>
     * </ul>
     * 
     * @param currentSegment The segment to add the parameter to.
     * 
     * @param m The matcher to obtain the 4 groups from
     * 
     * @param lineOffset the offset of the current line
     * 
     * @return The end offset of the matcher obtained by <code>m.end()</code>.
     */
    int grabParam(Segment currentSegment, Matcher m, int lineOffset)
    {
        return grabParam(currentSegment, m, lineOffset, -1);
    }

    /**
     * This method
     * 
     * @param br
     * @return The string representation of the whole package header.
     * @throws IOException
     */
    List parseBodyReader(IDocument document, BufferedReader br, String[] packageName)
            throws IOException
    {
        List segments = new ArrayList();
        int currentLineOffset = 0;
        try
        {
            currentLineOffset = parseHeader(currentLineOffset, document, br, segments, packageName);
            Segment pkgSegment = getPackageSegment(segments, packageName[0]);
            currentLineOffset = parseBody(currentLineOffset, document, br, segments, pkgSegment);
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
        for (Iterator it = segments.iterator(); it.hasNext();)
        {
            Segment segment = (Segment) it.next();
            List containedSegments = segment.getContainedSegments();
            fixSegments(segment, containedSegments);
        }
        return segments;
    }

    private void fixSegments(Segment parent, List containedSegments)
    {
        if (!containedSegments.isEmpty())
        {
            for (Iterator it = containedSegments.iterator(); it.hasNext();)
            {
                Segment containedSegment = (Segment) it.next();
                containedSegment.setParent(parent);
                fixSegments(containedSegment, containedSegment.getContainedSegments());
            }
        }
    }

    public static PackageSegment getPackageSegment(List segments, String packageName)
    {
        for (Iterator it = segments.iterator(); it.hasNext();)
        {
            Segment segment = (Segment) it.next();
            if (segment.getName().equals(packageName) && segment instanceof PackageSegment)
            {
                return (PackageSegment) segment;
            }
        }
        return null;
    }

    public List parseFile(IDocument document, String[] packageName, SegmentType[] filters)
            throws IOException
    {
        String wholeText = document.get();
        ByteArrayInputStream input = new ByteArrayInputStream(wholeText.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        List toFilter = parseBodyReader(document, br, packageName);
        List toReturn = new ArrayList();
        boolean add = true;
        // TODO add filtering at layers below the first level
        for (Iterator it = toFilter.iterator(); it.hasNext();)
        {
            Segment segment = (Segment) it.next();
            add = true;
            for (int i = 0; i < filters.length; i++)
            {
                if (segment.getType() == filters[i])
                {
                    add = false;
                    break;
                }
            }
            if (add)
            {
                toReturn.add(segment);
            }
        }
        return toReturn;
    }

    /**
     * This method indicates whether the supplied <code>line</code> contains a
     * string <code>toSearchFor</code> and that string
     * <code>toSearchFor</code> is actually embedded in a comment. If there is
     * no <code>toSearchFor</code> in the line, it will return true.
     * 
     * @param line The line that may contain a <code>toSearchFor</code>
     *            statement, and may have that <code>toSearchFor</code>
     *            statement in comments.
     * 
     * @param toSearchFor The regex containing an identified group (1) to search
     *            for in the supplied <code>line</code>.
     * 
     * @return <code>false</code> if there is a <code>toSearchFor</code> in
     *         the line that is not in comments, and <code>true</code>
     *         otherwise.
     */
    protected boolean isStringInComments(String line, String toSearchFor)
    {
        return isStringInComments(line, toSearchFor, true, false, 0);
    }

    /**
     * This method indicates whether the supplied <code>line</code> contains a
     * string <code>toSearchFor</code> and that string
     * <code>toSearchFor</code> is actually embedded in a comment. If there is
     * no <code>toSearchFor</code> in the line, it will return true.
     * 
     * @param line The line that may contain a <code>toSearchFor</code>
     *            statement, and may have that <code>toSearchFor</code>
     *            statement in comments.
     * 
     * @param toSearchFor The regex containing an identified group (1) to search
     *            for in the supplied <code>line</code>.
     * 
     * @return <code>false</code> if there is a <code>toSearchFor</code> in
     *         the line that is not in comments, and <code>true</code>
     *         otherwise.
     */
    protected boolean isStringInComments(String line,
                                         String toSearchFor,
                                         boolean isCheckingSingleComment,
                                         boolean isOnlyOneCheck,
                                         int singleCheckStartPoint)
    {
        Pattern p = Pattern.compile(toSearchFor);
        Matcher m = p.matcher(line);
        if (!m.find())
        {
            // toSearchFor not in the line, same as being in comments
            return true;
        }
        int toSearchForIndex = m.start(1);

        if (isCheckingSingleComment)
        {
            // fixes bug 1521168 - Comment causes eclipse to hang 
            int fullCommentStartPoint = 0;
            int fullCommentEndPoint = 0;
            while (singleCheckStartPoint >= 0)
            {
                singleCheckStartPoint = line.indexOf("--", singleCheckStartPoint);
                if (singleCheckStartPoint != -1)
                {
                    if (toSearchForIndex <= singleCheckStartPoint)
                    {
                        // string toSearchFor is before the comments
                        break;
                    }
                    // fixes bug 1521168 - Comment causes eclipse to hang
                    if (fullCommentEndPoint != -1 &&
                            fullCommentEndPoint < singleCheckStartPoint)
                    {
                        fullCommentStartPoint = line.indexOf("/*", fullCommentStartPoint);
                        fullCommentEndPoint = line.indexOf("*/", fullCommentStartPoint);
                        if (fullCommentStartPoint != -1)
                        {
                            if (fullCommentEndPoint == -1)
                            {
                                fullCommentEndPoint = line.length();
                            }
                        }
                    }
                    // singleCheckStartPoint < toSearchForIndex now
                    if (!isStringInComments(line, "(--)", false, true, singleCheckStartPoint)
                            || (fullCommentStartPoint < singleCheckStartPoint
                                    && singleCheckStartPoint < fullCommentEndPoint))
                    {
                        return true;
                    }
                }
            }
        }

        int commentIndex = line.indexOf("/*");
        if (commentIndex == -1)
        {
            return false;
        }
        if (toSearchForIndex < commentIndex)
        {
            // comment starts after string toSearchFor
            return false;
        }
        int endCommentIndex = line.indexOf("*/");
        if (endCommentIndex == -1)
        {
            // no end to the comment, must be in comment
            return true;
        }
        else if (toSearchForIndex < endCommentIndex)
        {
            // string toSearchFor inside closed comment
            return true;
        }
        else
        {
            // comment closed before string toSearchFor, check single comment
            // indicator
            if (!isOnlyOneCheck)
            {
                return isStringInComments(line.substring(endCommentIndex),
                                          toSearchFor,
                                          isCheckingSingleComment,
                                          false,
                                          0);
            }
            else
            {
                return false;
            }
        }
    }
}
