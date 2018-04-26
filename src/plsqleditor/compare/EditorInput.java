package plsqleditor.compare;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * 
 * @author tzines
 */
public class EditorInput extends CompareEditorInput
{

	private IFile myFile;
	private IFile myDbFile = null;
	private String myDbString = null;

	public EditorInput(CompareConfiguration configuration, IFile file, IFile dbFile)
	{
		super(configuration);
		myFile = file;
		myDbFile = dbFile;
	}

	public EditorInput(CompareConfiguration configuration, IFile file, String dbString)
	{
		super(configuration);
		myFile = file;
		myDbString = dbString;
	}

	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException
	{
		Differencer d = new Differencer();
		Input dbInput = myDbString != null ? new Input(myDbString) : new Input(myDbFile);
		Object diff = d.findDifferences(false, new NullProgressMonitor(), null,
				null, new Input(myFile), dbInput);
		return diff;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void saveChanges(IProgressMonitor monitor) throws CoreException
	{
		// TODO save changes in file...
		super.saveChanges(monitor);
	}
	
}