package plsqleditor.util;

/*
 * Table example snippet: sort a table by column
 *
 * For a list of all SWT example snippets see
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
 */
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import java.text.*;
import java.util.*;

public class SortableTableTest
{

    public static void main(String[] args)
    {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        final Table table = new Table(shell, SWT.BORDER);
        table.setHeaderVisible(true);
        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText("Column 1");
        TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText("Column 2");
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[]{"a", "3"});
        item = new TableItem(table, SWT.NONE);
        item.setText(new String[]{"b", "2"});
        item = new TableItem(table, SWT.NONE);
        item.setText(new String[]{"c", "1"});
        column1.pack();
        column2.pack();

        // addSortListeners(table, new int[] {0,1});
        addSortListeners(table, null, null);
        addTableEditor(table, true);
        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    /**
     * @param table
     * @param sortableColumns
     */
    public static void addSortListeners(Table table, int[] sortableColumns, ItemCreator creator)
    {
        int columnCount = table.getColumnCount();
        if (sortableColumns == null)
        {
            sortableColumns = new int[columnCount];
            for (int i = 0; i < sortableColumns.length; i++)
            {
                sortableColumns[i] = i;
            }
        }
        for (int i = 0; i < sortableColumns.length; i++)
        {
            int col = sortableColumns[i];
            TableColumn column = table.getColumn(col);
            // column.removeListener(SWT.Selection, previousListener?);
            column.addListener(SWT.Selection, getSortListener(table, columnCount, col, creator));
        }
    }

    public static Listener getSortListener(final Table table,
                                           final int numColumns,
                                           final int sortableIndex,
                                           final ItemCreator creator)
    {
        TableItem[] items = table.getItems();
        boolean isInteger = true;
        boolean isDouble = true;
        for (int i = 0; i < items.length; i++) 
        {
        	String value = items[i].getText(sortableIndex);
			try
			{
				Integer.valueOf(value);
			}
			catch (Exception e1)
			{
				isInteger = false;
				try
				{
					Double.valueOf(value);
				}
				catch (Exception e2)
				{
					isDouble = false;
					break;
				}
			}
		}
        final boolean isIntegerF = isInteger;
        final boolean isDoubleF = isDouble;
        return new Listener()
        {
            public void handleEvent(Event e)
            {
                Shell shell = new Shell();
                try
                {
                    if (!MessageDialog.openQuestion(shell,
                                                    "Sorting",
                                                    "Do you wish to sort this table?"))
                    {
                        return;
                    }
                    TableItem[] items = table.getItems();
                    Collator collator = Collator.getInstance(Locale.getDefault());
                    int comparisonResult = 0;
                    for (int i = 1; i < items.length; i++)
                    {
                        String value1 = items[i].getText(sortableIndex);
                        for (int j = 0; j < i; j++)
                        {
                            String value2 = items[j].getText(sortableIndex);
                            if (isIntegerF)
                            {
                            	comparisonResult = Integer.valueOf(value1).intValue() - Integer.valueOf(value2).intValue(); 
                            }
                            else if (isDoubleF)
                            {
                            	comparisonResult = Double.valueOf(value1).intValue() - Double.valueOf(value2).intValue(); 
                            }
                            else
                            {
                            	comparisonResult = collator.compare(value1, value2); 
                            }
                            if (comparisonResult < 0)
                            {
                                TableItem item = items[i];
                                String[] values = getValues(item, numColumns);
                                item.dispose();
                                if (creator != null)
                                {
                                    creator.createItem(values, j);
                                }
                                else
                                {
                                    item = new TableItem(table, SWT.NONE, j);
                                    item.setText(values);
                                }
                                items = table.getItems();
                                break;
                            }
                        }
                    }
                }
                finally
                {
                    shell.dispose();
                }
            }

            private String[] getValues(TableItem item, int numColumns)
            {
                String[] values = new String[numColumns];
                for (int i = 0; i < values.length; i++)
                {
                    values[i] = item.getText(i);
                }
                return values;
            }
        };
    }

    public static void addTableEditor(final Table table, final boolean isEditable)
    {
        final TableEditor editor = new TableEditor(table);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        table.addListener(SWT.MouseDown, new Listener()
        {
            public void handleEvent(Event event)
            {
                Rectangle clientArea = table.getClientArea();
                Point pt = new Point(event.x, event.y);
                int index = table.getTopIndex();
                while (index < table.getItemCount())
                {
                    boolean visible = false;
                    final TableItem item = table.getItem(index);
                    for (int i = 0; i < table.getColumnCount(); i++)
                    {
                        Rectangle rect = item.getBounds(i);
                        if (rect.contains(pt))
                        {
                            applyCellEditor(isEditable, editor, item, i);
                            return;
                        }
                        if (!visible && rect.intersects(clientArea))
                        {
                            visible = true;
                        }
                    }
                    if (!visible) return;
                    index++;
                }
            }

        });
    }

    /**
     * This method applies a cell editor to a table that allows the table to select each cell of an
     * <code>item</code>, and possibly modify it.
     * 
     * @param isEditable
     *            Whether the cells should be modifiable or not.
     * @param editor
     *            The table editor to apply the edits back to the table.
     * @param item
     *            The item that contains the cells that we wish to make accessible.
     * @param itemColumn
     *            The column of the item that is having its cell editor set.
     */
    public static void applyCellEditor(final boolean isEditable,
                                       final TableEditor editor,
                                       final TableItem item,
                                       final int itemColumn)
    {
        final Text text = new Text(item.getParent(), SWT.NONE);
        text.setEditable(isEditable);
        Listener textListener = new Listener()
        {
            public void handleEvent(final Event e)
            {
                switch (e.type)
                {
                    case SWT.FocusOut :
                        item.setText(itemColumn, text.getText());
                        text.dispose();
                        break;
                    case SWT.Traverse :
                        switch (e.detail)
                        {
                            case SWT.TRAVERSE_RETURN :
                                item.setText(itemColumn, text.getText());
                            // FALL THROUGH
                            case SWT.TRAVERSE_ESCAPE :
                                text.dispose();
                                e.doit = false;
                        }
                        break;
                }
            }
        };
        text.addListener(SWT.FocusOut, textListener);
        text.addListener(SWT.Traverse, textListener);
        editor.setEditor(text, item, itemColumn);
        text.setText(item.getText(itemColumn));
        text.selectAll();
        text.setFocus();
    }
}