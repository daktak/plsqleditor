/* ---------------------------------------------------------------------
 * (C) COPYRIGHT 2003 ALCATEL AUSTRALIA LIMITED
 * 
 * This program contains proprietary information which is a trade secret
 * of  ALCATEL  AUSTRALIA  LIMITED  and  also  is  protected  under  the
 * applicable  copyright law.  Recipient is  to retain  this program  in
 * confidence and is not permitted to use or make any copy thereof other
 * than as permitted  under a written agreement with  ALCATEL  AUSTRALIA
 * LIMITED.
 * 
 * ---------------------------------------------------------------------
 * PROJECT:         TNZ Fulfil
 * ---------------------------------------------------------------------
 * EDIT HISTORY:
 * ---------------------------------------------------------------------
 * Date     DDTS#   Author      Changes/Comments
 * ---------------------------------------------------------------------
 * 22/06/2003          Toby Zines  Created
 * ---------------------------------------------------------------------
 */
package au.com.gts.data;

import java.io.Serializable;

import au.com.zinescom.util.UsefulOperations;


/**
 * This type represents
 * 
 * Created on 22/06/2003
 * 
 * @author Toby Zines
 */
public abstract class DatabaseEntity implements Serializable
{

	/**
	 * This field represents any remarks that have been entered into the 
	 * database concerning this object.  It is merely for comments explaining 
	 * this object.
	 */
	private String myRemarks;

    /** This field represents the name of the entity. */
    private String myName;

	public String getRemarks()
	{
		return myRemarks;
	}

	public void setRemarks(String myRemarks)
	{
		this.myRemarks = myRemarks;
	}

    /**
     * This method gets the name of this table.
     * 
     * @return {@link #myName}.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * This method sets the name of the object.
     * 
     * @param name The name that this object should have.
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * This method produces the string version of this class.
     * 
     * @return The stringified version of each of the fields.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        try
        {
            return UsefulOperations.produceString(this, true);
        }
        catch (IllegalStateException e)
        {
            return super.toString();
        }
    }
}
