//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\ICompositeParseResult.java

package plsqleditor.parser.framework;

/**
 * This interface represents a parse result that contains other parse results.
 */
public interface ICompositeParseResult extends IParseResult 
{
   
   /**
    * This method gets the parse results that are contained in this composite parse 
    * result.
    * @return plsqleditor.parser.framework.IParseResult[]
    * @roseuid 43127DF502BF
    */
   public IParseResult[] getContainedParseResults();
}
