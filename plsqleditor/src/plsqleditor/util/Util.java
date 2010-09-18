package plsqleditor.util;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import plsqleditor.parsers.AbstractPlSqlParser;

/**
 * @author Toby Zines
 * 
 */
public class Util
{
    public interface Comparable
    {
        /**
         * Returns 0 if this and c are equal, >0 if this is greater than c, or
         * <0 if this is less than c.
         */
        int compareTo(Comparable c);
    }

    public interface Comparer
    {
        /**
         * Returns 0 if a and b are equal, >0 if a is greater than b, or <0 if a
         * is less than b.
         */
        int compare(Object a, Object b);
    }
    private static final String ARGUMENTS_DELIMITER = "#";                    //$NON-NLS-1$
    private static final String EMPTY_ARGUMENT      = "   ";                  //$NON-NLS-1$

    /**
     * This is a hidden constructor.
     */
    private Util()
    {
        // this is a utility class.
    }

    /**
     * This method checks if two arrays contain the same data. The data does not
     * have to be in the same order, just the same number of objects, and each
     * one is "equal" according to the equals method.
     * 
     * @param ary1 The first array.
     * 
     * @param ary2 The second array
     * 
     * @return Whether or not the two arrays contain the same data.
     */
    public static boolean arraysAreEqual(Object[] ary1, Object[] ary2)
    {
        if (ary1.length != ary2.length)
        {
            return false;
        }
        int size = ary1.length;
        Vector<Object> v1 = new Vector<Object>(size);
        Vector<Object> v2 = new Vector<Object>(size);
        for (int i = 0; i < size; i++)
        {
            v1.addElement(ary1[i]);
        }
        for (int i = 0; i < size; i++)
        {
            v2.addElement(ary2[i]);
        }
        for (int i = 0; i < size; i++)
        {
            int v2Size = v2.size();
            for (int j = 0; j < v2Size; j++)
            {
                if (v1.elementAt(i).equals(v2.elementAt(j)))
                {
                    v2.removeElementAt(j);
                    break;
                }
            }
        }
        return v2.isEmpty();
    }

    /**
     * This method returns a tokenised list representing the partially completed
     * statement since the last semi colon.
     * 
     * @param document The document in which we are examining the statement in
     *            which the cursor is is currently located at
     *            <code>currentOffset</code>.
     * 
     * @param currentOffset The offset into the supplied <code>document</code>
     *            around which location we are trying to find a statement.
     * 
     * @param emptyLineIsSeparator This is a boolean indicating whether an empty
     *            line should be used as a separator or not. If it is, the
     *            search for the beginning of the statement will stop when an
     *            empty line is encountered.
     * 
     * @return the list of tokens representing the current statement leading up
     *         to the <code>currentOffset</code>.
     */
    public static List<String> grabCurrentPlSqlTokens(IDocument document,
                                              int currentOffset,
                                              boolean emptyLineIsSeparator)
    {
        // this is the list of tokens representing the current statement leading
        // up to the
        // currentOffset
        List<String> tokenList = new ArrayList<String>();
        String toParse = grabCurrentPlSqlStatement(document,
                                                   currentOffset,
                                                   emptyLineIsSeparator);
        StringTokenizer st = new StringTokenizer(toParse, " \t()\r\n,");
        while (st.hasMoreTokens())
        {
            tokenList.add(st.nextToken());
        }
        return tokenList;
    }

    public static String findPreviousMethodName(IDocument document,
                                                int currentOffset)
    {
        int line;
        StringBuffer toParse = new StringBuffer();
        Pattern p = Pattern.compile("\\s*(" + AbstractPlSqlParser.PROCEDURE + "|" + AbstractPlSqlParser.FUNCTION + ")\\s+([\\w_]+)*");
        try
        {
            line = document.getLineOfOffset(currentOffset) - 1;
            while (line >= 0)
            {
                int startOfLine = document.getLineOffset(line);
                int length = document.getLineLength(line);
                String text = document.get(startOfLine, length);
                toParse.insert(0, text.trim().toCharArray()).insert(0," ");
                Matcher m = p.matcher(toParse.toString());
                if (m.find())
                {
                    return m.group(2);
                }
                line --;
            }
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        return "";
    }
                                                
    /**
     * This method returns a string representing the partially completed
     * statement since the last semi colon.
     * 
     * @param document The document in which we are examining the statement in
     *            which the cursor is is currently located at
     *            <code>currentOffset</code>.
     * 
     * @param currentOffset The offset into the supplied <code>document</code>
     *            around which location we are trying to find a statement.
     * 
     * @param emptyLineIsSeparator This is a boolean indicating whether an empty
     *            line should be used as a separator or not. If it is, the
     *            search for the beginning of the statement will stop when an
     *            empty line is encountered.
     * 
     * @return the string representing the current statement leading up to the
     *         <code>currentOffset</code>.
     */
    public static String grabCurrentPlSqlStatement(IDocument document,
                                                   int currentOffset,
                                                   boolean emptyLineIsSeparator)
    {
        int line;
        StringBuffer toParse = new StringBuffer();
        try
        {
            line = document.getLineOfOffset(currentOffset);
            int startOfLine = document.getLineOffset(line);
            String text = document
                    .get(startOfLine, currentOffset - startOfLine);
            toParse.insert(0, text);
            int index = -1;
            while (index < 0 && line > 0)
            {
                line--;
                startOfLine = document.getLineOffset(line);
                int linelength = document.getLineLength(line);
                text = document.get(startOfLine, linelength);
                if (emptyLineIsSeparator && (text.trim().length() == 0))
                {
                    // have found an empty line
                    break;
                }
                index = text.indexOf(";");
                toParse.insert(0, text.substring(index + 1) + " ");
            }
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        return toParse.toString();
    }

    /**
     * This method returns a string representing a block of code surrounded on
     * either side by an empty line.
     * 
     * @param document The document in which we are examining the statement in
     *            which the cursor is is currently located at
     *            <code>currentOffset</code>.
     * 
     * @param currentOffset The offset into the supplied <code>document</code>
     *            around which location we are trying to find a statement.
     * 
     * @return the string representing the current statement before and after
     *         the <code>currentOffset</code> (identified by empty lines
     *         before and after).
     */
    public static String grabSurroundingSqlBlock(IDocument document,
                                                 int currentOffset)
    {
        int line = 0;
        int startLine = 0;
        StringBuffer toParse = new StringBuffer();
        int startOfLine = 0;
        String text = null;
        try
        {
            startLine = line = document.getLineOfOffset(currentOffset);
            while (line >= 0)
            {
                startOfLine = document.getLineOffset(line);
                int linelength = document.getLineLength(line);
                text = document.get(startOfLine, linelength);
                if (text.trim().length() == 0)
                {
                    // have found an empty line
                    break;
                }
                toParse.insert(0, text + " ");
                line--;
            }
        }
        catch (BadLocationException e)
        {
            // couldn't start, not looking good...
            e.printStackTrace();
        }
        line = startLine + 1;
        while (line >= 0)
        {
            try
            {
                startOfLine = document.getLineOffset(line);
                int linelength = document.getLineLength(line);
                text = document.get(startOfLine, linelength);
                if (text.trim().length() == 0)
                {
                    // have found an empty line
                    break;
                }
                toParse.append(text + " ");
                line++;
            }
            catch (BadLocationException e)
            {
                break;
            }
        }
        return toParse.toString();
    }

    /**
     * Returns a new array adding the second array at the end of first array. It
     * answers null if the first and second are null. If the first array is null
     * or if it is empty, then a new array is created with second. If the second
     * array is null, then the first array is returned. <br>
     * <br>
     * For example:
     * <ol>
     * <li>
     * <pre>
     *     first = null
     *     second = &quot;a&quot;
     *     =&gt; result = {&quot;a&quot;}
     * </pre>
     * <li>
     * <pre>
     *     first = {&quot;a&quot;}
     *     second = null
     *     =&gt; result = {&quot;a&quot;}
     * </pre>
     * </li>
     * <li>
     * <pre>
     *     first = {&quot;a&quot;}
     *     second = {&quot;b&quot;}
     *     =&gt; result = {&quot;a&quot;, &quot;b&quot;}
     * </pre>
     * </li>
     * </ol>
     * 
     * @param first the first array to concatenate
     * @param second the array to add at the end of the first array
     * @return a new array adding the second array at the end of first array, or
     *         null if the two arrays are null.
     */
    public static final String[] arrayConcat(String[] first, String second)
    {
        if (second == null) return first;
        if (first == null) return new String[]{second};
        int length = first.length;
        if (first.length == 0)
        {
            return new String[]{second};
        }
        String[] result = new String[length + 1];
        System.arraycopy(first, 0, result, 0, length);
        result[length] = second;
        return result;
    }

    /**
     * Combines two hash codes to make a new one.
     */
    public static int combineHashCodes(int hashCode1, int hashCode2)
    {
        return hashCode1 * 17 + hashCode2;
    }

    /**
     * Compares two byte arrays. Returns <0 if a byte in a is less than the
     * corresponding byte in b, or if a is shorter, or if a is null. Returns >0
     * if a byte in a is greater than the corresponding byte in b, or if a is
     * longer, or if b is null. Returns 0 if they are equal or both null.
     */
    public static int compare(byte[] a, byte[] b)
    {
        if (a == b) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; ++i)
        {
            int diff = a[i] - b[i];
            if (diff != 0) return diff;
        }
        if (a.length > len) return 1;
        if (b.length > len) return -1;
        return 0;
    }

    /**
     * Compares two strings lexicographically. The comparison is based on the
     * Unicode value of each character in the strings.
     * 
     * @return the value <code>0</code> if the str1 is equal to str2; a value
     *         less than <code>0</code> if str1 is lexicographically less than
     *         str2; and a value greater than <code>0</code> if str1 is
     *         lexicographically greater than str2.
     */
    public static int compare(char[] str1, char[] str2)
    {
        int len1 = str1.length;
        int len2 = str2.length;
        int n = Math.min(len1, len2);
        int i = 0;
        while (n-- != 0)
        {
            char c1 = str1[i];
            char c2 = str2[i++];
            if (c1 != c2)
            {
                return c1 - c2;
            }
        }
        return len1 - len2;
    }

    /**
     * Concatenate two strings with a char in between.
     * 
     * @see #concat(String, String)
     */
    public static String concat(String s1, char c, String s2)
    {
        if (s1 == null) s1 = "null"; //$NON-NLS-1$
        if (s2 == null) s2 = "null"; //$NON-NLS-1$
        int l1 = s1.length();
        int l2 = s2.length();
        char[] buf = new char[l1 + 1 + l2];
        s1.getChars(0, l1, buf, 0);
        buf[l1] = c;
        s2.getChars(0, l2, buf, l1 + 1);
        return new String(buf);
    }

    /**
     * Concatenate two strings. Much faster than using +, which: - creates a
     * StringBuffer, - which is synchronized, - of default size, so the
     * resulting char array is often larger than needed. This implementation
     * creates an extra char array, since the String constructor copies its
     * argument, but there's no way around this.
     */
    public static String concat(String s1, String s2)
    {
        if (s1 == null) s1 = "null"; //$NON-NLS-1$
        if (s2 == null) s2 = "null"; //$NON-NLS-1$
        int l1 = s1.length();
        int l2 = s2.length();
        char[] buf = new char[l1 + l2];
        s1.getChars(0, l1, buf, 0);
        s2.getChars(0, l2, buf, l1);
        return new String(buf);
    }

    /**
     * Returns the concatenation of the given array parts using the given
     * separator between each part. <br>
     * <br>
     * For example:<br>
     * <ol>
     * <li>
     * 
     * <pre>
     * 
     *     array = {&quot;a&quot;, &quot;b&quot;}
     *     separator = '.'
     *     =&gt; result = &quot;a.b&quot;
     *  
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * 
     *     array = {}
     *     separator = '.'
     *     =&gt; result = &quot;&quot;
     *  
     * </pre>
     * 
     * </li>
     * </ol>
     * 
     * @param array the given array
     * @param separator the given separator
     * @return the concatenation of the given array parts using the given
     *         separator between each part
     */
    public static final String concatWith(String[] array, char separator)
    {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, length = array.length; i < length; i++)
        {
            buffer.append(array[i]);
            if (i < length - 1) buffer.append(separator);
        }
        return buffer.toString();
    }

    /**
     * Returns the concatenation of the given array parts using the given
     * separator between each part and appending the given name at the end. <br>
     * <br>
     * For example:<br>
     * <ol>
     * <li>
     * 
     * <pre>
     * 
     *     name = &quot;c&quot;
     *     array = { &quot;a&quot;, &quot;b&quot; }
     *     separator = '.'
     *     =&gt; result = &quot;a.b.c&quot;
     *  
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * 
     *     name = null
     *     array = { &quot;a&quot;, &quot;b&quot; }
     *     separator = '.'
     *     =&gt; result = &quot;a.b&quot;
     *  
     * </pre>
     * 
     * </li>
     * <li>
     * 
     * <pre>
     * 
     *     name = &quot; c&quot;
     *     array = null
     *     separator = '.'
     *     =&gt; result = &quot;c&quot;
     *  
     * </pre>
     * 
     * </li>
     * </ol>
     * 
     * @param array the given array
     * @param name the given name
     * @param separator the given separator
     * @return the concatenation of the given array parts using the given
     *         separator between each part and appending the given name at the
     *         end
     */
    public static final String concatWith(String[] array,
                                          String name,
                                          char separator)
    {
        if (array == null || array.length == 0) return name;
        if (name == null || name.length() == 0) return concatWith(array,
                                                                  separator);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, length = array.length; i < length; i++)
        {
            buffer.append(array[i]);
            buffer.append(separator);
        }
        buffer.append(name);
        return buffer.toString();
    }

    /**
     * Concatenate three strings.
     * 
     * @see #concat(String, String)
     */
    public static String concat(String s1, String s2, String s3)
    {
        if (s1 == null) s1 = "null"; //$NON-NLS-1$
        if (s2 == null) s2 = "null"; //$NON-NLS-1$
        if (s3 == null) s3 = "null"; //$NON-NLS-1$
        int l1 = s1.length();
        int l2 = s2.length();
        int l3 = s3.length();
        char[] buf = new char[l1 + l2 + l3];
        s1.getChars(0, l1, buf, 0);
        s2.getChars(0, l2, buf, l1);
        s3.getChars(0, l3, buf, l1 + l2);
        return new String(buf);
    }

    /**
     * s * Converts a type signature from the IBinaryType representation to the
     * DC representation.
     */
    public static String convertTypeSignature(char[] sig, int start, int length)
    {
        return new String(sig, start, length).replace('/', '.');
    }

    /**
     * Returns true iff str.toLowerCase().endsWith(end.toLowerCase())
     * implementation is not creating extra strings.
     */
    public final static boolean endsWithIgnoreCase(String str, String end)
    {
        int strLength = str == null ? 0 : str.length();
        int endLength = end == null ? 0 : end.length();
        // return false if the string is smaller than the end.
        if (endLength > strLength) return false;
        // return false if any character of the end are
        // not the same in lower case.
        for (int i = 1; i <= endLength; i++)
        {
            if (Character.toLowerCase(end.charAt(endLength - i)) != Character
                    .toLowerCase(str.charAt(strLength - i))) return false;
        }
        return true;
    }

    /**
     * Compares two arrays using equals() on the elements. Neither can be null.
     * Only the first len elements are compared. Return false if either array is
     * shorter than len.
     */
    public static boolean equalArrays(Object[] a, Object[] b, int len)
    {
        if (a == b) return true;
        if (a.length < len || b.length < len) return false;
        for (int i = 0; i < len; ++i)
        {
            if (a[i] == null)
            {
                if (b[i] != null) return false;
            }
            else
            {
                if (!a[i].equals(b[i])) return false;
            }
        }
        return true;
    }

    /**
     * Compares two arrays using equals() on the elements. Either or both arrays
     * may be null. Returns true if both are null. Returns false if only one is
     * null. If both are arrays, returns true iff they have the same length and
     * all elements are equal.
     */
    public static boolean equalArraysOrNull(int[] a, int[] b)
    {
        if (a == b) return true;
        if (a == null || b == null) return false;
        int len = a.length;
        if (len != b.length) return false;
        for (int i = 0; i < len; ++i)
        {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    /**
     * Compares two arrays using equals() on the elements. Either or both arrays
     * may be null. Returns true if both are null. Returns false if only one is
     * null. If both are arrays, returns true iff they have the same length and
     * all elements compare true with equals.
     */
    public static boolean equalArraysOrNull(Object[] a, Object[] b)
    {
        if (a == b) return true;
        if (a == null || b == null) return false;
        int len = a.length;
        if (len != b.length) return false;
        for (int i = 0; i < len; ++i)
        {
            if (a[i] == null)
            {
                if (b[i] != null) return false;
            }
            else
            {
                if (!a[i].equals(b[i])) return false;
            }
        }
        return true;
    }

    /**
     * Compares two arrays using equals() on the elements. The arrays are first
     * sorted. Either or both arrays may be null. Returns true if both are null.
     * Returns false if only one is null. If both are arrays, returns true iff
     * they have the same length and iff, after sorting both arrays, all
     * elements compare true with equals. The original arrays are left
     * untouched.
     */
    public static boolean equalArraysOrNullSortFirst(Comparable[] a,
                                                     Comparable[] b)
    {
        if (a == b) return true;
        if (a == null || b == null) return false;
        int len = a.length;
        if (len != b.length) return false;
        if (len >= 2)
        { // only need to sort if more than two items
            a = sortCopy(a);
            b = sortCopy(b);
        }
        for (int i = 0; i < len; ++i)
        {
            if (!a[i].equals(b[i])) return false;
        }
        return true;
    }

    /**
     * Compares two String arrays using equals() on the elements. The arrays are
     * first sorted. Either or both arrays may be null. Returns true if both are
     * null. Returns false if only one is null. If both are arrays, returns true
     * iff they have the same length and iff, after sorting both arrays, all
     * elements compare true with equals. The original arrays are left
     * untouched.
     */
    public static boolean equalArraysOrNullSortFirst(String[] a, String[] b)
    {
        if (a == b) return true;
        if (a == null || b == null) return false;
        int len = a.length;
        if (len != b.length) return false;
        if (len >= 2)
        { // only need to sort if more than two items
            a = sortCopy(a);
            b = sortCopy(b);
        }
        for (int i = 0; i < len; ++i)
        {
            if (!a[i].equals(b[i])) return false;
        }
        return true;
    }

    /**
     * Compares two objects using equals(). Either or both array may be null.
     * Returns true if both are null. Returns false if only one is null.
     * Otherwise, return the result of comparing with equals().
     */
    public static boolean equalOrNull(Object a, Object b)
    {
        if (a == b)
        {
            return true;
        }
        if (a == null || b == null)
        {
            return false;
        }
        return a.equals(b);
    }

    /**
     * Given a qualified name, extract the last component. If the input is not
     * qualified, the same string is answered.
     */
    public static String extractLastName(String qualifiedName)
    {
        int i = qualifiedName.lastIndexOf('.');
        if (i == -1) return qualifiedName;
        return qualifiedName.substring(i + 1);
    }

    /**
     * Finds the first line separator used by the given text.
     * 
     * @return </code>"\n"</code> or </code>"\r"</code> or </code>"\r\n"</code>,
     *         or <code>null</code> if none found
     */
    public static String findLineSeparator(char[] text)
    {
        // find the first line separator
        int length = text.length;
        if (length > 0)
        {
            char nextChar = text[0];
            for (int i = 0; i < length; i++)
            {
                char currentChar = nextChar;
                nextChar = i < length - 1 ? text[i + 1] : ' ';
                switch (currentChar)
                {
                    case '\n' :
                        return "\n"; //$NON-NLS-1$
                    case '\r' :
                        return nextChar == '\n' ? "\r\n" : "\r"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        // not found
        return null;
    }

    /**
     * Put all the arguments in one String.
     */
    public static String getProblemArgumentsForMarker(String[] arguments)
    {
        StringBuffer args = new StringBuffer(10);
        args.append(arguments.length);
        args.append(':');
        for (int j = 0; j < arguments.length; j++)
        {
            if (j != 0) args.append(ARGUMENTS_DELIMITER);
            if (arguments[j].length() == 0)
            {
                args.append(EMPTY_ARGUMENT);
            }
            else
            {
                args.append(arguments[j]);
            }
        }
        return args.toString();
    }

    /**
     * Separate all the arguments of a String made by
     * getProblemArgumentsForMarker
     */
    public static String[] getProblemArgumentsFromMarker(String argumentsString)
    {
        if (argumentsString == null) return null;
        int index = argumentsString.indexOf(':');
        if (index == -1) return null;
        int length = argumentsString.length();
        int numberOfArg;
        try
        {
            numberOfArg = Integer.parseInt(argumentsString.substring(0, index));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
        argumentsString = argumentsString.substring(index + 1, length);
        String[] args = new String[length];
        int count = 0;
        StringTokenizer tokenizer = new StringTokenizer(argumentsString,
                ARGUMENTS_DELIMITER);
        while (tokenizer.hasMoreTokens())
        {
            String argument = tokenizer.nextToken();
            if (argument.equals(EMPTY_ARGUMENT)) argument = ""; //$NON-NLS-1$
            args[count++] = argument;
        }
        if (count != numberOfArg) return null;
        System.arraycopy(args, 0, args = new String[count], 0, count);
        return args;
    }

    /*
     * Returns the index of the first argument paths which is equal to the path
     * to check
     */
    public static int indexOfMatchingPath(IPath checkedPath,
                                          IPath[] paths,
                                          int pathCount)
    {
        for (int i = 0; i < pathCount; i++)
        {
            if (paths[i].equals(checkedPath)) return i;
        }
        return -1;
    }

    /*
     * Returns the index of the first argument paths which is strictly nested
     * inside the path to check
     */
    public static int indexOfNestedPath(IPath checkedPath,
                                        IPath[] paths,
                                        int pathCount)
    {
        for (int i = 0; i < pathCount; i++)
        {
            if (checkedPath.equals(paths[i])) continue;
            if (checkedPath.isPrefixOf(paths[i])) return i;
        }
        return -1;
    }

    /**
     * Normalizes the cariage returns in the given text. They are all changed to
     * use the given buffer's line separator.
     */
    public static char[] normalizeCRs(char[] text, char[] buffer)
    {
        CharArrayBuffer result = new CharArrayBuffer();
        int lineStart = 0;
        int length = text.length;
        if (length == 0) return text;
        String lineSeparator = System.getProperty("line.separator");
        char nextChar = text[0];
        for (int i = 0; i < length; i++)
        {
            char currentChar = nextChar;
            nextChar = i < length - 1 ? text[i + 1] : ' ';
            switch (currentChar)
            {
                case '\n' :
                    int lineLength = i - lineStart;
                    char[] line = new char[lineLength];
                    System.arraycopy(text, lineStart, line, 0, lineLength);
                    result.append(line);
                    result.append(lineSeparator);
                    lineStart = i + 1;
                    break;
                case '\r' :
                    lineLength = i - lineStart;
                    if (lineLength >= 0)
                    {
                        line = new char[lineLength];
                        System.arraycopy(text, lineStart, line, 0, lineLength);
                        result.append(line);
                        result.append(lineSeparator);
                        if (nextChar == '\n')
                        {
                            nextChar = ' ';
                            lineStart = i + 2;
                        }
                        else
                        {
                            // when line separator are mixed in the same file
                            // \r might not be followed by a \n. If not, we
                            // should increment
                            // lineStart by one and not by two.
                            lineStart = i + 1;
                        }
                    }
                    else
                    {
                        // when line separator are mixed in the same file
                        // we need to prevent NegativeArraySizeException
                        lineStart = i + 1;
                    }
                    break;
            }
        }
        char[] lastLine;
        if (lineStart > 0)
        {
            int lastLineLength = length - lineStart;
            if (lastLineLength > 0)
            {
                lastLine = new char[lastLineLength];
                System.arraycopy(text, lineStart, lastLine, 0, lastLineLength);
                result.append(lastLine);
            }
            return result.getContents();
        }
        return text;
    }

    /**
     * Normalizes the cariage returns in the given text. They are all changed to
     * use given buffer's line sepatator.
     */
    public static String normalizeCRs(String text, String buffer)
    {
        return new String(
                normalizeCRs(text.toCharArray(), buffer.toCharArray()));
    }

    /**
     * Returns the length of the common prefix between s1 and s2.
     */
    public static int prefixLength(char[] s1, char[] s2)
    {
        int len = 0;
        int max = Math.min(s1.length, s2.length);
        for (int i = 0; i < max && s1[i] == s2[i]; ++i)
            ++len;
        return len;
    }

    /**
     * Returns the length of the common prefix between s1 and s2.
     */
    public static int prefixLength(String s1, String s2)
    {
        int len = 0;
        int max = Math.min(s1.length(), s2.length());
        for (int i = 0; i < max && s1.charAt(i) == s2.charAt(i); ++i)
            ++len;
        return len;
    }

    private static void quickSort(char[][] list, int left, int right)
    {
        int original_left = left;
        int original_right = right;
        char[] mid = list[(left + right) / 2];
        do
        {
            while (compare(list[left], mid) < 0)
            {
                left++;
            }
            while (compare(mid, list[right]) < 0)
            {
                right--;
            }
            if (left <= right)
            {
                char[] tmp = list[left];
                list[left] = list[right];
                list[right] = tmp;
                left++;
                right--;
            }
        }
        while (left <= right);
        if (original_left < right)
        {
            quickSort(list, original_left, right);
        }
        if (left < original_right)
        {
            quickSort(list, left, original_right);
        }
    }

    /**
     * Sort the comparable objects in the given collection.
     */
    private static void quickSort(Comparable[] sortedCollection,
                                  int left,
                                  int right)
    {
        int original_left = left;
        int original_right = right;
        Comparable mid = sortedCollection[(left + right) / 2];
        do
        {
            while (sortedCollection[left].compareTo(mid) < 0)
            {
                left++;
            }
            while (mid.compareTo(sortedCollection[right]) < 0)
            {
                right--;
            }
            if (left <= right)
            {
                Comparable tmp = sortedCollection[left];
                sortedCollection[left] = sortedCollection[right];
                sortedCollection[right] = tmp;
                left++;
                right--;
            }
        }
        while (left <= right);
        if (original_left < right)
        {
            quickSort(sortedCollection, original_left, right);
        }
        if (left < original_right)
        {
            quickSort(sortedCollection, left, original_right);
        }
    }

    private static void quickSort(int[] list, int left, int right)
    {
        int original_left = left;
        int original_right = right;
        int mid = list[(left + right) / 2];
        do
        {
            while (list[left] < mid)
            {
                left++;
            }
            while (mid < list[right])
            {
                right--;
            }
            if (left <= right)
            {
                int tmp = list[left];
                list[left] = list[right];
                list[right] = tmp;
                left++;
                right--;
            }
        }
        while (left <= right);
        if (original_left < right)
        {
            quickSort(list, original_left, right);
        }
        if (left < original_right)
        {
            quickSort(list, left, original_right);
        }
    }

    /**
     * Sort the objects in the given collection using the given comparer.
     */
    private static void quickSort(Object[] sortedCollection,
                                  int left,
                                  int right,
                                  Comparer comparer)
    {
        int original_left = left;
        int original_right = right;
        Object mid = sortedCollection[(left + right) / 2];
        do
        {
            while (comparer.compare(sortedCollection[left], mid) < 0)
            {
                left++;
            }
            while (comparer.compare(mid, sortedCollection[right]) < 0)
            {
                right--;
            }
            if (left <= right)
            {
                Object tmp = sortedCollection[left];
                sortedCollection[left] = sortedCollection[right];
                sortedCollection[right] = tmp;
                left++;
                right--;
            }
        }
        while (left <= right);
        if (original_left < right)
        {
            quickSort(sortedCollection, original_left, right, comparer);
        }
        if (left < original_right)
        {
            quickSort(sortedCollection, left, original_right, comparer);
        }
    }

    /**
     * Sort the objects in the given collection using the given sort order.
     */
    private static void quickSort(Object[] sortedCollection,
                                  int left,
                                  int right,
                                  int[] sortOrder)
    {
        int original_left = left;
        int original_right = right;
        int mid = sortOrder[(left + right) / 2];
        do
        {
            while (sortOrder[left] < mid)
            {
                left++;
            }
            while (mid < sortOrder[right])
            {
                right--;
            }
            if (left <= right)
            {
                Object tmp = sortedCollection[left];
                sortedCollection[left] = sortedCollection[right];
                sortedCollection[right] = tmp;
                int tmp2 = sortOrder[left];
                sortOrder[left] = sortOrder[right];
                sortOrder[right] = tmp2;
                left++;
                right--;
            }
        }
        while (left <= right);
        if (original_left < right)
        {
            quickSort(sortedCollection, original_left, right, sortOrder);
        }
        if (left < original_right)
        {
            quickSort(sortedCollection, left, original_right, sortOrder);
        }
    }

    /**
     * Sort the strings in the given collection.
     */
    private static void quickSort(String[] sortedCollection, int left, int right)
    {
        int original_left = left;
        int original_right = right;
        String mid = sortedCollection[(left + right) / 2];
        do
        {
            while (sortedCollection[left].compareTo(mid) < 0)
            {
                left++;
            }
            while (mid.compareTo(sortedCollection[right]) < 0)
            {
                right--;
            }
            if (left <= right)
            {
                String tmp = sortedCollection[left];
                sortedCollection[left] = sortedCollection[right];
                sortedCollection[right] = tmp;
                left++;
                right--;
            }
        }
        while (left <= right);
        if (original_left < right)
        {
            quickSort(sortedCollection, original_left, right);
        }
        if (left < original_right)
        {
            quickSort(sortedCollection, left, original_right);
        }
    }

    /**
     * Sort the strings in the given collection in reverse alphabetical order.
     */
    private static void quickSortReverse(String[] sortedCollection,
                                         int left,
                                         int right)
    {
        int original_left = left;
        int original_right = right;
        String mid = sortedCollection[(left + right) / 2];
        do
        {
            while (sortedCollection[left].compareTo(mid) > 0)
            {
                left++;
            }
            while (mid.compareTo(sortedCollection[right]) > 0)
            {
                right--;
            }
            if (left <= right)
            {
                String tmp = sortedCollection[left];
                sortedCollection[left] = sortedCollection[right];
                sortedCollection[right] = tmp;
                left++;
                right--;
            }
        }
        while (left <= right);
        if (original_left < right)
        {
            quickSortReverse(sortedCollection, original_left, right);
        }
        if (left < original_right)
        {
            quickSortReverse(sortedCollection, left, original_right);
        }
    }

    /**
     * Reads in a string from the specified data input stream. The string has
     * been encoded using a modified UTF-8 format.
     * <p>
     * The first two bytes are read as if by <code>readUnsignedShort</code>.
     * This value gives the number of following bytes that are in the encoded
     * string, not the length of the resulting string. The following bytes are
     * then interpreted as bytes encoding characters in the UTF-8 format and are
     * converted into characters.
     * <p>
     * This method blocks until all the bytes are read, the end of the stream is
     * detected, or an exception is thrown.
     * 
     * @param in a data input stream.
     * @return a Unicode string.
     * @exception EOFException if the input stream reaches the end before all
     *                the bytes.
     * @exception IOException if an I/O error occurs.
     * @exception UTFDataFormatException if the bytes do not represent a valid
     *                UTF-8 encoding of a Unicode string.
     * @see java.io.DataInputStream#readUnsignedShort()
     */
    public final static char[] readUTF(DataInput in) throws IOException
    {
        int utflen = in.readUnsignedShort();
        char str[] = new char[utflen];
        int count = 0;
        int strlen = 0;
        while (count < utflen)
        {
            int c = in.readUnsignedByte();
            int char2, char3;
            switch (c >> 4)
            {
                case 0 :
                case 1 :
                case 2 :
                case 3 :
                case 4 :
                case 5 :
                case 6 :
                case 7 :
                    // 0xxxxxxx
                    count++;
                    str[strlen++] = (char) c;
                    break;
                case 12 :
                case 13 :
                    // 110x xxxx 10xx xxxx
                    count += 2;
                    if (count > utflen) throw new UTFDataFormatException();
                    char2 = in.readUnsignedByte();
                    if ((char2 & 0xC0) != 0x80) throw new UTFDataFormatException();
                    str[strlen++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                    break;
                case 14 :
                    // 1110 xxxx 10xx xxxx 10xx xxxx
                    count += 3;
                    if (count > utflen) throw new UTFDataFormatException();
                    char2 = in.readUnsignedByte();
                    char3 = in.readUnsignedByte();
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) throw new UTFDataFormatException();
                    str[strlen++] = (char) (((c & 0x0F) << 12)
                            | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                    break;
                default :
                    // 10xx xxxx, 1111 xxxx
                    throw new UTFDataFormatException();
            }
        }
        if (strlen < utflen)
        {
            System.arraycopy(str, 0, str = new char[strlen], 0, strlen);
        }
        return str;
    }

    /**
     * Returns the toString() of the given full path minus the first given
     * number of segments. The returned string is always a relative path (it has
     * no leading slash)
     */
    public static String relativePath(IPath fullPath, int skipSegmentCount)
    {
        boolean hasTrailingSeparator = fullPath.hasTrailingSeparator();
        String[] segments = fullPath.segments();
        // compute length
        int length = 0;
        int max = segments.length;
        if (max > skipSegmentCount)
        {
            for (int i1 = skipSegmentCount; i1 < max; i1++)
            {
                length += segments[i1].length();
            }
            // add the separator lengths
            length += max - skipSegmentCount - 1;
        }
        if (hasTrailingSeparator) length++;
        char[] result = new char[length];
        int offset = 0;
        int len = segments.length - 1;
        if (len >= skipSegmentCount)
        {
            // append all but the last segment, with separators
            for (int i = skipSegmentCount; i < len; i++)
            {
                int size = segments[i].length();
                segments[i].getChars(0, size, result, offset);
                offset += size;
                result[offset++] = '/';
            }
            // append the last segment
            int size = segments[len].length();
            segments[len].getChars(0, size, result, offset);
            offset += size;
        }
        if (hasTrailingSeparator) result[offset++] = '/';
        return new String(result);
    }

    /**
     * Return a new array which is the split of the given string using the given
     * divider. The given end is exclusive and the given start is inclusive.
     * <br>
     * <br>
     * For example:
     * <ol>
     * <li>
     * 
     * <pre>
     * 
     *     divider = 'b'
     *     string = &quot;abbaba&quot;
     *     start = 2
     *     end = 5
     *     result =&gt; { &quot;&quot;, &quot;a&quot;, &quot;&quot; }
     *  
     * </pre>
     * 
     * </li>
     * </ol>
     * 
     * @param divider the given divider
     * @param string the given string
     * @param start the given starting index
     * @param end the given ending index
     * @return a new array which is the split of the given string using the
     *         given divider
     * @throws ArrayIndexOutOfBoundsException if start is lower than 0 or end is
     *             greater than the array length
     */
    public static final String[] splitOn(char divider,
                                         String string,
                                         int start,
                                         int end)
    {
        int length = string == null ? 0 : string.length();
        if (length == 0 || start > end) return new String[0];
        int wordCount = 1;
        for (int i = start; i < end; i++)
            if (string.charAt(i) == divider) wordCount++;
        String[] split = new String[wordCount];
        int last = start, currentWord = 0;
        for (int i = start; i < end; i++)
        {
            if (string.charAt(i) == divider)
            {
                split[currentWord++] = string.substring(last, i);
                last = i + 1;
            }
        }
        split[currentWord] = string.substring(last, end);
        return split;
    }

    public static boolean isReadOnly(IResource resource)
    {
        ResourceAttributes resourceAttributes = resource
                .getResourceAttributes();
        if (resourceAttributes == null) return false; // not supported on this
                                                        // platform for this
                                                        // resource
        return resourceAttributes.isReadOnly();
    }

    public static void setReadOnly(IResource resource, boolean readOnly)
    {
        ResourceAttributes resourceAttributes = resource
                .getResourceAttributes();
        if (resourceAttributes == null) return; // not supported on this
                                                // platform for this resource
        resourceAttributes.setReadOnly(readOnly);
        try
        {
            resource.setResourceAttributes(resourceAttributes);
        }
        catch (CoreException e)
        {
            // ignore
        }
    }

    public static void sort(char[][] list)
    {
        if (list.length > 1) quickSort(list, 0, list.length - 1);
    }

    /**
     * Sorts an array of Comparable objects in place.
     */
    public static void sort(Comparable[] objects)
    {
        if (objects.length > 1) quickSort(objects, 0, objects.length - 1);
    }

    public static void sort(int[] list)
    {
        if (list.length > 1) quickSort(list, 0, list.length - 1);
    }

    /**
     * Sorts an array of objects in place. The given comparer compares pairs of
     * items.
     */
    public static void sort(Object[] objects, Comparer comparer)
    {
        if (objects.length > 1) quickSort(objects,
                                          0,
                                          objects.length - 1,
                                          comparer);
    }

    /**
     * Sorts an array of objects in place, using the sort order given for each
     * item.
     */
    public static void sort(Object[] objects, int[] sortOrder)
    {
        if (objects.length > 1) quickSort(objects,
                                          0,
                                          objects.length - 1,
                                          sortOrder);
    }

    /**
     * Sorts an array of strings in place using quicksort.
     */
    public static void sort(String[] strings)
    {
        if (strings.length > 1) quickSort(strings, 0, strings.length - 1);
    }

    /**
     * Sorts an array of Comparable objects, returning a new array with the
     * sorted items. The original array is left untouched.
     */
    public static Comparable[] sortCopy(Comparable[] objects)
    {
        int len = objects.length;
        Comparable[] copy = new Comparable[len];
        System.arraycopy(objects, 0, copy, 0, len);
        sort(copy);
        return copy;
    }

    /**
     * Sorts an array of Strings, returning a new array with the sorted items.
     * The original array is left untouched.
     */
    public static Object[] sortCopy(Object[] objects, Comparer comparer)
    {
        int len = objects.length;
        Object[] copy = new Object[len];
        System.arraycopy(objects, 0, copy, 0, len);
        sort(copy, comparer);
        return copy;
    }

    /**
     * Sorts an array of Strings, returning a new array with the sorted items.
     * The original array is left untouched.
     */
    public static String[] sortCopy(String[] objects)
    {
        int len = objects.length;
        String[] copy = new String[len];
        System.arraycopy(objects, 0, copy, 0, len);
        sort(copy);
        return copy;
    }

    /**
     * Sorts an array of strings in place using quicksort in reverse
     * alphabetical order.
     */
    public static void sortReverseOrder(String[] strings)
    {
        if (strings.length > 1) quickSortReverse(strings, 0, strings.length - 1);
    }

    /*
     * Returns whether the given compound name starts with the given prefix.
     * Returns true if the n first elements of the prefix are equals and the
     * last element of the prefix is a prefix of the corresponding element in
     * the compound name.
     */
    public static boolean startsWithIgnoreCase(String[] compoundName,
                                               String[] prefix)
    {
        int prefixLength = prefix.length;
        int nameLength = compoundName.length;
        if (prefixLength > nameLength) return false;
        for (int i = 0; i < prefixLength - 1; i++)
        {
            if (!compoundName[i].equalsIgnoreCase(prefix[i])) return false;
        }
        return compoundName[prefixLength - 1].toLowerCase()
                .startsWith(prefix[prefixLength - 1].toLowerCase());
    }

    /**
     * Converts a String[] to char[][].
     */
    public static char[][] toCharArrays(String[] a)
    {
        int len = a.length;
        char[][] result = new char[len][];
        for (int i = 0; i < len; ++i)
        {
            result[i] = a[i].toCharArray();
        }
        return result;
    }

    /**
     * Converts a String to char[][], where segments are separate by '.'.
     */
    public static char[][] toCompoundChars(String s)
    {
        int len = s.length();
        if (len == 0)
        {
            return new char[0][];
        }
        int segCount = 1;
        for (int off = s.indexOf('.'); off != -1; off = s.indexOf('.', off + 1))
        {
            ++segCount;
        }
        char[][] segs = new char[segCount][];
        int start = 0;
        for (int i = 0; i < segCount; ++i)
        {
            int dot = s.indexOf('.', start);
            int end = (dot == -1 ? s.length() : dot);
            segs[i] = new char[end - start];
            s.getChars(start, end, segs[i], 0);
            start = end + 1;
        }
        return segs;
    }

    /**
     * Converts a char[] to String.
     */
    public static String toString(char[] c)
    {
        return new String(c);
    }

    /**
     * Converts a char[][] to String, where segments are separated by '.'.
     */
    public static String toString(char[][] c)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, max = c.length; i < max; ++i)
        {
            if (i != 0) sb.append('.');
            sb.append(c[i]);
        }
        return sb.toString();
    }

    /**
     * Converts a char[][] and a char[] to String, where segments are separated
     * by '.'.
     */
    public static String toString(char[][] c, char[] d)
    {
        if (c == null) return new String(d);
        StringBuffer sb = new StringBuffer();
        for (int i = 0, max = c.length; i < max; ++i)
        {
            sb.append(c[i]);
            sb.append('.');
        }
        sb.append(d);
        return sb.toString();
    }

    public static void verbose(String log)
    {
        verbose(log, System.out);
    }

    public static synchronized void verbose(String log, PrintStream printStream)
    {
        int start = 0;
        do
        {
            int end = log.indexOf('\n', start);
            printStream.print(Thread.currentThread());
            printStream.print(" "); //$NON-NLS-1$
            printStream.print(log.substring(start, end == -1
                    ? log.length()
                    : end + 1));
            start = end + 1;
        }
        while (start != 0);
        printStream.println();
    }

    /**
     * Writes a string to the given output stream using UTF-8 encoding in a
     * machine-independent manner.
     * <p>
     * First, two bytes are written to the output stream as if by the
     * <code>writeShort</code> method giving the number of bytes to follow.
     * This value is the number of bytes actually written out, not the length of
     * the string. Following the length, each character of the string is output,
     * in sequence, using the UTF-8 encoding for the character.
     * 
     * @param str a string to be written.
     * @return the number of bytes written to the stream.
     * @exception IOException if an I/O error occurs.
     * @since JDK1.0
     */
    public static int writeUTF(OutputStream out, char[] str) throws IOException
    {
        int strlen = str.length;
        int utflen = 0;
        for (int i = 0; i < strlen; i++)
        {
            int c = str[i];
            if ((c >= 0x0001) && (c <= 0x007F))
            {
                utflen++;
            }
            else if (c > 0x07FF)
            {
                utflen += 3;
            }
            else
            {
                utflen += 2;
            }
        }
        if (utflen > 65535) throw new UTFDataFormatException();
        out.write((utflen >>> 8) & 0xFF);
        out.write((utflen >>> 0) & 0xFF);
        if (strlen == utflen)
        {
            for (int i = 0; i < strlen; i++)
                out.write(str[i]);
        }
        else
        {
            for (int i = 0; i < strlen; i++)
            {
                int c = str[i];
                if ((c >= 0x0001) && (c <= 0x007F))
                {
                    out.write(c);
                }
                else if (c > 0x07FF)
                {
                    out.write(0xE0 | ((c >> 12) & 0x0F));
                    out.write(0x80 | ((c >> 6) & 0x3F));
                    out.write(0x80 | ((c >> 0) & 0x3F));
                }
                else
                {
                    out.write(0xC0 | ((c >> 6) & 0x1F));
                    out.write(0x80 | ((c >> 0) & 0x3F));
                }
            }
        }
        return utflen + 2; // the number of bytes written to the stream
    }
}
