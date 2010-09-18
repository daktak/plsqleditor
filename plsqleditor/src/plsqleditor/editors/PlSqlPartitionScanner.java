package plsqleditor.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import plsqleditor.PlsqleditorPlugin;

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
	public static final String PL_DOC = "__plsql_pldoc";
	public static final String SQL_STRING = "sql_string";

	public static final String DEFAULT = IDocument.DEFAULT_CONTENT_TYPE;
	public static final String VARIABLE = "Variable";
	public static final String LITERAL1 = SQL_STRING;
	public static final String LITERAL2 = "Literal2";
	public static final String COMMENT = PLSQL_MULTILINE_COMMENT;
	public static final String KEYWORD1 = "sql_keyword";
	public static final String KEYWORD2 = "keyword2";
	public static final String NUMBER = "sql_number";
	public static final String OPERATOR = "sql_operators";

	public static final String SQL_DATATYPES = "sql_datatypes";

	// added SQL_STRING to list for for bug 1295164
	public static final String PLSQL_PARTITION_TYPES[] = { DEFAULT,
			PLSQL_MULTILINE_COMMENT, PL_DOC, SQL_STRING };

	public PlSqlPartitionScanner()
	{
		IToken plsqlComment = new Token(PLSQL_MULTILINE_COMMENT);
		IToken plDoc = new Token(PL_DOC);
		IToken sqlString = new Token(SQL_STRING);

		List<IRule> rules = new ArrayList<IRule>();
		rules.add(new MultiLineRule("/**", "*/", plDoc, '\0', true));

		// enhancement for [ 1428741 ] Format header details as code
		rules.add(new MultiOptionMultiLineRule("/*", new String[] {
				"header details", "*/" }, plsqlComment, '\0', true));
		// rules.add(new MultiLineRule("/*", "*/", plsqlComment, '\0', true));
		rules.add(new MultiLineRule("end header details", "*/", plsqlComment,
				'\0', true));
		rules.add(new EndOfLineRule("--", plsqlComment));
		// enhancement for [ 1894058 ] text after "PROMPT" keyword should be
		// treated as comments
		rules.add(new EndOfLineRule("PROMPT", plsqlComment));
		rules.add(new EndOfLineRule("prompt", plsqlComment));
		// rules.add(new EndOfLineRule("//", plsqlComment));
		rules.add(new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\'));
		// added for for bug 1295164 - can't use ' as an escape char
		rules.add(new MultiLineRule("'", "'", sqlString, '\\'));
		rules.add(new WordPredicateRule(plsqlComment));

		IPredicateRule result[] = new IPredicateRule[rules.size()];
		rules.toArray(result);

		setPredicateRules(result);
	}

	/**
	 * Helper method which acts as org.eclipse.jface.text.IDocument#getPartition
	 * for the document partitioning managed by PerlPartitioner.
	 */
	public static ITypedRegion getPlsqlPartition(IDocument doc, int offset)
			throws BadLocationException
	{
		if (!(doc instanceof IDocumentExtension3))
			return doc.getPartition(offset); // should never occur

		try
		{
			return ((IDocumentExtension3) doc).getPartition(
					PlsqleditorPlugin.PLSQL_PARTITIONING, offset, false);
		}
		catch (BadPartitioningException e)
		{
			return doc.getPartition(offset); // should never occur
		}
	}

	public static IDocumentPartitioner getPartitioner(IDocument doc)
	{
		if (!(doc instanceof IDocumentExtension3)) return null;
		else return ((IDocumentExtension3) doc)
				.getDocumentPartitioner(PlsqleditorPlugin.PLSQL_PARTITIONING);
	}

	public static IDocumentPartitioner createPartitioner(IDocument doc)
	{
		return new FastPartitioner(new PlSqlPartitionScanner(),
				PlSqlPartitionScanner.PLSQL_PARTITION_TYPES);
	}

}
