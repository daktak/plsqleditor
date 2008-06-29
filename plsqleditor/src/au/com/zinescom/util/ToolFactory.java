package au.com.zinescom.util;


/**
 * Factory for creating various compiler tools, such as scanners, parsers and
 * compilers.
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 * 
 * @since 2.0
 */
public class ToolFactory 
{

//    /**
//     * Create an instance of the built-in code formatter.
//     * 
//     * @param options - the options map to use for formatting with the default
//     *            code formatter. Recognized options are documented on
//     *            <code>PlsqleditorPlugin#getDefaultOptions()</code>. If set to
//     *            <code>null</code>, then use the current settings from
//     *            <code>JavaCore#getOptions</code>.
//     * @return an instance of the built-in code formatter
//     * @see CodeFormatter
//     * @since 3.0
//     */
//    public static CodeFormatter createCodeFormatter(Map options){
//        if (options == null) options = PlsqleditorPlugin.getOptions();
//        return new DefaultCodeFormatter(options);
//    }
//
//    /**
//     * Create a scanner, indicating the level of detail requested for
//     * tokenizing. The scanner can then be used to tokenize some source in a
//     * Java aware way. Here is a typical scanning loop:
//     * 
//     * <code>
//     * <pre>
//     * IScanner scanner = ToolFactory.createScanner(false, false, false, false);
//     * scanner.setSource(&quot;int i = 0;&quot;.toCharArray());
//     * while (true)
//     * {
//     *     int token = scanner.getNextToken();
//     *     if (token == ITerminalSymbols.TokenNameEOF) break;
//     *     System.out.println(token + &quot; : &quot;
//     *             + new String(scanner.getCurrentTokenSource()));
//     * }
//     * </pre>
//     * </code>
//     * 
//     * <p>
//     * The returned scanner will tolerate unterminated line comments (missing
//     * line separator). It can be made stricter by using API with extra boolean
//     * parameter (<code>strictCommentMode</code>).
//     * <p>
//     * 
//     * @param tokenizeComments if set to <code>false</code>, comments will be
//     *            silently consumed
//     * @param tokenizeWhiteSpace if set to <code>false</code>, white spaces
//     *            will be silently consumed,
//     * @param assertMode if set to <code>false</code>, occurrences of
//     *            'assert' will be reported as identifiers (<code>ITerminalSymbols#TokenNameIdentifier</code>),
//     *            whereas if set to <code>true</code>, it would report assert
//     *            keywords (<code>ITerminalSymbols#TokenNameassert</code>).
//     *            Java 1.4 has introduced a new 'assert' keyword.
//     * @param recordLineSeparator if set to <code>true</code>, the scanner
//     *            will record positions of encountered line separator ends. In
//     *            case of multi-character line separators, the last character
//     *            position is considered. These positions can then be extracted
//     *            using <code>IScanner#getLineEnds</code>. Only non-unicode
//     *            escape sequences are considered as valid line separators.
//     * @return a scanner
//     * @see org.eclipse.jdt.core.compiler.IScanner
//     */
//    public static IScanner createScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace, boolean assertMode, boolean recordLineSeparator){
//
//        PublicScanner scanner = new PublicScanner(tokenizeComments, tokenizeWhiteSpace, false/* nls */, assertMode ? ClassFileConstants.JDK1_4 : ClassFileConstants.JDK1_3/* sourceLevel */, null/* taskTags */, null/* taskPriorities */, true/* taskCaseSensitive */);
//        scanner.recordLineSeparator = recordLineSeparator;
//        return scanner;
//    }  
}
