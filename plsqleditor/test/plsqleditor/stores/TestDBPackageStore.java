package plsqleditor.stores;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import junit.framework.TestCase;

import plsqleditor.db.ConnectionPool;
import plsqleditor.parsers.Segment;
//import plsqleditor.stores.DBPackageStore;

public class TestDBPackageStore extends TestCase {

	private static DBPackageStore dbps;
	
	private static ConnectionPool myCP;

	private Connection myConn;
	
	public static void main(String[] args) throws SQLException {
	}

	protected void setUp() throws Exception {
		super.setUp();
	    String url = "jdbc:oracle:oci8:@DEV53";

	    // Load the Oracle JDBC driver
	    DriverManager.registerDriver(new oracle.jdbc.OracleDriver());

	    // Connect to the database
	    myConn =  
	      DriverManager.getConnection (url, "svcmdl", "pass123");
		

	}

	protected void tearDown() throws Exception {
		super.tearDown();
		myConn.close();
	}

	public void testDBPackageStore() throws SQLException {
	    String url = "jdbc:oracle:oci8:@DEV53";

		myCP = new ConnectionPool("oracle.jdbc.OracleDriver",url,"svcmdl", "pass123",1,1,true);
		dbps = new DBPackageStore(myCP);
		System.out.println("Created DBPackageStore in constructor");
	}

	public void testGetSysDate() throws SQLException {
		SimpleDateFormat f = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss z");
		System.out.println("Timestamp:" + f.format(dbps.getSysTimestamp()));
		long t1 = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++)
		{
			dbps.getSysTimestamp(); //System.currentTimeMillis(); //
		}
		long t2 = System.currentTimeMillis();
		System.out.println("Time to get 1000 sysdates: " + (t2-t1));
	}

	/*
	 * Class under test for Object[] getObjectsByVariables(String, Object[])
	 */
	public void testGetObjectsByVariablesStringObjectArray() {
	}

	/*
	 * Class under test for Object[] getObjectsByVariables(String, Object[], Class)
	 */
	public void testGetObjectsByVariablesStringObjectArrayClass() {
	}

	public void testGetResultSetByVariables() {
	}

	public void testGetSchemas() throws SQLException {
		long t1 = System.currentTimeMillis();
        SortedSet schemas = dbps.getSchemas();
		long t2 = System.currentTimeMillis();
		System.out.println("Time to get schemas: " + (t2-t1) + " :");
		for (Iterator it = schemas.iterator(); it.hasNext(); )
		{
            String s = (String) it.next();
			System.out.println(s);
			if (s.equals("SVCMDL"))
			{
				assertTrue(true);
			}
		}
		long t3 = System.currentTimeMillis();
        SortedSet schemas2 = dbps.getSchemas();
		long t4 = System.currentTimeMillis();
		System.out.println("Time to get schemas on second try: " + (t4-t3) + " :");
		double timesFaster = ((t4 - t3 == 0) ? 999 : (t2-t1)/(t4-t3)); 
		assertTrue("Time to refresh is at least 10x as fast as initial time", (timesFaster >= 10));
	}

	public void testGetSegments() throws SQLException, InterruptedException {
	    Statement stmt = myConn.createStatement ();

	    stmt.execute("create or replace package foobar " + 
      				 "as procedure foo; " +
      				 "function bar return number; " +
		             "end;");
		long t1 = System.currentTimeMillis();
		List segments = dbps.getSegments("SVCMDL","FOOBAR");
		long t2 = System.currentTimeMillis();
		System.out.println("Time to get segments: " + (t2-t1) + " :");
        for (Iterator it = segments.iterator(); it.hasNext();)
        {
            Segment s = (Segment) it.next();
			System.out.println(s.getName() + "|" + s.getReturnType());
			if (s.getName().equals("FOOBAR"))
			{
				assertTrue(true);
				List l = s.getParameterList();
				System.out.println("Parameters:");
                for (Iterator it2 = l.iterator(); it2.hasNext();)
                {
                    Segment param  = (Segment) it2.next();
					System.out.println(param.getName()+ "|" + param.getReturnType());
				}
			}
		}
		Thread.sleep(2000); //just to make sure the DDL time gets updated
	    stmt.execute("create or replace package foobar " + 
 				 "as procedure foo; " +
 				 "function bar return number; " +
 				 "procedure foobar(p1 in number, p2 in out varchar2); " +
	             "end;");
		long t3 = System.currentTimeMillis();
		List segments2 = dbps.getSegments("SVCMDL","FOOBAR");
		long t4 = System.currentTimeMillis();
		double timesFaster = ((t4 - t3 == 0) ? 999 : (t2-t1)/(t4-t3)); 
		System.out.println("Time to get segments second time: " + (t4-t3) + " :");
		assertTrue("Time to refresh is at least 10x as fast as initial time", (timesFaster >= 10));
		assertTrue("First version of FOOBAR has 2 elements",(segments.size() == 2));
		for (Iterator it = segments2.iterator(); it.hasNext();)
		{
            Segment s = (Segment) it.next();
			System.out.println(s.getName() + "|" + s.getReturnType());
			if (s.getName().equals("FOOBAR"))
			{
				assertTrue(true);
				List l = s.getParameterList();
				System.out.println("Parameters:");
				for (Iterator it2 = l.iterator(); it2.hasNext();)
				{
                    Segment param = (Segment) it2.next();
					System.out.println(param.getName()+ "|" + param.getReturnType());
				}
			}
		}
		Thread.sleep(11000);
		segments2 = dbps.getSegments("SVCMDL","FOOBAR");
		assertTrue("Second version of FOOBAR has 3 elements",(segments2.size() == 3));
	    stmt.execute("drop package foobar");
		stmt.close();
		
		//Check that the refresh of aliases works
		segments = dbps.getSegments("SVCMDL","DBMS_OUTPUT");
		assertTrue("dbms_output segments found", (segments.size() > 0));
		segments = dbps.getSegments("SVCMDL","DBMS_OUTPUT");		
		assertTrue("dbms_output segments found", (segments.size() > 0));
		int putLineCount = 0;
		for (Iterator it = segments.iterator(); it.hasNext();)
		{
            Segment s = (Segment) it.next();
			System.out.println(s.getName() + "|" + s.getReturnType());
			if (s.getName().equals("PUT_LINE"))
			{
				putLineCount++;
				List l = s.getParameterList();
				System.out.println("Parameters:");
				for (Iterator it2 = l.iterator(); it2.hasNext();)
				{
                    Segment param = (Segment) it2.next();
					System.out.println(param.getName()+ "|" + param.getReturnType());
				}
			}
		}
		System.out.println("putLineCount=" + putLineCount);
		assertTrue(putLineCount == 4); //For 9i at least, there are 4 overloaded methods for put_line

	}

	public void testGetPackages() throws SQLException, InterruptedException {
		Statement stmt = myConn.createStatement ();
		try 
		{
			stmt.execute("drop package test_get_packages");
		}
		catch (SQLException e)
		{
		}
		long t1 = System.currentTimeMillis();
        SortedSet packages = dbps.getPackages("SVCMDL", true);
		long t2 = System.currentTimeMillis();
		int packagesSize = packages.size();
		System.out.println("Time to get packages: " + (t2-t1) + " :");
		for (Iterator it = packages.iterator(); it.hasNext();)
		{
            String p = (String) it.next();
			System.out.println(p);
			if (p.equals("INTERFACE"))
			{
				assertTrue("Found package "+p,true);
				System.out.println("Found package "+p);
			}
		}

		stmt.execute("create or replace package test_get_packages " + 
      				 "as procedure foo; " +
      				 "function bar return number; " +
		             "end;");
		long t3 = System.currentTimeMillis();
        SortedSet packages2 = dbps.getPackages("SVCMDL", true);
		long t4 = System.currentTimeMillis();
		System.out.println("Time to get packages second time: " + (t4-t3) + " :");
		double timesFaster = ((t4 - t3 == 0) ? 999 : (double)(t2-t1)/(double)(t4-t3)); 
		assertTrue("Time to refresh is at least 10x as fast as initial time", (timesFaster >= 10));
		Thread.sleep(11000);
		packages2 = dbps.getPackages("SVCMDL", true);
		
		assertTrue("((" + packagesSize + " + 1) == " + packages2.size() + "))",((packagesSize + 1) == packages2.size()));
		for (Iterator it = packages2.iterator(); it.hasNext();)
		{
            String p = (String) it.next();
			System.out.println(p);
			if (p.equals("FOOBAR"))
				assertTrue("Found package "+p,true);
		}
	    stmt.execute("drop package test_get_packages");
		stmt.close();
	}
	
	public void testGetSource() throws SQLException
	{
		String s = dbps.getSource("SVCMDL","INTERFACE");
		System.out.println(s);
		assertTrue(s.startsWith("PACKAGE"));
	}
	
}
