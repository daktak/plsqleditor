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
    public enum Type {
        Package, Package_Body, SqlScript
    }

    private PackageHeaderParser myPackageHeaderParser;
    private PackageBodyParser   myPackageBodyParser;

    public ContentOutlineParser()
    {
        myPackageBodyParser = new PackageBodyParser();
        myPackageHeaderParser = new PackageHeaderParser();
    }

    public List<Segment> parseFile(ContentOutlineParser.Type type, IDocument document, String[] packageName)
            throws IOException
    {
        switch (type)
        {
            default :
            case SqlScript :
            case Package :
            {
                return myPackageHeaderParser.parseBodyFile(document, packageName);
            }
            case Package_Body :
            {
                return myPackageBodyParser.parseBodyFile(document, packageName);
            }
        }
    }

    /**
     * This method parses a single section of a <code>document</code> of a particular <code>type</code>.
     * 
     * @param type
     * @param document
     * @param offset
     * @param length
     * @param packageName
     * 
     * @return The segments inside the specified section of the document.
     * 
     * @throws IOException
     * @throws BadLocationException
     */
    public List<Segment> parseBodySection(ContentOutlineParser.Type type,
                                          IDocument document,
                                          int offset,
                                          int length,
                                          String packageName) throws IOException, BadLocationException
    {
        String toParse = document.get(offset, length);
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(toParse.getBytes())));
        int currentLineOffset = document.getLineOfOffset(offset);
        List<Segment> segments = new ArrayList<Segment>();
        switch (type)
        {
            default :
            case SqlScript :
            case Package :
            {
                myPackageHeaderParser.parseBody(currentLineOffset, document, br, segments, packageName);
            }
            case Package_Body :
            {
                myPackageBodyParser.parseBody(currentLineOffset, document, br, segments, packageName);
            }
        }
        return segments;
    }

    public static final Type getType(String filename)
    {
        return filename.contains(".pkb") ? ContentOutlineParser.Type.Package_Body : filename
                .contains(".pkh")
                ? Type.Package
                : Type.SqlScript;
    }
    
}
