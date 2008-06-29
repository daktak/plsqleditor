/*
 * Created on 24/02/2005
 *
 * @version $Id$
 */
package plsqleditor.rules;

import java.util.HashMap;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 24/02/2005
 */
public class KeywordsRule implements IRule
{
    private HashMap myKeywords;

    public KeywordsRule(IToken defaultToken)
    {
        myKeywords = new HashMap();
    }

    public void addKeyword(IToken token, String word)
    {
        myKeywords.put(word.toUpperCase(), token);
    }

    public IToken evaluate(ICharacterScanner scanner)
    {
        char c = (char) scanner.read();
        if (Character.isLetter(c))
        {
            StringBuffer value = new StringBuffer();
            do
            {
                value.append(c);
                c = (char) scanner.read();
            }
            while (Character.isJavaIdentifierPart(c));
            scanner.unread();
            IToken retVal = (IToken) myKeywords.get(value.toString().toUpperCase());
            if (retVal != null)
            {
                return retVal;
            }
            else
            {
                return Token.UNDEFINED;
            }
        }
        else
        {
            scanner.unread();
            return Token.UNDEFINED;
        }
    }

}
