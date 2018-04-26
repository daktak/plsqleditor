package plsqleditor.doc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;
import org.eclipse.swt.SWT;

import plsqleditor.editors.ColorManager;
import plsqleditor.editors.IPlSqlColorConstants;
import plsqleditor.rules.PlSqlWhitespaceDetector;

/**
 * @author Toby Zines
 * 
 */
public class PlSqlDocScanner extends RuleBasedScanner
{

    /**
     * A key word detector.
     */
    static class PlSqlWordDetector implements IWordDetector
    {

        /*
         * (non-Javadoc) Method declared on IWordDetector
         */
        public boolean isWordStart(char c)
        {
            return (c == '@');
        }

        /*
         * (non-Javadoc) Method declared on IWordDetector
         */
        public boolean isWordPart(char c)
        {
            return Character.isLetter(c);
        }
    }

    protected static String[] fgKeywords = {
            "@author", "@deprecated", "@exception", "@param", "@return", "@see", "@refer", "@since", "@throws", "@version", "@link", "@private", "@pragma"}; 

    /**
     * Create a new pldoc scanner.
     */
    public PlSqlDocScanner(ColorManager cm)
    {
        super();

        IToken keyword = new Token(new TextAttribute(cm
                .getColor(IPlSqlColorConstants.JAVADOC_KEYWORD),null,SWT.BOLD));
        IToken tag = new Token(new TextAttribute(cm.getColor(IPlSqlColorConstants.JAVADOC_TAG)));
        IToken link = new Token(
                new TextAttribute(cm.getColor(IPlSqlColorConstants.JAVADOC_LINK)));

        List<IRule> list = new ArrayList<IRule>();

        // Add rule for tags.
        list.add(new SingleLineRule("<", ">", tag)); 

        // Add rule for links.
        list.add(new SingleLineRule("{", "}", link)); 

        // Add generic whitespace rule.
        list.add(new WhitespaceRule(new PlSqlWhitespaceDetector()));

        // Add word rule for keywords.
        WordRule wordRule = new WordRule(new PlSqlWordDetector());
        for (int i = 0; i < fgKeywords.length; i++)
        {
            wordRule.addWord(fgKeywords[i], keyword);
        }
        list.add(wordRule);

        IRule[] result = new IRule[list.size()];
        list.toArray(result);
        setRules(result);
    }
}
