package lavit.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import lavit.Lang;

public final class ModalSettingDialog
{
	@SuppressWarnings("serial")
	private static class GradientPanel extends JPanel
	{
		private static final Color COLOR1 = Color.WHITE;
		private static final Color COLOR2 = new Color(240, 240, 255);

		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D)g;
			Paint paint = new GradientPaint(0, 0, COLOR1, getWidth(), getHeight(), COLOR2);
			g2.setPaint(paint);
			g2.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	private JDialog dialog;
	private JLabel headLabel;
	private JLabel descLabel;
	private boolean approved;

	private ModalSettingDialog(JComponent content)
	{
		dialog = new JDialog();

		//
		// Header
		//
		JPanel header = new GradientPanel();
		header.setLayout(new BorderLayout(0, 4));
		header.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		header.setOpaque(true);
		header.setBackground(Color.WHITE);
		headLabel = new JLabel();
		headLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		headLabel.setFont(headLabel.getFont().deriveFont(Font.BOLD));
		header.add(headLabel, BorderLayout.NORTH);
		descLabel = new JLabel();
		descLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
		header.add(descLabel, BorderLayout.CENTER);
		dialog.add(header, BorderLayout.NORTH);

		//
		// Content
		//
		Border innerBorder = content.getBorder();
		content.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(8, 8, 8, 8),
			innerBorder
		));
		dialog.add(content, BorderLayout.CENTER);

		//
		// Buttons
		//
		JButton ok = new JButton(Lang.d[6]);
		JButton cancel = new JButton(Lang.d[2]);
		ok.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				approved = true;
				dialog.dispose();
			}
		});
		cancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				approved = false;
				dialog.dispose();
			}
		});
		Dimension dim1 = ok.getPreferredSize();
		Dimension dim2 = cancel.getPreferredSize();
		Dimension size = new Dimension(
			Math.max(Math.max(dim1.width, dim2.width), 90),
			Math.max(Math.max(dim1.height, dim2.height), 24));
		ok.setPreferredSize(size);
		cancel.setPreferredSize(size);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(4, 4, 4, 4),
			BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE)
		)));
		buttonPanel.add(ok);
		buttonPanel.add(cancel);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		dialog.getRootPane().setDefaultButton(ok);

		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void setDialogTitle(String title)
	{
		dialog.setTitle(title);
	}

	public void setDialogResizable(boolean resizable)
	{
		dialog.setResizable(resizable);
	}

	public void setDialogAlwaysOnTop(boolean alwaysOnTop)
	{
		dialog.setAlwaysOnTop(alwaysOnTop);
	}

	public void setHeadLineText(String s)
	{
		headLabel.setText(s);
	}

	public void setDescriptionText(String s)
	{
		descLabel.setText(s);
	}

	public boolean showDialog()
	{
		return showDialog(null);
	}

	public boolean showDialog(Component parent)
	{
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setVisible(true);
		return approved;
	}

	public static ModalSettingDialog createDialog(JComponent content)
	{
		return createDialog(content, "", "", "");
	}

	public static ModalSettingDialog createDialog(JComponent content, String title, String headline, String desc)
	{
		ModalSettingDialog settingDialog = new ModalSettingDialog(content);
		settingDialog.setDialogTitle(title);
		settingDialog.setHeadLineText(headline);
		settingDialog.setDescriptionText(desc);
		return settingDialog;
	}
}
