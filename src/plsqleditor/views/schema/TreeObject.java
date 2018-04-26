/**
 * 
 */
package plsqleditor.views.schema;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.ISharedImages;

import plsqleditor.parsers.Segment;
import au.com.gts.data.Column;
import au.com.gts.data.Constraint;
import au.com.gts.data.DatabaseEntity;
import au.com.gts.data.Function;
import au.com.gts.data.Grant;
import au.com.gts.data.Procedure;
import au.com.gts.data.Schema;
import au.com.gts.data.Table;
import au.com.gts.data.Trigger;

public class TreeObject implements IAdaptable
{
	private String name;
	private TreeParent parent;
	private String type;
	private IFile file;
	private Object myObject;

	public TreeObject(String name, String type, IFile associatedFile)
	{
		this.name = name;
		this.type = type;
		this.file = associatedFile;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public void setParent(TreeParent parent)
	{
		this.parent = parent;
	}

	public TreeParent getParent()
	{
		return parent;
	}

	public String toString()
	{
		if (myObject != null)
		{
			return myObject.toString();
		}
		return getName();
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class key)
	{
		if (key.equals(TreeObject.class))
		{
			return this;
		}
		return null;
	}

	protected IFile getFile()
	{
		return file;
	}

	public String getType()
	{
		return type;
	}

	public Object getObject()
	{
		return myObject;
	}

	public void setObject(Object object)
	{
		myObject = object;
	}

	protected String getNameForType(String typeName)
	{
		if (type.equals(typeName))
		{
			return name;
		}
		else if (parent != null)
		{
			return ((TreeObject) parent).getNameForType(typeName);
		}
		else
		{
			return null;
		}
	}

	/**
	 * This returns the image key for the tree object, or null if there is
	 * not a specific image key to use.
	 * 
	 * @return The image key to use for this tree object. The default
	 *         implementation will check if there is a contained object and
	 *         return the key for that. If there is no contained object, or
	 *         no key for that object, then null will be returned.
	 */
	public String getImageKey()
	{
		String imageKey = null;
		Object containedObject = getObject();
		if (containedObject == null)
		{
			return null;
		}
		if (containedObject instanceof Segment)
		{
			Segment seg = (Segment) containedObject;
			imageKey = "Schema" + seg.getType().toString();
		}
		else if (containedObject instanceof Constraint)
		{
			imageKey = containedObject.getClass().getName();
			imageKey = imageKey.substring(imageKey.lastIndexOf(".") + 1);
		}
		else if (containedObject instanceof Trigger)
		{
			imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		}
		else if (containedObject instanceof Function)
		{
			imageKey = "SchemaFunction";
		}
		else if (containedObject instanceof Procedure)
		{
			imageKey = "SchemaProcedure";
		}
		else if (containedObject instanceof Table)
		{
			imageKey = "Table";
		}
		else if (containedObject instanceof Schema)
		{
			imageKey = "Schema";
		}
		else if (containedObject instanceof Column)
		{
			imageKey = "Column";
		}
		else if (containedObject instanceof Grant)
		{
			imageKey = "Grant." + ((Grant) containedObject).getPrivilege();
		}
		return imageKey;
	}

	public String getDisplayName()
	{
		Object object = getObject();
		if (object != null && object instanceof DatabaseEntity)
		{
			return ((DatabaseEntity) object).getDisplayName();
		}
		return getName();
	}
}