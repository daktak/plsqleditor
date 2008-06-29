//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\CompositeParseResult.java

package plsqleditor.parser.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.Position;


public class CompositeParseResult extends ParseResult implements ICompositeParseResult
{
    private List myParseResults;

    /**
     * @param spec
     * @roseuid 43127CA901D4
     */
    public CompositeParseResult(IParseSpecification spec)
    {
        super(spec);
        myParseResults = new ArrayList();
    }

    /**
     * @return plsqleditor.parser.framework.IParseResult[]
     * @roseuid 43127E1C02BF
     */
    public IParseResult[] getContainedParseResults()
    {
        return (IParseResult[]) myParseResults.toArray(new IParseResult[myParseResults.size()]);
    }

    /**
     * This method adds another parse result to the set of results already
     * stored. The parse result <code>pr</code> is always added to the end.
     * This method also updates the start and end positions obtained by calls to
     * {@link ParseResult#getStartPosition()} and
     * {@link ParseResult#getEndPosition()} where required.
     * 
     * @param pr The parse result to add to this container.
     * 
     * @roseuid 43127CB0031C
     */
    public void addParseResult(IParseResult pr)
    {
        if (myParseResults.isEmpty())
        {
            setStartPosition(pr.getStartPosition());
        }
        else
        {
            setEndPosition(pr.getEndPosition());
        }
        myParseResults.add(pr);
    }

    /**
     * This method gets the first non whitespace token located in the set of
     * contained parse results.
     * 
     * @return the first non whitespace token located in the set of contained
     *         parse results.
     */
    public Position getFirstNonWhitespacePosition()
    {
        return ((IParseResult) myParseResults.get(0)).getFirstNonWhitespacePosition();
    }

    /**
     * This method returns the first non whitespace string in the result.
     * 
     * @return the first non whitespace string in the result.
     */
    public String getFirstToken()
    {
        return ((IParseResult) myParseResults.get(0)).getFirstToken();
    }
    
    /**
     * This method gets the full text of the parsed input.
     * 
     * @return The unadulterated full parsed text (whitespace, comments and
     *         tokens).
     */
    public String getText()
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = myParseResults.iterator(); it.hasNext();)
        {
            ParseResult result = (ParseResult) it.next();
            sb.append(result.getText());
        }
        return sb.toString();
    }
}
