/*
 * Created on 12.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package plsqleditor.editors;

import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author ruehl
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PlsqlSourceAnnotationModel extends ResourceMarkerAnnotationModel
{
    public PlsqlSourceAnnotationModel(IResource input)
    {
        super(input);
    }

    protected MarkerAnnotation createMarkerAnnotation(IMarker marker)
    {
        return new PlsqlMarkerAnnotation(marker);
    }
}
