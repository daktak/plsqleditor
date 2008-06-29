/*
 * Created on 22/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import java.util.ArrayList;
import java.util.Iterator;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.parsers.PackageSegment;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;

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
                // FIX for feature 1387684  
                // need private identifiers
                String key = segment.getImageKey();
                return PlsqleditorPlugin.getDefault().getImageRegistry().get(key);
            }
            return null;
        }

    }

    protected Object            fInput;
    protected IDocumentProvider fDocumentProvider;
    protected ITextEditor       fTextEditor;

    class ContentProvider implements ITreeContentProvider
    {
        protected static final String SEGMENTS = "__plsql_segments";
        protected IPositionUpdater    myPositionUpdater;
        protected List                mySegments;

        protected ContentProvider()
        {
            myPositionUpdater = new DefaultPositionUpdater(SEGMENTS);
            mySegments = new ArrayList(10);
        }

        protected void parse(IDocument document)
        {
            parse(document, false);
        }

        protected void parse(IDocument document, boolean isPriorToSetFocus)
        {
            List segments;
            IEditorInput dfltInput = fTextEditor.getEditorInput();
            // FIX for bug 1306379 getting class cast exception on
            // show annotation view
            if (dfltInput instanceof IFileEditorInput)
            {
                IFileEditorInput input = (IFileEditorInput) dfltInput;
                segments = PlsqleditorPlugin.getDefault().getSegments(input.getFile(),
                                                                      document,
                                                                      isPriorToSetFocus);
                mySegments.addAll(sortAndFilterSegments(segments, document));
            }
            else
            {
                // FIX for bug 1306379 getting class cast exception on
                // show annotation view
                System.out.println("Received unprocessable input type [" + dfltInput
                        + "] doing nothing");
            }
        }

        private List sortAndFilterSegments(List segments, IDocument document)
        {
            SortedSet sortedSegments = new TreeSet();
            sortedSegments.addAll(segments);
            List segmentsToReturn = new ArrayList();
            for (Iterator it = sortedSegments.iterator(); it.hasNext();)
            {
                Segment segment = (Segment) it.next();
                try
                {
                    if (segment.getType() != SegmentType.Code)
                    {
                        document.addPosition(SEGMENTS, segment.getPosition());
                        if (segment instanceof PackageSegment)
                        {
                            PackageSegment pkgSegment = (PackageSegment) segment;
                            PackageSegment clone = (PackageSegment) pkgSegment.clone();
                            List containedSegments = clone.getContainedSegments();
                            List sortedContainedSegments = sortAndFilterSegments(clone.getContainedSegments(), document);
                            containedSegments.clear();
                            containedSegments.addAll(sortedContainedSegments);
                            segmentsToReturn.add(clone);
                        }
                        else
                        {
                            segmentsToReturn.add(segment);
                        }
                    }
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
            return segmentsToReturn;
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
                    document.removePositionUpdater(myPositionUpdater);
                }
            }
            mySegments.clear();
            if (newInput != null)
            {
                IDocument document = fDocumentProvider.getDocument(newInput);
                if (document != null)
                {
                    document.addPositionCategory(SEGMENTS);
                    document.addPositionUpdater(myPositionUpdater);
                    parse(document, true);
                }
            }
        }

        public void dispose()
        {
            if (mySegments != null)
            {
                mySegments.clear();
                mySegments = null;
            }
        }

        public boolean isDeleted(Object element)
        {
            return false;
        }

        public Object[] getElements(Object element)
        {
            if (element instanceof PackageSegment)
            {
                PackageSegment pkgSegment = (PackageSegment) element;
                return pkgSegment.getContainedSegments().toArray();
            }
            else if (element == fInput)
            {
                return mySegments.toArray();
            }
            else
            {
                return new Object[0];
            }
        }

        public boolean hasChildren(Object element)
        {
            return element == fInput || element instanceof PackageSegment;
        }

        public Object getParent(Object element)
        {
            if (element instanceof Segment)
            {
                Segment segment = (Segment) element;
                Object parent = segment.getParent();
                if (parent == null)
                {
                    return fInput;
                }
                else 
                {
                    return parent;
                }
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
                return mySegments.toArray();
            }
            else if (element instanceof PackageSegment)
            {
                PackageSegment pkgSegment = (PackageSegment) element;
                return pkgSegment.getContainedSegments().toArray(); 
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
            // Fix to cause all outlines to have the tree opened at all times
            viewer.expandAll();
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
