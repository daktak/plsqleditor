package plsqleditor.objects;

import java.io.PrintStream;
import java.io.PrintWriter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import plsqleditor.PlsqleditorPlugin;

public class PlSqlModelException extends CoreException
{
    CoreException nestedCoreException;

    /**
     * Creates a Java model exception that wrappers the given
     * <code>Throwable</code>. The exception contains a Java-specific status
     * object with severity <code>IStatus.ERROR</code> and the given status
     * code.
     * 
     * @param e the <code>Throwable</code>
     * @param code one of the Java-specific status codes declared in
     *            <code>IJavaModelStatusConstants</code>
     * @see org.eclipse.core.runtime.IStatus#ERROR
     */
    public PlSqlModelException(Throwable e, int code)
    {
        this(new Status(IStatus.ERROR, PlsqleditorPlugin.theId, code, "", e));
    }

    /**
     * Creates a Java model exception for the given <code>CoreException</code>.
     * Equivalent to
     * <code>PlSqlModelException(exception,IJavaModelStatusConstants.CORE_EXCEPTION</code>.
     * 
     * @param exception the <code>CoreException</code>
     */
    public PlSqlModelException(CoreException exception)
    {
        super(exception.getStatus());
        this.nestedCoreException = exception;
    }

    /**
     * Creates a Java model exception for the given Java-specific status object.
     * 
     * @param status the Java-specific status object
     */
    public PlSqlModelException(IStatus status)
    {
        super(status);
    }

    /**
     * Returns the underlying <code>Throwable</code> that caused the failure.
     * 
     * @return the wrappered <code>Throwable</code>, or <code>null</code>
     *         if the direct case of the failure was at the Java model layer
     */
    public Throwable getException()
    {
        if (this.nestedCoreException == null)
        {
            return getStatus().getException();
        }
        else
        {
            return this.nestedCoreException;
        }
    }

    /**
     * Prints this exception's stack trace to the given print stream.
     * 
     * @param output the print stream
     */
    public void printStackTrace(PrintStream output)
    {
        synchronized (output)
        {
            super.printStackTrace(output);
            Throwable throwable = getException();
            if (throwable != null)
            {
                output.print("Caused by: "); //$NON-NLS-1$
                throwable.printStackTrace(output);
            }
        }
    }

    /**
     * Prints this exception's stack trace to the given print writer.
     * 
     * @param output the print writer
     * @since 3.0
     */
    public void printStackTrace(PrintWriter output)
    {
        synchronized (output)
        {
            super.printStackTrace(output);
            Throwable throwable = getException();
            if (throwable != null)
            {
                output.print("Caused by: "); //$NON-NLS-1$
                throwable.printStackTrace(output);
            }
        }
    }

    /*
     * Returns a printable representation of this exception suitable for
     * debugging purposes only.
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("PL/SQL Model Exception: "); //$NON-NLS-1$
        if (getException() != null)
        {
            if (getException() instanceof CoreException)
            {
                CoreException c = (CoreException) getException();
                buffer.append("Core Exception [code "); //$NON-NLS-1$
                buffer.append(c.getStatus().getCode());
                buffer.append("] "); //$NON-NLS-1$
                buffer.append(c.getStatus().getMessage());
            }
            else
            {
                buffer.append(getException().toString());
            }
        }
        else
        {
            buffer.append(getStatus().toString());
        }
        return buffer.toString();
    }
}
