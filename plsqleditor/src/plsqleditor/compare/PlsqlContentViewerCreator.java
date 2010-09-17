package plsqleditor.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class PlsqlContentViewerCreator implements IViewerCreator
{

    @Override
    public Viewer createViewer(Composite parent, CompareConfiguration config)
    {
        // TODO Auto-generated method stub
        return new PlsqlFileToDbMergeComparer(parent, config);
    }

}
