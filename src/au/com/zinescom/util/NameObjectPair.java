package au.com.zinescom.util;

/**
 * @author zinest
 */
public class NameObjectPair
{
	/** This field is the name of the object held. */
	private String myName;

	/** This field is the object being held in this pair. */
	private Object myValue;

	/**
	 * This constructor creates the name object pair,
	 * associating an object with a name.
	 *
	 * @param name The name of the object <code>obj</code>.
	 *
	 * @param obj The object that this NameObjectPair
	 *         is representing.
	 */
	public NameObjectPair(String name, Object obj)
	{
		myName  = name;
		myValue = obj;
	}

	/**
	 * This method gets the stored name.
	 *
	 * @return {@link #myName}.
	 */
	public String getName()
	{
		return myName;
	}

	/**
	 * This method gets the stored object.
	 *
	 * @return {@link #myValue}.
	 */
	public Object getObject()
	{
		return myValue;
	}

	/**
	 * This method prints the name and object (as a string).
	 *
	 * @return a string version of this name object pair.
	 */
	public String toString()
	{
		return "Name : " + myName + ", Value = " + myValue;
	}
}
