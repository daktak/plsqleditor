package au.com.gts.data;

public class UniqueKeyConstraint extends Constraint
{
	public UniqueKeyConstraint(String name, Column col)
	{
		super(name, col);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4720250668471261543L;

	public String getDisplayName()
	{
		return getName() + " is unique";
	}

	public String toString()
	{
		return "Unique Key " + super.toString();
	}
}
