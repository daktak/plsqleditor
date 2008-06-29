package plsqleditor.parser.framework;

public class ContainsLoopException extends Exception
{
    public ContainsLoopException(String message)
    {
        super(message);
    }

    public ContainsLoopException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
