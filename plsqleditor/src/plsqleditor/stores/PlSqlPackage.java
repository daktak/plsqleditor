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
    private PlSqlSchema   mySchema;
    /**
     * This field represents the modification timestamp of the file from which the last segment 
     * modification to this object came.
     */
    private long          myLatestChange = -2;

    public PlSqlPackage(PlSqlSchema owner, String name, Source type)
    {
        mySchema = owner;
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
    public void setSegments(List<Segment> segments, long timestamp)
    {
        mySegments = segments;
        if (timestamp > myLatestChange)
        {
            myLatestChange = timestamp;
        }
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

    public boolean add(Segment segment, long timestamp)
    {
        myLatestChange = timestamp;
        return mySegments.add(segment);
    }

    public boolean contains(Segment segment)
    {
        return mySegments.contains(segment);
    }

    /**
     * This method returns the schema.
     * 
     * @return {@link #mySchema}.
     */
    public PlSqlSchema getSchema()
    {
        return mySchema;
    }


    /**
     * This method sets the schema. It should only ever get called by a schema when this object is
     * being added to it.
     * 
     * @param schema The schema to set.
     */
    void setSchema(PlSqlSchema schema)
    {
        mySchema = schema;
    }

    /**
     * This method returns the latestChange.
     * 
     * @return {@link #myLatestChange}.
     */
    public long getLatestChange()
    {
        return myLatestChange;
    }
    

}
