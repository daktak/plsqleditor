/*
 * Created on 22/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 22/02/2005
 */
public class PlSqlEditorMessages
{
    private PlSqlEditorMessages()
    {
        //
    }

    public static String getString(String key)
    {
        try
        {
            return fgResourceBundle.getString(key);
        }
        catch (MissingResourceException _ex)
        {
            return "!" + key + "!";
        }
    }

    /**
     * Returns the formatted message for the given key in
     * the resource bundle. 
     *
     * @param key the resource name
     * @param args the message arguments
     * @return the string
     */
    public static String format(String key, Object[] args) {
        return MessageFormat.format(getString(key), args);
    }

    public static ResourceBundle getResourceBundle()
    {
        return fgResourceBundle;
    }

    private static final String   RESOURCE_BUNDLE  = PlSqlEditorMessages.class.getName();
    private static ResourceBundle fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);

}
