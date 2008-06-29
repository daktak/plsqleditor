//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\IParseSpecification.java

package plsqleditor.parser.framework;

import java.util.Set;
import java.util.Stack;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.util.IInput;

/**
 * This interface represents the interface of a parse specification.
 */
public interface IParseSpecification 
{
   /**
    * This method parses a particular body of text, starting at the position specified and 
    * ending at the position located in the end position parameter.
    * @param curPos
    * @param resultingPos
    * @param input
    * @return plsqleditor.parser.framework.IParseResult
    * @roseuid 43126C700128
    */
   public IParseResult parse(Position curPos, Position resultingPos, IInput input) throws ParseException;
   
   /**
    * This method gets the string representation of the spec in a BNF like format.
    *
    * @param depth The depth to which to display contained specs. Once this hits
    *        zero, just the name of the contained spec should be identified.
    */
   public String toString(int depth);
   
   /**
    * This method checks that no infinite loop is contained in the specification.
    * This throws a ContainsLoopException if there is a loop.
    * It must be implemented by subclasses of this class.
    *
    * @param previouslyContainedParseSpecifications The list of <b>names</b> of the 
    *        previous possible calls in a sequence in which this specification could 
    *        being called.
    */
   public void checkForInfiniteLoop(Stack previouslyContainedSpecs)
   throws ContainsLoopException;

   /**
    * This method gets the delimiter specifications that are used by this specification.
    * It the name of this parse specification is contained in the list of 
    * <code>previousCalledNames</code>
    *
    * @param previousCalledNames The list of names of the specifications that have 
    *        already been requested in this call stack.
    *
    * @return The list of delimiter specs used by this specification (excluding the
    *         specifications contained in the supplied list of 
    *         <code>previousCalledNames</code>).
    */
   //public IDelimiterSpec [] getUtilisedDelimiterSpecs(List previousCalledNames);
   
   /**
    * This method returns the name of the specification.
    */
   public String getName();

   /**
    * This method returns the possible immediately next tokens for the given parse specification.
    * @return The string list of the next tokens that are supported by this specification.
    */
   public Set getFirstTokens(); 
}
