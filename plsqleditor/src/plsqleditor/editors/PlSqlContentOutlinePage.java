/*
 * Created on 22/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.parsers.ContentOutlineParser;
import plsqleditor.parsers.Segment;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 22/02/2005
 */
public class PlSqlContentOutlinePage extends ContentOutlinePage
{
    /**
     * This class
     * 
     * @author Toby Zines
     * 
     * Created on 27/02/2005
     */
    public class PlSqlContentLabelProvider extends LabelProvider implements IBaseLabelProvider
    {

//        private static final int IMAGE_HEIGHT = 8;

        /**
         * @param display
         * 
         */
        public PlSqlContentLabelProvider(Display display)
        {
            //
        }

        /**
         * This method
         */
        public Image getImage(Object element)
        {
            if (element instanceof Segment)
            {
                Segment segment = (Segment) element;
                return PlsqleditorPlugin.getDefault().getImageRegistry().get(segment.getType().toString());
            }
            return null;
        }

    }

    protected Object            fInput;
    protected IDocumentProvider fDocumentProvider;
    protected ITextEditor       fTextEditor;

    ContentOutlineParser        myParser = new ContentOutlineParser();

    class ContentProvider implements ITreeContentProvider
    {
        protected static final String SEGMENTS = "__java_segments";
        protected IPositionUpdater    fPositionUpdater;
        protected List<Segment>       fContent;

        protected ContentProvider()
        {
            fPositionUpdater = new DefaultPositionUpdater(SEGMENTS);
            fContent = new ArrayList<Segment>(10);
        }

        protected void parse(IDocument document)
        {
            List<Segment> segments;
            IFileEditorInput input = (IFileEditorInput) fTextEditor.getEditorInput();
            
            segments = PlsqleditorPlugin.getDefault().getSegments(input.getFile(), input.getName(), document);
            SortedSet<Segment> sortedSegments = new TreeSet<Segment>();
            sortedSegments.addAll(segments);
            for (Segment segment : sortedSegments)
            {
                try
                {
                    document.addPosition(SEGMENTS, segment.getPosition());
                    fContent.add(segment);
                    // fContent.add(new Segment(MessageFormat
                    // .format(PlSqlEditorMessages
                    // .getString("OutlinePage.segment.title_pattern"),
                    // new Object[]{new Integer(offset)}), p));
                }
                catch (BadPositionCategoryException _ex)
                {
                    //
                }
                catch (BadLocationException _ex)
                {
                    //
                }
            }
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
            if (oldInput != null)
            {
                IDocument document = fDocumentProvider.getDocument(oldInput);
                if (document != null)
                {
                    try
                    {
                        document.removePositionCategory(SEGMENTS);
                    }
                    catch (BadPositionCategoryException _ex)
                    {
                        //
                    }
                    document.removePositionUpdater(fPositionUpdater);
                }
            }
            fContent.clear();
            if (newInput != null)
            {
                IDocument document = fDocumentProvider.getDocument(newInput);
                if (document != null)
                {
                    document.addPositionCategory(SEGMENTS);
                    document.addPositionUpdater(fPositionUpdater);
                    parse(document);
                }
            }
        }

        public void dispose()
        {
            if (fContent != null)
            {
                fContent.clear();
                fContent = null;
            }
        }

        public boolean isDeleted(Object element)
        {
            return false;
        }

        public Object[] getElements(Object element)
        {
            return fContent.toArray();
        }

        public boolean hasChildren(Object element)
        {
            return element == fInput;
        }

        public Object getParent(Object element)
        {
            if (element instanceof Segment)
            {
                return fInput;
            }
            else
            {
                return null;
            }
        }

        public Object[] getChildren(Object element)
        {
            if (element == fInput)
            {
                return fContent.toArray();
            }
            else
            {
                return new Object[0];
            }
        }
    }


    public PlSqlContentOutlinePage(IDocumentProvider provider, ITextEditor editor)
    {
        fDocumentProvider = provider;
        fTextEditor = editor;
    }

    public void createControl(Composite parent)
    {
        super.createControl(parent);
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider(new ContentProvider());
        viewer.setLabelProvider(new PlSqlContentLabelProvider(viewer.getControl().getDisplay()));
        viewer.addSelectionChangedListener(this);
        if (fInput != null)
        {
            viewer.setInput(fInput);
        }
    }

    public void selectionChanged(SelectionChangedEvent event)
    {
        super.selectionChanged(event);
        ISelection selection = event.getSelection();
        if (selection.isEmpty())
        {
            fTextEditor.resetHighlightRange();
        }
        else
        {
            Segment segment = (Segment) ((IStructuredSelection) selection).getFirstElement();
            int start = segment.getPosition().getOffset();
            int length = segment.getPosition().getLength();
            try
            {
                fTextEditor.setHighlightRange(start, length, true);
            }
            catch (IllegalArgumentException _ex)
            {
                fTextEditor.resetHighlightRange();
            }
        }
    }

    public void setInput(Object input)
    {
        // this object is a FileEditorInput (in previous runs)
        fInput = input;
        update();
    }

    public void update()
    {
        TreeViewer viewer = getTreeViewer();
        if (viewer != null)
        {
            Control control = viewer.getControl();
            if (control != null && !control.isDisposed())
            {
                control.setRedraw(false);
                viewer.setInput(fInput);
                viewer.expandAll();
                control.setRedraw(true);
            }
        }
    }
}
