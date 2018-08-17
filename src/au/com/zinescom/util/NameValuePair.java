/*
 * @version $Id$
 */
package au.com.zinescom.util;

import java.util.StringTokenizer;

/**
 * This class represents an arbitrary Name Value pair.
 * @author  default
 * @version 1.0
 */
public class NameValuePair
{

    /** This is the name in the pair. */
    private String myName;

    /** This is the value in the pair. */
    private String myValue;


	/**
	 * Constructor that accepts one string of the format "propertyName=propertyValue", parses it and
	 * stores the propertyName and property value in this class.
	 *
	 * @param	propertyNameAndValueString	<font face="Courier New, Courier, mono">propertyNameAndValueString</font><BR>
	 *								A name-value pair of the format <font face="Courier New, Courier, mono">"name=value"</font>.<BR>
	 *								eg <font face="Courier New, Courier, mono">"admp.namingservice.hostname=aalh07"</FONT>
	 *								Leading and trailing spaces are trimmed off.
	 */
	public NameValuePair(String propertyNameAndValueString)
	{
		//String tokens are: "=", " " (whitespace)
		StringTokenizer st = new StringTokenizer(propertyNameAndValueString, "= ");
		if (st.countTokens() != 2)
		{
			System.out.println("<In NameValuePair::NameValuePair(String)>Fatal error. Usage: 'name=value'.");
		}
		else
		{
			//Set the values to this object's instance variables.
			//Trim all white spaces from both end's of each string.
			myName  = (st.nextToken()).trim();
			myValue = (st.nextToken()).trim();
		}
	}

    /**
     * This method creates new NameValuePair.  If either value
     * is null, an IllegalArgumentException will be thrown.
     *
     * @param name The name in the pair.  Must not be null.
     *
     * @param value The value of the pair. Must not be null.
     */
    public NameValuePair(String name, String value)
    {
        final String METHOD_NAME = "NameValuePair(name, value) ";
        if (name == null)
        {
            throw new IllegalArgumentException(METHOD_NAME +
                ": the name is null.");
        }
        if (value == null)
        {
            throw new IllegalArgumentException(METHOD_NAME +
                ": the value is null.");
        }
        myName = name;
        myValue = value;
    }

    /**
     * This method gets the name of the pair.
     *
     * @return The name of the pair
     */
    public String getName()
    {
        return myName;
    }

    /**
     * This method gets the value of the pair.
     *
     * @return The value of the pair
     */
    public String getValue()
    {
        return myValue;
    }
}