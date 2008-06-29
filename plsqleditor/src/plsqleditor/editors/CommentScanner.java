/*
 * Created on 21/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

import plsqleditor.preferences.PreferenceConstants;


/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 21/02/2005
 */
public class CommentScanner extends RuleBasedScanner
{
    public CommentScanner(ColorManager provider)
    {
        IToken defaultToken = new Token(new TextAttribute(provider
                .getColor(IPlSqlColorConstants.DEFAULT)));
        IToken string = new Token(new ConfigurableTextAttribute(
                PreferenceConstants.P_STRING_COLOUR, PreferenceConstants.P_BACKGROUND_COLOUR, 0));
        List rules = new ArrayList();
        rules.add(new MultiLineRule("header details", "end header details", string));
        rules.add(new IPredicateRule()
        {
            IToken comment = new Token(new ConfigurableTextAttribute(
                                   PreferenceConstants.P_COMMENT_COLOUR,
                                   PreferenceConstants.P_BACKGROUND_COLOUR, 0));

            public IToken getSuccessToken()
            {
                return comment;
            }

            public IToken evaluate(ICharacterScanner scanner, boolean resume)
            {
                return comment;
            }

            public IToken evaluate(ICharacterScanner scanner)
            {
                return comment;
            }

        });
        IRule result[] = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
    }
}
