package plsqleditor.parser.framework;

import junit.framework.TestCase;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.util.StringInput;

public class TestSimpleParserSpecification extends TestCase
{
    public void testParseToken_EqualsB()
    {
        StringInput si = new StringInput(" /* comment first */ := B;");
        new DelimiterSpec("AssignTo", ":=", true);
        new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        Position curPos = new Position(" /* comment first */ :=".length(),0);
        Position endPos = new Position(0,0);
        try
        {
            IParseResult result = ssp.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(" /* comment first */ :=".length(), startPosition.getOffset());
    
            Position endPosition = result.getEndPosition();
            assertEquals(" /* comment first */ := ".length(), endPosition.getOffset());
    
            assertEquals(" B", result.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }

    public void testParseToken_AEqualsB()
    {
        StringInput si = new StringInput("/*trying something out */ A   /* comment first */ := B;");
        new DelimiterSpec("AssignTo", ":=", true);
        new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        try
        {
            IParseResult result = ssp.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(0, startPosition.getOffset());
    
            Position endPosition = result.getEndPosition();
            assertEquals("/*trying something out */ ".length(), endPosition.getOffset());
    
            assertEquals("/*trying something out */ A", result.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }

    public void testParseToken_AEqualsBMultiline()
    {
        String preDelimiter = 
            "\n   " +
            "\n" +
            "  /**\n" +
            "   * commenting\n" +
            "   * on many lines \n" +
            "   */\n" +
            "/*trying something out */ ";
            
        StringInput si = new StringInput(preDelimiter + "A   /* comment first */ := B;");
        new DelimiterSpec("AssignTo", ":=", true);
        new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        try
        {
            IParseResult result = ssp.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(0, startPosition.getOffset());
    
            Position endPosition = result.getEndPosition();
            assertEquals(preDelimiter.length(), endPosition.getOffset());
    
            assertEquals(preDelimiter + "A", result.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }

    public void testParseToken_AEqualsBFoundDelimiter()
    {
        StringInput si = new StringInput(" /* comment first */ := B;");
        new DelimiterSpec("AssignTo", ":=", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        boolean caughtException = false;
        try
        {
            ssp.parse(curPos, endPos, si);
        }
        catch (ParseException e)
        {
            caughtException = true;
        }
        assertEquals(true, caughtException);
    }

    public void testParseToken_ABC()
    {
        StringInput si = new StringInput("/*trying something out */ A   /* comment first */B C;");
        new DelimiterSpec("AssignTo", ":=", true);
        new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        try
        {
            // get the A
            IParseResult result = ssp.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(0, startPosition.getOffset());
    
            Position endPosition = result.getEndPosition();
            assertEquals("/*trying something out */ ".length(), endPosition.getOffset());
    
            assertEquals("/*trying something out */ A", result.getText());

            // get the B
            curPos = endPos;
            endPos = new Position(0,0);
            result = ssp.parse(curPos, endPos, si);
            
            startPosition = result.getStartPosition();
            assertEquals("/*trying something out */ A".length(), startPosition.getOffset());
    
            endPosition = result.getEndPosition();
            assertEquals("/*trying something out */ A   /* comment first */".length(), endPosition.getOffset());
    
            assertEquals("   /* comment first */B", result.getText());
            
            // get the C
            curPos = endPos;
            endPos = new Position(0,0);
            result = ssp.parse(curPos, endPos, si);
            startPosition = result.getStartPosition();
            assertEquals("/*trying something out */ A   /* comment first */B".length(), startPosition.getOffset());
    
            endPosition = result.getEndPosition();
            assertEquals("/*trying something out */ A   /* comment first */B ".length(), endPosition.getOffset());
    
            assertEquals(" C", result.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }
}
