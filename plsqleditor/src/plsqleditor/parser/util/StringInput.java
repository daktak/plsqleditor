package plsqleditor.parser.util;


/**
 * 
 */
public class StringInput implements IInput
{
    private String myString;

    public StringInput(String input)
    {
        myString = input;
    }

    public String get()
    {
        return myString;
    }

    public String get(int offset, int length) throws BadLocationException
    {
        try
        {
            return myString.substring(offset, offset + length);
        }
        catch (StringIndexOutOfBoundsException e)
        {
            throw new IInput.BadLocationException("Out of Bounds", e);
        }
    }

    public char getChar(int offset) throws BadLocationException
    {
        try
        {
            return myString.charAt(offset);
        }
        catch (StringIndexOutOfBoundsException e)
        {
            throw new IInput.BadLocationException("Out of Bounds", e);
        }
    }

    public int getLength()
    {
        return myString.length();
    }

    public int getLineOfOffset(int offset) throws BadLocationException
    {
        try
        {
            String s = myString.substring(0,offset);
            int index = -1;
            int count = 0;
            while ((index = s.indexOf("\n", index + 1)) != -1)
            {
                count ++;
            }
            return count;
        }
        catch (StringIndexOutOfBoundsException e)
        {
            throw new IInput.BadLocationException("Out of Bounds", e);
        }
    }

    public int getLineOffset(int line) throws BadLocationException
    {
        try
        {
            int offset = -1;
            for (int i = 0; i < line; i++)
            {
                offset = myString.indexOf("\n", offset + 1);
            }
            return offset + 1;
        }
        catch (StringIndexOutOfBoundsException e)
        {
            throw new IInput.BadLocationException("Out of Bounds", e);
        }
    }

    public String toString()
    {
        return myString;
    }
}