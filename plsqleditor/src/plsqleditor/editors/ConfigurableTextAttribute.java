/*
 * Created on 27/02/2005
 *
 * @version $Id$
 */
package plsqleditor.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;

import plsqleditor.PlsqleditorPlugin;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 27/02/2005
 */
public class ConfigurableTextAttribute extends TextAttribute
{
    private String myForegroundString;
    private String myBackGroundString;

    /**
     * This method
     * 
     * @return The preferences store.
     */
    static IPreferenceStore prefs()
    {
        return PlsqleditorPlugin.getDefault().getPreferenceStore();
    }

    static ColorManager cm()
    {
        return PlsqleditorPlugin.getDefault().getPlSqlColorProvider();
    }

    /**
     * @param foreground
     * @param background
     * @param style
     */
    public ConfigurableTextAttribute(String foreground, String background, int style)
    {
        super(cm().getColor(PreferenceConverter.getColor(prefs(), foreground)), cm()
                .getColor(PreferenceConverter.getColor(prefs(), background)), style);
        myForegroundString = foreground;
        myBackGroundString = background;
    }

    /**
     * Returns the attribute's foreground color.
     * 
     * @return the attribute's foreground color
     */
    public Color getForeground()
    {
        return cm().getColor(PreferenceConverter.getColor(prefs(), myForegroundString));
    }

    /**
     * Returns the attribute's background color.
     * 
     * @return the attribute's background color
     */
    public Color getBackground()
    {
        return cm().getColor(PreferenceConverter.getColor(prefs(), myBackGroundString));
    }
}
