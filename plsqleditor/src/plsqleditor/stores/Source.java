package plsqleditor.stores;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.core.runtime.IPath;

/**
 * This class represents the source of a schema/package/function etc.
 * 
 * @version $Id$
 * 
 */
public class Source
{
    /**
     * This enum represents the type of the source of a schema/package/function etc.
     * 
     * @version $Id$
     */
    public enum Type
    {
        /** This indicates the source is a file. */
        File,
        /** This indicates the sources is the database. */
        Database
    }

    private IPath mySource;
    private Type   myType;

    public Source(IPath source, Type type)
    {
        mySource = source;
        myType = type;
    }

    /**
     * This method returns the source.
     * 
     * @return {@link #mySource}.
     */
    public IPath getSource()
    {
        return mySource;
    }


    /**
     * This method returns the type.
     * 
     * @return {@link #myType}.
     */
    public Type getType()
    {
        return myType;
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
        Source rhs = (Source) object;
        return new EqualsBuilder().append(this.mySource, rhs.mySource).append(this.myType,
                                                                              rhs.myType)
                .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(23, 397).append(this.mySource).append(this.myType).toHashCode();
    }

    /**
     * A standard comparTo implementation.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        Source rhs = (Source) o;
        return new CompareToBuilder().append(this.mySource, rhs.mySource).append(this.myType,
                                                                                 rhs.myType)
                .toComparison();
    }
}
