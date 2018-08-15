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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.model.SourceFile;
import plsqleditor.parsers.PackageSegment;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 *         Created on 22/02/2005
 */
public class PlSqlContentOutlinePage extends ContentOutlinePage
{
    /**
     * This class
     * 
     * @author Toby Zines
     * 
     *         Created on 27/02/2005
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

    protected Object       fInput;
    protected PlSqlEditor  fTextEditor;
    TreeViewer             viewer;
    private Action         sortAction;
    TreePathViewerSorter   myDefaultSorter;
    boolean                myIsSorting   = false;
    private Action         filterFieldsAction;
    protected ViewerFilter myFieldFilter;
    protected boolean      myIsFiltering = false;
    private Segment        myLastCaretSegment;

    class SourceSegmentContentProvider implements ITreeContentProvider
    {
        protected static final String SEGMENTS = "__plsql_segments";
        protected IPositionUpdater    myPositionUpdater;
        protected List<Segment>       mySegments;

        protected SourceSegmentContentProvider()
        {
            myPositionUpdater = new DefaultPositionUpdater(SEGMENTS);
            mySegments = new ArrayList<Segment>(10);
        }

        protected void parse(SourceFile f)
        {
            mySegments.addAll(sortAndFilterSegments(f.getSegments(), f.getDocument()));
        }

        private List<Segment> sortAndFilterSegments(List<?> segments, IDocument document)
        {
            SortedSet<Object> sortedSegments = new TreeSet<Object>();
            sortedSegments.addAll(segments);
            List<Segment> segmentsToReturn = new ArrayList<Segment>();
            for (Iterator<Object> it = sortedSegments.iterator(); it.hasNext();)
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
                            List<Segment> containedSegments = clone.getContainedSegments();
                            List<Segment> sortedContainedSegments = sortAndFilterSegments(clone.getContainedSegments(),
                                                                                 document);
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
                SourceFile f = (SourceFile) oldInput;
                IDocument document = f.getDocument();
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
                SourceFile f = (SourceFile) newInput;
                IDocument document = f.getDocument();
                if (document != null)
                {
                    document.addPositionCategory(SEGMENTS);
                    document.addPositionUpdater(myPositionUpdater);
                    parse(f);
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

        public List<Segment> getSegments()
        {
            return mySegments;
        }
    }

    public PlSqlContentOutlinePage(IDocumentProvider provider, PlSqlEditor editor)
    {
        fTextEditor = editor;
    }

    public void createControl(Composite parent)
    {
        super.createControl(parent);
        viewer = getTreeViewer();
        viewer.setContentProvider(new SourceSegmentContentProvider());
        viewer.setLabelProvider(new PlSqlContentLabelProvider(viewer.getControl().getDisplay()));
        viewer.addSelectionChangedListener(this);
        myDefaultSorter = new TreePathViewerSorter();
        myFieldFilter = new ViewerFilter()
        {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                if (element instanceof Segment)
                {
                    Segment segment = (Segment) element;
                    SegmentType st = segment.getType();
                    return (st.equals(SegmentType.Package_Body) || st.equals(SegmentType.Procedure) || st
                            .equals(SegmentType.Function));
                }
                return true;
            }
        };

        if (fInput != null)
        {
            viewer.setInput(fInput);
            // Fix to cause all outlines to have the tree opened at all times
            viewer.expandAll();
        }
        createActions();
        createMenu();
        createToolbar();
    }

    public void createActions()
    {
        sortAction = new Action("Sort", IAction.AS_CHECK_BOX)
        {

			public void run()
            {
                if (myIsSorting)
                {
                    viewer.setComparator(null);
                    myIsSorting = false;
                }
                else
                {
                    viewer.setComparator(myDefaultSorter);
                    myIsSorting = true;
                }
            }
        };
        sortAction.setImageDescriptor(PlsqleditorPlugin.getImageDescriptor("sort.gif"));

        filterFieldsAction = new Action("Filter Fields", IAction.AS_CHECK_BOX)
        {
            public void run()
            {
                if (myIsFiltering)
                {
                    viewer.removeFilter(myFieldFilter);
                    myIsFiltering = false;
                }
                else
                {
                    viewer.addFilter(myFieldFilter);
                    myIsFiltering = true;
                }
            }
        };
        filterFieldsAction.setImageDescriptor(PlsqleditorPlugin.getImageDescriptor("filter.gif"));

        // Add selection listener.
        // viewer.addSelectionChangedListener(new ISelectionChangedListener()
        // {
        // public void selectionChanged(SelectionChangedEvent event)
        // {
        // updateActionEnablement();
        // }
        // });
    }

    /**
     * Create menu.
     */
    private void createMenu()
    {
        // IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
        // mgr.add(someActionForTheMenu);
    }

    /**
     * Create toolbar.
     */
    private void createToolbar()
    {
        IToolBarManager mgr = getSite().getActionBars().getToolBarManager();
        mgr.add(sortAction);
        mgr.add(filterFieldsAction);
    }

    public void selectionChanged(SelectionChangedEvent event)
    {
        super.selectionChanged(event);
        if (fTextEditor.isSyncingToOutline()) return;
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
        //
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

    public void updateSelection(int caretOffset)
    {
        // check lastCaretSub first to speed up things in the most common case
        // if (myLastCaretSegment == null
        // || caretLine < myLastCaretSegment.getStartLine()
        // || caretLine > myLastCaretSegment.getEndLine())
        if (myLastCaretSegment == null
                || caretOffset < myLastCaretSegment.getOffset()
                || caretOffset > myLastCaretSegment.getLastPosition().getOffset()
                        + myLastCaretSegment.getLastPosition().getLength())
        {
            myLastCaretSegment = null;
            SourceSegmentContentProvider cp = (SourceSegmentContentProvider) getTreeViewer()
                    .getContentProvider();
            if (cp == null)
            {
                return;
            }
            for (Segment seg : cp.getSegments())
            {
                if (caretOffset >= seg.getOffset()
                        && caretOffset <= seg.getLastPosition().getOffset())
                {
                    myLastCaretSegment = seg;
                    break;
                }
                else if (seg instanceof PackageSegment)
                {
                    boolean shouldBreak = false;
                    for (Segment innerSeg : ((PackageSegment) seg).getContainedSegments())
                    {
                        if (caretOffset >= innerSeg.getOffset()
                                && caretOffset <= innerSeg.getLastPosition().getOffset()
                                        + innerSeg.getLastPosition().getLength())
                        {
                            myLastCaretSegment = innerSeg;
                            shouldBreak = true;
                            break;
                        }
                    }
                    if (shouldBreak) break;
                }
            }
        }
        if (myLastCaretSegment != null)
        {
            setSelection(new StructuredSelection(myLastCaretSegment));
        }
        else
            setSelection(StructuredSelection.EMPTY);
    }

}
