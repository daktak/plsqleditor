package plsqleditor.parser.framework;

import plsqleditor.parser.util.StringInput;
import plsqleditor.parser.util.IInput.BadLocationException;
import junit.framework.TestCase;

public class TestStringInput extends TestCase
{
    public void testGetLineOfOffset() throws BadLocationException
    {
        String str = 
            "\n" +
            "   \n" +
            "  /**\n" +
            "   * commenting\n" +
            "   * on many lines \n" +
            "   */\n" +
            "/*trying something out */ A   /* comment first */ := B;\n";
            
        StringInput si = new StringInput(str);
        assertEquals(0,si.getLineOffset(0));
        for (int i = 1; i < 5; i++)
        {
            assertEquals(1,si.getLineOfOffset(1));
        }
        for (int i = 5; i < 11; i++)
        {
            assertEquals(2,si.getLineOfOffset(i));
        }
        for (int i = 11; i < 27; i++)
        {
            assertEquals(3,si.getLineOfOffset(i));
        }
        for (int i = 27; i < 47; i++)
        {
            assertEquals(4,si.getLineOfOffset(i));
        }
        for (int i = 47; i < 53; i++)
        {
            assertEquals(5,si.getLineOfOffset(i));
        }
        for (int i = 53; i < 109; i++)
        {
            assertEquals(6,si.getLineOfOffset(i));
        }
        assertEquals(7,si.getLineOfOffset(109));
    }
    
    public void testGetLineOffset() throws BadLocationException
    {
        String str = 
            "\n" +
            "   \n" +
            "  /**\n" +
            "   * commenting\n" +
            "   * on many lines \n" +
            "   */\n" +
            "/*trying something out */ A   /* comment first */ := B;\n";
            
        StringInput si = new StringInput(str);
        
        assertEquals(0,si.getLineOffset(0));
        assertEquals(1,si.getLineOffset(1));
        assertEquals(5,si.getLineOffset(2));
        assertEquals(11,si.getLineOffset(3));
        assertEquals(27,si.getLineOffset(4));
        assertEquals(47,si.getLineOffset(5));
        assertEquals(53,si.getLineOffset(6));
        assertEquals(109,si.getLineOffset(7));
    }
}
