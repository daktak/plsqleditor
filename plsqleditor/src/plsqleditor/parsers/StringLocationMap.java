/**
 * 
 */
package plsqleditor.parsers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class represents a mapping from one location in one string to another location in another
 * string. The idea is that the two strings represent the same thing, but one of them has been
 * formatted for whatever reason. In these cases we need to know index in the first string maps to
 * in the second string.
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
     * This method replaces all the single line comments in the supplied <code>str</code> with
     * closed commments. It ignores single line comments already inside double line comments.
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
}
