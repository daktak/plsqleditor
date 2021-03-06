package plsqleditor.parsers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * This class manages parsing of plsql files in one place, delegating to the
 * required parser once the type of the file is determined.
 *
 * @author tzines
 * @author vradzius
 */
public class PlSqlParserManager
{
    private static PlSqlParserManager  theInstance;
    private PackageHeaderParser        myPackageHeaderParser        = new PackageHeaderParser();
    private PackageBodyParser          myPackageBodyParser          = new PackageBodyParser();
    private PackageHeaderAndBodyParser myPackageHeaderAndBodyParser = new PackageHeaderAndBodyParser();
    private PlSqlParser                mySqlScriptParser            = myPackageHeaderParser; //new FullGrammarParser();

    private Comparator<Object>                 thePositionComparator        = new Comparator<Object>()
                                                                    {
                                                                        public int compare(Object o1,
                                                                                           Object o2)
                                                                        {
                                                                            Segment lhs = (Segment) o1;
                                                                            Segment rhs = (Segment) o2;
                                                                            return lhs
                                                                                    .getPosition().offset
                                                                                    - rhs
                                                                                            .getPosition().offset;
                                                                        }
                                                                    };

    // TODO create a real parser here

    private PlSqlParserManager()
    {
        super();
    }

    private static final ParseType getTypeFromContext(IFile file)
    {
        boolean containsHeader = false, containsBody = false;
        String header = "\\W*[Cc][Rr][Ee][Aa][Tt][Ee] +[Oo][Rr] +[Rr][Ee][Pp][Ll][Aa][Cc][Ee] +[Pp][Aa][Cc][Kk][Aa][Gg][Ee] ";
        String body = header + "+[Bb][Oo][Dd][Yy] ";
        String withoutSchema = "\\W*(\\w+).*";
        String withSchema = "\\W*(\\w+)\\.\\W*(\\w+).*";
        Pattern headerWithSchema = Pattern.compile(header+withSchema);
        Pattern bodyWithSchema = Pattern.compile(body+withSchema);
        Pattern headerWithout = Pattern.compile(header+withoutSchema);
        Pattern bodyWithout = Pattern.compile(body+withoutSchema);
        Pattern declare = Pattern.compile("\\W*[Dd][Ee][Cc][Ll][Aa][Rr][Ee].*");
        Pattern begin = Pattern.compile("\\W*[Bb][Ee][Gg][Ii][Nn] ");
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
            String line = null;
            while ((line = br.readLine()) != null)
            {
                Matcher headerM = headerWithout.matcher(line);
                Matcher bodyM = bodyWithout.matcher(line);
                Matcher headerWithM = headerWithSchema.matcher(line);
                Matcher bodyWithM = bodyWithSchema.matcher(line);
                Matcher declareM = declare.matcher(line);
                Matcher beginM = begin.matcher(line);
                if (bodyWithM.matches()||bodyM.matches())
                {
                    containsBody = true;
                    break;
                }
                else if (headerWithM.matches()||headerM.matches())
                {
                    containsHeader = true;
                    break;
                } else if (beginM.matches() || declareM.matches()) {
                    break;
                }
            }
            br.close();
            if (containsBody && containsHeader)
            {
                return ParseType.Package_Header_And_Body;
            }
            if (containsBody)
            {
                return ParseType.Package_Body;
            }
            if (containsHeader)
            {
                return ParseType.Package;
            }
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return ParseType.SqlScript;
    }

    /**
     * This method gets the schema name from the file if it is defined
     *
     * @param the body of the file
     *
     * @return The name of the schema.
     */
    public static String getSchemaName(String str)
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
        String header = "\\W*[Cc][Rr][Ee][Aa][Tt][Ee] +[Oo][Rr] +[Rr][Ee][Pp][Ll][Aa][Cc][Ee] +[Pp][Aa][Cc][Kk][Aa][Gg][Ee] ";
        String body = header + "+[Bb][Oo][Dd][Yy] ";
        String withoutSchema = "\\W*(\\w+).*";
        String withSchema = "\\W*(\\w+)\\.\\W*(\\w+).*";
        Pattern headerWithSchema = Pattern.compile(header+withSchema);
        Pattern bodyWithSchema = Pattern.compile(body+withSchema);
        Pattern headerWithout = Pattern.compile(header+withoutSchema);
        Pattern bodyWithout = Pattern.compile(body+withoutSchema);
        String schemaName = null;
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(bis));
            String line = null;
            while ((line = br.readLine()) != null)
            {
                Matcher headerM = headerWithout.matcher(line);
                Matcher bodyM = bodyWithout.matcher(line);
                Matcher headerWithM = headerWithSchema.matcher(line);
                Matcher bodyWithM = bodyWithSchema.matcher(line);
                if (bodyWithM.matches()) {
                    schemaName = bodyWithM.group(1);
                    break;
                } else if (headerWithM.matches()) {
                    schemaName = headerWithM.group(1);
                    break;
                } else if (bodyM.matches() || headerM.matches()) {
                    return null;
                }
            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return schemaName;
    }

    /**
     * This method gets the package name from the file. If this is not a package header or body, then null is returned.
     *
     * @param file The file whose package name is sought.
     *
     * @return The name of the package.
     */
    public static String getPackageName(IFile file)
    {
        String header = "\\W*[Cc][Rr][Ee][Aa][Tt][Ee] +[Oo][Rr] +[Rr][Ee][Pp][Ll][Aa][Cc][Ee] +[Pp][Aa][Cc][Kk][Aa][Gg][Ee] ";
        String body = header + "+[Bb][Oo][Dd][Yy] ";
        String withoutSchema = "\\W*(\\w+).*";
        String withSchema = "\\W*(\\w+)\\.\\W*(\\w+).*";
        Pattern headerWithSchema = Pattern.compile(header+withSchema);
        Pattern bodyWithSchema = Pattern.compile(body+withSchema);
        Pattern headerWithout = Pattern.compile(header+withoutSchema);
        Pattern bodyWithout = Pattern.compile(body+withoutSchema);
        String packageName = null;
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
            String line = null;
            while ((line = br.readLine()) != null)
            {
                Matcher headerM = headerWithout.matcher(line);
                Matcher bodyM = bodyWithout.matcher(line);
                Matcher headerWithM = headerWithSchema.matcher(line);
                Matcher bodyWithM = bodyWithSchema.matcher(line);
                if (bodyWithM.matches()) {
                    packageName = bodyWithM.group(2);
                    break;
                } else if (headerWithM.matches()) {
                    packageName = headerWithM.group(2);
                    break;
                } else if (bodyM.matches()) {
                    packageName = bodyM.group(1);
                    break;
                } else if (headerM.matches()){
                    packageName = headerM.group(1);
                    break;
                }
            }
            br.close();
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return packageName;
    }

    public synchronized static PlSqlParserManager instance()
    {
        if (theInstance == null)
        {
            theInstance = new PlSqlParserManager();
        }
        return theInstance;
    }

    public PlSqlParser getParser(ParseType type) throws IOException
    {
        if (type == ParseType.Package_Body)
        {
            return myPackageBodyParser;
        }
        else if (type == ParseType.Package)
        {
            return myPackageHeaderParser;
        }
        else if (type == ParseType.Package_Header_And_Body)
        {
            return myPackageHeaderAndBodyParser;
        }
        else
        // if (type == ParseType.SqlScript)
        {
            return mySqlScriptParser;
        }
    }

    public static final ParseType getType(IFile file)
    {
        String filename = file.getName();
        if (filename.indexOf(".pkb") != -1)
        {
            return ParseType.Package_Body;
        }
        else if (filename.indexOf(".pkh") != -1)
        {
            return ParseType.Package;
        }
        else if (filename.indexOf(".pkg") != -1)
        {
            return ParseType.Package_Header_And_Body;
        }
        else
        {
            return getTypeFromContext(file);
        }
    }

    public Segment findNextSegment(List<?> segments, int offset)
    {
        return findNextSegment(segments, offset, true);
    }

    public Segment findNextSegment(List<?> segments, int offset, boolean goDeep)
    {
        Segment foundSegment = null;
        for (Iterator<?> it = segments.iterator(); it.hasNext();)
        {
            Segment s = (Segment) it.next();
            if (goDeep)
            {
                List<?> containedSegments = s.getContainedSegments();
                foundSegment = findNextSegment(containedSegments, offset);
                if (foundSegment != null)
                {
                    break;
                }
            }
            if (s.getPosition().getOffset() > offset)
            {
                foundSegment = s;
                break;
            }
        }
        return foundSegment;
    }

    public Segment findPreviousSegment(List<?> segments,
                                       int offset,
                                       boolean goDeep,
                                       boolean useInternal)
    {
        boolean[] success = new boolean[1];
        success[0] = false;
        Segment toReturn = findPreviousSegment(segments, offset, goDeep, success, false);
        return toReturn;
    }

    /**
     * This method gets the first non code segment located previous to the
     * supplied <code>offset</code> within the list of supplied
     * <code>segments</code>.
     *
     * @param segments
     * @param offset
     * @param goDeep
     * @param lastInternal
     * @param success An array of length 1 which will contain <code>true</code>
     *            if the returned value is previous to the supplied offset, and
     *            <code>false</code> if the returned value is the next value
     *            after the supplied offset (because a previous one was not
     *            within the supplied segments list.
     * @return
     */
    public Segment findPreviousSegment(List<?> segments, int offset, boolean goDeep, boolean[] success, boolean isParentPackage)
    {
        if (segments.isEmpty())
        {
            return null;
        }
        TreeSet<Object> positionSortedSet = new TreeSet<Object>(thePositionComparator);
        positionSortedSet.addAll(segments);
        Segment previousSegment = null;
        for (Iterator<?> it = positionSortedSet.iterator(); it.hasNext();)
        {
            Segment s = (Segment) it.next();
            if (s.getType() == SegmentType.Code)
            {
                continue;
            }
            if (s.getPosition().getOffset() > offset)
            {
                if (previousSegment != null)
                {
                    success[0] = true;
                    return previousSegment;
                }
                else
                {
                    success[0] = false;
                    return s;
                }
            }
            if (goDeep)
            {
                List<?> containedSegments = s.getContainedSegments();
                Segment foundSegment = findPreviousSegment(containedSegments,
                                                           offset,
                                                           goDeep,
                                                           success,
                                                           s instanceof PackageSegment);
                if (success[0] && foundSegment != null)
                {
                    return foundSegment;
                }
                if (!it.hasNext() && foundSegment != null && isParentPackage)
                {
                    s = foundSegment;
                }
                if (s instanceof PackageSegment && foundSegment != null)
                {
                    //success[0] = true;
                    return foundSegment;
                }
            }
            previousSegment = s;
        }
        success[0] = false;
        return previousSegment;
    }

    /**
     * This method finds the segment (i.e a parameter declaration) with the name
     * equivalent to the supplied <code>identifier</code> that is referenced
     * (i.e a parameter reference) at the supplied <code>documentOffset</code>.
     *
     * @param segments The segments to search through
     *
     * @param identifier The name of the segment we are looking for.
     *
     * @param documentOffset The offset at which the segment was <b>referenced</b>
     *            (i.e not where the segment was declared).
     *
     * @return
     */
    public Segment findNamedSegment(List<?> segments, String identifier, int documentOffset)
    {
        Segment foundSegment = null;
        // TODO fix the last false in the next line - it breaks the loading
        // mechanism
        Segment firstFoundSegment = findPreviousSegment(segments, documentOffset, true, false);
        if (firstFoundSegment != null)
        {
            List<?> containedSegments = firstFoundSegment.getContainedSegments();
            Segment secondFoundSegment = findNamedSegment(containedSegments, identifier, true);
            if (secondFoundSegment != null)
            {
                return secondFoundSegment;
            }
            else if (firstFoundSegment.getName().toUpperCase().equals(identifier.toUpperCase()))
            {
                return firstFoundSegment;
            }
            else
            {
                Object parent = firstFoundSegment.getParent();
                if (parent != null && parent instanceof Segment)
                {
                    secondFoundSegment = (Segment) parent;
                    containedSegments = secondFoundSegment.getContainedSegments();
                    Segment thirdFoundSegment = findNamedSegment(containedSegments, identifier, true);
                    if (thirdFoundSegment != null)
                    {
                        foundSegment = thirdFoundSegment;
                    }
                    else
                    {
                        // may not need this now
//                        foundSegment = findNamedSegment(segments, identifier, secondFoundSegment
//                                .getPosition().offset - 1);
                    }
                }
            }
        }
        if (foundSegment == null)
        {
            foundSegment = findNamedSegment(segments, identifier, false);
        }
        return foundSegment;
    }

    public Segment findNamedSegment(List<?> segments, String identifier, boolean goDeep)
    {
        Segment foundSegment = null;
        for (Iterator<?> it = segments.iterator(); it.hasNext();)
        {
            Segment s = (Segment) it.next();
            if (goDeep)
            {
                List<?> containedSegments = s.getContainedSegments();
                foundSegment = findNamedSegment(containedSegments, identifier, true);
                if (foundSegment != null)
                {
                    break;
                }
            }
            // Resolves Bug Id: 1328347
            // "F3 does not open a func/proc if the case doesn't match"
            if (s.getName().toUpperCase().equals(identifier.toUpperCase()))
            {
                foundSegment = s;
                break;
            }
        }
        return foundSegment;
    }

    public Segment findNextMethod(List<?> segments, int offset)
    {
        Segment foundSegment = findNextSegment(segments, offset);
        if (foundSegment != null)
        {
            SegmentType type = foundSegment.getType();
            if (type == SegmentType.Field)
            {
                foundSegment = foundSegment.getParent();
            }
        }
        return foundSegment;
    }
}
