package plsqleditor.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.*;

public class PlSqlPartitionScanner extends RuleBasedPartitionScanner
{
    static class EmptyCommentDetector implements IWordDetector
    {
        public boolean isWordStart(char c)
        {
            return c == '/';
        }

        public boolean isWordPart(char c)
        {
            return c == '*' || c == '/';
        }
    }

    static class WordPredicateRule extends WordRule implements IPredicateRule
    {
        private IToken fSuccessToken;

        public WordPredicateRule(IToken successToken)
        {
            super(new EmptyCommentDetector());
            fSuccessToken = successToken;
            addWord("/**/", fSuccessToken);
        }

        public IToken evaluate(ICharacterScanner scanner, boolean resume)
        {
            return super.evaluate(scanner);
        }

        public IToken getSuccessToken()
        {
            return fSuccessToken;
        }
    }

    public final static String PLSQL_MULTILINE_COMMENT = "__plsql_multiline_comment";
    public static final String PL_DOC                  = "__plsql_pldoc";

    public static final String OPERATORS = "sql_operators";
    public static final String SQL_DATATYPES = "sql_datatypes";
    public static final String SQL_KEYWORD = "sql_keyword";
    public static final String SQL_NUMBER = "sql_number";
    public static final String SQL_STRING = "sql_string";


    // added SQL_STRING to list for for bug 1295164
    public static final String PLSQL_PARTITION_TYPES[] = {IDocument.DEFAULT_CONTENT_TYPE, PLSQL_MULTILINE_COMMENT, PL_DOC, SQL_STRING};

    public PlSqlPartitionScanner()
    {
        IToken plsqlComment = new Token(PLSQL_MULTILINE_COMMENT);
        IToken plDoc = new Token(PL_DOC);
        IToken sqlString = new Token(SQL_STRING);

        List rules = new ArrayList();
        rules.add(new MultiLineRule("/**", "*/", plDoc, '\0', true));
        
//      enhancement for [ 1428741 ] Format header details as code
        rules.add(new MultiOptionMultiLineRule("/*", new String [] {"header details", "*/"}, plsqlComment, '\0', true));
        //rules.add(new MultiLineRule("/*", "*/", plsqlComment, '\0', true));
        rules.add(new MultiLineRule("end header details", "*/", plsqlComment, '\0', true));
        rules.add(new EndOfLineRule("--", plsqlComment));
        // enhancement for [ 1894058 ] text after "PROMPT" keyword should be treated as comments
        rules.add(new EndOfLineRule("PROMPT", plsqlComment));
        rules.add(new EndOfLineRule("prompt", plsqlComment));
        //rules.add(new EndOfLineRule("//", plsqlComment));
        rules.add(new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\'));
        // added for for bug 1295164 - can't use ' as an escape char
        rules.add(new MultiLineRule("'", "'", sqlString, '\\'));
        rules.add(new WordPredicateRule(plsqlComment));

        IPredicateRule result[] = new IPredicateRule[rules.size()];
        rules.toArray(result);

        setPredicateRules(result);
    }
}
