package plsqleditor.rules;


import java.util.ArrayList;

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
public class NonKeywordsRules implements IRule
{

    private IToken    myToken;
    private ArrayList<String> myKeywords;

    public NonKeywordsRules(IToken token)
    {
        myKeywords = new ArrayList<String>();
        this.myToken = token;
    }

    public void addWord(String word)
    {
        myKeywords.add(word);
    }

    private String getString(char c)
    {
        for (int i = 0; i < myKeywords.size(); i++)
        {
            String word = (String) myKeywords.get(i);
            char cArray[] = word.toCharArray();
            if (cArray[0] == c)
            {
                return word;
            }
        }

        return null;
    }

    public IToken evaluate(ICharacterScanner scanner)
    {
        char character = (char) scanner.read();
        String word = getString(character);
        if (word != null)
        {
            int size = word.length();
            StringBuffer value = new StringBuffer();
            for (int i = 1; i <= size; i++)
            {
                value.append(character);
                character = (char) scanner.read();
            }

            scanner.unread();
            if (value.toString().toUpperCase().equals(word))
            {
                return myToken;
            }
        }
        scanner.unread();
        return Token.UNDEFINED;
    }

}
