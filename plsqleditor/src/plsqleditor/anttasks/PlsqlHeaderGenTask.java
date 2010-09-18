package plsqleditor.anttasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import plsqleditor.popup.actions.PlsqlHeaderGenerator;

public class PlsqlHeaderGenTask extends Task
{

	private String plsqlFilename;
	private String plsqlOutFilename;
	private String headerExtention;
	private List<FileSet> fileSets;
	private PlsqlHeaderGenerator generator;

	public void init()
	{
		generator = new PlsqlHeaderGenerator();
		fileSets = new ArrayList<FileSet>();
	}

	public void setPlsqlfile(String plsqlFilename)
	{
		this.plsqlFilename = plsqlFilename;
	}

	public void setDestfile(String plsqlOutFilename)
	{
		this.plsqlOutFilename = plsqlOutFilename;
	}

	public void setHeaderextention(String headerExtention)
	{
		this.headerExtention = headerExtention;
	}

	public void addFileset(FileSet fileSet)
	{
		this.fileSets.add(fileSet);
	}

	public void execute() throws BuildException
	{
		FileWriter fw = null;
		try
		{
			if (plsqlFilename == null && fileSets.isEmpty())
			{
				throw new BuildException(
						"Either plsqlfile or file set needs to be included");
			}
			if (headerExtention == null)
			{
				this.headerExtention = ".pkh";
			}
			if (plsqlFilename != null && plsqlOutFilename == null)
			{
				this.plsqlOutFilename = plsqlFilename.substring(0,
						plsqlFilename.lastIndexOf("."))
						+ headerExtention;
			}
			if (plsqlFilename != null)
			{
				try
				{
					fw = new FileWriter(plsqlOutFilename);
					fw.write(generator.parseBodyFile(plsqlFilename));
					fw.close();
				}
				catch (IOException io)
				{
					throw new BuildException(io);
				}
			}
			else
			{
				try
				{
					Iterator<FileSet> i = fileSets.iterator();
					while (i.hasNext())
					{
						FileSet fs = i.next();
						String[] files = fs.getDirectoryScanner()
								.getIncludedFiles();

						for (int a = 0; a < files.length; a++)
						{
							File f = new File(fs.getDirectoryScanner()
									.getBasedir(), files[a].substring(0,
									files[a].lastIndexOf("."))
									+ headerExtention);
							fw = new FileWriter(f);
							fw.write(generator
									.parseBodyFile(new FileInputStream(
											new File(fs.getDirectoryScanner()
													.getBasedir(), files[a]))));
							fw.close();
						}
					}
				}
				catch (IOException io)
				{
					throw new BuildException(io);
				}
			}
		}
		finally
		{
			try
			{
				fw.close();
			}
			catch (Exception _ex)
			{
				;
			}
		}
	}

}
