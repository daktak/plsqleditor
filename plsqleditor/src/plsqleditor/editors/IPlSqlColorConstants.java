package plsqleditor.editors;

import org.eclipse.swt.graphics.RGB;

public interface IPlSqlColorConstants
{
    public static final RGB PLSQL_COMMENT       = new RGB(240, 0, 0);
    public static final RGB CONSTANT            = new RGB(128, 64, 240);
    public static final RGB OPERATOR            = new RGB(0, 128, 128);
    public static final RGB MULTI_LINE_COMMENT  = new RGB(128, 0, 0);
    public static final RGB SINGLE_LINE_COMMENT = new RGB(128, 128, 0);
    public static final RGB KEYWORD             = new RGB(140, 0, 0);
    public static final RGB NUMBER              = new RGB(255, 0, 0);
    public static final RGB TYPE                = new RGB(0, 0, 240);
    public static final RGB STRING              = new RGB(0, 128, 0);
    public static final RGB DEFAULT             = new RGB(0, 0, 0);
    
    public static final RGB JAVADOC_KEYWORD     = new RGB(0, 128, 0);
    public static final RGB WHITE               = new RGB(255,255,255);
    public static final RGB JAVADOC_TAG         = new RGB(128, 128, 128);
    public static final RGB JAVADOC_LINK        = new RGB(128, 128, 128);
    public static final RGB JAVADOC_DEFAULT     = new RGB(0, 128, 128);
}
