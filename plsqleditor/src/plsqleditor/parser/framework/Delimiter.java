//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\Delimiter.java

package plsqleditor.parser.framework;

import org.eclipse.jface.text.Position;

/**
 * This class represents a Delimiter. It is a particular type of parse result.
 */
public class Delimiter extends SimpleParseResult
{
    public DelimiterSpec theDelimiterSpec;

    /**
     * @param spec
     * @param preString
     * @param prePos
     * @param delim
     * @param delimPos
     * @roseuid 4313A3BD02EE
     */
    public Delimiter(IParseSpecification spec,
                     String preString,
                     Position prePos,
                     String delim,
                     Position delimPos)
    {
        super(spec, preString, prePos, delim, delimPos);
    }
}
