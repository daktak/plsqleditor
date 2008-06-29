package plsqleditor.parser.util;

import junit.framework.TestCase;

public class TestTokenGrabber extends TestCase
{
    private class DelimiterSpec implements IDelimiterSpecification
    {
        private String myString;
        private boolean myIsStandalone;

        public DelimiterSpec(String delimiter, boolean isStandalone)
        {
            myString = delimiter;
            myIsStandalone = isStandalone;
        }

        public int length()
        {
            return myString.length();
        }

        public char firstChar()
        {
            return myString.charAt(0);
        }

        public String getString()
        {
            return myString;
        }

        public boolean isStandaloneDelimiter()
        {
            return myIsStandalone;
        }
    }

    public void testParseToken_AEqualsB()
    {
        StringInput si = new StringInput("A := B ");
        DelimiterSpec ds = new DelimiterSpec(":=", true);
        TokenGrabber tg = new TokenGrabber(si, 0, new IDelimiterSpecification[]{ds});
        ParseToken pt = tg.nextToken();
        assertEquals(pt.getToken(), "A");
        pt = tg.nextToken();
        assertEquals(pt.getToken(), ":=");
        pt = tg.nextToken();
        assertEquals(pt.getToken(), "B");
    }

    public void testParseToken_AEqualsB_NoPreTokenSpace()
    {
        StringInput si = new StringInput("A:=B");
        DelimiterSpec ds = new DelimiterSpec(":=", true);
        TokenGrabber tg = new TokenGrabber(si, 0, new IDelimiterSpecification[]{ds});
        ParseToken pt = tg.nextToken();
        assertEquals(pt.getToken(), "A");
        pt = tg.nextToken();
        assertEquals(pt.getToken(), ":=");
        pt = tg.nextToken();
        assertEquals(pt.getToken(), "B");
    }

    public void testParseToken_AEqualsBNoEndingSpace()
    {
        StringInput si = new StringInput("A := B");
        DelimiterSpec ds = new DelimiterSpec(":=", true);
        TokenGrabber tg = new TokenGrabber(si, 0, new IDelimiterSpecification[]{ds});
        ParseToken pt = tg.nextToken();
        assertEquals(pt.getToken(), "A");
        pt = tg.nextToken();
        assertEquals(pt.getToken(), ":=");
        pt = tg.nextToken();
        assertEquals(pt.getToken(), "B");
    }

    public void testParseToken_AEqualsBWithComments()
    {
        StringInput si = new StringInput(" /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B ");
        DelimiterSpec ds = new DelimiterSpec(":=", true);
        TokenGrabber tg = new TokenGrabber(si, 0, new IDelimiterSpecification[]{ds});
        ParseToken pt = tg.nextToken();
        assertEquals("A", pt.getToken());
        assertEquals("A".length(), pt.getTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ ".length(), pt.getTokenPosition().getOffset());
        assertEquals(" /*  asdfdd    \n afddasf */ ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(0, pt.getPreTokenPosition().getOffset());
        assertEquals(" /*  asdfdd    \n afddasf */ ", pt.getPreTokenString());

        pt = tg.nextToken();
        assertEquals(":=", pt.getToken());
        assertEquals(":=".length(), pt.getTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A ".length(), pt.getTokenPosition().getOffset());
        assertEquals(" ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A".length(), pt.getPreTokenPosition().getOffset());
        assertEquals(" ", pt.getPreTokenString());

        pt = tg.nextToken();
        assertEquals("B", pt.getToken());
        assertEquals("B".length(), pt.getTokenPosition().getLength());
        assertEquals(pt.getTokenPosition().getOffset(), " /*  asdfdd    \n afddasf */ A := -- just testing \r\n    "
                .length());
        assertEquals(" -- just testing \r\n    ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A :=".length(), pt.getPreTokenPosition().getOffset());
        assertEquals(" -- just testing \r\n    ", pt.getPreTokenString());
    }

    public void testParseToken_AEqualsBMinusCWithComments()
    {
        StringInput si = new StringInput(" /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B - C");
        DelimiterSpec ds = new DelimiterSpec(":=", true);
        TokenGrabber tg = new TokenGrabber(si, 0, new IDelimiterSpecification[]{ds});
        ParseToken pt = tg.nextToken();
        assertEquals("A", pt.getToken());
        assertEquals("A".length(), pt.getTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ ".length(), pt.getTokenPosition().getOffset());
        assertEquals(" /*  asdfdd    \n afddasf */ ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(0, pt.getPreTokenPosition().getOffset());
        assertEquals(" /*  asdfdd    \n afddasf */ ", pt.getPreTokenString());

        pt = tg.nextToken();
        assertEquals(":=", pt.getToken());
        assertEquals(":=".length(), pt.getTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A ".length(), pt.getTokenPosition().getOffset());
        assertEquals(" ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A".length(), pt.getPreTokenPosition().getOffset());
        assertEquals(" ", pt.getPreTokenString());

        pt = tg.nextToken();
        assertEquals("B", pt.getToken());
        assertEquals("B".length(), pt.getTokenPosition().getLength());
        assertEquals(pt.getTokenPosition().getOffset(), " /*  asdfdd    \n afddasf */ A := -- just testing \r\n    "
                .length());
        assertEquals(" -- just testing \r\n    ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A :=".length(), pt.getPreTokenPosition().getOffset());
        assertEquals(" -- just testing \r\n    ", pt.getPreTokenString());

        pt = tg.nextToken();
        assertEquals("-", pt.getToken());
        assertEquals("-".length(), pt.getTokenPosition().getLength());
        assertEquals(pt.getTokenPosition().getOffset(), " /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B "
                .length());
        assertEquals(" ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B".length(), pt.getPreTokenPosition().getOffset());
        assertEquals(" ", pt.getPreTokenString());

        pt = tg.nextToken();
        assertEquals("C", pt.getToken());
        assertEquals("C".length(), pt.getTokenPosition().getLength());
        assertEquals(pt.getTokenPosition().getOffset(), " /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B - "
                .length());
        assertEquals(" ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B -".length(), pt.getPreTokenPosition().getOffset());
        assertEquals(" ", pt.getPreTokenString());
}

    public void testParseToken_MultiDelimiters()
    {
        StringInput si = new StringInput(" /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B -C;");
        DelimiterSpec ds1 = new DelimiterSpec(":=", true);
        DelimiterSpec ds2 = new DelimiterSpec("-", true);
        DelimiterSpec ds3 = new DelimiterSpec(";", true);
        TokenGrabber tg = new TokenGrabber(si, 0, new IDelimiterSpecification[]{ds1, ds2, ds3});
        
        // first
        ParseToken pt = tg.nextToken();
        assertEquals("A", pt.getToken());
        assertEquals("A".length(), pt.getTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ ".length(), pt.getTokenPosition().getOffset());
        assertEquals(" /*  asdfdd    \n afddasf */ ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(0, pt.getPreTokenPosition().getOffset());
        assertEquals(" /*  asdfdd    \n afddasf */ ", pt.getPreTokenString());

        // second
        pt = tg.nextToken();
        assertEquals(":=", pt.getToken());
        assertEquals(":=".length(), pt.getTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A ".length(), pt.getTokenPosition().getOffset());
        assertEquals(" ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A".length(), pt.getPreTokenPosition().getOffset());
        assertEquals(" ", pt.getPreTokenString());

        // third
        pt = tg.nextToken();
        assertEquals("B", pt.getToken());
        assertEquals("B".length(), pt.getTokenPosition().getLength());
        assertEquals(pt.getTokenPosition().getOffset(), " /*  asdfdd    \n afddasf */ A := -- just testing \r\n    "
                .length());
        assertEquals(" -- just testing \r\n    ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A :=".length(), pt.getPreTokenPosition().getOffset());
        assertEquals(" -- just testing \r\n    ", pt.getPreTokenString());
        
        // fourth
        pt = tg.nextToken();
        assertEquals("-", pt.getToken());
        assertEquals("-".length(), pt.getTokenPosition().getLength());
        assertEquals(pt.getTokenPosition().getOffset(), " /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B "
                .length());
        assertEquals(" ".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B".length(), pt.getPreTokenPosition().getOffset());
        assertEquals(" ", pt.getPreTokenString());
        
        // fifth
        pt = tg.nextToken();
        assertEquals("C", pt.getToken());
        assertEquals("C".length(), pt.getTokenPosition().getLength());
        assertEquals(pt.getTokenPosition().getOffset(), " /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B -"
                .length());
        assertEquals("".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B -".length(), pt.getPreTokenPosition().getOffset());
        assertEquals("", pt.getPreTokenString());
        
        // sixth
        pt = tg.nextToken();
        assertEquals(";", pt.getToken());
        assertEquals(";".length(), pt.getTokenPosition().getLength());
        assertEquals(pt.getTokenPosition().getOffset(), " /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B -C"
                .length());
        assertEquals("".length(), pt.getPreTokenPosition().getLength());
        assertEquals(" /*  asdfdd    \n afddasf */ A := -- just testing \r\n    B -C".length(), pt.getPreTokenPosition().getOffset());
        assertEquals("", pt.getPreTokenString());
    }
}
