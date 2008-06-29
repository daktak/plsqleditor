//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\IParseResult.java

package plsqleditor.parser.framework;

import org.eclipse.jface.text.Position;

/**
 * This interface represents the result of using an IParseSpecification to parse
 * a document and produce a parse result.
 */
public interface IParseResult
{

    /**
     * This means the first identifiable block of text in the parsed result.
     * This is usually the first whitespace and comments in the block.
     * 
     * @return javax.swing.text.Position
     * @roseuid 43127D1A0148
     */
    public Position getStartPosition();

    /**
     * This method gets the last Position of the parse result. This means the
     * last identifiable block of text in the parsed result.
     * 
     * @return javax.swing.text.Position
     * @roseuid 43127D2B02FD
     */
    public Position getEndPosition();

    /**
     * This gets the location of the first non whitespace or comment position in
     * the parse represented by this result.
     * 
     * @return The location of the first non whitespace or comment position in
     *         the parse represented by this result.
     */
    public Position getFirstNonWhitespacePosition();

    /**
     * This method returns the first non whitespace string in the result.
     * 
     * @return the first non whitespace string in the result.
     */
    public String getFirstToken();
    
    /**
     * @return int
     * @roseuid 43127D3102FD
     */
    public int getLength();

    /**
     * This method returns a single block of text (equivalent to the block of text that was parsed
     * to produce this result).
     * 
     * @return The whole text parsed to produce this result.
     * 
     * @roseuid 43127D3D01A5
     */
    public String getText();
}
