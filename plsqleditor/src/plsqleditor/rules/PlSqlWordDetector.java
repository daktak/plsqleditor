package plsqleditor.rules;
import org.eclipse.jface.text.rules.IWordDetector;

/**
 * This class 
 * 
 * @author Toby Zines
 * 
 * Created on 21/02/2005 
 */
public class PlSqlWordDetector
    implements IWordDetector
{

    public PlSqlWordDetector()
    {
        // do nothing
    }

    public boolean isWordPart(char character)
    {
        return Character.isJavaIdentifierPart(character);
    }

    public boolean isWordStart(char character)
    {
        return Character.isJavaIdentifierStart(character);
    }
}