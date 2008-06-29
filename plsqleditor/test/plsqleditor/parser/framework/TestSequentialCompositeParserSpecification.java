package plsqleditor.parser.framework;

import junit.framework.TestCase;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.util.StringInput;


public class TestSequentialCompositeParserSpecification extends TestCase
{
    public void testParseToken_EqualsB_NoOptional_NoMulti()
    {
        StringInput si = new StringInput(" /* comment first */ := B;");
        DelimiterSpec ds1 = new DelimiterSpec("AssignTo", ":=", true);
        DelimiterSpec ds2 = new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        
        SequentialLoopingParseSpecification scps = new SequentialLoopingParseSpecification("EqualsB");
        scps.addParseSpecification(ds1, 1, 1);
        scps.addParseSpecification(ssp, 1, 1);
        scps.addParseSpecification(ds2, 1, 1);
        
        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        try
        {
            IParseResult result = scps.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(0, startPosition.getOffset());
    
            Position endPosition = result.getEndPosition();
            assertEquals(" /* comment first */ := B".length(), endPosition.getOffset());
    
            assertEquals(" /* comment first */ := B;", result.getText());
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
        DelimiterSpec ds1 = new DelimiterSpec("AssignTo", ":=", true);
        DelimiterSpec ds2 = new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        
        SequentialLoopingParseSpecification scps = new SequentialLoopingParseSpecification("EqualsB");
        scps.addParseSpecification(ssp, 1, 1);
        scps.addParseSpecification(ds1, 1, 1);
        scps.addParseSpecification(ssp, 1, 1);
        scps.addParseSpecification(ds2, 1, 1);

        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        try
        {
            IParseResult result = scps.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(0, startPosition.getOffset());
    
            Position endPosition = result.getEndPosition();
            assertEquals("/*trying something out */ A   /* comment first */ := B".length(), endPosition.getOffset());
    
            assertEquals("/*trying something out */ A   /* comment first */ := B;", result.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }

    public void testParseToken_AEqualsCommaSeparatedListEndDelimited()
    {
        StringInput si1 = new StringInput("A := B,C,D,E;");
        StringInput si2 = new StringInput("A := B,C;");
        StringInput si3 = new StringInput("A := B;");
        StringInput siFail = new StringInput("A := B,;");
        DelimiterSpec assignTo = new DelimiterSpec("AssignTo", ":=", true);
        DelimiterSpec comma = new DelimiterSpec("Comma", ",", true);
        DelimiterSpec endStatement = new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        
        SequentialLoopingParseSpecification main = new SequentialLoopingParseSpecification("Main");
        SequentialLoopingParseSpecification commaList = new SequentialLoopingParseSpecification("CommaList");
        commaList.addParseSpecification(comma, 1, 1);
        commaList.addParseSpecification(ssp, 1, 1);
        
        main.addParseSpecification(ssp, 1, 1);
        main.addParseSpecification(assignTo, 1, 1);
        main.addParseSpecification(ssp, 1, 1);
        main.addParseSpecification(commaList, 0, Integer.MAX_VALUE);
        main.addParseSpecification(endStatement, 1, 1);

        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        StringInput [] sis = new StringInput[] {si1,si2,si3};
        try
        {
            for (int i = 0; i < sis.length; i++)
            {
                StringInput si = sis[i];
                IParseResult result = main.parse(curPos, endPos, si);
                Position startPosition = result.getStartPosition();
                assertEquals(0, startPosition.getOffset());
        
                Position endPosition = result.getEndPosition();
                String txt = si.get();
                assertEquals(txt.length() - 1, endPosition.getOffset());
        
                assertEquals(txt, result.getText());
            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
       
        boolean exceptionCaught = false;
        try
        {
            main.parse(curPos, endPos, siFail);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            exceptionCaught = true;
        }
        assertTrue("Expected to catch exception on siFail", exceptionCaught);
    }

    public void testParseToken_AEqualsCommaSeparatedList_Invalid()
    {
        StringInput si1 = new StringInput("A := B,C,D,E");
        DelimiterSpec assignTo = new DelimiterSpec("AssignTo", ":=", true);
        DelimiterSpec comma = new DelimiterSpec("Comma", ",", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        
        SequentialLoopingParseSpecification main = new SequentialLoopingParseSpecification("Main");
        SequentialLoopingParseSpecification commaList = new SequentialLoopingParseSpecification("CommaList");
        commaList.addParseSpecification(comma, 1, 1);
        commaList.addParseSpecification(ssp, 1, 1);
        
        main.addParseSpecification(ssp, 1, 1);
        main.addParseSpecification(assignTo, 1, 1);
        main.addParseSpecification(ssp, 1, 1);
        main.addParseSpecification(commaList, 0, Integer.MAX_VALUE);

        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        boolean exceptionCaught = false;
        try
        {
            main.parse(curPos, endPos, si1);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            exceptionCaught = true;
        }
        assertTrue("Expect exception to be thrown", exceptionCaught);
    }

    public void testParseToken_AEqualsBNoEndDelimiter()
    {
        StringInput si = new StringInput("/*trying something out */ A   /* comment first */ := B");
        DelimiterSpec ds1 = new DelimiterSpec("AssignTo", ":=", true);
        DelimiterSpec ds2 = new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        
        SequentialLoopingParseSpecification scps = new SequentialLoopingParseSpecification("EqualsB");
        scps.addParseSpecification(ssp, 1, 1);
        scps.addParseSpecification(ds1, 1, 1);
        scps.addParseSpecification(ssp, 1, 1);
        scps.addParseSpecification(ds2, 1, 1);

        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        boolean exceptionCaught = false;
        try
        {
            scps.parse(curPos, endPos, si);
        }
        catch (ParseException e)
        {
            assertTrue(e.getMessage().startsWith("The end of the input was reached while expecting a [\";\"]"));
            assertEquals("/*trying something out */ A   /* comment first */ := B".length(),e.getOffset());
            exceptionCaught = true;
        }
        assertTrue("Expected parse exception ", exceptionCaught);
    }

    public void testParseToken_A_NoEqualsB()
    {
        StringInput si = new StringInput("/*trying something out */ A   /* comment first */  B;");
        DelimiterSpec ds1 = new DelimiterSpec("AssignTo", ":=", true);
        DelimiterSpec ds2 = new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        
        SequentialLoopingParseSpecification scps = new SequentialLoopingParseSpecification("EqualsB");
        scps.addParseSpecification(ssp, 1, 1);
        scps.addParseSpecification(ds1, 1, 1);
        scps.addParseSpecification(ssp, 1, 1);
        scps.addParseSpecification(ds2, 1, 1);

        Position curPos = new Position(0,0);
        Position endPos = new Position(0,0);
        boolean exceptionCaught = false;
        try
        {
            scps.parse(curPos, endPos, si);
        }
        catch (ParseException e)
        {
            assertTrue(e.getMessage().startsWith("Expected the Delimiter [:=]"));
            assertEquals("/*trying something out */ A   /* comment first */  ".length(),e.getOffset());
            exceptionCaught = true;
        }
        assertTrue("Expected parse exception ", exceptionCaught);
    }
}
