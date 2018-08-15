/**
 * 
 */
package plsqleditor.formatter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Toby Zines
 * 
 */
public class PlSqlCodeFormatter
{
    private int                      myLineLength          = 80;
    private static final String      COMMENT_PAD           = " * ";
    private boolean                  myIsKeepingEmptyLines = true;
    private int                      myNumKeptEmptyLines   = 2;
    private String                   myOneTabSpaces        = "    ";
    //private static final char        backslash             = '\\';
    private static final char        doublequote           = '"';
    private static final char        quote                 = '\'';
    private static final char        forwardslash          = '/';
    private static final char        asterisk              = '*';
    private static final char        dash                  = '-';

    
    private boolean myIsAtBeginningOfLine = true;

    private static final Set<String> beginStrings;
    private static final Set<String> endStrings;
    private static final String newLine;

    static
    {
        beginStrings = new HashSet<String>();
        beginStrings.add("BEGIN");
        beginStrings.add("PROCEDURE");
        beginStrings.add("FUNCTION");
        endStrings = new HashSet<String>();
        endStrings.add("END");
        
        newLine = System.getProperty("line.separator");

    }

    public PlSqlCodeFormatter()
    {
        // use defaults
    }
    
    /**
     * 
     */
    public PlSqlCodeFormatter(int linelength, boolean keepEmptyLines, int numEmptyLines, int indentSpacing)
    {
        myLineLength = linelength;
        myIsKeepingEmptyLines = keepEmptyLines;
        myNumKeptEmptyLines = numEmptyLines;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indentSpacing; i++)
        {
            sb.append(" ");
        }
        myOneTabSpaces = sb.toString();
    }

    public String format(InputStream instream) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(instream));
        String line = null;
        boolean isInComment = false;
        StringBuffer currentLine = new StringBuffer(myLineLength);
        StringBuffer fullText = new StringBuffer();
        int currentEmptyLineCount = 0;
        boolean isIgnoringEmptyLines = false;
        String padSpaces = "";
        String last = null;

        StringBuffer lastWord = new StringBuffer();

        while ((line = br.readLine()) != null)
        {
            if (line.trim().length() == 0)
            {
                if (myIsKeepingEmptyLines && !isIgnoringEmptyLines)
                {
                    currentEmptyLineCount++;
                    if (myNumKeptEmptyLines <= currentEmptyLineCount)
                    {
                        fullText.append(newLine);
                    }
                    else
                    {
                        currentEmptyLineCount = 0;
                        isIgnoringEmptyLines = true;
                    }
                }
            }
            else
            {
                isIgnoringEmptyLines = false;
                char[] characters = line.toCharArray();
                for (int i = 0; i < characters.length; i++)
                {
                    char c = characters[i];
                    if (Character.isWhitespace(c) && myIsAtBeginningOfLine)
                    {
                        continue;
                    }
                    currentLine.append(c);
                    if (c == quote || c == doublequote)
                    {
                        lastWord = new StringBuffer(c);
                        i = captureQuotedString(c, i, characters, lastWord, currentLine);
                    }
                    else if (c == asterisk && isInComment)
                    {
                        if (checkCommenting(i, forwardslash, characters, lastWord, currentLine))
                        {
                            i++;
                            isInComment = false;
                        }
                    }
                    else if (c == forwardslash && !isInComment)
                    {
                        if (checkCommenting(i, asterisk, characters, lastWord, currentLine))
                        {
                            i++;
                            isInComment = true;
                        }
                    }
                    else if (c == dash && !isInComment)
                    {
                        if (checkCommenting(i, dash, characters, lastWord, currentLine))
                        {
                            i+=2;
                            for (; i < characters.length; i++)
                            {
                                currentLine.append(characters[i]);
                            }
                            lastWord = new StringBuffer();
                            appendNewText(fullText, currentLine, "", false, padSpaces, true);
                            continue;
                        }
                    }
                    // else if (c == backslash) { // dunno yet }
                    else if (!Character.isJavaIdentifierPart(c))
                    {
                        last = lastWord.toString();
                        lastWord = new StringBuffer();
                    }
                    else
                    {
                        last = null;
                        lastWord.append(c);
                    }

                    myIsAtBeginningOfLine = false;
                    
                    if (last != null && !isInComment && beginStrings.contains(last))
                    {
                        appendNewText(fullText, currentLine, last, isInComment, padSpaces, false);
                        padSpaces += myOneTabSpaces;
                        appendNewText(fullText, currentLine, "", isInComment, padSpaces, true);
                        lastWord = new StringBuffer();
                    }
                    else if (last != null && !isInComment && endStrings.contains(last))
                    {
                        currentLine.replace(0,myOneTabSpaces.length(), "");
                        padSpaces = padSpaces.replaceFirst(myOneTabSpaces, "");
                        appendNewText(fullText, currentLine, "", isInComment, padSpaces, true);
                        lastWord = new StringBuffer();
                    }
                    else if (currentLine.length() >= myLineLength)
                    {
                        appendNewText(fullText, currentLine, lastWord.toString(), isInComment, padSpaces, false);
                    }
                    last = null;
                }
            }
        }
        return fullText.toString();
    }

    private void appendNewText(StringBuffer fullText,
                               StringBuffer currentLine,
                               String lastWord,
                               boolean isInComment,
                               String padSpaces,
                               boolean keepExtraChar)
    {
        fullText.append(currentLine.substring(0, (currentLine.length() - (keepExtraChar ? 0 : 1)) - lastWord.length()));
        fullText.append(newLine);
        currentLine.replace(0,currentLine.length(), "");
        currentLine.append(padSpaces);
        if (isInComment)
        {
            currentLine.append(COMMENT_PAD);
        }
        // add the last word
        currentLine.append(lastWord);
        myIsAtBeginningOfLine = true;
    }

    private boolean checkCommenting(int index,
                                    char secondChar,
                                    char[] characters,
                                    StringBuffer lastWord,
                                    StringBuffer currentLine)
    {
        char c = characters[index];
        if (characters.length == index + 1)
        {
            lastWord.append(c);
        }
        else if (characters[index + 1] == secondChar)
        {
            lastWord.append(secondChar);
            currentLine.append(secondChar);
            return true;
        }
        else
        {
            lastWord.append(c);
        }
        return false;
    }

    private int captureQuotedString(char c,
                                    int index,
                                    char[] characters,
                                    StringBuffer lastWord,
                                    StringBuffer currentLine)
    {
        for (int i = index + 1; i < characters.length; i++)
        {
            lastWord.append(c);
            currentLine.append(c);
            if (characters[i] == c)
            {
                return i;
            }
        }
        throw new IllegalStateException("Failed to capture quoted string");
    }
    
    public static void main (String [] args)
    {
        PlSqlCodeFormatter formatter = new PlSqlCodeFormatter();
        
        String text = "/**\r\n" + 
                "     * This method resolves the service changes for a particular ip service object.\r\n" + 
                "     * \r\n" + 
                "     * @param pin_service_object_id The id of the service to resolve.\r\n" + 
                "     */\r\n" + 
                "    PROCEDURE resolve_service_changes(pin_service_object_id IN NUMBER) IS\r\n" + 
                "    BEGIN \r\n" + 
                "-- TODO need to check if this resolves service changes to sub objects\r\n" + 
                "         -- may have to go to sub objects (or particular sub objects) and resolve\r\n" + 
                "    -- those too.\r\n" + 
                "    custom.solutions.resolve_service_changes(pin_service_object_id); \r\n" + 
                "    END;\r\n" + 
                "    ";
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes());
        try
        {
            System.out.println(formatter.format(bis));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
