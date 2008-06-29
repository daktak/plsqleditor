package plsqleditor.objects;

public interface IPlSqlModel
{

    IPlSqlProject[] getPlSqlProjects() throws PlSqlModelException;

    boolean isOpen();

    void open(Object object);
}
