package plsqleditor.parsers;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;

/**
 * Implements a case-insensitive ANTLR Lexer input stream from and IDocument
 * 
 * @author Toby Zines
 */
public class ANTLRNoCaseStringStream extends ANTLRStringStream
{

    /**
     * @param data
     * @param location
     */
    public ANTLRNoCaseStringStream(char[] data, int location)
    {
        super(data, location);
    }

    /**
     * @param data
     */
    public ANTLRNoCaseStringStream(String data)
    {
        super(data);
    }

    public int LA(int i)
    {
        if (i == 0)
        {
            return 0;
        }
        if (i < 0)
        {
            i++;
        }
        if ((p + i - 1) >= n)
        {
            return CharStream.EOF;
        }
        return Character.toLowerCase(data[p + i - 1]);
    }
}
