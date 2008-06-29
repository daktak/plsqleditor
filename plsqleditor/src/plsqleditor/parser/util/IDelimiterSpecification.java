//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\util\\IDelimiterSpecification.java

package plsqleditor.parser.util;


/**
 * This interface represents a Delimiter. It is important because the token
 * grabber must be able to distinguish delimiters.
 */
public interface IDelimiterSpecification
{

    /**
     * This method returns the length of the delimiter that this is specifying.
     * 
     * @return int
     * @roseuid 43190DE70271
     */
    public int length();

    /**
     * This method returns the first char of the delimiter which will help the
     * token grabber to determine whether the next token is a delimiter or not.
     * 
     * @return char
     * @roseuid 43190DF50271
     */
    public char firstChar();

    /**
     * This method returns the string representation of the delimiter.
     * 
     * @return java.lang.String
     * @roseuid 43190F0E036B
     */
    public String getString();

    /**
     * This method indicates that the spec will be a delimiter WHEREVER it
     * appears in an input. Examples of this would be the semi colon and the
     * colon in java, or a semi colon in PL/SQL.
     * 
     * @return <code>true</code> if the string representing this delimiter is
     *         always a delimiter.
     */
    public boolean isStandaloneDelimiter();
}
