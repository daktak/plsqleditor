/*
 * Created on 26/02/2005
 *
 * @version $Id$
 */
package plsqleditor.parsers;

import java.io.IOException;
import java.util.List;

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
    public enum Type
    {
        Package,Package_Body,SqlScript
    }
    
    private PackageHeaderParser myPackageHeaderParser;
    private PackageBodyParser myPackageBodyParser;

    public ContentOutlineParser()
    {
        myPackageBodyParser = new PackageBodyParser();
        myPackageHeaderParser = new PackageHeaderParser();
    }

    public List<Segment> parseFile(ContentOutlineParser.Type type, IDocument document, String[] packageName) throws IOException
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
}
