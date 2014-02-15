package lavit.frame.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
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
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import lavit.Env;
import extgui.util.GUIUtils;

@SuppressWarnings("serial")
public class PropertyEditorDialog extends JDialog
{
	private DefaultTableModel tableModel;
	private JTable table;

	private Map<String, String> originalValues = new HashMap<String, String>();
	private Set<String> editedKeys = new HashSet<String>();

	public PropertyEditorDialog(Window owner)
	{
		super(owner);

		setTitle("Property Editor");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

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
					String key = (String)table.getValueAt(row, 0);
					if (editedKeys.contains(key))
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
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
		panelBottom.setBorder(GUIUtils.createDialogBottomBorder());
		panelBottom.add(buttonApply);
		panelBottom.add(buttonReload);
		panelBottom.add(buttonClose);
		GUIUtils.fixButtonWidth(buttonApply, buttonReload, buttonClose);
		add(panelBottom, BorderLayout.SOUTH);

		tableModel.addTableModelListener(new TableModelListener()
		{
			public void tableChanged(TableModelEvent e)
			{
				if (e.getType() == TableModelEvent.UPDATE)
				{
					int row = e.getFirstRow();
					String key = (String)tableModel.getValueAt(row, 0);
					String value = (String)tableModel.getValueAt(row, 1);
					if (value.equals(originalValues.get(key)))
					{
						editedKeys.remove(key);
						if (editedKeys.isEmpty())
						{
							buttonApply.setEnabled(false);
						}
					}
					else
					{
						editedKeys.add(key);
						buttonApply.setEnabled(true);
					}
					repaint();
				}
			}
		});

		reload();

		getRootPane().setDefaultButton(buttonApply);

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
		editedKeys.clear();
	}

	private void addRow(String key, String value)
	{
		tableModel.addRow(new Object[] { key, value });
	}

	private void applyChanges()
	{
		for (int i = 0; i < tableModel.getRowCount(); i++)
		{
			String key = (String)tableModel.getValueAt(i, 0);
			if (editedKeys.contains(key))
			{
				String newValue = (String)tableModel.getValueAt(i, 1);
				Env.set(key, newValue);
				System.err.println(key + " has been changed to " + newValue);
			}
		}
		reload();
	}

	private void close()
	{
		dispose();
	}
}
