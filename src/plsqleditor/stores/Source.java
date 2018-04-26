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
    private IPath           mySource;
    private PersistenceType myPersistenceType;

    public Source(IPath source, PersistenceType sourceType)
    {
        mySource = source;
        myPersistenceType = sourceType;
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
     * @return {@link #myPersistenceType}.
     */
    public PersistenceType getType()
    {
        return myPersistenceType;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object)
    {
        if (!(object instanceof Source))
        {
            return false;
        }
        Source rhs = (Source) object;
        return new EqualsBuilder().append(String.valueOf(this.mySource),
                                          String.valueOf(rhs.mySource))
                .append(this.myPersistenceType, rhs.myPersistenceType).isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return new HashCodeBuilder(23, 397).append(String.valueOf(this.mySource))
                .append(this.myPersistenceType).toHashCode();
    }

    /**
     * A standard comparTo implementation.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        Source rhs = (Source) o;
        return new CompareToBuilder().append(String.valueOf(this.mySource),
                                             String.valueOf(rhs.mySource))
                .append(this.myPersistenceType, rhs.myPersistenceType).toComparison();
    }
}
