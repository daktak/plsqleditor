/*
 * Created on 8/06/2004
 *
 * @version $Id$
 */
package au.com.gts.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

/**
 * This class represents an item that can be placed on a canvas and moved about. The actual moving is done in the canvas
 * code.
 * 
 * @author Toby Zines
 * 
 * Created on 8/06/2004
 */
public class MovableItem
{
    private CLabel             myContainedComponent;
    private Point              myCurrentLocation;
    private List  myLinkedItems = new ArrayList();
    private MouseTrackListener myMouseTrackListener;
    public static final Cursor HAND_CURSOR   = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
    public static final Cursor NO_CURSOR     = new Cursor(Display.getDefault(), SWT.CURSOR_NO);
    public static final Cursor ARROW_CURSOR  = new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);

    /**
     * This constructor creates the contained button and associated layout details, preparing this object to be moved
     * around in the specified <code>parent</code>.
     * 
     * @param parent
     *            The composite in which this movable item will be located.
     * 
     * @param percentageLocation
     *            The left and top percentage start points of the entire space that this item should take up.
     * 
     * @param percentageWidth
     *            The percentage end point of the item.
     * 
     * @param height
     *            The actual height of the object.
     */
    public MovableItem(Composite parent, Point percentageLocation, int percentageWidth, int height)
    {
        myContainedComponent = new CLabel(parent, SWT.SHADOW_OUT | SWT.CENTER); // new Button(parent, SWT.PUSH);
        myContainedComponent.setText("Button");
        FormData formData = new FormData();
        formData.height = height;
        formData.top = new FormAttachment(percentageLocation.y);
        formData.left = new FormAttachment(percentageLocation.x);
        formData.right = new FormAttachment(percentageWidth);
        myContainedComponent.setLayoutData(formData);

        myMouseTrackListener = new MouseTrackListener()
        {
            public void mouseEnter(MouseEvent event)
            {
                mouseEnteredEvent(event);
            }

            public void mouseExit(MouseEvent event)
            {
                mouseExittedEvent(event);
            }

            public void mouseHover(MouseEvent event)
            {
                mouseHoveredEvent(event);
            }
        };

        setDefaultMouseTrackListenerEnabled(true);
    }

    /**
     * This method is executed when a mouse enters the widget that this item is proxying.
     * 
     * @param event
     *            The mouse event.
     */
    public void mouseEnteredEvent(MouseEvent event)
    {
        myContainedComponent.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        if ((event.stateMask & SWT.CTRL) != 0)
        {
            myContainedComponent.setCursor(NO_CURSOR);
        }
        System.out.println("mouseEnter: " + event);
    }

    /**
     * This method is executed when a mouse exits the widget that this item is proxying.
     * 
     * @param event
     *            The mouse event.
     */
    public void mouseExittedEvent(MouseEvent event)
    {
        if (event != null)
        {
            // avoids warning
            myContainedComponent.setCursor(ARROW_CURSOR);
            myContainedComponent.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        }
    }

    /**
     * This method is executed when a mouse hovers over the widget that this item is proxying.
     * 
     * @param event
     *            The mouse event.
     */
    public void mouseHoveredEvent(MouseEvent event)
    {
        if (event != null)
        {
            // avoids warning
        }
    }

    /**
     * This method indicates whether the default mouse track listener should be turned on or not. When the mouse events
     * occur, they are pushed through the {@link #mouseEnteredEvent(MouseEvent)},
     * {@link #mouseExittedEvent(MouseEvent)} and {@link #mouseHoveredEvent(MouseEvent)} methods.
     * 
     * @param isOn
     *            If this is true, the listener will be active, otherwise it will not
     */
    public void setDefaultMouseTrackListenerEnabled(boolean isOn)
    {
        myContainedComponent.removeMouseTrackListener(myMouseTrackListener);
        if (isOn)
        {
            myContainedComponent.addMouseTrackListener(myMouseTrackListener);
        }
    }

    /**
     * @return The image for this item.
     */
    public Image getImage()
    {
        return myContainedComponent.getImage();
    }

    /**
     * @return The location of this item.
     */
    public Point getLocation()
    {
        if (myCurrentLocation == null)
        {
            myCurrentLocation = myContainedComponent.getLocation();
        }
        return myCurrentLocation;
    }

    /**
     * @param arg0
     */
    public void setImage(Image arg0)
    {
        myContainedComponent.setImage(arg0);
    }

    /**
     * @param point
     */
    public void setLocation(Point point)
    {
        if (myCurrentLocation == null)
        {
            myCurrentLocation = point;
        }
        else
        {
            int xdiff = point.x - myCurrentLocation.x;
            int ydiff = point.y - myCurrentLocation.y;
            myContainedComponent.setLocation(point);
            myCurrentLocation = point;
            FormData fld = (FormData) myContainedComponent.getLayoutData();
            FormAttachment fa = fld.left;
            fa.offset = fa.offset + xdiff;
            fa = fld.right;
            fa.offset = fa.offset + xdiff;
            fa = fld.top;
            fa.offset = fa.offset + ydiff;
        }
    }

    /**
     * @return The bounds of this item.
     */
    public Rectangle getBounds()
    {
        return myContainedComponent.getBounds();
    }

    /**
     * @param x
     * @param y
     */
    public void setLocation(int x, int y)
    {
        setLocation(new Point(x, y));
    }

    /**
     * @param x
     * @param y
     * @return The display point location of the supplied point relative to this item.
     */
    public Point toDisplay(int x, int y)
    {
        return myContainedComponent.toDisplay(x, y);
    }

    /**
     * @param arg0
     * @return The display point location of the supplied point relative to this item.
     */
    public Point toDisplay(Point arg0)
    {
        return myContainedComponent.toDisplay(arg0);
    }

    public Widget getContainedComponent()
    {
        return myContainedComponent;
    }

    /**
     * @param arg0
     */
    public void setBounds(Rectangle arg0)
    {
        myContainedComponent.setBounds(arg0);
    }

    /**
     * @param mi
     */
    public void link(MovableItem mi)
    {
        myLinkedItems.add(mi);
    }

    public MovableItem[] getLinks()
    {
        return (MovableItem[]) myLinkedItems.toArray(new MovableItem[myLinkedItems.size()]);
    }

    /**
     * This method gets the centre of the label.
     * 
     * @return The centre of this item.
     */
    public Point getCentre()
    {
        Point loc = new Point(getLocation().x, getLocation().y);
        Rectangle bounds = getBounds();
        loc.x += bounds.width / 2;
        loc.y += bounds.height / 2;
        return loc;
    }

    /**
     * @param event
     */
    public void addMouseListener(MouseListener event)
    {
        myContainedComponent.addMouseListener(event);
    }

    /**
     * @param event
     */
    public void addMouseMoveListener(MouseMoveListener event)
    {
        myContainedComponent.addMouseMoveListener(event);
    }

    /**
     * This method indicates whether it is possible for the specified <code>movableItem</code> to link to this object.
     * 
     * @param movableItem
     *            The item that is trying to link to this.
     * 
     * @return true if the specified item can link to this, false otherwise.
     */
    public boolean canBeLinkedToBy(MovableItem movableItem)
    {
        if (movableItem != null && !movableItem.myLinkedItems.contains(this))
        {
            return true;
        }
        return false;
    }

    /**
     * @param arg0
     */
    public void addMouseTrackListener(MouseTrackListener arg0)
    {
        myContainedComponent.addMouseTrackListener(arg0);
    }

    /**
     * @param arg0
     */
    public void setCursor(Cursor arg0)
    {
        myContainedComponent.setCursor(arg0);
    }
}
