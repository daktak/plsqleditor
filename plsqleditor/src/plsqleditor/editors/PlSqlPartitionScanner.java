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


    public static final String PLSQL_PARTITION_TYPES[] = {IDocument.DEFAULT_CONTENT_TYPE, PLSQL_MULTILINE_COMMENT, PL_DOC};

    public PlSqlPartitionScanner()
    {
        IToken plsqlComment = new Token(PLSQL_MULTILINE_COMMENT);
        IToken plDoc = new Token(PL_DOC);
        IToken sqlString = new Token(SQL_STRING);

        List<IRule> rules = new ArrayList<IRule>();
        rules.add(new MultiLineRule("/**", "*/", plDoc, '\0', true));
        rules.add(new MultiLineRule("/*", "*/", plsqlComment, '\0', true));
        rules.add(new EndOfLineRule("--", plsqlComment));
        rules.add(new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\'));
        rules.add(new SingleLineRule("'", "'", sqlString, '\\'));
        rules.add(new WordPredicateRule(plsqlComment));

        IPredicateRule result[] = new IPredicateRule[rules.size()];
        rules.toArray(result);

        setPredicateRules(result);
    }
}
