/*
 * Created on 24/02/2005
 *
 * @version $Id$
 */
package plsqleditor.rules;

import java.util.HashMap;
import org.eclipse.jface.text.rules.*;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 24/02/2005
 */
public class KeywordsRule implements IRule
{
    private HashMap<String, IToken> myKeywords;
    private IToken                  myToken;

    public KeywordsRule(IToken defaultToken)
    {
        myToken = defaultToken;
        myKeywords = new HashMap<String, IToken>();
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
            while (Character.isLetterOrDigit(c) || c == '_');
            scanner.unread();
            IToken retVal = myKeywords.get(value.toString().toUpperCase());
            if (retVal != null)
            {
                return retVal;
            }
            else
            {
                return myToken;
            }
        }
        else
        {
            scanner.unread();
            return Token.UNDEFINED;
        }
    }

}
