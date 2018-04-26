/**
 * 
 */
package plsqleditor.process;

/**
 * This class 
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 16/03/2005 
 *
 */
public class CannotCompleteException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 3257848787840938805L;

    /**
     * @param message
     */
    public CannotCompleteException(String message)
    {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public CannotCompleteException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
