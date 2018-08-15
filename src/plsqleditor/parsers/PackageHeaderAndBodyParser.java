package plsqleditor.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$ Created on
 *          26/02/2005
 */
public class PackageHeaderAndBodyParser extends AbstractPlSqlParser
{
    private PackageHeaderParser myHeaderParser;
    private PackageBodyParser myBodyParser;
    private boolean myIsInHeaderSection = true;
    
    public PackageHeaderAndBodyParser()
    {
        myHeaderParser = new PackageHeaderParser();
        myBodyParser = new PackageBodyParser();
    }

    protected String getStartOfFilePrefix()
    {
        return myHeaderParser.getStartOfFilePrefix();
    }

    protected String getParametersTerminator()
    {
        if (myIsInHeaderSection)
        {
            return myHeaderParser.getParametersTerminator();
        }
        else
        {
            return myBodyParser.getParametersTerminator();
        }
    }

    protected PackageSegment createPackageSegment(String pkgName, int offset)
    {
        if (myIsInHeaderSection)
        {
            return myHeaderParser.createPackageSegment(pkgName, offset);
        }
        else
        {
            return myBodyParser.createPackageSegment(pkgName, offset);
        }
    }

    public int parseBody(int currentLineOffset,
                            IDocument document,
                            BufferedReader file,
                            List<Segment> segments,
                            Segment packageSegment) throws IOException, BadLocationException
    {
        myIsInHeaderSection = true;
        currentLineOffset = myHeaderParser.parseBody(currentLineOffset, document, file, segments, packageSegment);
        myIsInHeaderSection = false;
        List<Segment> secondSegments = new ArrayList<Segment>();
        String pkgSegName = "unknown";
        if (packageSegment != null)
        {
            pkgSegName = packageSegment.getName();
        }
        String [] pkgName = new String[] {pkgSegName};
        currentLineOffset = myBodyParser.parseHeader(currentLineOffset, document, file, secondSegments, pkgName);
        Segment pkgSegment = getPackageSegment(secondSegments, pkgName[0]);
        currentLineOffset = myBodyParser.parseBody(currentLineOffset, document, file, secondSegments, pkgSegment);
        segments.addAll(secondSegments);
        myIsInHeaderSection = true;
        return currentLineOffset;
    }
}
