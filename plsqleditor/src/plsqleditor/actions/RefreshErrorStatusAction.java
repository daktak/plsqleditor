package plsqleditor.actions;

import java.util.ResourceBundle;

import org.eclipse.ui.texteditor.ITextEditor;

import plsqleditor.db.LoadPackageManager;
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
                                       LoadPackageManager.PackageType type,
                                       String schema)
    {
        return myLoadPackageManager.getErrorDetails(schema, packageName, type);
    }

    protected String modifyCodeToLoad(String toLoad, String packageName, String separator)
    {
        return toLoad;
    }
}
