package plsqleditor.parsers;

import junit.framework.TestCase;

public class TestPackageBodyParser extends TestCase
{
    public void testIsStringInQuotesDoubleEnd()
    {
        String line = "    ln_end := dbms_lob.instr(pic_clob_text,'### CONFIGURATION END ###', ln_start,1)";
        assertEquals(PackageBodyParser.isStringInQuotes(line, "([Ee][Nn][Dd])"), true);
    }

    public void testIsStringInQuotesDoubleEndLineEnd()
    {
        String line = "    ln_end := dbms_lob.instr(pic_clob_text,'### CONFIGURATION END";
        assertEquals(PackageBodyParser.isStringInQuotes(line, "([Ee][Nn][Dd])"), true);
    }

    public void testIsStringInQuotesDoubleEndSendLineEnd()
    {
        String line = "    ln_end := dbms_lob.instr(pic_clob_text,'### CONFIGURATION SEND";
        assertEquals(PackageBodyParser.isStringInQuotes(line, "([Ee][Nn][Dd])"), true);
    }

    public void testIsStringInQuotesDoubleEndSendLineEndWithPriorEnd()
    {
        String line = "    end if; dbms_output.put_line(pic_clob_text,'### CONFIGURATION SEND";
        assertEquals(PackageBodyParser.isStringInQuotes(line, "([Ee][Nn][Dd])"), false);
    }

    public void testIsStringInQuotesDoubleEndLineEndPlusSpace()
    {
        String line = "    ln_end := dbms_lob.instr(pic_clob_text,'### CONFIGURATION END ";
        assertEquals(PackageBodyParser.isStringInQuotes(line, "([Ee][Nn][Dd])"), true);
    }

    public void testIsStringInQuotesEndLineStart()
    {
        String line = "end if; dbms_output.put_line('### CONFIGURATION END ###')";
        assertEquals(PackageBodyParser.isStringInQuotes(line, "([Ee][Nn][Dd])"), false);
    }

    public void testIsStringInQuotesEndingLineStart()
    {
        String line = "ending := dbms_lob.instr(pic_clob_text,'### CONFIGURATION END ###', ln_start,1)";
        assertEquals(PackageBodyParser.isStringInQuotes(line, "([Ee][Nn][Dd])"), true);
    }
}
