package plsqleditor.compare;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

public class Input implements ITypedElement, IStreamContentAccessor
{
	IFile fContent;
	String backupContent;

	public Input(IFile file)
	{
		fContent = file;
	}
	
	public Input(String data)
	{
		backupContent = data;
	}

	public String getName()
	{
		if (fContent != null)
		{
			return fContent.getName();
		}
		return "dummy";
	}

	public Image getImage()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getType()
	{
		// TODO Auto-generated method stub
		return "txt";
	}

	public InputStream getContents() throws CoreException
	{
		if (fContent != null)
		{
			return fContent.getContents();
		}
		else
		{
		    return new ByteArrayInputStream(backupContent.getBytes());
		}
	}

}
