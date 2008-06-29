package plsqleditor.objects;

public interface IPlSqlElement
{

    int PLSQL_PROJECT = 0;
    int IMPORT_CONTAINER = 1;
    int TYPE = 2;
    int METHOD = 3;

    int getElementType();
}
