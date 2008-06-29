//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\util\\ParseToken.java

package plsqleditor.parser.util;

import org.eclipse.jface.text.Position;

/**
 * Delimiter (as determined by the list of delimiter specs passede to the TokenGrabber (that generated this ParseToken)
 * on construction.
 */
public class ParseToken
{
    private String   myPreTokenString;
    private Position myPreTokenPosition;
    private String   myToken;
    private Position myTokenPosition;
    private boolean myIsDelimiter;

    /**
     * @param whiteSpaceAndComments
     * @param wscPos
     * @param token
     * @param tokenPos
     * @roseuid 4313990B02BF
     */
    public ParseToken(String whiteSpaceAndComments, Position wscPos, String token, Position tokenPos, boolean isDelimiter)
    {
        myPreTokenString= whiteSpaceAndComments;
        myPreTokenPosition = wscPos;
        myToken = token;
        myTokenPosition = tokenPos;
        myIsDelimiter = isDelimiter;
    }

    /**
     * @return java.lang.String
     * @roseuid 4313A2E90213
     */
    public String getToken()
    {
        return myToken;
    }

    /**
     * @return javax.swing.text.Position
     * @roseuid 4313A2EE033C
     */
    public Position getTokenPosition()
    {
        return myTokenPosition;
    }

    /**
     * @return java.lang.String
     * @roseuid 4313A2F401C5
     */
    public String getPreTokenString()
    {
        return myPreTokenString;
    }

    /**
     * @return javax.swing.text.Position
     * @roseuid 4313A2FB00EA
     */
    public Position getPreTokenPosition()
    {
        return myPreTokenPosition;
    }

    /**
     * This method indicates whether the existing ParseToken is a delimiter or not (if not, it must be a standard Token.
     * 
     * @return boolean
     * @roseuid 43190E22029F
     */
    public boolean isDelimiter()
    {
        return myIsDelimiter;
    }
}
