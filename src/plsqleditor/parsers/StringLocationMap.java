/**
 * 
 */
package plsqleditor.parsers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a mapping from one location in one string to another
 * location in another string. The idea is that the two strings represent the
 * same thing, but one of them has been formatted for whatever reason. In these
 * cases we need to know index in the first string maps to in the second string.
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 10/03/2005
 * 
 */
public class StringLocationMap
{
    /**
     * This method replaces all the single line comments in the supplied
     * <code>str</code> with closed commments. It ignores single line comments
     * already inside double line comments.
     * 
     * @param str The string to convert.
     * 
     * @return The converted string.
     */
    public static String replacePlSqlSingleLineComments(String str)
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(bis));
        String line = null;
        boolean isCommenting = false;
        StringBuffer sb = new StringBuffer(1000);
        String spaces = System.getProperty("line.separator");
        try
        {
            while ((line = br.readLine()) != null)
            {
                if (isCommenting)
                {
                    if (AbstractPlSqlParser.isEndingComment(line))
                    {
                        isCommenting = false;
                        // TODO check for -- after the end of the comments
                    }
                }
                else if (AbstractPlSqlParser.isStartingComment(line))
                {
                    isCommenting = true;
                    // TODO check for -- before the start of the comment.
                }
                else
                {
                    line = line.replaceAll("^(.*?)--(.*?)..$", "$1/*$2*/");
                    line = line.replaceAll("^(.*?)--(.*?).$", "$1/*$2*/");
                    line = line.replaceAll("^(.*?)--( ?)$", "$1  $2");
                }
                sb.append(line).append(spaces);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * This method replaces all the line.separator lines to just /n lines
     * because oracle doesn't like \r lines.
     * 
     * @param toFix The code to load to the database.
     * 
     * @return The fixed code to load to the database.
     */
    public static String replaceNewLines(String toFix)
    {
        String separator = System.getProperty("line.separator");
        StringBuffer spacesBuffer = new StringBuffer();
        // FIX for bug 1363370 - jdbc forces code onto one line
        int length = separator.length() - 1;
        for (int i = 0; i < length; i++)
        {
            spacesBuffer.append(' ');
        }
        // FIX for bug 1363370 - jdbc forces code onto one line
        String spaces = spacesBuffer.append("\n").toString();
        return toFix.replaceAll(separator, spaces);
    }

    public static String escapeSingleCommentedQuotes(String toFix)
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(toFix.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(bis));
        String line = null;
        boolean isCommenting = false;
        StringBuffer sb = new StringBuffer(1000);
        try
        {
            while ((line = br.readLine()) != null)
            {
                if (isCommenting)
                {
                    if (AbstractPlSqlParser.isEndingComment(line))
                    {
                        isCommenting = false;
                    }
                    line = line.replaceAll("'", " ");
                }
                else if (AbstractPlSqlParser.isStartingComment(line))
                {
                    isCommenting = true;
                }
                else
                {
                    String tmpLine = null;
                    // fix for bug 1445186 - Compile errors when quote contains '--'
                    int dashIndex = getUnquotedIndexOfString(line, "(--)", 0);
                    if (dashIndex != -1)
                    {
                        tmpLine = line.substring(dashIndex);
                        tmpLine = tmpLine.replaceAll("'", " ");
                        line = line.substring(0, dashIndex) + tmpLine;
                    }
                    while (!(tmpLine = line.replaceFirst("^(.*?)/\\*(.*?)'(.*?)\\*/(.*?)$",
                                                         "$1/*$2 $3*/$4")).equals(line))
                    {
                        line = tmpLine;
                    }
                }
                sb.append(line).append("\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static int getUnquotedIndexOfString(String line, String toSearchFor, int lostPrefixSize)
    {
        Pattern p = Pattern.compile(toSearchFor);
        Matcher m = p.matcher(line);
        if (!m.find())
        {
            return -1;
        }
        int toSearchForIndex = m.start(1);

        int quoteIndex = line.indexOf("'");
        if (quoteIndex == -1)
        {
            return toSearchForIndex + lostPrefixSize;
        }
        if (toSearchForIndex < quoteIndex)
        {
            return toSearchForIndex + lostPrefixSize;
        }
        int endCommentIndex = 0;
        while (true)
        {
            endCommentIndex = line.indexOf("'", quoteIndex + 1);
            if (endCommentIndex == -1)
            {
                return -1;
            }
            if (endCommentIndex + 1 < line.length())
            {
                if (line.charAt(endCommentIndex + 1) == '\'')
                {
                    quoteIndex = endCommentIndex + 1;
                }
                else
                {
                    break;
                }
            }
            else if (endCommentIndex + 1 == line.length())
            {
                break;
            }
        }
        if (toSearchForIndex < endCommentIndex)
        {
            if (!m.find(endCommentIndex))
            {
                return -1;
            }
            else
            {
                return getUnquotedIndexOfString(line.substring(endCommentIndex + 1),
                                                toSearchFor,
                                                // FIX for bug 1541819 - quote and comments in one line
                                                endCommentIndex + 1 + lostPrefixSize);
            }
        }
        else
        {
            return getUnquotedIndexOfString(line.substring(endCommentIndex + 1),
                                            toSearchFor,
                                            // FIX for bug 1541819 - quote and comments in one line
                                            endCommentIndex + 1 + lostPrefixSize);
        }
    }


}
