package plsqleditor.actions;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.PlsqleditorPlugin;
import plsqleditor.db.PackageType;
import plsqleditor.db.SQLErrorDetail;

public class RefreshErrorStatusAction extends LoadToDatabaseAction
{
    /**
     * Constructor for Action1.
     */
    public RefreshErrorStatusAction(ResourceBundle bundle, String prefix, ITextEditor editor)
    {
        super(bundle, prefix, editor);
    }

    protected SQLErrorDetail[] execute(String toLoad,
                                       String packageName,
                                       PackageType type,
                                       String schema)
    {
        IProject project = PlsqleditorPlugin.getDefault().getProject();

        return theLoadPackageManager.getErrorDetails(project, schema, packageName, type);
    }

    protected String getRuntimeErrorMsg(RuntimeException e)
    {
        // fix for bug 1436209 - the "refresh error status" prints wrong msg
        return "There was a failure while trying to retrieve the error status: " + e;
    }

    protected String getNoErrorMsg()
    {
        // fix for bug 1436209 - the "refresh error status" prints wrong msg
        return "The file has no errors.";
    }
}
