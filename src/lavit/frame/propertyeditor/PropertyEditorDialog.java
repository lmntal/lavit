package lavit.frame.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import lavit.Env;

@SuppressWarnings("serial")
public class PropertyEditorDialog extends JDialog
{
	private DefaultTableModel tableModel;
	private JTable table;

	private Map<String, String> originalValues = new HashMap<String, String>();
	private Set<Integer> editedRows = new HashSet<Integer>();

	public PropertyEditorDialog()
	{
		setTitle("Property Editor");

		tableModel = new DefaultTableModel()
		{
			public boolean isCellEditable(int row, int column)
			{
				return column == 1;
			}
		};
		tableModel.addColumn("Key");
		tableModel.addColumn("Value");

		table = new JTable(tableModel);
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
		{
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (!isSelected && !hasFocus)
				{
					if (editedRows.contains(row))
					{
						comp.setBackground(new Color(255, 220, 220));
					}
					else
					{
						if (row % 2 == 0)
						{
							comp.setBackground(Color.WHITE);
						}
						else
						{
							comp.setBackground(new Color(240, 250, 255));
						}
					}
				}
				return comp;
			}
		});
		table.setGridColor(Color.LIGHT_GRAY);
		table.setColumnSelectionAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoCreateRowSorter(true);
		add(new JScrollPane(table), BorderLayout.CENTER);

		final JButton buttonApply = new JButton("Apply");
		buttonApply.setEnabled(false);
		buttonApply.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				applyChanges();
			}
		});

		JButton buttonReload = new JButton("Reload");
		buttonReload.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				reload();
			}
		});

		JButton buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				close();
			}
		});

		JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panelBottom.add(buttonApply);
		panelBottom.add(buttonReload);
		panelBottom.add(buttonClose);
		add(panelBottom, BorderLayout.SOUTH);

		tableModel.addTableModelListener(new TableModelListener()
		{
			public void tableChanged(TableModelEvent e)
			{
				if (e.getType() == TableModelEvent.UPDATE)
				{
					int row = table.getSelectedRow();
					String key = (String)tableModel.getValueAt(row, 0);
					String value = (String)tableModel.getValueAt(row, 1);
					if (value.equals(originalValues.get(key)))
					{
						editedRows.remove(row);
						if (editedRows.isEmpty())
						{
							buttonApply.setEnabled(false);
						}
					}
					else
					{
						editedRows.add(row);
						buttonApply.setEnabled(true);
					}
				}
			}
		});

		reload();

		pack();
	}

	private void reload()
	{
		tableModel.setRowCount(0);
		originalValues.clear();
		for (String[] e : Env.getEntries())
		{
			originalValues.put(e[0], e[1]);
			addRow(e[0], e[1]);
		}
		editedRows.clear();
	}

	private void addRow(String key, String value)
	{
		tableModel.addRow(new Object[] { key, value });
	}

	private void applyChanges()
	{
		for (int row : editedRows)
		{
			String key = (String)tableModel.getValueAt(row, 0);
			String newValue = (String)tableModel.getValueAt(row, 1);
			Env.set(key, newValue);
			System.err.println(key + " has been changed to " + newValue);
		}
		reload();
	}

	private void close()
	{
		dispose();
	}
}
