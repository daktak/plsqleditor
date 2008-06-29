package plsqleditor.parser.framework;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.util.IInput;

public class TypedParseSpecification extends SimpleParseSpecification
{
    public static final int INTEGER   = 1;
    public static final int CHARACTER = 2;

    private Class           myType;
    private Method          myConverterMethod;
    private Method          myValidatorMethod;

    public TypedParseSpecification(String name, int type)
    {
        super(name);
        switch (type)
        {
            case (INTEGER) :
            {
                myType = Integer.class;
                try
                {
                    myConverterMethod = myType.getMethod("valueOf", new Class[] {String.class});
                }
                catch (SecurityException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (NoSuchMethodException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            }
            case (CHARACTER) :
            {
                myType = Character.class;
                try
                {
                    myValidatorMethod = this.getClass().getMethod("processChar", new Class[]{String.class});
                }
                catch (SecurityException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (NoSuchMethodException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            }
            default :
            {
                throw new IllegalArgumentException("The supplied type [" + type + "] is not valid");
            }
        }
    }
    
    public void processChar(String charString)
    {
        if (charString == null || charString.length() != 1)
        {
            throw new IllegalStateException("The string ["+ charString + "] should be one character");
        }
        return;
    }

    /**
     * This method overrides the base parse to determine that the single token
     * is of a particular type.
     * 
     * @param curPos
     * @param resultingPos
     * @param input
     * @return plsqleditor.parser.framework.IParseResult
     * @throws ParseException when a delimiter is found at the current parse
     *             location.
     */
    public IParseResult parse(Position curPos, Position resultingPos, IInput input)
            throws ParseException
    {
        IParseResult result = super.parse(curPos, resultingPos, input);

        Position endPos = result.getEndPosition();
        String tokenVal = result.getFirstToken();
        try
        {
            if (myConverterMethod != null)
            {
                myConverterMethod.invoke(tokenVal, null);
            }
            else
            {
                myValidatorMethod.invoke(this, new Object [] {tokenVal});
            }
        }
        catch (Exception e)
        {
            throw new ParseException("Failed to find a token of type [" + myType + "] at position "
                    + getPosition(endPos, input), tokenVal, endPos.getOffset(), endPos
                    .getLength());
        }
        return result;
    }
    
    public Set getFirstTokens()
    {
        Set set = new TreeSet();
        if (myType == Integer.class)
        {
            set.add("a_number");
        }
        else if (myType == Character.class)
        {
            set.add("a_character");
        }
        return set;
    }

}
