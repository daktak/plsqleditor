/**
 * 
 */
package plsqleditor.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Toby Zines
 * 
 */
public class HeaderDetailsIdentifierRule implements IPredicateRule
{
    char[] startSequence = "header details".toCharArray();
    char[] endSequence   = "end header details".toCharArray();

    IToken myCommentToken;
    IToken myHeaderDetailsToken;

    /**
     * 
     */
    public HeaderDetailsIdentifierRule(IToken commentsToken, IToken headerToken)
    {
        myCommentToken = commentsToken;
        myHeaderDetailsToken = headerToken;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
     */
    public IToken getSuccessToken()
    {
        // TODO Auto-generated method stub
        return myCommentToken;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner,
     *      boolean)
     */
    public IToken evaluate(ICharacterScanner scanner, boolean resume)
    {
        if (resume)
        {
            if (sequenceDetected(scanner, startSequence, resume))
            {
                return myHeaderDetailsToken;

            }
        }
        else
        {
            int c = scanner.read();
            if (c == startSequence[0])
            {
                if (sequenceDetected(scanner, startSequence, false))
                {
                    //if (endSequenceDetected(scanner)) return fToken;
                }
            }
        }

        scanner.unread();
        return Token.UNDEFINED;
    }

    /**
     * Returns whether the next characters to be read by the character scanner
     * are an exact match with the given sequence. No escape characters are
     * allowed within the sequence. If specified the sequence is considered to
     * be found when reading the EOF character.
     * 
     * @param scanner the character scanner to be used
     * @param sequence the sequence to be detected
     * @param eofAllowed indicated whether EOF terminates the pattern
     * @return <code>true</code> if the given sequence has been detected
     */
    protected boolean sequenceDetected(ICharacterScanner scanner,
                                       char[] sequence,
                                       boolean eofAllowed)
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
                // Non-matching character detected, rewind the scanner back to
                // the start.
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
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
        return evaluate(scanner, false);
    }
}
