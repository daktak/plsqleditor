package plsqleditor.popup.actions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id: PlsqlHeaderGenerator.java,v 1.1.2.4 2006/02/02 00:52:06 tobyz
 *          Exp $
 * 
 * Created on 21/02/2005
 */
public class PlsqlHeaderGenerator
{
	public static final String CREATE_PKG_BODY_START = "\\W*[Cc][Rr][Ee][Aa][Tt][Ee]\\s+[Oo][Rr]\\s+[Rr][Ee][Pp][Ll][Aa][Cc][Ee]\\s+[Pp][Aa][Cc][Kk][Aa][Gg][Ee]\\s+[Bb][Oo][Dd][Yy]";
    String lineSeparator;

    public PlsqlHeaderGenerator()
    {
        System.out.println("Creating header generator");
        lineSeparator = System.getProperty("line.separator");
    }

    private void grabHeaderDetails(BufferedReader file, List details) throws IOException
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
                    // we have found the end of the comment for a particular
                    // file
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

    private String processWrappedComment(BufferedReader file, List header) throws IOException
    {
        String line = null;
        String tmpLine = null;
        String packageNameIs = "";
    	boolean isProcessingPackageName = false;
        while ((line = file.readLine()) != null)
        {
            if (!(tmpLine = line.replaceFirst(".*\\*/(.*)","$1")).equals(line)) // end of comment
            {
            	isProcessingPackageName = true;
            	Pattern p = Pattern.compile("(.*\\*/).*");
                Matcher m = p.matcher(line);
                m.find();
                header.add(m.group(1));
                if (tmpLine.trim().length() > 0)
                {
                	packageNameIs += " " + tmpLine;
                }
                if (!(tmpLine = packageNameIs.replaceFirst("\\W*(\\w+)\\W+[IiAa][Ss].*","$1")).equals(packageNameIs))
                {
                	return tmpLine;
                }
            }
            else if (isProcessingPackageName)
        	{
            	packageNameIs += " " + line;
                if (!(tmpLine = packageNameIs.replaceFirst("\\W*(\\w+)\\W+[IiAa][Ss].*","$1")).equals(packageNameIs))
                {
                	return tmpLine;
                }
        	}
        	else
        	{
        		header.add(line);
        	}	
        }
        return null;
    }

    private String parseHeader(BufferedReader file, String[] packageName) throws IOException
    {
        boolean isInHeader = false;
        // this can be CURRENT_USER or DEFINER 
        // there is no checking, but these are the only two valid values...
        // support for feature 1448560 - header generator needs ability to specify authid
        String authIdString = null;
        List header = new ArrayList();
        List details = new ArrayList();
        String line = null;
        header.add("/*" + lineSeparator + " * Warning! This file is auto generated. "
                + lineSeparator + " * Please do not modify this file. Modify the body instead."
                + lineSeparator + " */" + lineSeparator);
        String tmpLine = null;
        while ((line = file.readLine()) != null)
        {
        	if (line.matches(CREATE_PKG_BODY_START + ".*"))
        	{
	            if (!(tmpLine = line.replaceFirst(CREATE_PKG_BODY_START + "\\W+(\\w+).*",
	                                              "$1")).equals(line))
	            {
	            	// we have a standard package name directly after the create or replace statement
	                packageName[0] = tmpLine;
	            }
	            else
	            {
	            	// we have a wrapped package declaration - must cycle through to the name
	            	String endOfCreateLine = line.replaceFirst(CREATE_PKG_BODY_START + "(.*)", "$1");
	            	if (endOfCreateLine.trim().length() > 0)
	            	{
	            		header.add(endOfCreateLine);
	            	}
	            	packageName[0] = processWrappedComment(file, header);
	            }
        	}
            else if (line.matches(".*header details.*"))
            {
                grabHeaderDetails(file, details);
            }
            else if (isInHeader)
            {
                if (!(tmpLine = line.replaceFirst("\\W*@authid\\W+(\\w+).*", "$1")).equals(line))
                {
                    authIdString = tmpLine;
                }
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
        sb.append(lineSeparator);
        if (authIdString != null)
        {
            sb.append("AUTHID ").append(authIdString).append(lineSeparator);
        }
        sb.append("AS").append(lineSeparator);
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
        List header = new ArrayList();
        List pragmas = new ArrayList();
        String line = null;
        boolean isPublic = true;
        boolean isHeaderComment = false; // flag denoting if this is a header
        // comment.

        String tmpLine = null;
        while ((line = file.readLine()) != null)
        {
            if (isInHeader)
            {
                if (line.matches("^\\s*[IiAa][Ss].*"))
                {
                    isInHeader = false;
                    String last = (String) header.remove(header.size() - 1);
                    // address comment after last line
                    // Bug Id: 1387877
                    tmpLine = last.replaceFirst("^(.*?) ?(--.*)$", "$1; $2");
                    if (tmpLine.equals(last))
                    {
                        last = last + ";";
                    }
                    else
                    {
                        last = tmpLine;
                    }
                    header.add(last + lineSeparator);
                }
                // fix for bug id 1441832 - PRAGMA keywords not parsed
                else if (!(tmpLine = line.replaceFirst("\\W*@pragma\\W+(.*)", "$1")).equals(line))
                {
                    pragmas.add("PRAGMA " + tmpLine + ";");
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
                    header = new ArrayList();
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
                else if (!(tmpLine = line.replaceFirst("(^[^\\*]*)\\s+[IiAa][Ss](\\W+.*$|$)", "$1;")).equals(line))
                {
                    isInHeader = false;
                    header.add(tmpLine);
                }
                else if (!(tmpLine = line.replaceFirst("^(.*?\\*/)\\s*[IiAa][Ss](\\W+.*$|$)", "$1;")).equals(line))
                {
                    isInHeader = false;
                    header.add(tmpLine);
                }
                else if (line.matches(".*/\\*\\*.*"))
                {
                    isPublic = true;
                    isHeaderComment = false;
                    header = new ArrayList();
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
                        for (Iterator it = pragmas.iterator(); it.hasNext();)
                        {
                            String str = (String) it.next();
                            sb.append(str).append(lineSeparator);
                        }
                        sb.append(lineSeparator);
                    }
                    isPublic = true;
                    header = new ArrayList();
                    pragmas = new ArrayList();
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
                header = new ArrayList();
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
