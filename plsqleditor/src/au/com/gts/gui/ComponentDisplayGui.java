/*
 * @version $Id$
 */
package au.com.gts.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

/**
 * @author zinest
 */
public class ComponentDisplayGui extends Canvas
{
    private static final int RIGHT_CLICK_JITTER = 3;
    public static final String CONTAINED_COMBO_ID   = "ContainedCombo";
    public static final char   DOT                  = '.';
    public static final int    MARGIN_HEIGHT        = 10;
    public static final int    MARGIN_WIDTH         = 10;
    public static final int    NOT_FOUND_INDEX      = -1;

    private Display            myDisplay;
    private MenuItem           myAboutMenuItem;
    private MenuItem           myContentsMenuItem;
    private Menu               myHelpMenu;
    private MenuItem           myHelpMenuItem;
    private MenuItem           myExitMenuItem;
    private MenuItem           myCloseFileMenuItem;
    private MenuItem           mySaveFileMenuItem;
    private MenuItem           newFileMenuItem;
    private MenuItem           myOpenFileMenuItem;
    private Menu               myFileMenu;
    private MenuItem           myFileMenuItem;
    private Menu               myMainMenu;

    MovableItemManager myMovableItems       = new MovableItemManager();

    public ComponentDisplayGui(Composite parent, int style)
    {
        super(parent, SWT.BORDER | style);
        initGUI();
    }

    /**
     * Initializes the GUI
     */
    public void initGUI()
    {
        try
        {
            this.setSize(new org.eclipse.swt.graphics.Point(379, 234));
            final Color colour = new Color(Display.getDefault(), 128, 128, 128);
            this.setBackground(colour);

            myMainMenu = new Menu(getShell(), SWT.BAR);

            myFileMenuItem = new MenuItem(myMainMenu, SWT.CASCADE);
            myFileMenu = new Menu(myFileMenuItem);
            myFileMenuItem.setText("File");
            myFileMenuItem.setMenu(myFileMenu);

            myOpenFileMenuItem = new MenuItem(myFileMenu, SWT.CASCADE);
            myOpenFileMenuItem.setText("Open");

            newFileMenuItem = new MenuItem(myFileMenu, SWT.CASCADE);
            newFileMenuItem.setText("New");

            mySaveFileMenuItem = new MenuItem(myFileMenu, SWT.CASCADE);
            mySaveFileMenuItem.setText("Save");

            myCloseFileMenuItem = new MenuItem(myFileMenu, SWT.CASCADE);
            myCloseFileMenuItem.setText("Close");

            myExitMenuItem = new MenuItem(myFileMenu, SWT.CASCADE);
            myExitMenuItem.setText("Exit");

            myHelpMenuItem = new MenuItem(myMainMenu, SWT.CASCADE);
            myHelpMenuItem.setText("Help");

            myHelpMenu = new Menu(myHelpMenuItem);
            myHelpMenuItem.setMenu(myHelpMenu);

            myContentsMenuItem = new MenuItem(myHelpMenu, SWT.CASCADE);
            myContentsMenuItem.setText("Contents");

            myAboutMenuItem = new MenuItem(myHelpMenu, SWT.CASCADE);
            myAboutMenuItem.setText("About");

            getShell().setMenuBar(myMainMenu);

            addDisposeListener(new DisposeListener()
            {
                public void widgetDisposed(DisposeEvent e)
                {
                    colour.dispose();
                }
            });

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void paintPalette(PaintEvent event)
    {
        if (event != null) // avoids warning
        {
            myMovableItems.drawlinks(this);
        }
    }

    /*
     * Called when the palette canvas' vertical scrollbar is selected.
     */
    void scrollPalette(ScrollBar scrollBar)
    {
        System.out.println("In scroll palette");
        Rectangle canvasBounds = getClientArea();
        int y = -scrollBar.getSelection();
        scroll(0, y, 0, 20, canvasBounds.width, canvasBounds.height, false);
    }

    /**
     * This method creates the layout of the QueryBuilder so that it can be used
     * by an Operator.
     */
    public void createLayout()
    {
        FormLayout formLayout = new FormLayout();
        setLayout(formLayout);
        formLayout.marginHeight = MARGIN_HEIGHT;
        formLayout.marginWidth = MARGIN_WIDTH;
        setEnabled(true);

        ScrollBar vertical = getParent().getVerticalBar();
        vertical.setVisible(true);
        vertical.setMinimum(0);
        vertical.setIncrement(10);
        vertical.setEnabled(true);
        vertical.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent event)
            {
                scrollPalette((ScrollBar) event.widget);
            }
        });
        addPaintListener(new PaintListener()
        {
            public void paintControl(PaintEvent event)
            {
                paintPalette(event);
            }
        });

        setBackground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
        MovableItem mi = new MovableItem(this, new Point(0, 0), 15, 20);
        myMovableItems.add(mi);

        mi = new MovableItem(this, new Point(0, 0), 15, 20);
        myMovableItems.add(mi);

        this.pack();

        MouseListener listener = new MouseListener()
        {
            Point myClickPoint;
            
            public void mouseDown(MouseEvent event)
            {
                System.out.println(event);
                System.out.println(event.button);
                if (event.button == 1)
                {
                    myMovableItems
                            .captureItemForMove(event.widget, event.x, event.y);
                }
                else if (event.button == 3)
                {
                    myMovableItems
                            .captureItemForLink(event.widget, event.x, event.y);
                    myClickPoint = new Point(event.x, event.y);
                }
            }

            public void mouseUp(MouseEvent event)
            {
                myMovableItems.releaseCapturedItem(event.x, event.y);
                redraw();
                if (isCloseEnough(myClickPoint, event.x, event.y))
                {
                    showPopupForEvent(event);
                }
            }

            private boolean isCloseEnough(Point clickPoint, int x, int y)
            {
                if (Math.abs(clickPoint.x - x) < RIGHT_CLICK_JITTER 
                 && Math.abs(clickPoint.y - y) < RIGHT_CLICK_JITTER)
                {
                    return true;
                }
                return false;
            }

            public void mouseDoubleClick(MouseEvent event)
            {
                //
            }
            
        };

        MouseMoveListener mml = new MouseMoveListener()
        {
            MovableItem myPreviousTarget = null;
            
            public void mouseMove(MouseEvent event)
            {
                myMovableItems
                        .moveCapturedItem(ComponentDisplayGui.this, event.x, event.y);
                
                MovableItem linker = myMovableItems.getLinkingObject();
                if (linker == null)
                {
                    return;
                }
                MovableItem target = myMovableItems.getItemForLocationFromLinkingObject(event.x, event.y);
                
                if (myPreviousTarget != null)
                {
                    if (target == null)
                    {
                        linker.setCursor(MovableItem.ARROW_CURSOR);
                    }
                }
                myPreviousTarget = target;
                
                if (linker != null && target != null)
                {
                    if (target.canBeLinkedToBy(linker))
                    {
                        linker.setCursor(MovableItem.HAND_CURSOR);
                    }
                    else
                    {
                        linker.setCursor(MovableItem.NO_CURSOR);
                    }
                }
            }
        };
        
        MouseTrackListener mtl = new MouseTrackListener()
        {
            public void mouseEnter(MouseEvent event)
            {
                // do nothing
            }

            public void mouseExit(MouseEvent event)
            {
                myMovableItems.getItemForWidget(event.widget).setCursor(MovableItem.ARROW_CURSOR);
            }

            public void mouseHover(MouseEvent event)
            {
                // do nothing
            }
        };
        
        myMovableItems.addMouseTrackListener(mtl);
        myMovableItems.addMouseListener(listener);
        myMovableItems.addMouseMoveListener(mml);
        /*
         * getParent().addMouseListener(listener);
         * getParent().addMouseMoveListener(mml);
         */}

    /**
     * @param event
     */
    protected void showPopupForEvent(MouseEvent event)
    {
		MovableItem mi = myMovableItems.getItemForWidget(event.widget);

		Menu menu = new Menu (getShell(), SWT.POP_UP);
		MenuItem item = new MenuItem (menu, SWT.PUSH);
		item.setText ("Menu Item");
		item.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				System.out.println ("Item Selected");
			}
		});
		
		menu.setLocation (mi.toDisplay(event.x, event.y));
		menu.setVisible (true);
		while (!menu.isDisposed () && menu.isVisible ()) {
			if (!getDisplay().readAndDispatch ()) getDisplay().sleep ();
		}
		menu.dispose ();    
    }

    public static void main(String[] args)
    {
        Display display = Display.getDefault();
        final Shell shell = new Shell(display);

        Image small = new Image(display, 16, 16);
        GC gc = new GC(small);
        gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
        gc.fillArc(0, 0, 16, 16, 45, 270);
        gc.dispose();

        shell.setText("Component Display");
        shell.setImage(small);
        shell.setLayout(new FillLayout());

        Composite scroller = new Composite(shell, SWT.V_SCROLL);
        scroller.setLayout(new FillLayout());

        ComponentDisplayGui qb = new ComponentDisplayGui(scroller, SWT.BORDER);
        qb.setDisplay(display);

        qb.createLayout();
        shell.pack();
        shell.open();
        shell.setSize(750, 400);
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    /**
     * @return The display for this gui.
     */
    public Display getDisplay()
    {
        return myDisplay;
    }

    /**
     * @param display
     */
    public void setDisplay(Display display)
    {
        myDisplay = display;
    }
}