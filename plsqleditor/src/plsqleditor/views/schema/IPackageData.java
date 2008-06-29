package plsqleditor.views.schema;

/**
 */
public interface IPackageData 
{
    /**
     * This gets the name of the package in question.
     * 
     * @return The name of the package.
     */
    String getName();
    
    /**
     * This gets the type of the package.
     * 
     * @return The type of the package.
     */
    String getType();
    
    /**
     * This method sets the type
     * 
     * @param type The type the package ought to be.
     */
    void setType(String type);
    
    /**
     * This gets the current status of the package - it can be "VALID" or "INVALID".
     * @return
     */
    String getStatus();
    
    /**
     * This method sets the status of the package - it can be "VALID" or "INVALID".
     * 
     * @param status One of "VALID" or "INVALID".
     */
    void setStatus(String status);
}
