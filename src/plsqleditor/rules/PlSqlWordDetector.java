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
    private boolean myIsDotAWordPart = false;
    
    public PlSqlWordDetector(boolean isDotAWordPart)
    {
        myIsDotAWordPart = isDotAWordPart;
    }

    public boolean isWordPart(char character)
    {
        // fix for 1439880 - Keywords as functions/procedures
        return character == '.' ? myIsDotAWordPart : Character.isJavaIdentifierPart(character);
    }

    public boolean isWordStart(char character)
    {
        return Character.isJavaIdentifierStart(character);
    }
}