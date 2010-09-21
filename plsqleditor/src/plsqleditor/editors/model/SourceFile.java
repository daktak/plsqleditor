package plsqleditor.editors.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.parsers.PackageSegment;
import plsqleditor.parsers.Segment;
import plsqleditor.parsers.SegmentType;

/**
 * A parsed plsql source file. This class provides access to
 * {@link org.epic.core.model.ISourceElement}s recognised in a plsql source
 * file.
 * 
 */
public class SourceFile
{
	private final List<ISourceFileListener> listeners = new ArrayList<ISourceFileListener>(
			1);
	private final IDocument doc;
	private final IFile file;
	private List<Segment> mySegments;

	/**
	 * Creates a SourceFile which will be reflecting contents of the given
	 * source document. As a second step of initialisation, {@link #parse} has
	 * to be called.
	 */
	public SourceFile(IDocument doc, IFile file)
	{
		assert doc != null;
		assert file != null;
		this.file = file;
		this.doc = doc;
		this.mySegments = Collections.emptyList();
	}

	/**
	 * Adds a listener for changes of this SourceFile. Has no effect if an
	 * identical listener is already registered.
	 */
	public synchronized void addListener(ISourceFileListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * @return the source document based on which this SourceFile was created;
	 *         note that depending on the time when this method is called, the
	 *         document may be more up to date than the information provided by
	 *         this SourceFile instance
	 */
	public IDocument getDocument()
	{
		return doc;
	}

	public List<Segment> getSegments()
	{
		return mySegments;
	}
	
	/**
	 * @return an iterator over {@link Subroutine} instances representing
	 *         subroutines found in the source, in their original order
	 */
	public Iterator<Segment> getMethods()
	{
		return getMethods(mySegments).iterator();
	}
	
	private List<Segment> getMethods(List<Segment> segments)
	{
		List<Segment> toReturn = new ArrayList<Segment>();
		for (Segment segment : segments)
		{
			if (segment instanceof PackageSegment)
			{
				PackageSegment pkgSegment = (PackageSegment) segment;
				List<Segment> containedSegments = pkgSegment
						.getContainedSegments();
				toReturn.addAll(getMethods(containedSegments));
			}
			else
			{
				SegmentType type = segment.getType();
				if (type == SegmentType.Function || type == SegmentType.Procedure)
				{
					toReturn.add(segment);
				}
			}
		}
		return toReturn;
	}

	public Iterator<Segment> getPlDocs()
	{
		return new ArrayList<Segment>().iterator();
	}

	public synchronized void parse()
	{
		this.mySegments = PlsqleditorPlugin.getDefault().getSegments(file, doc, false);
		// TODO eventually the primary parse should be in here.
		// after that the content outline can get the segments from here
		// and anyone else who needs them can get them from here
		fireSourceFileChanged();
	}

	/**
	 * Removes the given listener from this SourceFile. Has no affect if an
	 * identical listener is not registered.
	 */
	public synchronized void removeListener(ISourceFileListener listener)
	{
		listeners.remove(listener);
	}

	private void fireSourceFileChanged()
	{
		for (ISourceFileListener listener : listeners)
		{
			listener.sourceFileChanged(this);
		}
	}

}
