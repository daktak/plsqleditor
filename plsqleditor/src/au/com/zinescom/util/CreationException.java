/**
 * 
 */
package au.com.zinescom.util;

/**
 * @author Toby Zines
 *
 */
public class CreationException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 3618417137756943415L;

    /**
     * 
     */
    public CreationException()
    {
        super();
    }

    /**
     * @param message
     */
    public CreationException(String message)
    {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public CreationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public CreationException(Throwable cause)
    {
        super(cause);
    }

}
