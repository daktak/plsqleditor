package plsqleditor.popup.actions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 21/02/2005
 */
public class PlsqlHeaderGenerator
{
    String lineSeparator;

    public PlsqlHeaderGenerator()
    {
        System.out.println("Creating header generator");
        lineSeparator = System.getProperty("line.separator");
    }

    private void grabHeaderDetails(BufferedReader file, List<String> details) throws IOException
    {
        String line = null;
        boolean isCommenting = false;

        // this represents the internal comments
        // to be converted to javadoc comments
        String internalCommentsId = "--";
        // this represents the last line's starting spaces
        String lastSpace = "";
        // parse header details here
        String tmpLine = null;
        while ((line = file.readLine()) != null)
        {
            if (line.matches(".*end header details.*"))
            {
                break;
            }
            String detailsLine = line;
            if (isCommenting)
            {
                // find the middle of a comment for a particular file
                if (!(tmpLine = detailsLine.replaceFirst("(\\W*)" + internalCommentsId + "(.*)$",
                                                         "$1 \\*$2")).equals(detailsLine))
                {
                    lastSpace = tmpLine.substring(0, tmpLine.indexOf(" *"));
                    detailsLine = tmpLine;
                }
                else
                {
                    // we have found the end of the comment for a particular file
                    detailsLine = lastSpace + " */" + lineSeparator + detailsLine;
                    isCommenting = false;
                }
            }
            else
            {
                // find the start of the commenting for a particular file
                if (!(tmpLine = detailsLine.replaceFirst("(\\W*)" + internalCommentsId + "(.*)$",
                                                         "$1/\\*\\*" + lineSeparator + "$1 \\*$2"))
                        .equals(detailsLine))
                {
                    detailsLine = tmpLine;
                    lastSpace = tmpLine.substring(0, tmpLine.indexOf("/**"));
                    isCommenting = true;
                }
            }
            details.add(detailsLine);
        }
    }

    private String parseHeader(BufferedReader file, String[] packageName) throws IOException
    {
        boolean isInHeader = false;
        List<String> header = new ArrayList<String>();
        List<String> details = new ArrayList<String>();
        String line = null;
        header.add("/*" + lineSeparator + " * Warning! This file is auto generated. "
                + lineSeparator + " * Please do not modify this file. Modify the body instead."
                + lineSeparator + " */" + lineSeparator);
        String tmpLine = null;
        while ((line = file.readLine()) != null)
        {
            if (!(tmpLine = line.replaceFirst("CREATE OR REPLACE PACKAGE BODY (\\w+).*", "$1"))
                    .equals(line))
            {
                packageName[0] = tmpLine;
            }
            else if (line.matches(".*header details.*"))
            {
                grabHeaderDetails(file, details);
            }
            else if (isInHeader)
            {
                header.add(line);
                if (line.matches(".*\\*/.*"))
                {
                    break;
                }
            }
            else if (line.matches(".*/\\*.*"))
            {
                isInHeader = true;
                header.add(line);
            }
        }
        StringBuffer sb = new StringBuffer("CREATE OR REPLACE PACKAGE ");
        sb.append(packageName[0]);
        sb.append(" AS " + lineSeparator);
        for (Iterator it = header.iterator(); it.hasNext();)
        {
            String hLine = (String) it.next();
            sb.append(hLine).append(lineSeparator);
        }
        sb.append(lineSeparator);
        for (Iterator it = details.iterator(); it.hasNext();)
        {
            String dLine = (String) it.next();
            sb.append(dLine).append(lineSeparator);
        }
        sb.append(lineSeparator);
        return sb.toString();
    }

    private String parseBody(BufferedReader file) throws IOException
    {
        StringBuffer sb = new StringBuffer();
        boolean isInHeader = false;
        List<String> header = new ArrayList<String>();
        String line = null;
        boolean isPublic = true;
        boolean isHeaderComment = false; // flag denoting if this is a header comment.

        String tmpLine = null;
        while ((line = file.readLine()) != null)
        {
            if (isInHeader)
            {
                if (line.matches("^[IA]S.*"))
                {
                    isInHeader = false;
                    String last = header.remove(header.size() - 1);
                    header.add(last + ";" + lineSeparator);
                }
                else if (line.matches(".*@private.*"))
                {
                    isPublic = false;
                }
                else if (line.matches(".*@headcom.*"))
                {
                    isHeaderComment = true;
                }
                else if (line.matches(".*\\*/.*") && isHeaderComment)
                {
                    header.add(line);
                    for (Iterator it = header.iterator(); it.hasNext();)
                    {
                        String str = (String) it.next();
                        sb.append(str).append(lineSeparator);
                    }
                    isPublic = true;
                    isHeaderComment = false;
                    header = new ArrayList<String>();
                    isInHeader = false;
                }
                else if (!(tmpLine = line.replaceFirst("(.*)\\@see (.*)\\.(.*)",
                                                       "$1See <CODE><B><AHREF=\\\"./$2.html#$3\\\">$2.$3</A></B></CODE>"
                                                               + lineSeparator)).equals(line))
                {
                    line = tmpLine;
                }
                else if (!(tmpLine = line.replaceFirst("(.*)\\@refer (.*)\\.(.*)",
                                                       "$1Refer to <CODE><B><AHREF=\\\"./$2.html#$3\\\">$2.$3</A></B></CODE>"
                                                               + lineSeparator)).equals(line))
                {
                    line = tmpLine;
                }
                else if (!(tmpLine = line.replaceFirst("(^[^\\*]*) [IA]S.*$", "$1;")).equals(line))
                {
                    isInHeader = false;
                    header.add(tmpLine);
                }
                else if (line.matches(".*/\\*\\*.*"))
                {
                    isPublic = true;
                    isHeaderComment = false;
                    header = new ArrayList<String>();
                    isInHeader = true;
                }

                if (!isInHeader)
                {
                    if (isPublic)
                    {
                        for (Iterator it = header.iterator(); it.hasNext();)
                        {
                            String str = (String) it.next();
                            sb.append(str).append(lineSeparator);
                        }
                        sb.append(lineSeparator);
                    }
                    isPublic = true;
                    header = new ArrayList<String>();
                }
                else
                {
                    header.add(line);
                }
            }
            else if (line.matches(".*/\\*.*") && line.matches(".*\\*/.*"))
            {
                // nothing
            }
            else if (line.matches(".*/\\*\\*.*"))
            {
                isHeaderComment = false;
                isPublic = true;
                header = new ArrayList<String>();
                isInHeader = true;
                header.add(line);
            }
            else
            {
                // sb.append(line);
            }
        }
        return sb.toString();
    }

    public String parseBodyFile(String fileName) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(fileName));

        return parseBodyReader(br);
    }

    /**
     * This method parses the body
     * 
     * @param br
     * @return The string representation of the whole package header.
     * @throws IOException
     */
    private String parseBodyReader(BufferedReader br) throws IOException
    {
        String[] packageName = new String[1];
        StringBuffer sb = new StringBuffer();
        sb.append(parseHeader(br, packageName));
        sb.append(parseBody(br));
        br.close();
        sb.append("END " + packageName[0] + ";" + lineSeparator + "/ " + lineSeparator
                + "SHOW ERRORS PACKAGE " + packageName[0] + ";" + lineSeparator);
        return sb.toString();
    }

    public String parseBodyFile(InputStream input) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));

        return parseBodyReader(br);
    }

    public static void main(String[] args) throws IOException
    {
        PlsqlHeaderGenerator generator = new PlsqlHeaderGenerator();
        System.out.println(generator.parseBodyFile(args[0]));
    }
}
