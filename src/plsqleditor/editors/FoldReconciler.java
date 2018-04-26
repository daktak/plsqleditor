package plsqleditor.editors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.core.util.StatusFactory;
import plsqleditor.editors.model.ISourceElement;
import plsqleditor.editors.model.SourceFile;
import plsqleditor.parsers.Segment;
import plsqleditor.preferences.PreferenceConstants;

/**
 * Responsible for keeping folds in sync with a source file's text within a
 * PlSqlEditor. This class relies on
 * {@link plsqleditor.editors.model.SourceFile} to obtain positions of foldable
 * {@link ISourceElement}s.
 * 
 *@author jploski
 */
public class FoldReconciler
{
	// ~ Instance fields

	private final PlSqlEditor editor;

	private final Set<Tuple> folds; // of annotations

	private boolean initialized = false;

	// ~ Constructors

	/**
	 * Creates a FoldReconciler for the given editor.
	 */
	public FoldReconciler(PlSqlEditor editor)
	{
		this.editor = editor;
		this.folds = new HashSet<Tuple>();
	}

	// ~ Methods

	/**
	 * Updates folds based on the current state of the editor's SourceFile. An
	 * invocation results in removing/adding zero or more fold annotations to
	 * the document's annotation model.
	 * 
	 * @see PerlEditor#getSourceFile()
	 * @see org.eclipse.jface.text.source.projection.ProjectionAnnotationModel
	 */
	public void reconcile()
	{
		if (!isFoldingEnabled())
		{
			return;
		}

		try
		{
			IAnnotationModel annotations = getAnnotations();
			if (annotations == null)
			{
				return;
			}

			Set<Tuple> tuples = computeFoldPositions();

			removeFolds(tuples);
			addFolds(tuples);

			/*
			 * this should probably be handled via some kind of initialization
			 * that occurs in the constructor to set up the initial folds. due
			 * to the way the editor calls this method after the class has been
			 * instanciated, this achieves the desired behavior
			 */
			initialized = true;
		}
		catch (BadLocationException e)
		{
			// this one should never occur
			String pluginId = PlsqleditorPlugin.getPluginId();
			getLog().log(
					StatusFactory.createError(pluginId,
							"Unexpected exception; report it as "
									+ "a bug in plug-in " + pluginId, e));
		}
	}

	protected ILog getLog()
	{
		return PlsqleditorPlugin.getDefault().getLog();
	}

	protected SourceFile getSourceFile()
	{
		return editor.getSourceFile();
	}

	/**
	 * Adds the specified set of new folds to the document's annotation model
	 * (and the <code>
     * folds</code> instance variable).
	 * 
	 * @param tuples
	 *            <code>Tuple</code> instances representing new folds
	 */
	private void addFolds(Set<Tuple> tuples)
	{
		for (Tuple t : tuples)
		{
			if (!folds.contains(t))
			{
				getAnnotations().addAnnotation(t.annotation, t.position);
				folds.add(t);
			}
		}
	}

	/**
	 * Computes fold positions for <code>SourceElement</code>s
	 */
	private Set<Tuple> computeFoldPositions() throws BadLocationException
	{
		HashSet<Tuple> tuples = new HashSet<Tuple>();

		computeFoldPositions(tuples, getSourceFile().getPlDocs(),
				initialized ? false : isFoldPldoc());

		computeFoldPositions(tuples, getSourceFile().getMethods(),
				initialized ? false : isFoldMethods());

		// TODO: add new fold position computations here

		return tuples;
	}

	/**
	 * Computes fold elements for a given collection of
	 * <code>SourceElement</code>s
	 * 
	 * @param tuples
	 *            object <code>Tuple</code>s representing folds will be added to
	 * @param elements
	 *            iterator for a collection of <code>SourceElement</code>s
	 * @param collapse
	 *            true if fold is initially collapsed, false otherwise
	 */
	private void computeFoldPositions(Set<Tuple> tuples,
			Iterator<Segment> elements, boolean collapse)
			throws BadLocationException
	{
		//IDocument doc = getSourceFile().getDocument();
		while (elements.hasNext())
		{
			// IMultilineElement e = (IMultilineElement) elements.next();
			//
			// if (e.getStartLine() == e.getEndLine())
			// {
			// continue;
			// }
			//
			// int offset = doc.getLineOffset(e.getStartLine());
			// int length = doc.getLineOffset(e.getEndLine()) - offset
			// + doc.getLineLength(e.getEndLine());

			Segment e = elements.next();
			int offset = e.getPosition().offset;
			int length = e.getLength();
			/*
			 * store the position and annotation - the position is
			 * needed to create the fold, while the annotation is needed
			 * to remove it
			 */
			Tuple t = new Tuple(new Position(offset, length),
					new ProjectionAnnotation(collapse));
			tuples.add(t);
		}
	}

	/**
	 * @return the annotation model used for adding/removing folds
	 */
	protected IAnnotationModel getAnnotations()
	{
		return (IAnnotationModel) editor
				.getAdapter(ProjectionAnnotationModel.class);
	}

	protected boolean getPreference(String name)
	{
		IPreferenceStore store = PlsqleditorPlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(name);
	}

	private boolean isFoldingEnabled()
	{
		return getPreference(PreferenceConstants.SOURCE_FOLDING);
	}

	private boolean isFoldPldoc()
	{
		return getPreference(PreferenceConstants.PLDOC_FOLDING);
	}

	private boolean isFoldMethods()
	{
		return getPreference(PreferenceConstants.METHOD_FOLDING);
	}

	/**
	 * Removes no longer required folds from the document's annotation model
	 * (and the <code>
     * folds</code> instance variable). Removes positions of existing
	 * required folds from the argument set.
	 * 
	 * @param toRemove
	 *            the set of Tuple instances representing required positions of
	 *            folds according to the current SourceFile of the editor; this
	 *            set is updated by the method
	 */
	private void removeFolds(Set<Tuple> toRemove)
	{
		for (Tuple t : folds)
		{
			Position p = getAnnotations().getPosition(t.annotation);

			if ((p != null) && (p.isDeleted() || !toRemove.contains(t)))
			{
				getAnnotations().removeAnnotation(t.annotation);
				folds.remove(t);
			}
			else
			{
				// filter out any tuple instances that already exist
				toRemove.remove(t);
			}
		}
	}

	// ~ Inner Classes

	/**
	 *Fold data container
	 */
	private class Tuple
	{
		Annotation annotation;
		Position position;

		Tuple(Position p, Annotation a)
		{
			this.position = p;
			this.annotation = a;
		}

		/*
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (obj == null)
			{
				return false;
			}

			if (getClass() != obj.getClass())
			{
				return false;
			}

			final Tuple other = (Tuple) obj;
			if (position == null)
			{
				if (other.position != null)
				{
					return false;
				}
			}
			else if (!position.equals(other.position))
			{
				return false;
			}

			return true;
		}

		/*
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			final int PRIME = 31;
			int result = 1;
			result = (PRIME * result)
					+ ((position == null) ? 0 : position.hashCode());
			return result;
		}
	}

}
