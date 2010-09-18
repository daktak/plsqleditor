package plsqleditor.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public abstract class PersistentPropertyAction extends AbstractHandler
{
	private QualifiedName exampleQn = new QualifiedName("html", "path");

	public PersistentPropertyAction()
	{
		super();
	}

	/**
	 * This does nothing - just a reminder of what a qualified name looks like.
	 * @return {@link #exampleQn}
	 */
	protected QualifiedName getExampleQualifiedName()
	{
		return exampleQn;
	}
	
	protected String getPersistentProperty(IResource res, QualifiedName qn)
	{
		try {
			return (String) res.getPersistentProperty(qn);
		} catch (CoreException e) {
			return "";
		}
	}

	protected void setPersistentProperty(IResource res, QualifiedName qn,
			String value)
	{
		try {
			res.setPersistentProperty(qn, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}