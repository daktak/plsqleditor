package plsqleditor.parsers.test;

import plsqleditor.parsers.StringLocationMap;
import junit.framework.TestCase;

public class TestStringLocationMap extends TestCase
{
    public void testGetUnquotedIndexOfStringDashDashNoQuotes()
    {
        String toTest = "line of text with -- in it";
        int index = toTest.indexOf("--");
        int found = StringLocationMap.getUnquotedIndexOfString(toTest, "(--)", 0);
        assertEquals(index, found);
    }

    public void testGetUnquotedIndexOfStringDashDashQuotes()
    {
        String toTest = "line of text with ' -- ' in it";
        int index = -1;
        int found = StringLocationMap.getUnquotedIndexOfString(toTest, "(--)", 0);
        assertEquals(index, found);
    }

    public void testGetUnquotedIndexOfStringDashDashQuotesDashDash()
    {
        String toTest = "line of text with ' -- ' -- in it";
        int index = toTest.indexOf("--");
        index = toTest.indexOf("--", index + 1);
        int found = StringLocationMap.getUnquotedIndexOfString(toTest, "(--)", 0);
        assertEquals(index, found);
    }

    public void testGetUnquotedIndexOfStringPriorDashDashQuotes()
    {
        String toTest = "line of text with -- ' -- ' in it";
        int index = toTest.indexOf("--");
        int found = StringLocationMap.getUnquotedIndexOfString(toTest, "(--)", 0);
        assertEquals(index, found);
    }

    public void testGetUnquotedIndexOfStringQuotes()
    {
        String toTest = "line of text with ' just normal ' in it";
        int index = -1;
        int found = StringLocationMap.getUnquotedIndexOfString(toTest, "(--)", 0);
        assertEquals(index, found);
    }
}
