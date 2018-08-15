package plsqleditor.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;

import plsqleditor.parsers.AbstractPlSqlParser;
import plsqleditor.parsers.PackageBodyParser;
import plsqleditor.parsers.StringLocationMap;
import plsqleditor.stores.TaskListIdentityStore;

public class PlSqlReconcilingStrategy
        implements
            IReconcilingStrategy,
            IReconcilingStrategyExtension
{

    private static final String FOLDING_TAGS = "FoldingTags";

    private static final String TODO_TAGS    = "TodoTags";

    PlSqlEditor                 editor;

    private IDocument           fDocument;

    /** The offset of the next character to be read */
    protected int               fOffset;

    /** The end offset of the range to be scanned */
    protected int               fRangeEnd;

    /**
     * next character position - used locally and only valid while
     * {@link #calculatePositions()} is in progress.
     */
    protected int               cNextPos     = 0;

    Map<String, ArrayList>                         myPositionsTable;
    private int[]               myTodoIndices;

    public PlSqlReconcilingStrategy()
    {
        myPositionsTable = new HashMap<String, ArrayList>();
        myPositionsTable.put(FOLDING_TAGS, new ArrayList<Object>());
        myPositionsTable.put(TODO_TAGS, new ArrayList<Object>());

        myTodoIndices = new int[TaskListIdentityStore.instance().getMarkers().size()];
    }

    /**
     * @return Returns the editor.
     */
    public PlSqlEditor getEditor()
    {
        return editor;
    }

    public void setEditor(PlSqlEditor editor)
    {
        this.editor = editor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
     */
    public void setDocument(IDocument document)
    {
        this.fDocument = document;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion,
     *      org.eclipse.jface.text.IRegion)
     */
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion)
    {
        initialReconcile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
     */
    public void reconcile(IRegion partition)
    {
        initialReconcile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void setProgressMonitor(IProgressMonitor monitor)
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
     */
    public void initialReconcile()
    {
        fOffset = 0;
        fRangeEnd = fDocument.getLength();
        calculatePositions();
    }

    /**
     * uses {@link #fDocument}, {@link #fOffset} and {@link #fRangeEnd} to
     * calculate {@link #fPositions}. About syntax errors: this method is not a
     * validator, it is useful.
     */
    protected void calculatePositions()
    {
        for (Iterator<ArrayList> it = myPositionsTable.values().iterator(); it.hasNext();)
        {
            List<?> list = it.next();
            list.clear();
        }
        cNextPos = fOffset;

        try
        {
            processFile(false);
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }

        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
            	if (editor != null)
            	{
	                List<Position> positions = myPositionsTable.get(FOLDING_TAGS);
	                editor.updateFoldingStructure(positions);
	                positions = myPositionsTable.get(TODO_TAGS);
	                editor.updateTodoTags(positions);
            	}
            }

        });
    }

    /**
     * emits tokens to {@link #fPositions}.
     * 
     * @return The line on which the end token was discovered.
     * 
     * @throws BadLocationException
     */
    protected int processFile(boolean isRecursiveCall) throws BadLocationException
    {
        int currentLine = 0;
        boolean isCommenting = false;
        int maxTodoIndex = -1;
        while (cNextPos < fRangeEnd)
        {
            currentLine = fDocument.getLineOfOffset(cNextPos);
            int lineLength = fDocument.getLineLength(currentLine);
            int startPoint = cNextPos = fDocument.getLineOffset(currentLine);
            String line = fDocument.get(cNextPos, lineLength).replaceAll(PlSqlEditor.NEW_LINE, "");
            cNextPos += lineLength;
            if (AbstractPlSqlParser.isStartingComment(line)
                    && !PackageBodyParser.isStringInQuotes(line, ".*(/\\*).*"))
            {
                isCommenting = true;
            }
            else
            {
                // Supports 1445252 - fixme and todo functionality
                int index = 0;
                maxTodoIndex = setupTodoIndices(line, index);
                int commentIndex = StringLocationMap.getUnquotedIndexOfString(line, "(--)", 0);
                if (isCommenting)
                {
                    if (AbstractPlSqlParser.isEndingComment(line))
                    {
                        isCommenting = false;
                    }
                    else
                    {
                        addTodoTasks(startPoint, line);
                    }
                }
                else if (maxTodoIndex != -1 && (commentIndex != -1 && commentIndex < maxTodoIndex))
                {
                    addTodoTasks(startPoint, line);
                }
                else if (line.startsWith(PlSqlEditor.FOLD_START))
                {
                    while (cNextPos < fRangeEnd)
                    {
                        currentLine = fDocument.getLineOfOffset(cNextPos);
                        lineLength = fDocument.getLineLength(currentLine);
                        cNextPos = fDocument.getLineOffset(currentLine);
                        line = fDocument.get(cNextPos, lineLength);
                        cNextPos += lineLength;
                        if (line.startsWith(PlSqlEditor.FOLD_END))
                        {
                            int endLineLength = fDocument.getLineLength(currentLine);
                            int endLineOffset = fDocument.getLineOffset(currentLine);
                            emitPosition(startPoint,
                                         endLineOffset + endLineLength - startPoint,
                                         FOLDING_TAGS);
                            if (isRecursiveCall)
                            {
                                return currentLine;
                            }
                            break;
                        }
                        else if (line.startsWith(PlSqlEditor.FOLD_START))
                        {
                            cNextPos -= lineLength;
                            currentLine = processFile(true);
                        }
                        else
                        {
                            maxTodoIndex = setupTodoIndices(line, index);
                            commentIndex = StringLocationMap.getUnquotedIndexOfString(line,
                                                                                      "(--)",
                                                                                      0);
                            if (maxTodoIndex != -1
                                    && (commentIndex != -1 && commentIndex < maxTodoIndex))
                            {
                                addTodoTasks(cNextPos - lineLength, line);
                            }
                        }
                    }
                }
            }
        }
        return currentLine;
    }

    private int setupTodoIndices(String line, int index)
    {
        int maxTodoIndex = -1;
        for (Iterator<String> it = TaskListIdentityStore.instance().getMarkers().keySet().iterator(); it
                .hasNext();)
        {
            String marker = (String) it.next();
            int currentTodoIndex = StringLocationMap.getUnquotedIndexOfString(line, "(" + marker
                    + ")", 0);
            myTodoIndices[index++] = currentTodoIndex;
            maxTodoIndex = Math.max(maxTodoIndex, currentTodoIndex);
        }
        return maxTodoIndex;
    }

    private void addTodoTasks(int startPoint, String line)
    {
        for (int i = 0; i < myTodoIndices.length; i++)
        {
            int todoIndex = myTodoIndices[i];
            if (todoIndex != -1)
            {
                emitPosition(startPoint + todoIndex,
                             line.substring(todoIndex).trim().length(),
                             TODO_TAGS);
            }
        }
    }

    protected void emitPosition(int startOffset, int length, String type)
    {
        List<Position> positionsList = myPositionsTable.get(type);
        if (positionsList != null)
        {
            positionsList.add(new Position(startOffset, length));
        }
    }
}
