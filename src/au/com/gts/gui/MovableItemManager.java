/*
 * Created on 8/06/2004
 *
 * @version $Id$
 */
package au.com.gts.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

/**
 * This class
 * 
 * @author Toby Zines
 * 
 * Created on 8/06/2004
 */
public class MovableItemManager
{
    /**
     * This is the list of movable items.
     */
    private List<MovableItem> myMovableItems;

    /**
     * This is the item that a user has currently selected for moving and is operating on (with a
     * down pressed mouse button).
     */
    private MovableItem       myCurrentlyMovingItem;

    /**
     * This is the item that a user has currently selected for linking and is operating on (with a
     * down pressed mouse button).
     */
    private MovableItem       myCurrentlyLinkingItem;

    /**
     * This is the offset from the starting point of the drag.
     */
    private Point             myCurrentlyCapturedOffset;

    /**
     * This is the current point to which a line should be linked.
     */
    private Point             myCurrentLinkPoint;

    /**
     * This constructor creates the manager, initialising the list of movable items.
     */
    public MovableItemManager()
    {
        myMovableItems = new ArrayList<MovableItem>();
    }

    /**
     * This method adds another movable item to the list.
     * 
     * @param mi
     */
    public void add(MovableItem mi)
    {
        myMovableItems.add(mi);
    }

    /**
     * This method captures a mouse click, and identifies the movable item that was captured,
     * setting it up for being moved.
     * 
     * @param button
     *            The button in whose coordinate space the mouse event was captured.
     * 
     * @param x
     *            The x coordinate of the mouse event.
     * 
     * @param y
     *            The y coordinate of the mouse event.
     */
    public void captureItemForMove(Widget button, int x, int y)
    {
        MovableItem mi = getItemForWidget(button);
        if (mi != null)
        {
            myCurrentlyMovingItem = mi;
            myCurrentlyCapturedOffset = new Point(x, y);
        }
    }

    /**
     * This method captures a mouse click, and identifies the movable item that was captured,
     * setting it up for being linked.
     * 
     * @param button
     *            The button in whose coordinate space the mouse event was captured.
     * 
     * @param x
     *            The x coordinate of the mouse event.
     * 
     * @param y
     *            The y coordinate of the mouse event.
     */
    public void captureItemForLink(Widget button, int x, int y)
    {
        MovableItem mi = getItemForWidget(button);
        if (mi != null)
        {
            myCurrentlyLinkingItem = mi;
            myCurrentlyCapturedOffset = new Point(x, y);
        }
    }

    /**
     * This method moves the currently captured item to the newly specified location, specified in
     * the coordinates used to capture the item.
     * 
     * @param x
     * @param y
     */
    public void moveCapturedItem(Canvas canvas, int x, int y)
    {
        if (myCurrentlyMovingItem != null)
        {
            // System.out.println("x = " + x + ", y = " + y);
            Point pt = myCurrentlyCapturedOffset;
            Point pt2 = myCurrentlyMovingItem.getLocation();
            myCurrentlyMovingItem.setLocation(pt2.x + (x - pt.x), pt2.y + (y - pt.y));
            canvas.redraw();
        }
        else if (myCurrentlyLinkingItem != null)
        {
            Point pt2 = myCurrentlyLinkingItem.getCentre();
            canvas.redraw();
            Point loc = myCurrentlyLinkingItem.getLocation();
            myCurrentLinkPoint = new Point(loc.x + x, loc.y + y);
            drawline(canvas, myCurrentLinkPoint, pt2);
        }
    }

    /**
     * This method releases the captured item back into the list of all movable items.
     */
    public void releaseCapturedItem(int x, int y)
    {
        MovableItem mi = getItemForLocationFromLinkingObject(x, y);
        if (mi != null)
        {
            if (mi.canBeLinkedToBy(myCurrentlyLinkingItem))
            {
                myCurrentlyLinkingItem.link(mi);
            }
        }

        myCurrentlyMovingItem = null;
        myCurrentlyLinkingItem = null;
        myCurrentlyCapturedOffset = null;
        myCurrentLinkPoint = null;
    }

    private void drawline(Drawable canvas, Point start, Point finish)
    {
        GC gc = new GC(canvas);
        gc.setLineWidth(1);
        gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        gc.drawLine(start.x, start.y, finish.x, finish.y);
        gc.dispose();
    }

    /**
     * 
     */
    public void drawlinks(Canvas canvas)
    {
        if (myCurrentLinkPoint != null)
        {
            drawline(canvas, myCurrentLinkPoint, myCurrentlyLinkingItem.getCentre());
        }

        GC gc = new GC(canvas);
        gc.setLineWidth(1);
        gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        for (Iterator<MovableItem> it = myMovableItems.iterator(); it.hasNext();)
        {
            MovableItem mi = (MovableItem) it.next();
            Point start = mi.getCentre();
            MovableItem[] mis = mi.getLinks();

            for (int i = 0; i < mis.length; i++)
            {
                Point finish = mis[i].getCentre();
                gc.drawLine(start.x, start.y, finish.x, finish.y);
            }
        }
        gc.dispose();
    }

    /**
     * @param listener
     */
    public void addMouseListener(MouseListener listener)
    {
        for (Iterator<MovableItem> it = myMovableItems.iterator(); it.hasNext();)
        {
            MovableItem mi = (MovableItem) it.next();
            mi.addMouseListener(listener);
        }
    }

    /**
     * @param listener
     */
    public void addMouseMoveListener(MouseMoveListener listener)
    {
        for (Iterator<MovableItem> it = myMovableItems.iterator(); it.hasNext();)
        {
            MovableItem mi = (MovableItem) it.next();
            mi.addMouseMoveListener(listener);
        }
    }

    /**
     * @param listener
     */
    public void addMouseTrackListener(MouseTrackListener listener)
    {
        for (Iterator<MovableItem> it = myMovableItems.iterator(); it.hasNext();)
        {
            MovableItem mi = (MovableItem) it.next();
            mi.addMouseTrackListener(listener);
        }
    }

    /**
     * @return the movable item that is currently in the process of a linking operation.
     */
    public MovableItem getLinkingObject()
    {
        return myCurrentlyLinkingItem;
    }

    /**
     * @param widget
     * @return the movabe item for the specified widget.
     */
    public MovableItem getItemForWidget(Widget widget)
    {
        for (Iterator<MovableItem> it = myMovableItems.iterator(); it.hasNext();)
        {
            MovableItem mi = (MovableItem) it.next();
            if (mi.getContainedComponent().equals(widget))
            {
                return mi;
            }
        }
        return null;
    }

    /**
     * 
     * @param x
     * @param y
     * @return The item at the specified location (x and y) relative to the movable it that is
     *         currently involved in a linking operation.
     */
    public MovableItem getItemForLocationFromLinkingObject(int x, int y)
    {
        if (myCurrentlyLinkingItem != null)
        {
            for (Iterator<MovableItem> it = myMovableItems.iterator(); it.hasNext();)
            {
                MovableItem mi = (MovableItem) it.next();
                if (mi.equals(myCurrentlyLinkingItem))
                {
                    continue;
                }
                Rectangle rect = mi.getBounds();
                Point loc = myCurrentlyLinkingItem.getLocation();
                Point realLocation = new Point(loc.x + x, loc.y + y);

                if (rect.contains(realLocation))
                {
                    return mi;
                }
            }
        }
        return null;
    }
}
