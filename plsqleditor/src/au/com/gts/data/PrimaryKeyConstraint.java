/*
 * @version $Id$
 */
package au.com.gts.data;

/**
 * This type represents
 * 
 * Created on 22/06/2003
 * 
 * @author Toby Zines
 */
public class PrimaryKeyConstraint extends Constraint
{
    /**
     * This is the serial version uid.
     */
    private static final long serialVersionUID = 3689626978315679288L;

    /** This field represents the primary key of this constraint.*/
    private Column myPrimaryKeyColumn;
    
    /** This field represents the sequence number of this primary key. */
    private short  mySequenceNumber;
    
    /**
     * This constructor generates the foreign key constraint passing in 
     * the primary key, foreign key and constraint name (which is the
     * same as the foreign key).
     * 
     * @param name The name of the constraint.
     * 
     * @param pkColumn The primary key column referenced by this constraint.
     * 
     * @param seqNum The sequence number
     */
    public PrimaryKeyConstraint(String name, Column pkColumn, short seqNum)
    {
        setName(name);
        myPrimaryKeyColumn = pkColumn;
        mySequenceNumber   = seqNum;
    }
    /**
     * @return Returns the primaryKeyColumn.
     */
    public Column getPrimaryKeyColumn()
    {
        return myPrimaryKeyColumn;
    }
    /**
     * @return Returns the sequenceNumber.
     */
    public short getSequenceNumber()
    {
        return mySequenceNumber;
    }
}
