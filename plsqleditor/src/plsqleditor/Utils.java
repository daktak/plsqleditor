package plsqleditor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import plsqleditor.objects.PlSqlModelException;

public class Utils {
 /**
     * Return the number of rows available in the current display using the
     * current font.
     * @param parent The Composite whose Font will be queried.
     * @return int The result of the display size divided by the font size.
     */
    public static int availableRows(Composite parent) {

        int fontHeight = (parent.getFont().getFontData())[0].getHeight();
        int displayHeight = parent.getDisplay().getClientArea().height;

        return displayHeight / fontHeight;
    }
    /** Asserts that the given boolean is <code>true</code>. If this
     * is not the case, some kind of unchecked exception is thrown.
     *
     * @param expression the outcode of the check
     * @return <code>true</code> if the check passes (does not return
     *    if the check fails)
     */
    public static boolean isTrue(boolean expression) {
        return isTrue(expression, "");//$NON-NLS-1$
    }

    /** Asserts that the given boolean is <code>true</code>. If this
     * is not the case, some kind of unchecked exception is thrown.
     * The given message is included in that exception, to aid debugging.
     *
     * @param expression the outcode of the check
     * @param message the message to include in the exception
     * @return <code>true</code> if the check passes (does not return
     *    if the check fails)
     * @throws AssertionFailedException 
     */
    public static boolean isTrue(boolean expression, String message) {
        if (!expression)
            throw new AssertionFailedException("assertion failed; " + message);//$NON-NLS-1$
        return expression;
    }
    
    /*
     * Add a log entry
     */
    public static void log(Throwable e, String message) {
        Throwable nestedException;
        if (e instanceof PlSqlModelException 
                && (nestedException = ((PlSqlModelException)e).getException()) != null) {
            e = nestedException;
        }
        IStatus status= new Status(
            IStatus.ERROR, 
            PlsqleditorPlugin.theId, 
            IStatus.ERROR, 
            message, 
            e); 
        PlsqleditorPlugin.getDefault().getLog().log(status);
    }   
}
