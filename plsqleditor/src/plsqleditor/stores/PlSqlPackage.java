/**
 * 
 */
package plsqleditor.stores;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import plsqleditor.parsers.Segment;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 8/03/2005
 * 
 */
public class PlSqlPackage
{
    private String        myName;
    private List<Segment> mySegments;
    private Source        mySource;

    public PlSqlPackage(String name, Source type)
    {
        myName = name;
        mySource = type;
        mySegments = new ArrayList<Segment>();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object)
    {
        if (!(object instanceof PlSqlPackage))
        {
            return false;
        }
        PlSqlPackage rhs = (PlSqlPackage) object;
        return new EqualsBuilder().append(this.myName, rhs.myName).append(this.mySegments,
                                                                          rhs.mySegments)
                .append(this.mySource, rhs.mySource).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(23, 397).append(this.myName).append(this.mySegments)
                .append(this.mySource).toHashCode();
    }

    /**
     * A standard comparTo implementation.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        PlSqlPackage rhs = (PlSqlPackage) o;
        return new CompareToBuilder().append(this.myName, rhs.myName).append(this.mySegments,
                                                                             rhs.mySegments)
                .append(this.mySource, rhs.mySource).toComparison();
    }

    /**
     * This method returns the name.
     * 
     * @return {@link #myName}.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * This method returns the segments.
     * 
     * @return {@link #mySegments}.
     */
    public List<Segment> getSegments()
    {
        return mySegments;
    }


    /**
     * This method sets the segments for this package.
     * 
     * @param segments The segments to set.
     */
    public void setSegments(List<Segment> segments)
    {
        mySegments = segments;
    }


    /**
     * This method returns the sourceType.
     * 
     * @return {@link #mySource}.
     */
    public Source getSourceType()
    {
        return mySource;
    }

    public boolean add(Segment segment)
    {
        return mySegments.add(segment);
    }

    public boolean contains(Segment segment)
    {
        return mySegments.contains(segment);
    }
}
