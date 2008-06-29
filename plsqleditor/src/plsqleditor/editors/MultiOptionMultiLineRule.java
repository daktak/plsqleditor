/**
 * 
 */
package plsqleditor.editors;

import java.util.Arrays;

import java.util.Comparator;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * @author tzines
 * 
 */
public class MultiOptionMultiLineRule implements IPredicateRule
{
    /**
     * Comparator that orders <code>char[]</code> in decreasing array lengths.
     * 
     * @since 3.1
     */
    private static class DecreasingCharArrayLengthComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            return ((char[]) o2).length - ((char[]) o1).length;
        }
    }

    /** Internal setting for the un-initialized column constraint */
    protected static final int UNDEFINED                = -1;

    /** The token to be returned on success */
    protected IToken           fToken;
    /** The pattern's start sequence */
    protected char[]           fStartSequence;
    /** The pattern's end sequences */
    private char[][]           myEndSequences;
    /** The pattern's column constrain */
    protected int              fColumn                  = UNDEFINED;
    /** The pattern's escape character */
    protected char             fEscapeCharacter;
    /**
     * Indicates whether the escape character continues a line
     * 
     * @since 3.0
     */
    protected boolean          fEscapeContinuesLine;
    /** Indicates whether end of line terminates the pattern */
    protected boolean          fBreaksOnEOL;
    /** Indicates whether end of file terminates the pattern */
    protected boolean          fBreaksOnEOF;

    /**
     * Line delimiter comparator which orders according to decreasing delimiter length.
     * 
     * @since 3.1
     */
    private Comparator         fLineDelimiterComparator = new DecreasingCharArrayLengthComparator();
    /**
     * Cached line delimiters.
     * 
     * @since 3.1
     */
    private char[][]           fLineDelimiters;
    /**
     * Cached sorted {@linkplain #fLineDelimiters}.
     * 
     * @since 3.1
     */
    private char[][]           fSortedLineDelimiters;

    /**
     * Creates a rule for the given starting and ending sequence. When these sequences are detected the rule will return
     * the specified token. Alternatively, the sequence can also be ended by the end of the line. Any character which
     * follows the given escapeCharacter will be ignored.
     * 
     * @param startSequence the pattern's start sequence
     * @param endSequence the pattern's end sequence, <code>null</code> is a legal value
     * @param token the token which will be returned on success
     * @param escapeCharacter any character following this one will be ignored
     * @param breaksOnEOL indicates whether the end of the line also terminates the pattern
     */
    public MultiOptionMultiLineRule(String startSequence,
                                    String[] endSequences,
                                    IToken token,
                                    char escapeCharacter,
                                    boolean breaksOnEOF)
    {
        Assert.isTrue(startSequence != null && startSequence.length() > 0);
        Assert.isNotNull(token);

        fStartSequence = startSequence.toCharArray();
        fToken = token;
        fEscapeCharacter = escapeCharacter;
        fBreaksOnEOL = false;
        myEndSequences = new char[endSequences.length][];
        for (int i = 0; i < endSequences.length; i++)
        {
            myEndSequences[i] = endSequences[i].toCharArray();
        }
    }

    /**
     * Sets a column constraint for this rule. If set, the rule's token will only be returned if the pattern is detected
     * starting at the specified column. If the column is smaller then 0, the column constraint is considered removed.
     * 
     * @param column the column in which the pattern starts
     */
    public void setColumnConstraint(int column)
    {
        if (column < 0) column = UNDEFINED;
        fColumn = column;
    }


    /**
     * Evaluates this rules without considering any column constraints.
     * 
     * @param scanner the character scanner to be used
     * @return the token resulting from this evaluation
     */
    protected IToken doEvaluate(ICharacterScanner scanner)
    {
        return doEvaluate(scanner, false);
    }

    /**
     * Evaluates this rules without considering any column constraints. Resumes detection, i.e. look sonly for the end
     * sequence required by this rule if the <code>resume</code> flag is set.
     * 
     * @param scanner the character scanner to be used
     * @param resume <code>true</code> if detection should be resumed, <code>false</code> otherwise
     * @return the token resulting from this evaluation
     * @since 2.0
     */
    protected IToken doEvaluate(ICharacterScanner scanner, boolean resume)
    {

        if (resume)
        {

            if (endSequenceDetected(scanner)) return fToken;

        }
        else
        {

            int c = scanner.read();
            if (c == fStartSequence[0])
            {
                if (sequenceDetected(scanner, fStartSequence, false))
                {
                    if (endSequenceDetected(scanner)) return fToken;
                }
            }
        }

        scanner.unread();
        return Token.UNDEFINED;
    }

    /*
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
        return evaluate(scanner, false);
    }

    /**
     * Returns whether the end sequence was detected. As the pattern can be considered ended by a line delimiter, the
     * result of this method is <code>true</code> if the rule breaks on the end of the line, or if the EOF character
     * is read.
     * 
     * @param scanner the character scanner to be used
     * @return <code>true</code> if the end sequence has been detected
     */
    protected boolean endSequenceDetected(ICharacterScanner scanner)
    {

        char[][] originalDelimiters = scanner.getLegalLineDelimiters();
        int count = originalDelimiters.length;
        if (fLineDelimiters == null || originalDelimiters.length != count)
        {
            fSortedLineDelimiters = new char[count][];
        }
        else
        {
            while (count > 0 && fLineDelimiters[count - 1] == originalDelimiters[count - 1])
                count--;
        }
        if (count != 0)
        {
            fLineDelimiters = originalDelimiters;
            System.arraycopy(fLineDelimiters, 0, fSortedLineDelimiters, 0, fLineDelimiters.length);
            Arrays.sort(fSortedLineDelimiters, fLineDelimiterComparator);
        }

        int c;
        while ((c = scanner.read()) != ICharacterScanner.EOF)
        {
            if (c == fEscapeCharacter)
            {
                // Skip escaped character(s)
                if (fEscapeContinuesLine)
                {
                    c = scanner.read();
                    for (int i = 0; i < fSortedLineDelimiters.length; i++)
                    {
                        if (c == fSortedLineDelimiters[i][0]
                                && sequenceDetected(scanner, fSortedLineDelimiters[i], true)) break;
                    }
                }
                else
                    scanner.read();

            }
            else if (charStartsAnEndSequence(c))
            {
                // Check if the specified end sequence has been found.
                for (int i = 0; i < myEndSequences.length; i++)
                {
                    if (c == myEndSequences[i][0])
                    {
                        if (sequenceDetected(scanner, myEndSequences[i], true)) return true;
                        //scanner.unread();
                    }
                }
            }
            else if (fBreaksOnEOL)
            {
                // Check for end of line since it can be used to terminate the pattern.
                for (int i = 0; i < fSortedLineDelimiters.length; i++)
                {
                    if (c == fSortedLineDelimiters[i][0] && sequenceDetected(scanner, fSortedLineDelimiters[i], true)) return true;
                }
            }
        }
        if (fBreaksOnEOF) return true;
        scanner.unread();
        return false;
    }



    /**
     * @param c
     * @return
     */
    private boolean charStartsAnEndSequence(int c)
    {
        for (int i = 0; i < myEndSequences.length; i++)
        {
            if (c == myEndSequences[i][0]) return true;
        }
        return false;
    }

    /**
     * Returns whether the next characters to be read by the character scanner are an exact match with the given
     * sequence. No escape characters are allowed within the sequence. If specified the sequence is considered to be
     * found when reading the EOF character.
     * 
     * @param scanner the character scanner to be used
     * @param sequence the sequence to be detected
     * @param eofAllowed indicated whether EOF terminates the pattern
     * @return <code>true</code> if the given sequence has been detected
     */
    protected boolean sequenceDetected(ICharacterScanner scanner, char[] sequence, boolean eofAllowed)
    {
        for (int i = 1; i < sequence.length; i++)
        {
            int c = scanner.read();
            if (c == ICharacterScanner.EOF && eofAllowed)
            {
                return true;
            }
            else if (c != sequence[i])
            {
                // Non-matching character detected, rewind the scanner back to the start.
                // Do not unread the first character.
                scanner.unread();
                for (int j = i - 1; j > 0; j--)
                    scanner.unread();
                return false;
            }
        }

        return true;
    }

    /*
     * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
     * @since 2.0
     */
    public IToken evaluate(ICharacterScanner scanner, boolean resume)
    {
        if (fColumn == UNDEFINED) return doEvaluate(scanner, resume);

        int c = scanner.read();
        scanner.unread();
        if (c == fStartSequence[0]) return (fColumn == scanner.getColumn()
                ? doEvaluate(scanner, resume)
                : Token.UNDEFINED);
        return Token.UNDEFINED;
    }

    /*
     * @see IPredicateRule#getSuccessToken()
     * @since 2.0
     */
    public IToken getSuccessToken()
    {
        return fToken;
    }
}
