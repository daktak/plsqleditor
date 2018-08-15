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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 26/02/2005
 */
public class ContentOutlineParser
{
    public ContentOutlineParser()
    {
        //
    }
    
    public List<Segment> parseFile(ParseType type,
                          IDocument document,
                          String[] packageName,
                          SegmentType[] toIgnore) throws IOException
    {
        return PlSqlParserManager.instance().getParser(type).parseFile(document, packageName, toIgnore);
    }

    public List<Segment> parseFile(ParseType type,
                          IDocument document,
                          String[] packageName) throws IOException
    {
        return parseFile(type, document, packageName, new SegmentType[] {SegmentType.Code});
    }

    /**
     * This method parses a single section of a <code>document</code> of a particular
     * <code>type</code>.
     * 
     * @param type
     * @param document
     * @param offset
     * @param length
     * @param packageSegment
     * 
     * @return The segments inside the specified section of the document.
     * 
     * @throws IOException
     * @throws BadLocationException
     */
    public List<Segment> parseBodySection(ParseType type,
                                 IDocument document,
                                 int offset,
                                 int length,
                                 Segment packageSegment) throws IOException, BadLocationException
    {
        String toParse = document.get(offset, length);
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                toParse.getBytes())));
        int currentLineOffset = document.getLineOfOffset(offset);
        List<Segment> segments = new ArrayList<Segment>();
        PlSqlParserManager.instance().getParser(type).parseBody(currentLineOffset, document, br, segments, packageSegment);
        return segments;
    }
}
