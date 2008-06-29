//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\SimpleParseResult.java

package plsqleditor.parser.framework;

import org.eclipse.jface.text.Position;

/**
 * It will contain comments and spaces before the actual single token, and it
 * will not be a delimiter (as per the generic superclass).
 */
public class SimpleParseResult extends ParseResult
{
    private String   myCommentsAndWhiteSpace;
    private Position myCommentsAndWhiteSpacePosition;

    private String   myToken;
    private Position myTokenPosition;

    /**
     * @param spec
     * @param cmtsAndWtspc
     * @param cwPos
     * @param token
     * @param tokenPos
     * @roseuid 43127EFC0290
     */
    public SimpleParseResult(IParseSpecification spec,
                             String cmtsAndWtspc,
                             Position cwPos,
                             String token,
                             Position tokenPos)
    {
        super(spec);
        myCommentsAndWhiteSpace = cmtsAndWtspc;
        myCommentsAndWhiteSpacePosition = cwPos;
        myToken = token;
        myTokenPosition = tokenPos;
    }

    /**
     * @return java.lang.String
     * @roseuid 4312820F036B
     */
    public String getCommentsAndWhitespace()
    {
        return myCommentsAndWhiteSpace;
    }

    /**
     * @return javax.swing.text.Position
     * @roseuid 4312821B0271
     */
    public Position getCommentsAndWhitespacePosition()
    {
        return myCommentsAndWhiteSpacePosition;
    }

    /**
     * @return java.lang.String
     * @roseuid 4312822A02BF
     */
    public String getToken()
    {
        return myToken;
    }

    /**
     * @return javax.swing.text.Position
     * @roseuid 4312822E0138
     */
    public Position getTokenPosition()
    {
        return myTokenPosition;
    }

    /**
     * This method effectively returns the call to
     * getCommentsAndWhitespacePosition() as this is guaranteed to be the first
     * position. This can be done by setting the start position on
     * instantiation.
     * 
     * @return javax.swing.text.Position
     * @roseuid 431282DF036B
     */
    public Position getStartPosition()
    {
        return getCommentsAndWhitespacePosition();
    }

    /**
     * This method effectively returns the call to getTokenPosition() as this is
     * guaranteed to be the last position. This can be done by setting the end
     * position on instantiation.
     * 
     * @return javax.swing.text.Position
     * @roseuid 43128528001F
     */
    public Position getEndPosition()
    {
        return getTokenPosition();
    }

    /**
     * This returns the call to getTokenPosition().
     * 
     * @return javax.swing.text.Position
     * @roseuid 431283A700BB
     */
    public Position getFirstNonWhitespacePosition()
    {
        return getTokenPosition();
    }

    /**
     * This method returns the first non whitespace string in the result.
     * 
     * @return the first non whitespace string in the result.
     */
    public String getFirstToken()
    {
        return getToken();
    }
    
    /**
     * This method returns the text that the parse result contains.
     * 
     * @return The comments and single token contained in this parse result.
     */
    public String getText()
    {
        return getCommentsAndWhitespace() + getToken();
    }
}
