package au.com.gts.data;

import plsqleditor.db.DBMetaDataGatherer;

/**
 * This type represents a stand along procedure in a database.
 * 
 * @author Toby Zines
 */
public class Function extends Procedure
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4785635958784631270L;

	public Function(DBMetaDataGatherer dbmdg, String name)
	{
		super(dbmdg, name);
	}
}
