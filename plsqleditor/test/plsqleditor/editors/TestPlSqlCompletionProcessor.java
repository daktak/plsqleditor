package plsqleditor.editors;

import junit.framework.TestCase;

public class TestPlSqlCompletionProcessor extends TestCase
{
    public void testGetPreviousWord()
    {
        assertEquals("END", PlSqlCompletionProcessor.getPreviousWord("END  the", 3));
        assertEquals("END", PlSqlCompletionProcessor.getPreviousWord("END  the", 4));
        assertEquals("t", PlSqlCompletionProcessor.getPreviousWord("END  the", 5));
        assertEquals("END", PlSqlCompletionProcessor.getPreviousWord("END  the", 2));
        assertEquals("EN", PlSqlCompletionProcessor.getPreviousWord("END  the", 1));
    }
}
