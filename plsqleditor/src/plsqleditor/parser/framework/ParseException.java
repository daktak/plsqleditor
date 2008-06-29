package plsqleditor.parser.framework;

public class ParseException extends Exception
{

    /**
     * This is the serial version id.
     */
    private static final long serialVersionUID = -3420182824234319167L;
    
    /**
     * This is the token on which the parse failed.
     */
    private String myFailedToken;
    private int myOffset;
    private int myLength;

    public ParseException(String message, String token, int offset, int length, Throwable cause)
    {
        super(message, cause);
        myFailedToken = token;
        myOffset = offset;
        myLength = length;
    }

    public ParseException(String message, String token, int offset, int length)
    {
        super(message);
        myFailedToken = token;
        myOffset = offset;
        myLength = length;
    }

    public String getFailedToken()
    {
        return myFailedToken;
    }

    public int getLength()
    {
        return myLength;
    }

    public int getOffset()
    {
        return myOffset;
    }

    public String toString()
    {
        String s = super.toString();
        return "Failed at location [" + getOffset() + ", length " + getLength() + "]" + s;
    }
}
