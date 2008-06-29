package plsqleditor.parser.framework;

import junit.framework.TestCase;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.util.StringInput;

public class TestOrGroup extends TestCase
{
    public void testParseToken_AEqualsB()
    {
        StringInput si = new StringInput("/*trying something out */ A   /* comment first */ := B;");
        DelimiterSpec ds1 = new DelimiterSpec("AssignTo", ":=", true);
        DelimiterSpec ds2 = new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        OrGroup orGroup = new OrGroup("VariableOrAssignOrEndStatement");
        orGroup.addParseSpecification(ds1, 1, 1);
        orGroup.addParseSpecification(ds2, 1, 1);
        orGroup.addParseSpecification(ssp, 1, 1);
        Position curPos = new Position(0, 0);
        Position endPos = new Position(0, 0);
        try
        {
            // get the A
            IParseResult result = orGroup.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(0, startPosition.getOffset());

            Position endPosition = result.getEndPosition();
            assertEquals("/*trying something out */ ".length(), endPosition.getOffset());

            assertEquals("/*trying something out */ A", result.getText());

            // get the :=
            curPos = endPos;
            endPos = new Position(0, 0);
            result = orGroup.parse(curPos, endPos, si);

            startPosition = result.getStartPosition();
            assertEquals("/*trying something out */ A".length(), startPosition.getOffset());

            endPosition = result.getEndPosition();
            assertEquals("/*trying something out */ A   /* comment first */ ".length(), endPosition
                    .getOffset());

            assertEquals("   /* comment first */ :=", result.getText());

            // get the B
            curPos = endPos;
            endPos = new Position(0, 0);
            result = orGroup.parse(curPos, endPos, si);
            startPosition = result.getStartPosition();
            assertEquals("/*trying something out */ A   /* comment first */ :=".length(),
                         startPosition.getOffset());

            endPosition = result.getEndPosition();
            assertEquals("/*trying something out */ A   /* comment first */ := ".length(),
                         endPosition.getOffset());

            assertEquals(" B", result.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }
    
    public void testParseToken_AEqualsBWithOutEquals()
    {
        StringInput si = new StringInput("/*trying something out */ A   /* comment first */ := B;");
        new DelimiterSpec("AssignTo", ":=", true);
        DelimiterSpec ds2 = new DelimiterSpec("EndStatement", ";", true);
        SimpleParseSpecification ssp = new SimpleParseSpecification("Variable");
        OrGroup orGroup = new OrGroup("VariableOrAssignOrEndStatement");
        orGroup.addParseSpecification(ds2, 1, 1);
        orGroup.addParseSpecification(ssp, 1, 1);
        Position curPos = new Position(0, 0);
        Position endPos = new Position(0, 0);
        try
        {
            // get the A
            IParseResult result = orGroup.parse(curPos, endPos, si);
            Position startPosition = result.getStartPosition();
            assertEquals(0, startPosition.getOffset());

            Position endPosition = result.getEndPosition();
            assertEquals("/*trying something out */ ".length(), endPosition.getOffset());

            assertEquals("/*trying something out */ A", result.getText());

            // get the :=
            curPos = endPos;
            endPos = new Position(0, 0);
            boolean exceptionCaught = false;
            try
            {
                result = orGroup.parse(curPos, endPos, si);
            }
            catch (ParseException e)
            {
                exceptionCaught = true;
                e.printStackTrace();
            }
            assertTrue("Expected to catch exception", exceptionCaught);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            assertTrue("Caught parse exception ", false);
        }
    }

}
