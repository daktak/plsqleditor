/**
 * 
 */
package plsqleditor.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Event;

/**
 * This class 
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 16/03/2005 
 *
 */
public class FileHoldingEvent extends Event
{
    /**
     * This class 
     * 
     * @author Toby Zines
     * 
     * @version $Id$
     * 
     * Created on 16/03/2005 
     *
     */
    public static class CatchableRuntimeException extends RuntimeException
    {
        /**
         * This field represents the serial version uid. 
         */
        private static final long serialVersionUID = 3257850965389816376L;
        
        private Exception myException;

        public CatchableRuntimeException(Exception e)
        {
            myException = e;
        }

        /**
         * This method returns the exception.
         * 
         * @return {@link #myException}.
         */
        public Exception getException()
        {
            return myException;
        }
    }

    private IFile myFile;
    private String myHeaderName;
    
    public FileHoldingEvent(IFile file)
    {
        myFile = file;
    }

    /**
     * This method returns the file.
     * 
     * @return {@link #myFile}.
     */
    public IFile getFile()
    {
        return myFile;
    }

    /**
     * @param string
     */
    public void setHeaderName(String headerName)
    {
        myHeaderName = headerName;
    }

    /**
     * This method returns the headerName.
     * 
     * @return {@link #myHeaderName}.
     */
    public String getHeaderName()
    {
        return myHeaderName;
    }
    
    
}
