package plsqleditor.stores;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;

import junit.framework.TestCase;
import plsqleditor.db.ConnectionPool;
import plsqleditor.parsers.Segment;

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
		List<String> schemas = dbps.getSchemas();
		long t2 = System.currentTimeMillis();
		System.out.println("Time to get schemas: " + (t2-t1) + " :");
		for (String s : schemas)
		{
			System.out.println(s);
			if (s.equals("SVCMDL"))
			{
				assertTrue(true);
			}
		}
		long t3 = System.currentTimeMillis();
		List<String> schemas2 = dbps.getSchemas();
		long t4 = System.currentTimeMillis();
		System.out.println("Time to get schemas on second try: " + (t4-t3) + " :");
		double timesFaster = ((t4 - t3 == 0) ? 999 : (t2-t1)/(t4-t3)); 
		assertTrue("Time to refresh is at least 10x as fast as initial time", (timesFaster >= 10));
	}

	public void testGetSegments() throws SQLException {
	    Statement stmt = myConn.createStatement ();

	    stmt.execute("create or replace package foobar " + 
      				 "as procedure foo; " +
      				 "function bar return number; " +
		             "end;");
		long t1 = System.currentTimeMillis();
		List<Segment> segments = dbps.getSegments("SVCMDL","FOOBAR");
		long t2 = System.currentTimeMillis();
		System.out.println("Time to get segments: " + (t2-t1) + " :");
		for (Segment s : segments)
		{
			System.out.println(s.getName() + "|" + s.getReturnType());
			if (s.getName().equals("FOOBAR"))
			{
				assertTrue(true);
				List<Segment> l = s.getParameterList();
				System.out.println("Parameters:");
				for (Segment param : l)
				{
					System.out.println(param.getName()+ "|" + param.getReturnType());
				}
			}
		}
	    stmt.execute("create or replace package foobar " + 
 				 "as procedure foo; " +
 				 "function bar return number; " +
 				 "procedure foobar(p1 in number, p2 in out varchar2); " +
	             "end;");
		long t3 = System.currentTimeMillis();
		List<Segment> segments2 = dbps.getSegments("SVCMDL","FOOBAR");
		long t4 = System.currentTimeMillis();
		double timesFaster = ((t4 - t3 == 0) ? 999 : (t2-t1)/(t4-t3)); 
		System.out.println("Time to get segments second time: " + (t4-t3) + " :");
		assertTrue("Time to refresh is at least 1.5x as fast as initial time", (timesFaster >= 1.5));
		assertTrue("First version of FOOBAR has 2 elements",(segments2.size() == 2));
		for (Segment s : segments2)
		{
			System.out.println(s.getName() + "|" + s.getReturnType());
			if (s.getName().equals("FOOBAR"))
			{
				assertTrue(true);
				List<Segment> l = s.getParameterList();
				System.out.println("Parameters:");
				for (Segment param : l)
				{
					System.out.println(param.getName()+ "|" + param.getReturnType());
				}
			}
		}
	    stmt.execute("drop package foobar");
		stmt.close();
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
		List<String> packages = dbps.getPackages("SVCMDL");
		long t2 = System.currentTimeMillis();
		int packagesSize = packages.size();
		System.out.println("Time to get packages: " + (t2-t1) + " :");
		for (String p : packages)
		{
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
		List<String> packages2 = dbps.getPackages("SVCMDL");
		long t4 = System.currentTimeMillis();
		System.out.println("Time to get packages second time: " + (t4-t3) + " :");
		double timesFaster = ((t4 - t3 == 0) ? 999 : (double)(t2-t1)/(double)(t4-t3)); 
		assertTrue("Time to refresh is at least 1.5x as fast as initial time", (timesFaster >= 1.5));
		assertTrue("((" + packagesSize + " + 1) == " + packages2.size() + "))",((packagesSize + 1) == packages2.size()));
		for (String p : packages2)
		{
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
