package plsqleditor.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import plsqleditor.PlsqleditorPlugin;

/**
 * 
 */
public class PlSqlDocumentProvider extends TextFileDocumentProvider
{
	public void connect(Object element) throws CoreException
	{
		super.connect(element);

		connectPlsqlPartitioner(element);
	}

	protected IAnnotationModel createAnnotationModel(IFile file)
	{
		return new PlsqlSourceAnnotationModel(file);
	}

	private void connectPlsqlPartitioner(Object input)
	{
		IDocument doc = getDocument(input);
		if (!(doc instanceof IDocumentExtension3)) return; // should never occur

		if (PlSqlPartitionScanner.getPartitioner(doc) == null)
		{
			IDocumentPartitioner partitioner = PlSqlPartitionScanner.createPartitioner(doc);
			((IDocumentExtension3) doc).setDocumentPartitioner(PlsqleditorPlugin.PLSQL_PARTITIONING, partitioner);
			partitioner.connect(doc);
		}
	}
	
	
}
