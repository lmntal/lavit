/*
 *   Copyright (c) 2008, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are
 *   met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *    3. Neither the name of the Ueda Laboratory LMNtal Group nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

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
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import lavit.Lang;

/**
 * 設定画面用のダイアログテンプレート。
 * 中央にコンポーネントを設定して使う。
 */
public final class ModalSettingDialog
{
	public static class ClosingEvent
	{
		private final boolean approved;
		private boolean canceled;

		public ClosingEvent(boolean approved)
		{
			this.approved = approved;
		}

		public boolean isApproved()
		{
			return approved;
		}

		public boolean isCanceled()
		{
			return canceled;
		}

		public void setCanceled(boolean b)
		{
			canceled = b;
		}
	}

	public interface ClosingListener
	{
		public void dialogClosing(ClosingEvent e);
	}

	/**
	 * ヘッダー部のパネル
	 */
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
	private JButton okButton;
	private JButton cancelButton;
	private JLabel headLabel;
	private JLabel descLabel;
	private boolean approved;
	private ClosingListener closingListener;

	private ModalSettingDialog(JFrame owner, JComponent content)
	{
		dialog = new JDialog(owner);

		//
		// Header
		//
		JPanel header = new GradientPanel();
		header.setLayout(new BorderLayout(0, 4));
		header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		header.setOpaque(true);
		header.setBackground(Color.WHITE);
		headLabel = new JLabel();
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
			BorderFactory.createEmptyBorder(16, 16, 16, 16),
			innerBorder
		));
		dialog.add(content, BorderLayout.CENTER);

		//
		// Buttons
		//
		okButton = new JButton(Lang.d[6]);
		cancelButton = new JButton(Lang.d[2]);

		fixButtons(90, 24, okButton, cancelButton);

		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				approved = true;
				tryClose();
			}
		});
		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				approved = false;
				tryClose();
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(4, 4, 4, 4),
			BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE)
		)));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				approved = false;
				tryClose();
			}
		});
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

	public void setDialogIconImage(Image image)
	{
		dialog.setIconImage(image);
	}

	public void setDialogIconImages(List<? extends Image> icons)
	{
		dialog.setIconImages(icons);
	}

	public void setHeadLineText(String s)
	{
		headLabel.setText(s);
	}

	public void setDescriptionText(String s)
	{
		descLabel.setText(s);
	}

	public void setClosingListener(ClosingListener l)
	{
		closingListener = l;
	}

	public boolean showDialog()
	{
		return showDialog(null);
	}

	public boolean showDialog(Component parent)
	{
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setVisible(true);
		return approved;
	}

	private void tryClose()
	{
		ClosingEvent e = new ClosingEvent(approved);
		if (closingListener != null)
		{
			closingListener.dialogClosing(e);
		}
		if (!e.isCanceled())
		{
			dialog.dispose();
		}
	}

	public static ModalSettingDialog createDialog(JComponent content)
	{
		return createDialog(content, "", "", "");
	}

	public static ModalSettingDialog createDialog(JFrame owner, JComponent content)
	{
		return createDialog(owner, content, "", "", "");
	}

	public static ModalSettingDialog createDialog(JComponent content, String title, String headline, String desc)
	{
		return createDialog(null, content, title, headline, desc);
	}

	public static ModalSettingDialog createDialog(JFrame owner, JComponent content, String title, String headline, String desc)
	{
		ModalSettingDialog settingDialog = new ModalSettingDialog(owner, content);
		settingDialog.setDialogTitle(title);
		settingDialog.setHeadLineText(headline);
		settingDialog.setDescriptionText(desc);
		return settingDialog;
	}

	private static void fixButtons(int minWidth, int minHeight, JButton ... buttons)
	{
		int w = minWidth;
		for (JButton b : buttons)
		{
			w = Math.max(w, b.getPreferredSize().width);
		}
		for (JButton b : buttons)
		{
			Dimension size = b.getPreferredSize();
			size.width = w;
			size.height = Math.max(size.height, minHeight);
			b.setPreferredSize(size);
		}
	}
}
