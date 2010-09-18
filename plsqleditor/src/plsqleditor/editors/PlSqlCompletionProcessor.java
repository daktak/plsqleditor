package plsqleditor.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.parsers.BuiltInFunctionSegment;
import plsqleditor.parsers.PackageSegment;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;
import plsqleditor.preferences.PreferenceConstants;
import plsqleditor.stores.TableStore;
import plsqleditor.template.PlSqlContextType;
import plsqleditor.template.TemplateEditorUI;
import plsqleditor.template.TemplateEngine;
import plsqleditor.util.Util;
import au.com.gts.data.Column;
import au.com.gts.data.Table;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id: PlSqlCompletionProcessor.java,v 1.4.2.12 2005/12/19 20:26:11
 *          tobyz Exp $
 * 
 * Created on 22/02/2005
 */
public class PlSqlCompletionProcessor implements IContentAssistProcessor
{
    public static char[]           autoCompleteDelimiters = new char[]{' ', '\t', '(', ';', ',',
            '|'                                           };

	private static boolean isIllegalStateLogged = false; 

    private static final String[]  fgProposals;
    private static final SortedSet<String> ACS;

    private static final List<Segment>      BUILT_IN_FUNCTIONS     = new ArrayList<Segment>();

    static
    {
        ACS = new TreeSet<String>();

        addStrings(ACS, PlSqlCodeScanner.CONSTANTS);
        addStrings(ACS, PlSqlCodeScanner.DATATYPES);
        addStrings(ACS, PlSqlCodeScanner.DOT_ENABLED_KEYWORDS);
        addStrings(ACS, PlSqlCodeScanner.KEYWORDS);
        fgProposals = (String[]) ACS.toArray(new String[ACS.size()]);

        Segment seg = new BuiltInFunctionSegment("substr", "VARCHAR2");
        seg.addParameter("char", "", "VARCHAR2", "", 0);
        seg.addParameter("m", "", "NUMBER", "", 0);
        seg.addParameter("n", "", "NUMBER", "", 0);
        seg.setDocumentation("SUBSTR returns a portion of char, \n"
                + "beginning at character m, n characters long.\n"
                + "* If m is 0, it is treated as 1.\n"
                + "If m is positive, Oracle counts from the beginning \n"
                + "of char to find the first character.\n"
                + "* If m is negative, Oracle counts backwards from the \n" + "end of char.\n"
                + "* If n is omitted, Oracle returns all characters to \n"
                + "the end of char. If n is less than 1, a null is returned.\n"
                + "Floating-point numbers passed as arguments to SUBSTR \n"
                + "are automatically converted to integers.\n"
                + "EXAMPLE\nSELECT SUBSTR('ABCDEFG',3,4) \"Substring\"\n" + "FROM DUAL;\n"
                + "Substring\n" + "---------\n" + "CDEF");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("substrb", "VARCHAR2");
        seg.addParameter("char", "", "VARCHAR2", "", 0);
        seg.addParameter("m", "", "NUMBER", "", 0);
        seg.addParameter("n", "", "NUMBER", "", 0);
        seg.setDocumentation("This is the same as SUBSTR, except that the arguments \n"
                + "m and n are expressed in bytes, rather than in characters.\n"
                + "For a single-byte database character set, SUBSTRB is equivalent to SUBSTR.\n"
                + "Floating-point numbers passed as arguments to SUBSTRB are \n"
                + "automatically converted to integers\n"
                + "EXAMPLE\nAssume a double-byte database character set: \n"
                + "SELECT SUBSTRB('ABCDEFG',5,4.2)\n" + "Substring with bytes\n" + "FROM DUAL;\n"
                + "Substring with bytes\n" + "--------------------\n" + "CD");
        BUILT_IN_FUNCTIONS.add(seg);

        seg = new BuiltInFunctionSegment("instr", "NUMBER");
        seg.addParameter("stringToSearch", "", "VARCHAR2", "", 0);
        seg.addParameter("stringToSearchThrough", "", "VARCHAR2", "", 0);
        seg.addParameter("startIndex", "", "NUMBER", "", 0);
        seg.addParameter("findOccurence", "", "NUMBER", "", 0);
        seg
                .setDocumentation("The start index defaults to the beginning of the string\n"
                        + "If a single number is specified, it will be the start index\n"
                        + "The findOccurrence indicates which occurrence's index should be returned\n"
                        + "e.g select instr('helloe','e',1,1) would return 2 (the first occurrence of e from index 1 (the h)\n"
                        + "select instr('helloe','e',1,2) would return 6 (the second occurrence of e from the index 1 (the h)\n"
                        + "select instr('helloe','e',3,1) would return 6 (the first occurrence of e from the index 3 (the first l)\n"
                        + "select instr('helloe','e',3,2) would return 0 (there is NO second occurrence of e from the index 3 (the first l)");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("instrb", "NUMBER");
        seg.addParameter("stringToSearch", "", "VARCHAR2", "", 0);
        seg.addParameter("stringToSearchThrough", "", "VARCHAR2", "", 0);
        seg.addParameter("startIndex", "", "NUMBER", "", 0);
        seg.addParameter("findOccurence", "", "NUMBER", "", 0);
        seg
                .setDocumentation("The start index defaults to the beginning of the string\n"
                        + "If a single number is specified, it will be the start index\n"
                        + "The findOccurrence indicates which occurrence's index should be returned\n"
                        + "e.g select instr('helloe','e',1,1) would return 2 (the first occurrence of e from index 1 (the h)\n"
                        + "select instr('helloe','e',1,2) would return 6 (the second occurrence of e from the index 1 (the h)\n"
                        + "select instr('helloe','e',3,1) would return 6 (the first occurrence of e from the index 3 (the first l)\n"
                        + "select instr('helloe','e',3,2) would return 0 (there is NO second occurrence of e from the index 3 (the first l)");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("chr", "CHAR");
        seg.addParameter("numberToConvert", "", "NUMBER", "", 0);
        seg
                .setDocumentation("CHR returns the character having the binary equivalent to n in either the database character set or the national character set");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("concat", "VARCHAR2");
        seg.addParameter("char1", "", "VARCHAR2", "", 0);
        seg.addParameter("char2", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("CONCAT returns char1 concatenated with char2. This function is equivalent to the concatenation operator (||). ");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("decode", "VARCHAR2");
        seg.addParameter("targetString", "", "VARCHAR2", "", 0);
        seg.addParameter("name", "", "VARCHAR2", "", 0);
        seg.addParameter("value", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("This method searches for the targetString in a list of supplied name parameters, \n"
                        + "returning the value whose corresponding name matches the targetString.\n"
                        + "The name and value parameters can be repeated many times");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("initcap", "VARCHAR2");
        seg.addParameter("phrase", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("INITCAP returns the phrase, with the first letter of each word in uppercase, all other letters in lowercase. Words are delimited by white space or characters that are not alphanumeric");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("length", "VARCHAR2");
        seg.addParameter("char", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("returns the length of char in characters. If char has datatype CHAR, \n"
                        + "the length includes all trailing blanks. If char is null, this function \n"
                        + "returns null.\n"
                        + "Example\nSELECT LENGTH('CANDIDE') \"Length in characters\"\n"
                        + "FROM DUAL;\n"
                        + "Length in characters\n"
                        + "--------------------\n"
                        + "7");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("lengthb", "VARCHAR2");
        seg.addParameter("char", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("returns the length of char in bytes. If char is null, this function returns null.\n"
                        + "For a single-byte database character set, LENGTHB is equivalent to LENGTH.\n"
                        + "Example\nThis example assumes a double-byte database character set.\n"
                        + "SELECT LENGTHB ('CANDIDE') \"Length in bytes\"\n"
                        + "FROM DUAL;\n"
                        + "Length in bytes\n" + "---------------\n" + "14");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("lower", "VARCHAR2");
        seg.addParameter("toLower", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("returns toLower, with all letters lowercase.\n"
                        + "The return value has the same datatype as the argument char (CHAR or VARCHAR2).\n"
                        + "EXAMPLE\nSELECT LOWER('MR. SCOTT MCMILLAN') \"Lowercase\"\n"
                        + "FROM DUAL;\n" + "Lowercase\n" + "--------------------\n"
                        + "mr. scott mcmillan\n");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("lpad", "VARCHAR2");
        seg.addParameter("char1", "", "VARCHAR2", "", 0);
        seg.addParameter("n", "", "NUMBER", "", 0);
        seg.addParameter("char2", "", "VARCHAR2", "", 0);
        seg.setDocumentation("returns char1, left-padded to length n with the sequence of "
                + "characters in char2; char2 defaults to a single blank. If char1 "
                + "is longer than n, this function returns the portion of char1 that fits in n.\n"
                + "The argument n is the total length of the return value as it is displayed "
                + "on your terminal screen. In most character sets, this is also the number "
                + "of characters in the return value. However, in some multibyte character sets,"
                + "the display length of a character string can differ from the number of "
                + "characters in the string.\n"
                + "EXAMPLE\nSELECT LPAD('Page 1',15,'*.') \"LPAD example\"\n" + "FROM DUAL;\n"
                + "LPAD example\n" + "---------------\n" + "*.*.*.*.*Page 1");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("ltrim", "VARCHAR2");
        seg.addParameter("char", "", "VARCHAR2", "", 0);
        seg.addParameter("set", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("LTRIM removes characters from the left of char, with all the leftmost characters that appear in set removed;\n"
                        + "set defaults to a single blank. If char is a character literal, you must enclose it in single quotes.\n"
                        + "Oracle begins scanning char from its first character and removes all characters that appear in set until\n"
                        + "reaching a character not in set and then returns the result\n"
                        + "EXAMPLE\nSELECT LTRIM('xyxXxyLAST WORD','xy') \"LTRIM example\"\n"
                        + "FROM DUAL;\n" + "LTRIM example\n" + "------------\n" + "XxyLAST WORD\n");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("rpad", "VARCHAR2");
        seg.addParameter("char1", "", "VARCHAR2", "", 0);
        seg.addParameter("n", "", "NUMBER", "", 0);
        seg.addParameter("char2", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("returns char1, right-padded to length n with char2, replicated as many \n"
                        + "times as necessary; char2 defaults to a single blank. If char1 is longer \n"
                        + "than n, this function returns the portion of char1 that fits in n. \n"
                        + "The argument n is the total length of the return value as it is displayed \n"
                        + "on your terminal screen. In most character sets, this is also the number of \n"
                        + "characters in the return value. However, in some multibyte character sets,\n"
                        + "the display length of a character string can differ from the number of \n"
                        + "characters in the string.\n"
                        + "Example\n"
                        + "SELECT RPAD('MORRISON',12,'ab') \"RPAD example\"\n"
                        + "FROM DUAL;\n"
                        + "RPAD example\n" + "-----------------\n" + "MORRISONabab");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("replace", "VARCHAR2");
        seg.addParameter("char", "", "VARCHAR2", "", 0);
        seg.addParameter("searchString", "", "VARCHAR2", "", 0);
        seg.addParameter("replacementString", "", "VARCHAR2", "", 0);
        seg.setDocumentation("returns char with every occurrence of search_string replaced with\n"
                + "replacement_string. If replacement_string is omitted or null, all\n"
                + "occurrences of search_string are removed. If search_string is null,\n"
                + "char is returned. This function provides a superset of the \n"
                + "functionality provided by the TRANSLATE function. TRANSLATE provides\n"
                + "single-character, one-to-one substitution. REPLACE lets you substitute\n"
                + "one string for another as well as to remove character strings.\n" + "Example\n"
                + "SELECT REPLACE('JACK and JUE','J','BL') \"Changes\"\n" + "FROM DUAL;\n"
                + "Changes\n" + "--------------\n" + "BLACK and BLUE");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("rtrim", "VARCHAR2");
        seg.addParameter("char", "", "VARCHAR2", "", 0);
        seg.addParameter("set", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("returns char, with all the rightmost characters that appear in set removed;\n"
                        + "set defaults to a single blank. If char is a character literal, you must \n"
                        + "enclose it in single quotes. RTRIM works similarly to LTRIM.\n"
                        + "Example\n"
                        + "SELECT RTRIM('BROWNINGyxXxy','xy') \"RTRIM e.g.\"\n"
                        + "FROM DUAL;\n" + "RTRIM e.g\n" + "-------------\n" + "BROWNINGyxX");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("translate", "VARCHAR2");
        seg.addParameter("quotedString", "", "LITERAL STRING", "", 0);
        seg.addParameter("quotedFrom", "", "LITERAL STRING", "", 0);
        seg.addParameter("quotedTo", "", "LITERAL STRING", "", 0);
        seg
                .setDocumentation("returns quotedString with all occurrences of each character in quotedFrom replaced\n"
                        + "by its corresponding character in quotedTo. Characters in quotedString that are \n"
                        + "not in quotedFrom are not replaced. The argument quotedFrom can contain more \n"
                        + "characters than quotedTo. In this case, the extra characters at the end of from \n"
                        + "have no corresponding characters in quotedTo. If these extra characters appear in \n"
                        + "quotedString, they are removed from the return value. You cannot use an empty string \n"
                        + "for quotedTo to remove all characters in from from the return value. Oracle interprets \n"
                        + "the empty string as null, and if this function has a null argument, it returns null. \n"
                        + "Examples\n"
                        + "The following statement translates a license number. All letters 'ABC...Z' are \n"
                        + "translated to 'X' and all digits '012 . . . 9' are translated to '9': \n"
                        + "SELECT TRANSLATE('2KRW229',\n"
                        + "'0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ',\n"
                        + "'9999999999XXXXXXXXXXXXXXXXXXXXXXXXXX') \"License\"\n"
                        + "FROM DUAL;\n"
                        + "License\n"
                        + "--------\n"
                        + "9XXX999\n"
                        + "The following statement returns a license number with the characters removed and the \n"
                        + "digits remaining:\n"
                        + "SELECT TRANSLATE('2KRW229',\n"
                        + "'0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ', '0123456789') \"Translate example\"\n"
                        + "FROM DUAL;\n" + "Translate example\n" + "-----------------\n" + "2229");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("trim", "VARCHAR2");
        seg.addParameter("LEADING|TRAILING|BOTH", "", "LITERAL", "", 0);
        seg.addParameter("trim_character", "", "VARCHAR2", "", 0);
        seg.addParameter("FROM", "", "VARCHAR2", "", 0);
        seg.addParameter("trim_source", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("TRIM enables you to trim leading or trailing characters (or both) from a character string.\n"
                        + "If trim_character or trim_source is a character literal, you must enclose it in single \n"
                        + "quotes.\n"
                        + "* If you specify LEADING, Oracle removes any leading characters equal to trim_character. \n"
                        + "* If you specify TRAILING, Oracle removes any trailing characters equal to trim_character.\n"
                        + "* If you specify BOTH or none of the three, Oracle removes leading and trailing \n"
                        + "characters equal to trim_character.\n"
                        + "If you do not specify trim_character, the default value is a blank space.\n"
                        + "If you specify only trim_source, Oracle removes leading and trailing blank spaces. \n"
                        + "The function returns a value with datatype VARCHAR2. The maximum length of the value is \n"
                        + "the length of trim_source.\n"
                        + "If either trim_source or trim_character is a null value, then the TRIM function returns \n"
                        + "a null value.\n"
                        + "This example trims leading and trailing zeroes from a number:\n"
                        + "Example\n"
                        + "SELECT TRIM (0 FROM 0009872348900) \"TRIM Example\"\n"
                        + "FROM DUAL;\n" + "TRIM example\n" + "------------\n" + "98723489");
        BUILT_IN_FUNCTIONS.add(seg);
        seg = new BuiltInFunctionSegment("upper", "VARCHAR2");
        seg.addParameter("stringToSearch", "", "VARCHAR2", "", 0);
        seg
                .setDocumentation("returns char, with all letters uppercase. The return value has the same datatype as\n"
                        + "the argument char.\n"
                        + "Example\n"
                        + "SELECT UPPER('Large') \"Uppercase\"\n"
                        + "FROM DUAL;\n"
                        + "Upper\n"
                        + "-----\n" + "LARGE");
        BUILT_IN_FUNCTIONS.add(seg);
    }

    private static void addStrings(Set<String> toAddTo, String[] toAddFrom)
    {
        for (int i = 0; i < toAddFrom.length; i++)
        {
            String s = toAddFrom[i];
            toAddTo.add(s);
        }
    }

    protected IContextInformationValidator myValidator;

    private TemplateEngine                 myTemplateEngine;

    protected static class Validator
            implements
                IContextInformationValidator,
                IContextInformationPresenter
    {
        protected int fInstallOffset;

        protected Validator()
        {
            //
        }

        public boolean isContextInformationValid(int offset)
        {
            return Math.abs(fInstallOffset - offset) < 5;
        }

        public void install(IContextInformation info, ITextViewer viewer, int offset)
        {
            fInstallOffset = offset;
        }

        public boolean updatePresentation(int documentPosition, TextPresentation presentation)
        {
            return false;
        }
    }

    public PlSqlCompletionProcessor()
    {
        myValidator = new Validator();
        TemplateContextType contextType = TemplateEditorUI.getDefault().getContextTypeRegistry()
                .getContextType(PlSqlContextType.PLSQL_CONTEXT_TYPE);
        if (contextType == null)
        {
            contextType = new PlSqlContextType();
            TemplateEditorUI.getDefault().getContextTypeRegistry().addContextType(contextType);
        }
        if (contextType != null)
        {
            myTemplateEngine = new TemplateEngine(contextType);
        }
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
    {
        try
        {
            PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
            IPreferenceStore prefs = plugin.getPreferenceStore();
            boolean parametersOnNewline = prefs
                    .getBoolean(PreferenceConstants.P_PARAMETERS_ON_NEWLINE);
            boolean firstParameterOnNewline = prefs
                    .getBoolean(PreferenceConstants.P_FIRST_PARAMETER_ON_NEWLINE);
            boolean useNamesInsteadOfTypes = prefs
                    .getBoolean(PreferenceConstants.P_METHOD_USE_PARAM_NAMES_NOT_TYPES);
            boolean alignArrows = prefs.getBoolean(PreferenceConstants.P_METHOD_ALIGN_ARROWS);
            boolean semiColonAtEnd = prefs
                    .getBoolean(PreferenceConstants.P_METHOD_SEMI_COLON_AT_END);
            boolean commaOnNewline = plugin.getPreferenceStore()
                    .getBoolean(PreferenceConstants.P_COMMA_ON_NEWLINE);

            IDocument doc = viewer.getDocument();
            int line = doc.getLineOfOffset(documentOffset);
            int linestart = doc.getLineOffset(line);
            int length = documentOffset - linestart;
            String lineOfText = doc.get(linestart, length);

            int lastNonUsableCharacter = -1;
            for (int i = 0; i < autoCompleteDelimiters.length; i++)
            {
                char c = autoCompleteDelimiters[i];
                lastNonUsableCharacter = Math
                        .max(lastNonUsableCharacter, lineOfText.lastIndexOf(c));
            }
            String currText = lineOfText.substring(lastNonUsableCharacter + 1).toUpperCase();

            String previousWord = getPreviousWord(lineOfText, lastNonUsableCharacter);

            List<Segment> completions = new ArrayList<Segment>();
            currText = computeCompletionSegments(completions,
                                                 currText,
                                                 documentOffset,
                                                 doc,
                                                 previousWord);


            ICompletionProposal result[] = new ICompletionProposal[completions.size()];
            int index = 0;
            int currTextLength = currText.length();

            for (Iterator<Segment> it = completions.iterator(); it.hasNext();)
            {
                Segment proposal = (Segment) it.next();

                String proposalString = proposal.getPresentationName(true, true, true);
                String replacementString = proposalString.replaceAll(" [Ii][Nn] [Oo][Uu][Tt] ",
                                                                     " => ");
                replacementString = replacementString.replaceAll(" [Ii][Nn]/[Oo][Uu][Tt] ", " => ");
                replacementString = replacementString.replaceAll(" [Ii][Nn] ", " => ");
                replacementString = replacementString.replaceAll(" [Oo][Uu][Tt] ", " => ");
                replacementString = replacementString.replaceAll(" : .*", "");

                String contextString = replacementString.replaceAll(" => [^,)]+", "");

                String spacePrefix = null;
                // fixes feature to allow auto completion to go to a new line
                // Bug Id: 1387877
                if (parametersOnNewline)
                {
                    int nameLength = proposal.getName().length();
                    int numSpaces = length + (nameLength - currTextLength);
                    StringBuffer spaces = new StringBuffer();
                    int startPoint = -1;
                    if (commaOnNewline)
                    {
                        startPoint = 0;
                    }
                    for (int i = startPoint; i < numSpaces; i++)
                    {
                        spaces.append(" ");
                    }
                    replacementString = replacementString.replaceAll("(,) ", (commaOnNewline
                            ? PlSqlEditor.NEW_LINE + spaces + "$1"
                            : "$1" + PlSqlEditor.NEW_LINE + spaces));
                    spacePrefix = spaces.toString();
                }

                // adds feature 1430506 - Improvement of Code Completion
                if (useNamesInsteadOfTypes)
                {
                    replacementString = replacementString.replaceAll("(\\w+) => [^\n,)]+",
                                                                     "$1 => $1");
                }
                // adds feature 1430506 - Improvement of Code Completion
                if (alignArrows && parametersOnNewline)
                {
                    replacementString = alignArrows(replacementString, length - currTextLength);
                }
                // adds feature 1430506 - Improvement of Code Completion
                if (semiColonAtEnd)
                {
                    SegmentType type = proposal.getType();
                    if (type == SegmentType.Function || type == SegmentType.Procedure
                            || type == SegmentType.Cursor)
                    {
                        replacementString = replacementString + ";";
                    }
                }

                // added support for feature 1460322 - Format method parameters
                // on next line and indented
                if (firstParameterOnNewline)
                {
                    int numSpaces = length - currTextLength
                            + prefs.getInt(PreferenceConstants.P_EDITOR_TAB_WIDTH);
                    StringBuffer spaces = new StringBuffer();
                    for (int i = 0; i < numSpaces; i++)
                    {
                        spaces.append(" ");
                    }
                    String toReplaceWith = spaces.toString();
                    if (commaOnNewline)
                    {
                        toReplaceWith = toReplaceWith.substring(0, spaces.length() - 1);
                    }
                    if (spacePrefix != null)
                    {
                        replacementString = replacementString
                                .replaceAll(spacePrefix, toReplaceWith);
                    }
                    replacementString = replacementString.replaceFirst("\\(", "("
                            + PlSqlEditor.NEW_LINE + spaces);
                }

                // this one is used to pop up after the completion is selected
                IContextInformation info = new ContextInformation(proposalString, contextString);
                result[index++] = new CompletionProposal(replacementString, documentOffset
                        - currTextLength, currTextLength, replacementString.length(), plugin
                        .getImageRegistry().get(proposal.getImageKey()), proposalString, info,
                        proposal.getDocumentation());
            }
            if (myTemplateEngine != null)
            {
                myTemplateEngine.reset();
                myTemplateEngine.complete(viewer, documentOffset);
                TemplateProposal templateResults[] = myTemplateEngine.getResults();

                ICompletionProposal total[] = new ICompletionProposal[result.length
                        + templateResults.length];
                System.arraycopy(templateResults, 0, total, 0, templateResults.length);
                System.arraycopy(result, 0, total, templateResults.length, result.length);
                result = total;
            }
            return result;
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    protected static String getPreviousWord(String lineOfText, int lastNonWordCharacter)
    {
        StringBuffer toReturn = new StringBuffer();
        int index;
        for (index = lastNonWordCharacter; index >= 0; index--)
        {
            char c = lineOfText.charAt(index);
            if (Character.isJavaIdentifierPart(c))
            {
                toReturn.append(c);
                break;
            }
        }
        while (index > 0)
        {
            index--;
            char c = lineOfText.charAt(index);
            if (Character.isJavaIdentifierPart(c))
            {
                toReturn.append(c);
            }
            else
            {
                break;
            }
        }
        return toReturn.reverse().toString();
    }

    /**
     * This method aligns the arrows in a method completion, so that the result
     * of an original string looking like <code>
     * method_name(pis_value_one => type_one
     *            ,pis_v_two => type_two)</code>
     * can instead look like the resulting string <code>
     * method_name(pis_value_one => type_one
     *            ,pis_v_two     => type_two)</code>
     * 
     * @param originalString The original string, in a format identified by the
     *            comment above.
     * 
     * @param startingOffsetFromBeginningOfLine The number of characters in the
     *            line prior to the autocompleted method name.
     * 
     * @return The converted string, where all arrows line up with each other.
     */
    private String alignArrows(String originalString, int startingOffsetFromBeginningOfLine)
    {
        StringBuffer buf = new StringBuffer();

        int start = 0;
        int lf = originalString.indexOf('\n');

        if (lf < 0 || lf == originalString.length() - 1)
        {
            // no newlines
            buf.append(originalString);
            return buf.toString();
        }

        int arrowIndex = originalString.indexOf("=>");

        start = lf + 1;
        int maxArrowIndexInLine = arrowIndex + startingOffsetFromBeginningOfLine;

        while (start < originalString.length())
        {
            lf = originalString.indexOf('\n', start);
            arrowIndex = originalString.indexOf("=>", start);
            maxArrowIndexInLine = Math.max(arrowIndex - start, maxArrowIndexInLine);
            if (lf < 0)
            {
                break;
            }
            start = lf + 1;
        }

        start = 0;
        int numSpaces = 0;
        int arrowIndexInLine = 0;
        lf = originalString.indexOf('\n');
        while (start < originalString.length())
        {
            arrowIndex = originalString.indexOf("=>", start);
            if (arrowIndex == -1)
            {
                arrowIndex = start;
            }
            arrowIndexInLine = arrowIndex - start;

            if (start == 0)
            {
                arrowIndexInLine += startingOffsetFromBeginningOfLine;
            }
            buf.append(originalString.substring(start, arrowIndex));
            if (arrowIndexInLine < maxArrowIndexInLine)
            {
                numSpaces = maxArrowIndexInLine - arrowIndexInLine;
                for (int i = 0; i < numSpaces; i++)
                {
                    buf.append(" ");
                }
            }
            if (lf < 0)
            {
                buf.append(originalString.substring(arrowIndex));
                break;
            }
            buf.append(originalString.subSequence(arrowIndex, lf + 1));
            start = lf + 1;
            lf = originalString.indexOf('\n', start);
        }
        return buf.toString();
    }

    /**
     * @param completions
     * @return the current myOutputText
     */
    private String computeCompletionSegments(List<Segment> completions,
                                             String currentWord,
                                             int documentOffset,
                                             IDocument doc,
                                             String previousWord)
    {
        PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
        List<Segment> thisDocsSegments = plugin.getCurrentSegments(doc);

        return computeMatchedSegments(completions,
                                      documentOffset,
                                      doc,
                                      currentWord,
                                      previousWord,
                                      thisDocsSegments,
                                      false);
    }

    /**
     * @param completions
     * @return the current myOutputText
     */
    public String computeCompletionSegments(List<Segment> completions,
                                            String currentWord,
                                            int documentOffset,
                                            IDocument doc)
    {
        return computeCompletionSegments(completions, currentWord, documentOffset, doc, "");
    }

    /**
     * 
     * @param completions
     * @param documentOffset
     * @param doc
     * @param currText
     * @param thisDocsSegments
     * @param exactMatch
     * @return
     */
    public static String computeMatchedSegments(List<Segment> completions,
                                                int documentOffset,
                                                IDocument doc,
                                                String currText,
                                                String previousWord,
                                                List<Segment> thisDocsSegments,
                                                boolean exactMatch)
    {
        /*
         * Fix for Support Request 1418289 - "Describe" right-click. I put this
         * into a single method, and added exactMatch
         */
        PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
        IProject project = plugin.getProject();
        Position dummyPosition = new Position(0);
        TableStore tStore = plugin.getTableStore(project);

        // check qualifiers
        if (currText.indexOf(".") != -1)
        {
            int lastDotIndex = currText.lastIndexOf(".");
            String prior = currText.substring(0, lastDotIndex).toLowerCase();
            currText = currText.substring(lastDotIndex + 1);
            if (prior.indexOf(".") != -1)
            {
                // two dots
                int index = prior.lastIndexOf(".");
                String schema = prior.substring(0, index);
                String packageName = prior.substring(index + 1);
                List<Segment> segments = plugin.getSegments(schema, packageName);
                for (Iterator<Segment> it = segments.iterator(); it.hasNext();)
                {
                    Segment segment = (Segment) it.next();
                    checkSegment(documentOffset, currText, completions, segment, false, exactMatch);
                }
                storeColumns(completions,
                             currText,
                             dummyPosition,
                             tStore,
                             schema,
                             packageName,
                             exactMatch);
            }
            else
            {
                // only one dot, could be schema or package or table name
                // could also be t.columnName where t is previously defined.
                // could also be record.field_name or object.field_name

                String schema = null;
                String packageName = null;
                SortedSet<String> schemas = plugin.getSchemas(project);
                if (schemas.contains(prior))
                {
                    // prior is a schema
                    schema = prior;

                    // add packages for the schema
                    for (Iterator<String> it = plugin.getPackages(schema, false).iterator(); it.hasNext();)
                    {
                        String str = (String) it.next();
                        if (str != null)
                        {
                            str = str.toUpperCase();
                            if (exactMatch ? str.equals(currText) : str.startsWith(currText))
                            {
                                completions
                                        .add(new Segment(str, dummyPosition, SegmentType.Package));
                            }
                        }
                    }
                    // TODO should check in case the name is a package name,
                    // but it matches a schema name

                    storeTables(completions, currText, dummyPosition, tStore, schema, exactMatch);
                }
                else
                {
                    // prior is not a schema
                    schema = plugin.getCurrentSchema();
                    packageName = prior;

                    if (packageName != null)
                    {
                        List<Segment> segments = plugin.getSegments(schema, packageName);
                        for (Iterator<Segment> it = segments.iterator(); it.hasNext();)
                        {
                            Segment segment = (Segment) it.next();
                            // try to add the packages for the current schema
                            checkSegment(documentOffset,
                                         currText,
                                         completions,
                                         segment,
                                         false,
                                         exactMatch);
                        }
                        // add object/record fields
//                        addObjectOrRecordFields(completions,
//                                                currText,
//                                                dummyPosition,
//                                                tStore,
//                                                schema,
//                                                packageName,
//                                                exactMatch);

                        // add the columns for the table in the current schema
                        storeColumns(completions,
                                     currText,
                                     dummyPosition,
                                     tStore,
                                     schema,
                                     packageName,
                                     exactMatch);

                        // add the columns for a table specified earlier (and
                        // used as t.columnName)
                        storeQualifiedColumns(completions,
                                              currText,
                                              dummyPosition,
                                              tStore,
                                              schema,
                                              packageName,
                                              doc,
                                              documentOffset,
                                              exactMatch);
                    }
                }
            }
        }
        else
        // no dots
        {
            boolean isEndProcSpecified = false;
            // support for feature 1457468 - Auto-complete for END procedure
            if (previousWord.equalsIgnoreCase("END"))
            {
                String methodName = Util.findPreviousMethodName(doc, documentOffset);
                if (methodName.length() > 0 && methodName.toUpperCase().startsWith(currText))
                {
                    completions.add(new Segment(methodName, dummyPosition, SegmentType.Code));
                    isEndProcSpecified = true;
                }
            }
            if (!isEndProcSpecified)
            {
                for (Iterator<String> it = plugin.getSchemas(project).iterator(); it.hasNext();)
                {
                    String str = (String) it.next();
                    if (str != null)
                    {
                        str = str.toUpperCase();
                        if (exactMatch ? str.equals(currText) : str.startsWith(currText))
                        {
                            completions.add(new Segment(str, dummyPosition, SegmentType.Schema));
                        }
                    }
                }
                String schema = plugin.getCurrentSchema();
                if (schema != null)
                {
                    for (Iterator<String> it = plugin.getPackages(schema, true).iterator(); it.hasNext();)
                    {
                        String str = (String) it.next();
                        str = str.toUpperCase();
                        if (exactMatch ? str.equals(currText) : str.startsWith(currText))
                        {
                            completions.add(new Segment(str, dummyPosition, SegmentType.Package));
                        }
                    }
                    storeTables(completions, currText, dummyPosition, tStore, schema, exactMatch);
                }
                // add the columns for a table specified earlier (and used as
                // columnName where
                // the table is implied by the current statement)
                storeUnqualifiedColumns(completions,
                                        currText,
                                        dummyPosition,
                                        tStore,
                                        schema,
                                        doc,
                                        documentOffset,
                                        exactMatch);
                if (thisDocsSegments != null)
                {
                    for (Iterator<Segment> it = thisDocsSegments.iterator(); it.hasNext();)
                    {
                        Segment segment = (Segment) it.next();
                        checkSegment(documentOffset,
                                     currText,
                                     completions,
                                     segment,
                                     true,
                                     exactMatch);
                    }
                }
                for (Iterator<Segment> it = BUILT_IN_FUNCTIONS.iterator(); it.hasNext();)
                {
                    Segment segment = (Segment) it.next();
                    checkSegment(documentOffset, currText, completions, segment, false, exactMatch);
                }
                for (Iterator<String> it = ACS.iterator(); it.hasNext();)
                {
                    String string = (String) it.next();
                    Segment segment = new Segment(string, dummyPosition, SegmentType.Label);
                    if (exactMatch ? string.equals(currText) : string.startsWith(currText))
                    {
                        completions.add(segment);
                    }
                }
            }
        }
        return currText;
    }

    // TODO implement this
//    private static void addObjectOrRecordFields(List completions,
//                                                String currText,
//                                                Position dummyPosition,
//                                                TableStore store,
//                                                String schema,
//                                                String packageName,
//                                                String objectName,
//                                                boolean exactMatch)
//    {
//        PlSqlTypeManager mgr = PlSqlTypeManager.getTypeManager(PlsqleditorPlugin.getDefault()
//                .getProject());
//        // TODO Must look up type here
//        PlSqlType type = getTypeFromSeg();
//        type = mgr.getType(schema, packageName, objectName);
//        String[] fields = type.getAutoCompleteFields();
//        for (int i = 0; i < fields.length; i++)
//        {
//            String string = fields[i];
//            Segment segment = new Segment(string, dummyPosition, SegmentType.Label);
//            if (exactMatch ? string.equals(currText) : string.startsWith(currText))
//            {
//                completions.add(segment);
//            }
//        }
//    }

//    private static PlSqlType getTypeFromSeg(int documentOffset, IDocument doc, String currText, List thisDocsSegs, boolean exactMatch)
//    {
//        List segments = new ArrayList();
//        computeMatchedSegments(segments, documentOffset, doc, currText, "", thisDocsSegs, exactMatch);
//        
//        if (segments.size() > 0)
//        {
//            final String PRE_SPACES = "   ";
//            final String _COLON_ = " : ";
//            Segment foundSegment = (Segment) segments.get(0);
//            SegmentType type = foundSegment.getType();
//            if (type == SegmentType.Table)
//            {
//                Table t = (Table) foundSegment.getReferredData();
//                StringBuffer sb = new StringBuffer();
//                if (t != null)
//                {
//                    sb.append("Table ").append(t.getName());
//                    List cols = t.getColumns();
//                    int maxLength = 0;
//                    for (Iterator it = cols.iterator(); it.hasNext();)
//                    {
//                        Column col = (Column) it.next();
//                        int length = col.getName().length();
//                        maxLength = maxLength > length ? maxLength : length;
//                    }
//
//                    for (Iterator it = cols.iterator(); it.hasNext();)
//                    {
//                        StringBuffer tmpSb = new StringBuffer(PRE_SPACES);
//                        Column col = (Column) it.next();
//                        tmpSb.append(UsefulOperations.pad(col.getName(), UsefulOperations.SPACE, maxLength)).append(_COLON_);
//                        tmpSb.append(col.getSQLTypeName());
//
//                        sb.append(UsefulOperations.NEWLINE).append(tmpSb.toString());
//                    }
//                    return sb.toString();
//                }
//                else
//                {
//                    return "This is a table";
//                }
//            }
//            else if (type == SegmentType.Column)
//            {
//                Column c = (Column) foundSegment.getReferredData();
//                if (c != null)
//                {
//                    StringBuffer sb = new StringBuffer();
//                    sb.append("Column from table ").append(c.getTable().getName());
//                    sb.append("\n").append(PRE_SPACES).append(c.getName());
//                    sb.append(_COLON_).append(c.getSQLTypeName());
//                    return sb.toString();
//                }
//                else
//                {
//                    return "This is a column";
//                }
//            }
//            else if (type == SegmentType.Constant)
//            {
//                // TODO change parsing to pick up the constant part of the segment, and return it here
//                return "Constant: " + foundSegment.getReferredData() +
//                "\n" +
//                foundSegment.getDocumentation();
//            }
//            else if (type == SegmentType.Field)
//            {
//                return foundSegment.getPresentationName(true, true, true) +
//                "\n" +
//                foundSegment.getDocumentation();
//            }
//            return foundSegment.getDocumentation(true);
//        }
//
//    }

    /**
     * This method stores a list of column names that might be used against the
     * table with the <code>proxiedTableName</code>.
     * <p>
     * e.g in the statment <blockquote>select t.name from firsttable t,
     * othertable ot where t.xyz</blockquote> the <code>t</code> would be
     * interpreted as type firsttable and columns would be inserted based on
     * that type.
     * 
     * @param completions The list of completions to add the discovered columns
     *            to.
     * 
     * @param currText The current piece of text after the last dot. In the
     *            example above this would match to the <code>xyz</code>.
     * 
     * @param dummyPosition A dummy position to use in the added completion.
     * 
     * @param tableStore The store containing the tables and their columns.
     * 
     * @param schema The schema to use if the specified table (when located in
     *            the previous statement) needs to be qualified. It should
     *            represent the current schema in which the statement is being
     *            executed.
     * 
     * @param proxiedTableName The name of the proxied &qt;table&qt;
     * 
     * @param document The document in which we will extract the statement in
     *            which the cursor is <code>currentOffste</code> is currently
     *            located and at which we hope to find the full definition of
     *            the sought after <code>proxiedTableName</code>.
     * 
     * @param currentOffset The offset into the supplied <code>document</code>
     *            around which location we are trying to find a statement.
     * 
     * @param exactMatch If this is true, <code>currText</code> will have to
     *            be equal to the column name, rather than just the beginning of
     *            it.
     */
    private static void storeQualifiedColumns(List<Segment> completions,
                                              String currText,
                                              Position dummyPosition,
                                              TableStore tableStore,
                                              String schema,
                                              String proxiedTableName,
                                              IDocument document,
                                              int currentOffset,
                                              boolean exactMatch)
    {
        // TODO should make the emptyLineIsSeparator dependent on the file type
        try
		{
			List<String> tokens = Util.grabCurrentPlSqlTokens(document, currentOffset, false);
			String actualTableName = null;

			int size = tokens.size();

			// search backwards through the file to locate the most local occurrence
			for (int i = size - 1; i > 0; i--)
			{
			    String token = (String) tokens.get(i);
			    if (token.equals(proxiedTableName))
			    {
			        if (i > 0)
			        {
			            actualTableName = (String) tokens.get(i - 1);
			        }
			        break;
			    }
			}
			if (actualTableName != null)
			{
			    // found the real definition
			    Table foundTable = getTable(tableStore, actualTableName, schema);
			    if (foundTable != null)
			    {
			        List<Column> columns = foundTable.getColumns();

			        for (Iterator<Column> it = columns.iterator(); it.hasNext();)
			        {
			            Column c = (Column) it.next();
			            String columnName = c.getName();
			            if (exactMatch ? columnName.toUpperCase().equals(currText) : columnName
			                    .startsWith(currText))
			            {
			                Segment segment = new Segment(columnName, dummyPosition, SegmentType.Column);
			                segment.setReferredData(c);
			                completions.add(segment);
			            }
			        }
			    }
			}
		}
		catch (IllegalStateException e)
		{
			if (!isIllegalStateLogged)
			{
				PlsqleditorPlugin.log("Failed to get qualified columns", e);
				isIllegalStateLogged = true;
			}
		}
    }

    /**
     * This method stores a list of column names that might be used against a
     * table previously declared in the statement located around the
     * <code>currentOffset</code> within the supplied <code>document</code>.
     * <p>
     * e.g in the statment <blockquote>select name from firsttable, secondtable
     * where xyz</blockquote> the columns of firsttable and secondtable would
     * be inserted based on the presence of those two tables in the preceding
     * section of the statement..
     * 
     * @param completions The list of completions to add the discovered columns
     *            to.
     * 
     * @param currText The current piece of text after the last space
     *            (/separator). In the example above this would match to the
     *            <code>xyz</code>.
     * 
     * @param dummyPosition A dummy position to use in the added completion.
     * 
     * @param tableStore The store containing the tables and their columns.
     * 
     * @param schema The schema to use if the specified table (when located in
     *            the previous statement) needs to be qualified. It should
     *            represent the current schema in which the statement is being
     *            executed.
     * 
     * @param document The document in which we will extract the statement in
     *            which the cursor is <code>currentOffste</code> is currently
     *            located and at which we hope to find the full definition of
     *            the sought after <code>proxiedTableName</code>.
     * 
     * @param currentOffset The offset into the supplied <code>document</code>
     *            around which location we are trying to find a statement.
     * 
     * @param exactMatch If this is true, <code>currText</code> will have to
     *            be equal to the column name, rather than just the beginning of
     *            it.
     */
    private static void storeUnqualifiedColumns(List<Segment> completions,
                                                String currText,
                                                Position dummyPosition,
                                                TableStore tableStore,
                                                String schema,
                                                IDocument document,
                                                int currentOffset,
                                                boolean exactMatch)
    {
        List<String> tokens = Util.grabCurrentPlSqlTokens(document, currentOffset, true);

        boolean isStarted = false;
        boolean isPastWhere = false;

        List<String> possibleTablesList = new ArrayList<String>();

        // search through the statement, looking for the first WHERE (to stop
        // at, and become valid)
        for (Iterator<String> it = tokens.iterator(); it.hasNext();)
        {
            String token = it.next();
            if (isStarted)
            {
                if (token.equalsIgnoreCase("WHERE"))
                {
                    isPastWhere = true;
                    break;
                }
                possibleTablesList.add(token);
            }
            else if (token.equalsIgnoreCase("FROM"))
            {
                isStarted = true;
            }
        }
        if (isPastWhere)
        {
            // found the real definition
            for (Iterator<String> it = possibleTablesList.iterator(); it.hasNext();)
            {
                String possibleTableName = (String) it.next();
                Table foundTable = getTable(tableStore, possibleTableName, schema);
                if (foundTable != null)
                {
                    List<Column> columns = foundTable.getColumns();

                    for (Iterator<Column> columnIterator = columns.iterator(); columnIterator.hasNext();)
                    {
                        Column c = (Column) columnIterator.next();
                        String columnName = c.getName();
                        if (exactMatch ? columnName.toUpperCase().equals(currText) : columnName
                                .startsWith(currText))
                        {
                            Segment segment = new Segment(columnName, dummyPosition,
                                    SegmentType.Column);
                            segment.setReferredData(c);
                            completions.add(segment);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param tableStore The store containing the tables.
     * 
     * @param specifiedTableName This is the specified name of the table. It may
     *            be qualified by a schema name, but it does not have to be. If
     *            it is not, the supplied <code>schema</code> may be supplied
     *            as an aid.
     * 
     * @param schema The name of the schema to use if the specifiedTableName
     *            does not include a schema.
     * 
     * @return The actual instance of the table that is sought, or null if it
     *         could not be found.
     */
    private static Table getTable(TableStore tableStore, String specifiedTableName, String schema)
    {
        int dotIndex = specifiedTableName.indexOf(".");
        if (dotIndex != -1)
        {
            schema = specifiedTableName.substring(0, dotIndex);
            specifiedTableName = specifiedTableName.substring(dotIndex + 1);
        }
        Table[] tables = tableStore.getTables(schema);
        specifiedTableName = specifiedTableName.toUpperCase();
        for (int i = 0; i < tables.length; i++)
        {
            Table t = tables[i];
            if (t.getName().equals(specifiedTableName))
            {
                return t;
            }
        }
        return null;
    }

    /**
     * This method stores the cached tables that match the current text
     * <code>currText</code> in the list of <code>completions</code>.
     * 
     * @param completions The list of completions to store possible table
     *            matches.
     * 
     * @param currText The current text that the tables must match.
     * 
     * @param dummyPosition A dummy position to add for the table, since tables
     *            are only located in the database.
     * 
     * @param tStore The table store to use.
     * 
     * @param schema The schema name we will use to narrow the table search.
     */
    private static void storeTables(List<Segment> completions,
                                    String currText,
                                    Position dummyPosition,
                                    TableStore tStore,
                                    String schema,
                                    boolean exactMatch)
    {
        try
		{
			if (tStore != null)
			{
			    Table[] tables = tStore.getTables(schema);
			    for (int i = 0; i < tables.length; i++)
			    {
			        Table t = tables[i];
			        String tableName = t.getName().toUpperCase();
			        if (exactMatch ? tableName.equals(currText) : tableName.startsWith(currText))
			        {
			            Segment s = new Segment(t.getName(), dummyPosition, SegmentType.Table);
			            s.setReferredData(t);
			            completions.add(s);
			        }
			    }
			}
		}
		catch (IllegalStateException e)
		{
			if (!isIllegalStateLogged)
			{
				PlsqleditorPlugin.log("Failed to store tables", e);
				isIllegalStateLogged = true;
			}
		}
    }

    /**
     * This method stores the cached table columns that match the current text
     * <code>currText</code> in the list of <code>completions</code>.
     * 
     * @param completions The list of completions to store possible table column
     *            matches.
     * 
     * @param currText The current text that the columns must match.
     * 
     * @param dummyPosition A dummy position to add for the column, since tables
     *            are only located in the database.
     * 
     * @param tStore The table store to use.
     * 
     * @param schema The schema name we will use to narrow the table column
     *            search.
     */
    private static void storeColumns(List<Segment> completions,
                                     String currText,
                                     Position dummyPosition,
                                     TableStore tStore,
                                     String schema,
                                     String packageName,
                                     boolean exactMatch)
    {
        packageName = packageName.toUpperCase();
        try
		{
			if (tStore != null)
			{
			    Table[] tables = tStore.getTables(schema);
			    for (int i = 0; i < tables.length; i++)
			    {
			        Table t = tables[i];
			        if (t.getName().toUpperCase().equals(packageName))
			        {
			            List<Column> cols = t.getColumns();
			            for (Iterator<Column> it = cols.iterator(); it.hasNext();)
			            {
			                Column col = (Column) it.next();
			                String colName = col.getName().toUpperCase();
			                if (exactMatch ? colName.equals(currText) : colName.startsWith(currText))
			                {
			                    Segment s = new Segment(col.getName(), new Position(0),
			                            SegmentType.Column);
			                    s.setReferredData(col);
			                    completions.add(s);
			                }
			            }
			        }
			    }
			}
		}
		catch (IllegalStateException e)
		{
			if (!isIllegalStateLogged)
			{
				PlsqleditorPlugin.log("Failed to store columns", e);
				isIllegalStateLogged = true;
			}
		}
    }

    private static void checkSegment(int documentOffset,
                                     String currText,
                                     List<Segment> completions,
                                     Segment segment,
                                     boolean addLocals,
                                     boolean exactMatch)
    {
        if (segment instanceof PackageSegment)
        {
            for (Iterator<Segment> it = segment.getContainedSegments().iterator(); it.hasNext();)
            {
                Segment seg2 = it.next();
                checkSegment(documentOffset, currText, completions, seg2, addLocals, exactMatch);
            }
        }
        else
        {
            String segName = segment.getName().toUpperCase();
            // fix for 1460401 - hover text can't differentiate where only
            // suffixes differ
            if ((exactMatch ? segName.equals(currText) : segName.startsWith(currText))
                    && segment.getType() != SegmentType.Code)
            {
                completions.add(segment);
            }
        }
        if (addLocals)
        {
            addLocals(documentOffset, currText, completions, segment, exactMatch);
        }
    }

    private static void addLocals(int documentOffset,
                                  String currText,
                                  List<Segment> completions,
                                  Segment segment,
                                  boolean exactMatch)
    {
        if (segment.contains(documentOffset))
        {
            for (Iterator<Segment> it = segment.getFieldList().iterator(); it.hasNext();)
            {
                Segment local = (Segment) it.next();
                String lName = local.getName().toUpperCase();
                if (exactMatch ? lName.equals(currText.toUpperCase()) : lName.startsWith(currText))
                {
                    completions.add(local);
                }
            }
            for (Iterator<Segment> it = segment.getParameterList().iterator(); it.hasNext();)
            {
                Segment parameter = (Segment) it.next();
                String pName = parameter.getName().toUpperCase();
                if (exactMatch ? pName.equals(currText.toUpperCase()) : pName.startsWith(currText))
                {
                    completions.add(parameter);
                }
            }
        }
    }

    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset)
    {
        return null;
    }

    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return (new char[]{'.'});
    }

    public char[] getContextInformationAutoActivationCharacters()
    {
        return null; // (new char[]{'#','.'});
    }

    public IContextInformationValidator getContextInformationValidator()
    {
        return myValidator;
    }

    public String getErrorMessage()
    {
        return null;
    }

    /**
     * This method returns the fgProposals.
     * 
     * @return {@link #fgProposals}.
     */
    protected static String[] getFgProposals()
    {
        return fgProposals;
    }
}
