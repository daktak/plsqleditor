package plsqleditor.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.viewers.IDoubleClickListener;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 22/02/2005
 */
public class PlSqlAnnotationHover implements IAnnotationHover
{
	public interface ICallback {
		void run(IInformationControlExtension2 control);
	}

	/**
	 * Input used by the control to display the annotations.
	 * TODO move to top-level class
	 * TODO encapsulate fields
	 *
	 * @since 3.0
	 */
	public static class AnnotationHoverInput {
		public Annotation[] fAnnotations;
		public ISourceViewer fViewer;
		public IVerticalRulerInfo fRulerInfo;
		public IVerticalRulerListener fAnnotationListener;
		public IDoubleClickListener fDoubleClickListener;
		public ICallback redoAction;
		public IAnnotationModel model;
	}
    public PlSqlAnnotationHover()
    {
        //
    }

    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber)
    {
        AnnotationHoverInput input = (AnnotationHoverInput) getHoverInfoForLine(sourceViewer, lineNumber);
        if (input.fAnnotations.length > 0)
        {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < input.fAnnotations.length; i++)
            {
                Annotation a = input.fAnnotations[i];
                sb.append(a.getText());
                if (i < input.fAnnotations.length - 1)
                {
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
        IDocument document = sourceViewer.getDocument();
        try
        {
            IRegion info = document.getLineInformation(lineNumber);
            return document.get(info.getOffset(), info.getLength());
        }
        catch (BadLocationException _ex)
        {
            return null;
        }
    }

    public Object getHoverInfoForLine(ISourceViewer viewer, int lineNumber)
    {
        IAnnotationModel model = viewer.getAnnotationModel();
        IDocument document = viewer.getDocument();

        if (model == null) return null;

        List<Annotation> exact = new ArrayList<Annotation>();

        Iterator<?> e = model.getAnnotationIterator();
        while (e.hasNext())
        {
            Annotation annotation = (Annotation) e.next();
            Position position = model.getPosition(annotation);
            if (position == null)
            {
                continue;
            }
            try
            {
                if (document.getLineOfOffset(position.getOffset()) == lineNumber)
                {
                    exact.add(annotation);
                }
            }
            catch (BadLocationException e1)
            {
                e1.printStackTrace();
            }
        }

        AnnotationHoverInput input = new AnnotationHoverInput();
        input.fAnnotations = (Annotation[]) exact.toArray(new Annotation[0]);
        input.fViewer = viewer;
        //input.fRulerInfo = myCompositeRuler;
        //input.fDoubleClickListener = myDblClickListener;
        input.model = model;

        return input;
    }
}
