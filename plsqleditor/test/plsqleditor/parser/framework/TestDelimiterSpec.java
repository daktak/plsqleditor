package plsqleditor.parser.framework;

import junit.framework.TestCase;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.util.StringInput;

public class TestDelimiterSpec extends TestCase
{
    public void testParseToken_EqualsB()
    {
        StringInput si = new StringInput(" /* comment first */ := B;");
        DelimiterSpec ds = new DelimiterSpec("AssignTo", ":=", true);
        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        try
        {
            IParseResult result = ds.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(0, startPosition.getOffset());
    
            Position endPosition = result.getEndPosition();
            assertEquals(" /* comment first */ ".length(), endPosition.getOffset());
    
            assertEquals(" /* comment first */ :=", result.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }

    public void testParseToken_ParamContainingDelimiterNameInList()
    {
        StringInput si = new StringInput(" /* comment first */INLIST IN(a,b,c);");
        StringInput si2 = new StringInput(" /* comment first */INLIST IN (a,b,c);");
        /* DelimiterSpec comma = */new DelimiterSpec("Comma", ",", true);
        /* DelimiterSpec openBracket = */new DelimiterSpec("OpenBracket", "(", true);
        /* DelimiterSpec closeBracket = */new DelimiterSpec("CloseBracket", ")", true);
        DelimiterSpec in = new DelimiterSpec("In", "IN", false);
        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        try
        {
            
            IParseResult result = null;
            boolean exceptionCaught = false;
            try
            {
                result = in.parse(curPos, endPos, si);
            }
            catch (ParseException pe)
            {
                exceptionCaught = true;
            }
            assertTrue("Expected parse failure on INLIST", exceptionCaught);
            
            curPos.setOffset(" /* comment first */INLIST".length());
            result = in.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(" /* comment first */INLIST".length(), startPosition.getOffset());
            Position endPosition = result.getEndPosition();
            assertEquals(" /* comment first */INLIST ".length(), endPosition.getOffset());
            assertEquals(" IN", result.getText());

            // now try with space lower case
            curPos.setOffset(" /* comment first */INLIST".length());
            result = in.parse(curPos, endPos, si2);
            startPosition = result.getStartPosition();
            assertEquals(" /* comment first */INLIST".length(), startPosition.getOffset());
            endPosition = result.getEndPosition();
            assertEquals(" /* comment first */INLIST ".length(), endPosition.getOffset());
            assertEquals(" IN", result.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }

    public void testParseToken_ParamContainingDelimiterNameInListWithChangingCase()
    {
        StringInput si = new StringInput(" /* comment first */INLIST in(a,b,c);");
        StringInput si2 = new StringInput(" /* comment first */INLIST In(a,b,c);");
        /* DelimiterSpec comma = */new DelimiterSpec("Comma", ",", true);
        /* DelimiterSpec openBracket = */new DelimiterSpec("OpenBracket", "(", true);
        /* DelimiterSpec closeBracket = */new DelimiterSpec("CloseBracket", ")", true);
        DelimiterSpec in = new DelimiterSpec("In", "IN", false);
        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        try
        {
            
            IParseResult result = null;
            boolean exceptionCaught = false;
            try
            {
                result = in.parse(curPos, endPos, si);
            }
            catch (ParseException pe)
            {
                exceptionCaught = true;
            }
            assertTrue("Expected parse failure on INLIST", exceptionCaught);
            
            curPos.setOffset(" /* comment first */INLIST".length());
            result = in.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(" /* comment first */INLIST".length(), startPosition.getOffset());
            Position endPosition = result.getEndPosition();
            assertEquals(" /* comment first */INLIST ".length(), endPosition.getOffset());
            assertEquals(" in", result.getText());

            // now try with space lower case
            curPos.setOffset(" /* comment first */INLIST".length());
            result = in.parse(curPos, endPos, si2);
            startPosition = result.getStartPosition();
            assertEquals(" /* comment first */INLIST".length(), startPosition.getOffset());
            endPosition = result.getEndPosition();
            assertEquals(" /* comment first */INLIST ".length(), endPosition.getOffset());
            assertEquals(" In", result.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }

    public void testParseToken_EqualsBNoSpace()
    {
        StringInput si = new StringInput(" /* comment first */ :=B;");
        DelimiterSpec ds = new DelimiterSpec("AssignTo", ":=", true);
        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        try
        {
            IParseResult result = ds.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(0, startPosition.getOffset());
    
            Position endPosition = result.getEndPosition();
            assertEquals(" /* comment first */ ".length(), endPosition.getOffset());
    
            assertEquals(" /* comment first */ :=", result.getText());
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
        DelimiterSpec ds = new DelimiterSpec("AssignTo", ":=", true);
        Position curPos = new Position("/*trying something out */ A".length(),0);
        Position endPos = new Position(0,0);
        try
        {
            IParseResult result = ds.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals("/*trying something out */ A".length(), startPosition.getOffset());
    
            Position endPosition = result.getEndPosition();
            assertEquals("/*trying something out */ A   /* comment first */ ".length(), endPosition.getOffset());
    
            assertEquals("   /* comment first */ :=", result.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }
    public void testParseToken_AEqualsBFoundNonDelimiter()
    {
        StringInput si = new StringInput(" /* comment first */A := B;");
        DelimiterSpec ds = new DelimiterSpec("AssignTo", ":=", true);
        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        boolean caughtException = false;
        try
        {
            ds.parse(curPos, endPos, si);
        }
        catch (ParseException e)
        {
            caughtException = true;
        }
        assertEquals(true, caughtException);
    }

    public void testParseToken_AEqualsBFoundWrongDelimiter()
    {
        StringInput si = new StringInput("/*trying something out */ A   /* comment first */ := B;");
        new DelimiterSpec("AssignTo", ":=", true); // create a valid delimiter to find
        DelimiterSpec ds2 = new DelimiterSpec("EndOfStatement", ";", true);
        Position curPos = new Position("/*trying something out */ A".length(),0);
        Position endPos = new Position(0,0);
        String errorMsg = "not caught";
        try
        {
            ds2.parse(curPos, endPos, si);
        }
        catch (ParseException e)
        {
            errorMsg = e.getMessage();
        }
        assertTrue("Exception incorrect", errorMsg.indexOf("Expected the Delimiter") != 1);
    }
}
