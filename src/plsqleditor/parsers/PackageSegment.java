package plsqleditor.parsers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jface.text.Position;

public class PackageSegment extends Segment implements Cloneable
{
    /**
     * This is the list of segments contained in this package.
     */
    private List myContainedSegments = new ArrayList();

    /**
     * This is the name of the schema that owns this packageSegment
     */
	private String mySchemaName;

	public PackageSegment(String name, Position position, boolean isHeader)
    {
        super(name, position, isHeader ? SegmentType.Package : SegmentType.Package_Body);
    }

    public Object clone()
    {
        PackageSegment clone = new PackageSegment(getName(), getPosition(),
                getType() == SegmentType.Package);
        clone.setDocumentation(getDocumentation());
        clone.setLastPosition(myLastPosition);
        clone.myFieldList = myFieldList;
        clone.myIsPublic = myIsPublic;
        clone.myParameterList = myParameterList;
        clone.myReturnType = myReturnType;
        clone.myLines = myLines;
        clone.myParent = myParent;
        clone.myContainedSegments = new ArrayList();
        for (Iterator it = myContainedSegments.iterator(); it.hasNext();)
        {
            Segment segment = (Segment) it.next();
            Segment segClone = (Segment) segment.clone();
            segClone.setParent(clone);
            clone.myContainedSegments.add(segClone);
        }
        return clone;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object)
    {
        if (!(object instanceof PackageSegment))
        {
            return false;
        }
        PackageSegment rhs = (PackageSegment) object;
        return super.equals(object)
                && new EqualsBuilder().append(this.myContainedSegments, rhs.myContainedSegments)
                        .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(23, 397).append(this.myType).append(this.myName)
                .append(this.myParameterList).append(this.myPosition.getOffset())
                .append(this.myPosition.getLength()).append(this.myLastPosition.getOffset())
                .append(this.myLastPosition.getLength()).append(this.myReturnType)
                .append(this.myDocumentation).append(myContainedSegments).toHashCode();
    }

    /**
     * A standard comparTo implementation.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        PackageSegment rhs = (PackageSegment) o;
        return new CompareToBuilder().append(this.myType, rhs.myType).append(this.myName,
                                                                             rhs.myName)
                .append(this.myParameterList.toArray(new Parameter[myParameterList.size()]),
                        rhs.myParameterList.toArray(new Parameter[rhs.myParameterList.size()]))
                .append(this.myPosition.getOffset(), rhs.myPosition.getLength())
                .append(this.myLastPosition.getOffset(), rhs.myLastPosition.getLength())
                .append(this.myReturnType, rhs.myReturnType).append(this.myDocumentation,
                                                                    rhs.myDocumentation)
                .append(this.myContainedSegments, rhs.myContainedSegments).toComparison();
    }


    public List<Segment> getContainedSegments()
    {
        return myContainedSegments;
    }

    public void addSegment(Segment toAdd)
    {
        myContainedSegments.add(toAdd);
        toAdd.setParent(this);
    }

    /**
     * This method sets the schema name of the package (if it is specified).
     * 
     * @param schemaName The name of the schema owning this package.
     */
	public void setSchemaName(String schemaName) 
	{
		mySchemaName = schemaName;
	}
	
	/**
	 * This method returns the name of the schema that owns this package,
	 * or null if it has not been set. It does not need to be set.
	 * It will only be set if the file from which this package segment
	 * was generated contained an @schema marker indicating the schema
	 * it came from.
	 * 
	 * @return {@link #mySchemaName}.
	 */
	public String getSchemaName() 
	{
		return mySchemaName;
	}
}
