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
 * 18/06/2003          Toby Zines  Created
 * ---------------------------------------------------------------------
 */
package au.com.gts.data;

/**
 * This type represents
 * 
 * Created on 18/06/2003
 * 
 * @author Toby Zines
 */
public class DataConstraint extends Constraint
{
    /**
     * This is the serial version uid.
     */
    private static final long serialVersionUID = 3256439222540974135L;

    private String myRegularExpression;

	public String getRegularExpression()
	{
		return myRegularExpression;
	}

	public void setRegularExpression(String myRegularExpression)
	{
		this.myRegularExpression = myRegularExpression;
	}
}
