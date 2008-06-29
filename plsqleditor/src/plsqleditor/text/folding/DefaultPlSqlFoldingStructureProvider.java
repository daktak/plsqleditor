package plsqleditor.text.folding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.IProjectionPosition;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.editors.PlSqlEditor;
import plsqleditor.objects.DocumentCharacterIterator;
import plsqleditor.objects.ElementChangedEvent;
import plsqleditor.objects.IElementChangedListener;
import plsqleditor.objects.IMember;
import plsqleditor.objects.IParent;
import plsqleditor.objects.IPlSqlElement;
import plsqleditor.objects.IPlSqlElementDelta;
import plsqleditor.objects.ISourceRange;
import plsqleditor.objects.IType;
import plsqleditor.objects.PlSqlModelException;

/**
 * Updates the projection model of a plsql file.
 */
public class DefaultPlSqlFoldingStructureProvider
        implements
            IProjectionListener
{
    private static final class PlSqlProjectionAnnotation
            extends
                ProjectionAnnotation
    {
        private IPlSqlElement myPlSqlElement;
        private boolean       myIsComment;

        public PlSqlProjectionAnnotation(IPlSqlElement element,
                                         boolean isCollapsed,
                                         boolean isComment)
        {
            super(isCollapsed);
            myPlSqlElement = element;
            myIsComment = isComment;
        }

        public IPlSqlElement getElement()
        {
            return myPlSqlElement;
        }

        public void setElement(IPlSqlElement element)
        {
            myPlSqlElement = element;
        }

        public boolean isComment()
        {
            return myIsComment;
        }

        public void setIsComment(boolean isComment)
        {
            myIsComment = isComment;
        }

        /*
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return "PlSqlProjectionAnnotation:\n" + //$NON-NLS-1$
                    "\telement: \t" + myPlSqlElement.toString() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                    "\tcollapsed: \t" + isCollapsed() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                    "\tcomment: \t" + myIsComment + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static final class Tuple
    {
        PlSqlProjectionAnnotation annotation;
        Position                  position;

        Tuple(PlSqlProjectionAnnotation annotation, Position position)
        {
            this.annotation = annotation;
            this.position = position;
        }
    }

    private class ElementChangedListener implements IElementChangedListener
    {
        /*
         * @see IElementChangedListener#elementChanged(org.eclipse.jdt.core.ElementChangedEvent)
         */
        public void elementChanged(ElementChangedEvent e)
        {
            IPlSqlElementDelta delta = findElement(myInput, e.getDelta());
            if (delta != null) processDelta(delta);
        }

        private IPlSqlElementDelta findElement(IPlSqlElement target,
                                               IPlSqlElementDelta delta)
        {
            if (delta == null || target == null)
            {
                return null;
            }
            IPlSqlElement element = delta.getElement();
            if (target.equals(element))
            {
                return delta;
            }
            IPlSqlElementDelta[] children = delta.getAffectedChildren();
            for (int i = 0; i < children.length; i++)
            {
                IPlSqlElementDelta d = findElement(target, children[i]);
                if (d != null)
                {
                    return d;
                }
            }
            return null;
        }
    }

    /**
     * Projection position that will return two foldable regions: one folding
     * away the region from after the '/**' to the beginning of the content, the
     * other from after the first content line until after the comment.
     * 
     * @since 3.1
     */
    private static final class CommentPosition extends Position
            implements
                IProjectionPosition
    {
        CommentPosition(int offset, int length)
        {
            super(offset, length);
        }

        /*
         * @see org.eclipse.jface.text.source.projection.IProjectionPosition#computeFoldingRegions(org.eclipse.jface.text.IDocument)
         */
        public IRegion[] computeProjectionRegions(IDocument document)
                throws BadLocationException
        {
            DocumentCharacterIterator sequence = new DocumentCharacterIterator(
                    document, offset, offset + length);
            int prefixEnd = 0;
            int contentStart = findFirstContent(sequence, prefixEnd);
            int firstLine = document.getLineOfOffset(offset + prefixEnd);
            int captionLine = document.getLineOfOffset(offset + contentStart);
            int lastLine = document.getLineOfOffset(offset + length);
            Assert.isTrue(firstLine <= captionLine,
                          "first folded line is greater than the caption line"); //$NON-NLS-1$
            Assert.isTrue(captionLine <= lastLine,
                          "caption line is greater than the last folded line"); //$NON-NLS-1$
            IRegion preRegion;
            if (firstLine < captionLine)
            {
                // preRegion= new Region(offset + prefixEnd, contentStart -
                // prefixEnd);
                int preOffset = document.getLineOffset(firstLine);
                IRegion preEndLineInfo = document
                        .getLineInformation(captionLine);
                int preEnd = preEndLineInfo.getOffset();
                preRegion = new Region(preOffset, preEnd - preOffset);
            }
            else
            {
                preRegion = null;
            }
            if (captionLine < lastLine)
            {
                int postOffset = document.getLineOffset(captionLine + 1);
                IRegion postRegion = new Region(postOffset, offset + length
                        - postOffset);
                if (preRegion == null) return new IRegion[]{postRegion};
                return new IRegion[]{preRegion, postRegion};
            }
            if (preRegion != null) return new IRegion[]{preRegion};
            return null;
        }

        /**
         * Finds the offset of the first identifier part within
         * <code>content</code>. Returns 0 if none is found.
         * 
         * @param content the content to search
         * @return the first index of a unicode identifier part, or zero if none
         *         can be found
         */
        private int findFirstContent(final CharSequence content, int prefixEnd)
        {
            int lenght = content.length();
            for (int i = prefixEnd; i < lenght; i++)
            {
                if (Character.isUnicodeIdentifierPart(content.charAt(i))) return i;
            }
            return 0;
        }

        public int computeCaptionOffset(IDocument document)
        {
            // return 0;
            DocumentCharacterIterator sequence = new DocumentCharacterIterator(
                    document, offset, offset + length);
            return findFirstContent(sequence, 0);
        }
    }

    /**
     * Projection position that will return two foldable regions: one folding
     * away the lines before the one containing the simple name of the java
     * element, one folding away any lines after the caption.
     * 
     * @since 3.1
     */
    private static final class PlSqlElementPosition extends Position
            implements
                IProjectionPosition
    {
        private final IMember myMember;

        public PlSqlElementPosition(int offset, int length, IMember member)
        {
            super(offset, length);
            Assert.isNotNull(member);
            myMember = member;
        }

        /*
         * @see org.eclipse.jface.text.source.projection.IProjectionPosition#computeFoldingRegions(org.eclipse.jface.text.IDocument)
         */
        public IRegion[] computeProjectionRegions(IDocument document)
                throws BadLocationException
        {
            int nameStart = offset;
            try
            {
                /*
                 * The member's name range may not be correct. However,
                 * reconciling would trigger another element delta which would
                 * lead to reentrant situations. Therefore, we optimistically
                 * assume that the name range is correct, but double check the
                 * received lines below.
                 */
                ISourceRange nameRange = myMember.getNameRange();
                if (nameRange != null) nameStart = nameRange.getOffset();
            }
            catch (PlSqlModelException e)
            {
                // ignore and use default
            }
            int firstLine = document.getLineOfOffset(offset);
            int captionLine = document.getLineOfOffset(nameStart);
            int lastLine = document.getLineOfOffset(offset + length);
            /*
             * see comment above - adjust the caption line to be inside the
             * entire folded region, and rely on later element deltas to correct
             * the name range.
             */
            if (captionLine < firstLine) captionLine = firstLine;
            if (captionLine > lastLine) captionLine = lastLine;
            IRegion preRegion;
            if (firstLine < captionLine)
            {
                int preOffset = document.getLineOffset(firstLine);
                IRegion preEndLineInfo = document
                        .getLineInformation(captionLine);
                int preEnd = preEndLineInfo.getOffset();
                preRegion = new Region(preOffset, preEnd - preOffset);
            }
            else
            {
                preRegion = null;
            }
            if (captionLine < lastLine)
            {
                int postOffset = document.getLineOffset(captionLine + 1);
                IRegion postRegion = new Region(postOffset, offset + length
                        - postOffset);
                if (preRegion == null) return new IRegion[]{postRegion};
                return new IRegion[]{preRegion, postRegion};
            }
            if (preRegion != null) return new IRegion[]{preRegion};
            return null;
        }

        /*
         * @see org.eclipse.jface.text.source.projection.IProjectionPosition#computeCaptionOffset(org.eclipse.jface.text.IDocument)
         */
        public int computeCaptionOffset(IDocument document)
                throws BadLocationException
        {
            int nameStart = offset;
            try
            {
                // need a reconcile here?
                ISourceRange nameRange = myMember.getNameRange();
                if (nameRange != null) nameStart = nameRange.getOffset();
            }
            catch (PlSqlModelException e)
            {
                // ignore and use default
            }
            return nameStart - offset;
        }
    }
    private IDocument               fCachedDocument;
    private ITextEditor             myEditor;
    private ProjectionViewer        myViewer;
    private IPlSqlElement           myInput;
    private IElementChangedListener fElementListener;
    private boolean                 fAllowCollapsing         = false;
    private boolean                 fCollapseJavadoc         = false;
    private boolean                 fCollapseImportContainer = true;
    private boolean                 fCollapseInnerTypes      = true;
    private boolean                 fCollapseMethods         = false;
    private boolean                 fCollapseHeaderComments  = true;
    /* caches for header comment extraction. */
    private IType                   fFirstType;
    private boolean                 fHasHeaderComment;

    public DefaultPlSqlFoldingStructureProvider()
    {
        //
    }

    public void install(ITextEditor editor, ProjectionViewer viewer)
    {
        if (editor instanceof PlSqlEditor)
        {
            myEditor = editor;
            myViewer = viewer;
            myViewer.addProjectionListener(this);
        }
    }

    public void uninstall()
    {
        if (isInstalled())
        {
            projectionDisabled();
            myViewer.removeProjectionListener(this);
            myViewer = null;
            myEditor = null;
        }
    }

    protected boolean isInstalled()
    {
        return myEditor != null;
    }

    /*
     * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionEnabled()
     */
    public void projectionEnabled()
    {
        // http://home.ott.oti.com/teams/wswb/anon/out/vms/index.html
        // projectionEnabled messages are not always paired with
        // projectionDisabled
        // i.e. multiple enabled messages may be sent out.
        // we have to make sure that we disable first when getting an enable
        // message.
        projectionDisabled();
        if (myEditor instanceof PlSqlEditor)
        {
            initialize();
            fElementListener = new ElementChangedListener();
            PlsqleditorPlugin.addElementChangedListener(fElementListener);
        }
    }

    /*
     * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionDisabled()
     */
    public void projectionDisabled()
    {
        fCachedDocument = null;
        if (fElementListener != null)
        {
            PlsqleditorPlugin.removeElementChangedListener(fElementListener);
            fElementListener = null;
        }
    }

    public void initialize()
    {
        if (!isInstalled()) return;
        initializePreferences();
        try
        {
            IDocumentProvider provider = myEditor.getDocumentProvider();
            fCachedDocument = provider.getDocument(myEditor.getEditorInput());
            fAllowCollapsing = true;
            fFirstType = null;
            fHasHeaderComment = false;
        }
        finally
        {
            fCachedDocument = null;
            fAllowCollapsing = false;
            fFirstType = null;
            fHasHeaderComment = false;
        }
    }

    private void initializePreferences()
    {
        IPreferenceStore store = PlsqleditorPlugin.getDefault()
                .getPreferenceStore();
        // fCollapseInnerTypes = store
        // .getBoolean(PreferenceConstants.EDITOR_FOLDING_INNERTYPES);
        // fCollapseImportContainer = store
        // .getBoolean(PreferenceConstants.EDITOR_FOLDING_IMPORTS);
        // fCollapseJavadoc = store
        // .getBoolean(PreferenceConstants.EDITOR_FOLDING_JAVADOC);
        // fCollapseMethods = store
        // .getBoolean(PreferenceConstants.EDITOR_FOLDING_METHODS);
        // fCollapseHeaderComments = store
        // .getBoolean(PreferenceConstants.EDITOR_FOLDING_HEADERS);
    }

    private Map computeAdditions(IParent parent)
    {
        Map map = new LinkedHashMap(); // use a linked map to maintain ordering
        // of comments
        try
        {
            computeAdditions(parent.getChildren(), map);
        }
        catch (PlSqlModelException x)
        {
            //
        }
        return map;
    }

    private void computeAdditions(IPlSqlElement[] elements, Map map)
            throws PlSqlModelException
    {
        for (int i = 0; i < elements.length; i++)
        {
            IPlSqlElement element = elements[i];
            computeAdditions(element, map);
            if (element instanceof IParent)
            {
                IParent parent = (IParent) element;
                computeAdditions(parent.getChildren(), map);
            }
        }
    }

    private void computeAdditions(IPlSqlElement element, Map map)
    {
        boolean createProjection = false;
        boolean collapse = false;
        switch (element.getElementType())
        {
            case IPlSqlElement.IMPORT_CONTAINER :
                collapse = fAllowCollapsing && fCollapseImportContainer;
                createProjection = true;
                break;
            case IPlSqlElement.TYPE :
                collapse = fAllowCollapsing && fCollapseInnerTypes
                        && isInnerType((IType) element);
                createProjection = true;
                break;
            case IPlSqlElement.METHOD :
                collapse = fAllowCollapsing && fCollapseMethods;
                createProjection = true;
                break;
        }
        if (createProjection)
        {
            IRegion[] regions = computeProjectionRanges(element);
            if (regions != null)
            {
                // comments
                for (int i = 0; i < regions.length - 1; i++)
                {
                    Position position = createProjectionPosition(regions[i],
                                                                 null);
                    boolean commentCollapse;
                    if (position != null)
                    {
                        if (i == 0 && (regions.length > 2 || fHasHeaderComment)
                                && element == fFirstType)
                        {
                            commentCollapse = fAllowCollapsing
                                    && fCollapseHeaderComments;
                        }
                        else
                        {
                            commentCollapse = fAllowCollapsing
                                    && fCollapseJavadoc;
                        }
                        map.put(new PlSqlProjectionAnnotation(element,
                                commentCollapse, true), position);
                    }
                }
                // code
                Position position = createProjectionPosition(regions[regions.length - 1],
                                                             element);
                if (position != null) map.put(new PlSqlProjectionAnnotation(
                        element, collapse, false), position);
            }
        }
    }

    private boolean isInnerType(IType type)
    {
        try
        {
            return type.isMember();
        }
        catch (PlSqlModelException x)
        {
            IPlSqlElement parent = type.getParent();
            if (parent != null)
            {
                int parentType = parent.getElementType();
                return true;//(parentType != IPlSqlElement.COMPILATION_UNIT && parentType != IPlSqlElement.CLASS_FILE);
            }
        }
        return false;
    }

    /**
     * Computes the projection ranges for a given <code>IPlSqlElement</code>.
     * More than one range may be returned if the element has a leading comment
     * which gets folded separately. If there are no foldable regions,
     * <code>null</code> is returned.
     * 
     * @param element the java element that can be folded
     * @return the regions to be folded, or <code>null</code> if there are
     *         none
     */
    private IRegion[] computeProjectionRanges(IPlSqlElement element)
    {
//        try
//        {
//            if (element instanceof ISourceReference)
//            {
//                ISourceReference reference = (ISourceReference) element;
//                ISourceRange range = reference.getSourceRange();
//                String contents = reference.getSource();
//                if (contents == null) return null;
//                List regions = new ArrayList();
//                if (fFirstType == null && element instanceof IType)
//                {
//                    fFirstType = (IType) element;
//                    IRegion headerComment = computeHeaderComment(fFirstType);
//                    if (headerComment != null)
//                    {
//                        regions.add(headerComment);
//                        fHasHeaderComment = true;
//                    }
//                }
//                IScanner scanner = ToolFactory.createScanner(true,
//                                                             false,
//                                                             false,
//                                                             false);
//                scanner.setSource(contents.toCharArray());
//                final int shift = range.getOffset();
//                int start = shift;
//                while (true)
//                {
//                    int token = scanner.getNextToken();
//                    start = shift + scanner.getCurrentTokenStartPosition();
//                    switch (token)
//                    {
//                        case ITerminalSymbols.TokenNameCOMMENT_JAVADOC :
//                        case ITerminalSymbols.TokenNameCOMMENT_BLOCK :
//                        {
//                            int end = shift
//                                    + scanner.getCurrentTokenEndPosition() + 1;
//                            regions.add(new Region(start, end - start));
//                        }
//                        case ITerminalSymbols.TokenNameCOMMENT_LINE :
//                            continue;
//                    }
//                    break;
//                }
//                regions
//                        .add(new Region(start, shift + range.getLength()
//                                - start));
//                if (regions.size() > 0)
//                {
//                    IRegion[] result = new IRegion[regions.size()];
//                    regions.toArray(result);
//                    return result;
//                }
//            }
//        }
//        catch (PlSqlModelException e)
//        {
//        }
//        catch (InvalidInputException e)
//        {
//        }
        return null;
    }

    private IRegion computeHeaderComment(IType type) throws PlSqlModelException
    {
//        if (fCachedDocument == null) return null;
//        // search at most up to the first type
//        ISourceRange range = type.getSourceRange();
//        if (range == null) return null;
//        int start = 0;
//        int end = range.getOffset();
//        if (myInput instanceof ISourceReference)
//        {
//            String content;
//            try
//            {
//                content = fCachedDocument.get(start, end - start);
//            }
//            catch (BadLocationException e)
//            {
//                return null; // ignore header comment in that case
//            }
//            /*
//             * code adapted from CommentFormattingStrategy: scan the header
//             * content up to the first type. Once a comment is found, accumulate
//             * any additional comments up to the stop condition. The stop
//             * condition is reaching a package declaration, import container, or
//             * the end of the input.
//             */
//            IScanner scanner = ToolFactory.createScanner(true,
//                                                         false,
//                                                         false,
//                                                         false);
//            scanner.setSource(content.toCharArray());
//            int headerStart = -1;
//            int headerEnd = -1;
//            try
//            {
//                boolean foundComment = false;
//                int terminal = scanner.getNextToken();
//                while (terminal != ITerminalSymbols.TokenNameEOF
//                        && !(terminal == ITerminalSymbols.TokenNameclass
//                                || terminal == ITerminalSymbols.TokenNameinterface
//                                || terminal == ITerminalSymbols.TokenNameenum || (foundComment && (terminal == ITerminalSymbols.TokenNameimport || terminal == ITerminalSymbols.TokenNamepackage))))
//                {
//                    if (terminal == ITerminalSymbols.TokenNameCOMMENT_JAVADOC
//                            || terminal == ITerminalSymbols.TokenNameCOMMENT_BLOCK
//                            || terminal == ITerminalSymbols.TokenNameCOMMENT_LINE)
//                    {
//                        if (!foundComment) headerStart = scanner
//                                .getCurrentTokenStartPosition();
//                        headerEnd = scanner.getCurrentTokenEndPosition();
//                        foundComment = true;
//                    }
//                    terminal = scanner.getNextToken();
//                }
//            }
//            catch (InvalidInputException ex)
//            {
//                return null;
//            }
//            if (headerEnd != -1)
//            {
//                return new Region(headerStart, headerEnd - headerStart);
//            }
//        }
        return null;
    }

    private Position createProjectionPosition(IRegion region,
                                              IPlSqlElement element)
    {
        if (fCachedDocument == null) return null;
        try
        {
            int start = fCachedDocument.getLineOfOffset(region.getOffset());
            int end = fCachedDocument.getLineOfOffset(region.getOffset()
                    + region.getLength());
            if (start != end)
            {
                int offset = fCachedDocument.getLineOffset(start);
                int endOffset;
                if (fCachedDocument.getNumberOfLines() > end + 1) endOffset = fCachedDocument
                        .getLineOffset(end + 1);
                else if (end > start) endOffset = fCachedDocument
                        .getLineOffset(end)
                        + fCachedDocument.getLineLength(end);
                else
                    return null;
                if (element instanceof IMember)
                {
                    return new PlSqlElementPosition(offset, endOffset - offset,
                            (IMember) element);
                }
                else
                {
                    return new CommentPosition(offset, endOffset - offset);
                }
            }
        }
        catch (BadLocationException x)
        {
        }
        return null;
    }

    protected void processDelta(IPlSqlElementDelta delta)
    {
//        if (!isInstalled()) return;
//        if ((delta.getFlags() & (IPlSqlElementDelta.F_CONTENT | IPlSqlElementDelta.F_CHILDREN)) == 0) return;
//        ProjectionAnnotationModel model = (ProjectionAnnotationModel) myEditor
//                .getAdapter(ProjectionAnnotationModel.class);
//        if (model == null) return;
//        try
//        {
//            IDocumentProvider provider = myEditor.getDocumentProvider();
//            fCachedDocument = provider.getDocument(myEditor.getEditorInput());
//            fAllowCollapsing = false;
//            fFirstType = null;
//            fHasHeaderComment = false;
//            Map additions = new HashMap();
//            List deletions = new ArrayList();
//            List updates = new ArrayList();
//            Map updated = computeAdditions((IParent) myInput);
//            Map previous = createAnnotationMap(model);
//            Iterator e = updated.keySet().iterator();
//            while (e.hasNext())
//            {
//                PlSqlProjectionAnnotation newAnnotation = (PlSqlProjectionAnnotation) e
//                        .next();
//                IPlSqlElement element = newAnnotation.getElement();
//                Position newPosition = (Position) updated.get(newAnnotation);
//                List annotations = (List) previous.get(element);
//                if (annotations == null)
//                {
//                    additions.put(newAnnotation, newPosition);
//                }
//                else
//                {
//                    Iterator x = annotations.iterator();
//                    boolean matched = false;
//                    while (x.hasNext())
//                    {
//                        Tuple tuple = (Tuple) x.next();
//                        PlSqlProjectionAnnotation existingAnnotation = tuple.annotation;
//                        Position existingPosition = tuple.position;
//                        if (newAnnotation.isComment() == existingAnnotation
//                                .isComment())
//                        {
//                            if (existingPosition != null
//                                    && (!newPosition.equals(existingPosition)))
//                            {
//                                existingPosition.setOffset(newPosition
//                                        .getOffset());
//                                existingPosition.setLength(newPosition
//                                        .getLength());
//                                updates.add(existingAnnotation);
//                            }
//                            matched = true;
//                            x.remove();
//                            break;
//                        }
//                    }
//                    if (!matched) additions.put(newAnnotation, newPosition);
//                    if (annotations.isEmpty()) previous.remove(element);
//                }
//            }
//            e = previous.values().iterator();
//            while (e.hasNext())
//            {
//                List list = (List) e.next();
//                int size = list.size();
//                for (int i = 0; i < size; i++)
//                    deletions.add(((Tuple) list.get(i)).annotation);
//            }
//            match(model, deletions, additions, updates);
//            Annotation[] removals = new Annotation[deletions.size()];
//            deletions.toArray(removals);
//            Annotation[] changes = new Annotation[updates.size()];
//            updates.toArray(changes);
//            model.modifyAnnotations(removals, additions, changes);
//        }
//        finally
//        {
//            fCachedDocument = null;
//            fAllowCollapsing = true;
//            fFirstType = null;
//            fHasHeaderComment = false;
//        }
    }

    private void match(ProjectionAnnotationModel model,
                       List deletions,
                       Map additions,
                       List changes)
    {
        if (deletions.isEmpty() || (additions.isEmpty() && changes.isEmpty())) return;
        List newDeletions = new ArrayList();
        List newChanges = new ArrayList();
        Iterator deletionIterator = deletions.iterator();
        outer : while (deletionIterator.hasNext())
        {
            PlSqlProjectionAnnotation deleted = (PlSqlProjectionAnnotation) deletionIterator
                    .next();
            Position deletedPosition = model.getPosition(deleted);
            if (deletedPosition == null) continue;
            Iterator changesIterator = changes.iterator();
            while (changesIterator.hasNext())
            {
                PlSqlProjectionAnnotation changed = (PlSqlProjectionAnnotation) changesIterator
                        .next();
                if (deleted.isComment() == changed.isComment())
                {
                    Position changedPosition = model.getPosition(changed);
                    if (changedPosition == null) continue;
                    if (deletedPosition.getOffset() == changedPosition
                            .getOffset())
                    {
                        deletedPosition.setLength(changedPosition.getLength());
                        deleted.setElement(changed.getElement());
                        deletionIterator.remove();
                        newChanges.add(deleted);
                        changesIterator.remove();
                        newDeletions.add(changed);
                        continue outer;
                    }
                }
            }
            Iterator additionsIterator = additions.keySet().iterator();
            while (additionsIterator.hasNext())
            {
                PlSqlProjectionAnnotation added = (PlSqlProjectionAnnotation) additionsIterator
                        .next();
                if (deleted.isComment() == added.isComment())
                {
                    Position addedPosition = (Position) additions.get(added);
                    if (deletedPosition.getOffset() == addedPosition
                            .getOffset())
                    {
                        deletedPosition.setLength(addedPosition.getLength());
                        deleted.setElement(added.getElement());
                        deletionIterator.remove();
                        newChanges.add(deleted);
                        additionsIterator.remove();
                        break;
                    }
                }
            }
        }
        deletions.addAll(newDeletions);
        changes.addAll(newChanges);
    }

    private Map createAnnotationMap(IAnnotationModel model)
    {
        Map map = new HashMap();
        Iterator e = model.getAnnotationIterator();
        while (e.hasNext())
        {
            Object annotation = e.next();
            if (annotation instanceof PlSqlProjectionAnnotation)
            {
                PlSqlProjectionAnnotation java = (PlSqlProjectionAnnotation) annotation;
                Position position = model.getPosition(java);
                Assert.isNotNull(position);
                List list = (List) map.get(java.getElement());
                if (list == null)
                {
                    list = new ArrayList(2);
                    map.put(java.getElement(), list);
                }
                list.add(new Tuple(java, position));
            }
        }
        Comparator comparator = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Tuple) o1).position.getOffset()
                        - ((Tuple) o2).position.getOffset();
            }
        };
        for (Iterator it = map.values().iterator(); it.hasNext();)
        {
            List list = (List) it.next();
            Collections.sort(list, comparator);
        }
        return map;
    }
}
