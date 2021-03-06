/*
 * Created on 22/02/2005
 * 
 * @version $Id: PlSqlAutoEditStrategy.java,v 1.4.2.3 2005/04/07 06:34:27 tobyz
 *          Exp $
 */
package plsqleditor.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import plsqleditor.PlsqleditorPlugin;
import plsqleditor.preferences.PreferenceConstants;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 *         Created on 22/02/2005
 */
public class PlSqlAutoEditStrategy extends DefaultIndentLineAutoEditStrategy
{
	private List<AutoIndentMap> myAutoIndentMappings;
	private List<AutoIndentMap> mySecondaryAutoIndentMappings;
	private String[] myUpperCasings = PlSqlCompletionProcessor.getFgProposals();

	private SortedSet<String> myDotEnabledWords = new TreeSet<String>();

	/**
	 * This field represents the last word that was uppercased (prior to being
	 * uppercased). This is in case we need to un-uppercase it because it turns
	 * out to be part of another word.
	 */
	private String myLastUppercasedWord;

	/**
	 * This field represents the last offset of the last uppercased word, so
	 * that if the index is different, we won't uppercase/lowercase the wrong
	 * word.
	 */
	private int myLastUppercasedOffset;
	private ArrayList<Character> myUpperCaseDelimiters;

	static class AutoIndentMap
	{
		private String myOpenString;
		private String myCloseString;
		private String myTruncatedCloseString;
		private String myCloseStringLastChar;

		AutoIndentMap(String open, String close)
		{
			myOpenString = open;
			myCloseString = close;
			myTruncatedCloseString = close.substring(0, close.length() - 1);
			myCloseStringLastChar = String
					.valueOf(close.charAt(close.length() - 1));
		}

		/**
		 * This method returns the closeString.
		 * 
		 * @return {@link #myCloseString}.
		 */
		protected String getCloseString()
		{
			return myCloseString;
		}

		/**
		 * This method returns the openString.
		 * 
		 * @return {@link #myOpenString}.
		 */
		protected String getOpenString()
		{
			return myOpenString;
		}

		/**
		 * This method returns the truncatedCloseString.
		 * 
		 * @return {@link #myTruncatedCloseString}.
		 */
		protected String getTruncatedCloseString()
		{
			return myTruncatedCloseString;
		}

		/**
		 * This method returns the closeStringLastChar.
		 * 
		 * @return {@link #myCloseStringLastChar}.
		 */
		protected String getCloseStringLastChar()
		{
			return myCloseStringLastChar;
		}
	}

	public PlSqlAutoEditStrategy()
	{
		for (int i = 0; i < PlSqlCodeScanner.DOT_ENABLED_KEYWORDS.length; i++)
		{
			myDotEnabledWords.add(PlSqlCodeScanner.DOT_ENABLED_KEYWORDS[i]);
		}
		myAutoIndentMappings = new ArrayList<AutoIndentMap>();
		myAutoIndentMappings.add(new AutoIndentMap("IF", "END IF"));
		myAutoIndentMappings.add(new AutoIndentMap("IF", "ELSIF"));
		myAutoIndentMappings.add(new AutoIndentMap("IF", "ELSE"));
		myAutoIndentMappings.add(new AutoIndentMap("FOR", "END LOOP"));
		myAutoIndentMappings.add(new AutoIndentMap("LOOP", "END LOOP"));
		myAutoIndentMappings.add(new AutoIndentMap("BEGIN", "END"));
		myAutoIndentMappings.add(new AutoIndentMap("BEGIN", "EXCEPTION"));
		myAutoIndentMappings.add(new AutoIndentMap("CASE", "WHEN"));
		myAutoIndentMappings.add(new AutoIndentMap("CASE", "ELSE"));
		myAutoIndentMappings.add(new AutoIndentMap("CASE", "END CASE"));
		mySecondaryAutoIndentMappings = new ArrayList<AutoIndentMap>();
		// mySecondaryAutoIndentMappings.add(new AutoIndentMap("WHEN", "END
		// CASE"));
		mySecondaryAutoIndentMappings
				.add(new AutoIndentMap("EXCEPTION", "END"));
		mySecondaryAutoIndentMappings.add(new AutoIndentMap("ELSIF", "ELSE"));
		mySecondaryAutoIndentMappings.add(new AutoIndentMap("ELSIF", "END IF"));
		mySecondaryAutoIndentMappings.add(new AutoIndentMap("ELSE", "END IF"));
		// mySecondaryAutoIndentMappings.add(new AutoIndentMap("WHEN", "END
		// CASE"));
		myUpperCaseDelimiters = new ArrayList<Character>();
		for (int i = 0; i < PlSqlCompletionProcessor.autoCompleteDelimiters.length; i++)
		{
			char c = PlSqlCompletionProcessor.autoCompleteDelimiters[i];
			myUpperCaseDelimiters.add(Character.valueOf(c));
		}
	}

	/**
	 * @param c
	 * @param lineOfText
	 * @return The number of occurrences of the charactet <code>c</code> in the
	 *         supplied <code>lineOfText</code>.
	 */
	private int countChars(char c, String lineOfText)
	{
		char[] characters = lineOfText.toCharArray();
		int count = 0;
		for (int i = 0; i < characters.length; i++)
		{
			if (characters[i] == c)
			{
				count++;
			}
		}
		return count;
	}

	public void customizeDocumentCommand(IDocument d, DocumentCommand c)
	{
		// mlavergn - get the user preferences
		PlsqleditorPlugin plugin = PlsqleditorPlugin.getDefault();
		IPreferenceStore prefs = plugin.getPreferenceStore();
		boolean doLowerCase = prefs
				.getBoolean(PreferenceConstants.P_LOWERCASE_KEYWORDS);

		int numQuotes = 0;
		try
		{
			int thisline = d.getLineOfOffset(c.offset);
			int linestart = d.getLineOffset(thisline);
			int linelength = c.offset - linestart;
			String lineOfText = d.get(linestart, linelength);
			numQuotes = countChars('\'', lineOfText);
		}
		catch (BadLocationException e1)
		{
			e1.printStackTrace();
		}
		if (c.length != 0)
		{
			myLastUppercasedWord = null;
		}
		if (c.length == 0 && c.text != null && endsWithDelimiter(d, c.text))
		{
			smartIndentAfterNewLine(d, c);
			myLastUppercasedWord = null;
		}
		else if (c.length == 0 && c.text != null && c.text.equals("\t"))
		{
			StringBuffer sb = new StringBuffer();
			appendTabs(sb);
			c.text = sb.toString();
			myLastUppercasedWord = null;
			return;
		}
		else if ("}".equals(c.text))
		{
			smartInsertAfterBracket(d, c);
			myLastUppercasedWord = null;
		}
		else
		{
			for (Iterator<AutoIndentMap> it = myAutoIndentMappings.iterator(); it
					.hasNext();)
			{
				AutoIndentMap map = (AutoIndentMap) it.next();
				if (map.getCloseStringLastChar().equalsIgnoreCase(c.text))
				{
					int length = map.getTruncatedCloseString().length();
					if (c.offset >= length)
					{
						try
						{
							String endStr = d.get(c.offset - length, length);
							if (endStr.equalsIgnoreCase(map
									.getTruncatedCloseString()))
							{
								smartInsertAfterEnd(map.getOpenString(),
										map.getCloseString(), d, c);
								break;
							}
						}
						catch (BadLocationException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		if (c.length != 0)
		{
			// already processed indenting, or cut and paste job
			// do quick upper case if it is not a cut and paste job
			for (int i = 0; i < myUpperCasings.length; i++)
			{
				String toUpperCase = myUpperCasings[i];
				if (c.text.length() >= toUpperCase.length())
				{
					int index = c.text.length() - toUpperCase.length();
					if ((index == 0
					// the line below indicates that the character before
					// the string in question is a delimiter
					|| myUpperCaseDelimiters.contains(Character.valueOf(c.text
							.substring(index - 1, index).charAt(0))))
							&& (numQuotes % 2 == 0))
					{
						String toCheck = c.text.substring(index);
						if (toCheck.toUpperCase().equals(toUpperCase))
						{
							c.text = c.text.replaceAll(toCheck, toUpperCase);
							myLastUppercasedWord = toCheck;
							myLastUppercasedOffset = c.offset;
						}
					}
				}
			}
		}
		if (c.length == 0 && c.text != null)
		{
			for (int i = 0; i < myUpperCasings.length; i++)
			{
				String toUpperCase = myUpperCasings[i];
				try
				{
					int length = toUpperCase.length() - 1;
					String lastChar = toUpperCase.substring(toUpperCase
							.length() - 1);
					if (c.text.equalsIgnoreCase(lastChar) && c.offset >= length
							&& (numQuotes % 2 == 0))
					{
						String endStr = d.get(c.offset - length, length);
						String preChar = null;
						if (c.offset > length)
						{
							preChar = d.get(c.offset - (length + 1), 1);
						}
						else if (c.offset == length)
						{
							preChar = d.get(0, 1);
						}
						char firstPreChar = preChar.charAt(0);
						if (endStr.equalsIgnoreCase(toUpperCase.substring(0,
								toUpperCase.length() - 1))
								&& (c.offset == length || (!Character
										.isJavaIdentifierPart(firstPreChar) && !isDotEnabled(
										toUpperCase, firstPreChar))))
						{
							int p = c.offset != d.getLength() ? c.offset
									: c.offset - 1;
							int line = d.getLineOfOffset(p);
							int start = d.getLineOffset(line);
							int whiteend = findEndOfWhiteSpace(d, start,
									c.offset);
							StringBuffer replaceText = new StringBuffer(
									getIndentOfLine(d, line));
							replaceText.append(d.get(whiteend, c.offset
									- (whiteend + (toUpperCase.length() - 1))));
							// mlavergn - make upper case optional
							if (doLowerCase) replaceText.append(toUpperCase
									.toLowerCase());
							else replaceText.append(toUpperCase);
							c.length = c.offset - start;
							myLastUppercasedOffset = c.offset;
							myLastUppercasedWord = preChar + endStr + c.text;
							c.offset = start;
							c.text = replaceText.toString();
							break;
						}
					}
				}
				catch (BadLocationException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (c.length == 0 && c.text != null)
			{
				if (c.text.length() == 1 && myLastUppercasedWord != null
						&& (myLastUppercasedOffset == c.offset - 1))
				{
					char firstChar = c.text.charAt(0);
					// TODO fix this - i don't think it is the right logic
					if (Character.isJavaIdentifierPart(firstChar)
							|| isDotEnabled(myLastUppercasedWord, firstChar))
					{
						c.text = myLastUppercasedWord + c.text;
						c.length = myLastUppercasedWord.length();
						c.offset = c.offset - myLastUppercasedWord.length();
					}
				}
				myLastUppercasedWord = null;
			}
		}
	}

	private boolean isDotEnabled(String uppercasedWord, char firstChar)
	{
		return (firstChar == '.' && !myDotEnabledWords.contains(uppercasedWord
				.toUpperCase()));
	}

	protected void smartInsertAfterEnd(String openStr, String closeStr,
			IDocument document, DocumentCommand command)
	{
		if (command.offset == -1 || document.getLength() == 0)
		{
			return;
		}
		try
		{
			int p = command.offset != document.getLength() ? command.offset
					: command.offset - 1;
			int line = document.getLineOfOffset(p);
			int start = document.getLineOffset(line);
			int whiteend = findEndOfWhiteSpace(document, start, command.offset);
			if (whiteend == command.offset - (closeStr.length() - 1))
			{
				int indLine = findMatchingOpenStmt(openStr, closeStr, document,
						line, command.offset, 1);
				if (indLine != -1 && indLine != line)
				{
					StringBuffer replaceText = new StringBuffer(
							getIndentOfLine(document, indLine));
					replaceText.append(document.get(whiteend, command.offset
							- whiteend));
					replaceText.append(command.text);
					command.length = command.offset - start;
					command.offset = start;
					command.text = replaceText.toString();
				}
			}
		}
		catch (BadLocationException _ex)
		{
			System.out.println(PlSqlEditorMessages
					.getString("AutoIndent.error.bad_location_2"));
		}
	}

	protected int findMatchingOpenStmt(String openStr, String closeStr,
			IDocument document, int line, int end, int closingEndIfIncrease)
			throws BadLocationException
	{
		int start = document.getLineOffset(line);
		int ifcount = getStartEndCount(openStr, closeStr, document, start, end,
				false) - closingEndIfIncrease;
		for (; ifcount < 0; ifcount += getStartEndCount(openStr, closeStr,
				document, start, end, false))
		{
			if (--line < 0)
			{
				return -1;
			}
			start = document.getLineOffset(line);
			end = (start + document.getLineLength(line)) - 1;
		}
		return line;
	}

	/**
	 * This method gets the start (open) and end (close) count of an open/close
	 * pair within a document between a <code>start</code> and <code>end</code>
	 * point.
	 * 
	 * @param openStr
	 *            The starting string.
	 * 
	 * @param closeStr
	 *            The ending string.
	 * 
	 * @param document
	 *            The document containing these strings.
	 * 
	 * @param start
	 *            The starting offset in the document.
	 * 
	 * @param end
	 *            The endin offset in the document.
	 * 
	 * @param ignoreEnds
	 *            Ignore any discovered endings.
	 * 
	 * @return The number of start/end pairs.
	 * 
	 * @throws BadLocationException
	 */
	private int getStartEndCount(String openStr, String closeStr,
			IDocument document, int start, int end, boolean ignoreEnds)
			throws BadLocationException
	{
		int begin = start;
		int ifcount = 0;
		while (begin < end)
		{
			char curr = document.getChar(begin);
			begin++;
			switch (curr)
			{
			default:
				break;
			case '/':
			{
				if (begin >= end) break;
				char next = document.getChar(begin);
				if (next == '*')
				{
					begin = getCommentEnd(document, begin + 1, end);
					break;
				}
				if (next == '/') begin = end;
				break;
			}
			case '-':
			{
				if (begin >= end) break;
				char next = document.getChar(begin);
				if (next == '-')
				{
					begin = end;
				}
				break;
			}
			case '*':
			{
				if (begin >= end) break;
				char next = document.getChar(begin);
				if (next == '/')
				{
					ifcount = 0;
					begin++;
				}
				break;
			}
			case 'i':
			case 'I': // equivalent to '{'
			{
				char atZero = openStr.charAt(0);
				if (atZero == 'I' || atZero == 'i')
				{
					if (begin >= end) break;
					char next = document.getChar(begin);
					if (next == 'F' || next == 'f')
					{
						ifcount++;
						ignoreEnds = false;
						begin++;
					}
				}
				break;
			}
			case 'f':
			case 'F': // equivalent to '{'
			{
				char atZero = openStr.charAt(0);
				if (atZero == 'F' || atZero == 'f')
				{
					if (begin >= end) break;
					String startComparator = openStr.substring(1);
					int length = startComparator.length();
					if (begin + length > end)
					{
						continue;
					}
					String nextStr = document.get(begin, length);
					if (nextStrMatches(startComparator, nextStr, document,
							begin, end))
					{
						ifcount++;
						ignoreEnds = false;
						begin++;
					}
				}
				break;
			}
			case 'c':
			case 'C': // equivalent to '{'
			{
				char atZero = openStr.charAt(0);
				if (atZero == 'C' || atZero == 'c')
				{
					if (begin >= end) break;
					String startComparator = openStr.substring(1);
					int length = startComparator.length();
					if (begin + length > end)
					{
						continue;
					}
					String nextStr = document.get(begin, length);
					if (nextStrMatches(startComparator, nextStr, document,
							begin, end))
					{
						ifcount++;
						ignoreEnds = false;
						begin++;
					}
				}
				break;
			}
			case 'l':
			case 'L': // equivalent to '{'
			{
				char atZero = openStr.charAt(0);
				if (atZero == 'L' || atZero == 'l')
				{
					if (begin >= end) break;
					String startComparator = openStr.substring(1);
					int length = startComparator.length();
					if (begin + length > end)
					{
						continue;
					}
					String nextStr = document.get(begin, length);
					if (nextStrMatches(startComparator, nextStr, document,
							begin, end))
					{
						ifcount++;
						ignoreEnds = false;
						begin++;
					}
				}
				break;
			}
			case 'b':
			case 'B': // equivalent to '{'
			{
				char atZero = openStr.charAt(0);
				if (atZero == 'B' || atZero == 'b')
				{
					if (begin >= end) break;
					String startComparator = openStr.substring(1);
					int length = startComparator.length();
					if (begin + length > end)
					{
						continue;
					}
					String nextStr = document.get(begin, length);
					if (nextStrMatches(startComparator, nextStr, document,
							begin, end))
					{
						ifcount++;
						ignoreEnds = false;
						begin++;
					}
				}
				break;
			}
			case 'e':
			case 'E': // equivalent to '}'
			{
				if (begin >= end) break;
				String endComparator = closeStr.substring(1);
				int length = endComparator.length();
				if (begin + length > end)
				{
					continue;
				}
				String nextStr = document.get(begin, length);
				if (nextStrMatches(endComparator, nextStr, document, begin, end))
				{
					if (!ignoreEnds)
					{
						ifcount--;
					}
					begin += length;
				}
				break;
			}
			case '"':
			case '\'':
			{
				begin = getStringEnd(document, begin, end, curr);
				break;
			}
			}
		}
		return ifcount;
	}

	/**
	 * @param startComparator
	 * @param nextStr
	 * @param end
	 * @param begin
	 * @return
	 * @throws BadLocationException
	 */
	private boolean nextStrMatches(String startComparator, String nextStr,
			IDocument document, int begin, int end) throws BadLocationException
	{
		boolean matches = nextStr.equalsIgnoreCase(startComparator);
		if (!matches)
		{
			return false;
		}
		int nextPoint = begin + startComparator.length();
		if (nextPoint <= end)
		{
			char nextChar = document.getChar(nextPoint);
			matches &= !Character.isJavaIdentifierPart(nextChar);
		}
		return matches;
	}

	/**
	 * This method indicates whether the supplied <code>txt</code> ends with a
	 * valid line delimiter for the supplied document <code>d</code>.
	 * 
	 * @param d
	 *            The document from which to obtain valid line delimiters.
	 * 
	 * @param txt
	 *            The myOutputText whose end is being checked for end of line
	 *            delimiters.
	 * 
	 * @return <code>true</code> if <code>txt</code> ends with a line delimiter.
	 */
	private boolean endsWithDelimiter(IDocument d, String txt)
	{
		String delimiters[] = d.getLegalLineDelimiters();
		if (delimiters != null)
		{
			return TextUtilities.endsWith(delimiters, txt) > -1;
		}
		else
		{
			return false;
		}
	}

	protected int findMatchingOpenBracket(IDocument document, int line,
			int end, int closingBracketIncrease) throws BadLocationException
	{
		int start = document.getLineOffset(line);
		int brackcount = getBracketCount(document, start, end, false)
				- closingBracketIncrease;
		for (; brackcount < 0; brackcount += getBracketCount(document, start,
				end, false))
		{
			if (--line < 0)
			{
				return -1;
			}
			start = document.getLineOffset(line);
			end = (start + document.getLineLength(line)) - 1;
		}
		return line;
	}

	private int getBracketCount(IDocument document, int start, int end,
			boolean ignoreCloseBrackets) throws BadLocationException
	{
		int begin = start;
		int bracketcount = 0;
		while (begin < end)
		{
			char curr = document.getChar(begin);
			begin++;
			switch (curr)
			{
			default:
				break;
			case '/':
			{
				if (begin >= end) break;
				char next = document.getChar(begin);
				if (next == '*')
				{
					begin = getCommentEnd(document, begin + 1, end);
					break;
				}
				if (next == '/')
				{
					begin = end;
				}
				break;
			}
			case '*':
			{
				if (begin >= end) break;
				char next = document.getChar(begin);
				if (next == '/')
				{
					bracketcount = 0;
					begin++;
				}
				break;
			}
			case '{':
			{
				bracketcount++;
				ignoreCloseBrackets = false;
				break;
			}
			case '}':
			{
				if (!ignoreCloseBrackets)
				{
					bracketcount--;
				}
				break;
			}
			case '"':
			case '\'':
			{
				begin = getStringEnd(document, begin, end, curr);
				break;
			}
			}
		}
		return bracketcount;
	}

	private int getCommentEnd(IDocument document, int position, int end)
			throws BadLocationException
	{
		for (int currentPosition = position; currentPosition < end;)
		{
			char curr = document.getChar(currentPosition);
			currentPosition++;
			if (curr == '*' && currentPosition < end
					&& document.getChar(currentPosition) == '/')
			{
				return currentPosition + 1;
			}
		}
		return end;
	}

	protected String getIndentOfLine(IDocument document, int line)
			throws BadLocationException
	{
		if (line > -1)
		{
			int start = document.getLineOffset(line);
			int end = (start + document.getLineLength(line)) - 1;
			int whiteend = findEndOfWhiteSpace(document, start, end);
			return document.get(start, whiteend - start);
		}
		else
		{
			return "";
		}
	}

	private int getStringEnd(IDocument document, int position, int end,
			char character) throws BadLocationException
	{
		for (int currentPosition = position; currentPosition < end;)
		{
			char currentCharacter = document.getChar(currentPosition);
			currentPosition++;
			if (currentCharacter == '\\')
			{
				currentPosition++;
			}
			else if (currentCharacter == character)
			{
				return currentPosition;
			}
		}
		return end;
	}

	protected void smartIndentAfterNewLine(IDocument document,
			DocumentCommand command)
	{
		int docLength = document.getLength();
		if (command.offset == -1 || docLength == 0) return;
		try
		{
			int p = command.offset != docLength ? command.offset
					: command.offset - 1;
			int line = document.getLineOfOffset(p);
			StringBuffer buf = new StringBuffer(command.text);
			int prevP = document.getLineOffset(line);
			String prevLine = document.get(prevP, document.getLineLength(line));
			if (command.offset < docLength
					&& document.getChar(command.offset) == '}')
			{
				int indLine = findMatchingOpenBracket(document, line,
						command.offset, 0);
				if (indLine == -1) indLine = line;
				buf.append(getIndentOfLine(document, indLine));
			}
			// take care of an open bracket (with no close) on the previous
			// line.
			else if (command.offset < docLength
					&& (prevLine.indexOf("(") != -1)
					&& (prevLine.indexOf(")") == -1))
			{
				int index = prevLine.indexOf('(');
				for (int i = 0; i < index; i++)
				{
					buf.append(' ');
				}
			}
			else
			{
				int start = document.getLineOffset(line);
				int whiteend = findEndOfWhiteSpace(document, start,
						command.offset);
				buf.append(document.get(start, whiteend - start));
				if (getBracketCount(document, start, command.offset, true) > 0)
				{
					appendTabs(buf);
				}
				else
				{
					boolean isAppended = false;
					for (Iterator<AutoIndentMap> it = myAutoIndentMappings
							.iterator(); it.hasNext();)
					{
						AutoIndentMap map = (AutoIndentMap) it.next();
						if (getStartEndCount(map.getOpenString(),
								map.getCloseString(), document, start,
								command.offset, true) > 0)
						{
							appendTabs(buf);
							isAppended = true;
							break;
						}
					}
					if (!isAppended)
					{
						for (Iterator<AutoIndentMap> it = mySecondaryAutoIndentMappings
								.iterator(); it.hasNext();)
						{
							AutoIndentMap map = (AutoIndentMap) it.next();
							if (getStartEndCount(map.getOpenString(),
									map.getCloseString(), document, start,
									command.offset, true) > 0)
							{
								appendTabs(buf);
								break;
							}
						}
					}
				}
			}
			command.text = buf.toString();
		}
		catch (BadLocationException _ex)
		{
			System.out.println(PlSqlEditorMessages
					.getString("AutoIndent.error.bad_location_1"));
		}
	}

	/**
	 * This method
	 * 
	 * @param buf
	 */
	private void appendTabs(StringBuffer buf)
	{
		int val = PlsqleditorPlugin.getDefault().getPreferenceStore()
				.getInt(PreferenceConstants.P_EDITOR_TAB_WIDTH);
		for (int i = 0; i < val; i++)
		{
			buf.append(' ');
		}
	}

	protected void smartInsertAfterBracket(IDocument document,
			DocumentCommand command)
	{
		if (command.offset == -1 || document.getLength() == 0) return;
		try
		{
			int p = command.offset != document.getLength() ? command.offset
					: command.offset - 1;
			int line = document.getLineOfOffset(p);
			int start = document.getLineOffset(line);
			int whiteend = findEndOfWhiteSpace(document, start, command.offset);
			if (whiteend == command.offset)
			{
				int indLine = findMatchingOpenBracket(document, line,
						command.offset, 1);
				if (indLine != -1 && indLine != line)
				{
					StringBuffer replaceText = new StringBuffer(
							getIndentOfLine(document, indLine));
					replaceText.append(document.get(whiteend, command.offset
							- whiteend));
					replaceText.append(command.text);
					command.length = command.offset - start;
					command.offset = start;
					command.text = replaceText.toString();
				}
			}
		}
		catch (BadLocationException _ex)
		{
			System.out.println(PlSqlEditorMessages
					.getString("AutoIndent.error.bad_location_2"));
		}
	}
}
