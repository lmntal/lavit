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

package extgui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * Yes/No/Cancel 型の質問ダイアログを作成するビルダークラスです。
 * @author Yuuki.S
 */
public class AskDialogBuilder
{
	private static AskDialogBuilder builder;

	private AskDialog dialog;

	private AskDialogBuilder()
	{
		dialog = new AskDialog();
	}

	/**
	 * ダイアログのタイトル文字列を設定します。
	 */
	public AskDialogBuilder setDialogTitle(String title)
	{
		dialog.setTitle(title);
		return this;
	}

	/**
	 * ダイアログのメッセージタイプを設定します。
	 * 設定されたメッセージタイプによって表示されるアイコンが変化します。
	 */
	public AskDialogBuilder setMessageType(MessageType type)
	{
		dialog.setMessageIcon(type);
		return this;
	}

	/**
	 * ダイアログのメッセージを設定します。
	 */
	public AskDialogBuilder setText(String text)
	{
		dialog.setContentText(text);
		return this;
	}

	/**
	 * ダイアログの Yes/No/Cancel ボタンの表示文字列を設定します。
	 */
	public AskDialogBuilder setButtonCaptions(String yes, String no, String cancel)
	{
		dialog.setButtonCaptions(yes, no, cancel);
		return this;
	}

	/**
	 * 初期状態で Yes ボタンがフォーカスを持つように設定します。
	 */
	public AskDialogBuilder setYesFocused()
	{
		dialog.setInitialFocus(0);
		return this;
	}

	/**
	 * 初期状態で No ボタンがフォーカスを持つように設定します。
	 */
	public AskDialogBuilder setNoFocused()
	{
		dialog.setInitialFocus(1);
		return this;
	}

	/**
	 * 初期状態で Cancel ボタンがフォーカスを持つように設定します。
	 */
	public AskDialogBuilder setCancelFocused()
	{
		dialog.setInitialFocus(2);
		return this;
	}

	/**
	 * ダイアログを表示します。
	 * @return Yesボタンが押された場合は {@code DialogResult.YES}、
	 * Noボタンが押された場合は {@code DialogResult.NO}、
	 * Cancelボタンが押された場合は {@code DialogResult.CANCEL}。
	 */
	public DialogResult showDialog()
	{
		dialog.result = DialogResult.CANCEL;
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		return dialog.getDialogResult();
	}

	/**
	 * ダイアログビルダーのインスタンスを取得します。
	 */
	public static synchronized AskDialogBuilder getInstance()
	{
		if (builder == null)
		{
			builder = new AskDialogBuilder();
		}
		builder.setCancelFocused();
		return builder;
	}

	private static void fixButtonSize(int w, int h, JButton ... buttons)
	{
		for (JButton b : buttons)
		{
			b.setPreferredSize(null); // to calculate default preferred size
			Dimension size = b.getPreferredSize();
			w = Math.max(size.width, w);
			h = Math.max(size.height, h);
		}
		for (JButton b : buttons)
		{
			b.setPreferredSize(new Dimension(w, h));
		}
	}

	private static Icon getOptionPaneIcon(MessageType type)
	{
		String name = getOptionPaneIconName(type);
		return name != null ? UIManager.getIcon(name) : null;
	}

	private static String getOptionPaneIconName(MessageType type)
	{
		switch (type)
		{
		case ERROR:
			return "OptionPane.errorIcon";
		case INFORMATION:
			return "OptionPane.informationIcon";
		case WARNING:
			return "OptionPane.warningIcon";
		case QUESTION:
			return "OptionPane.questionIcon";
		default:
			return null;
		}
	}

	@SuppressWarnings("serial")
	private static class AskDialog extends JDialog
	{
		private JLabel iconLabel;
		private JLabel textLabel;
		private JButton buttonYes;
		private JButton buttonNo;
		private JButton buttonCancel;
		private JButton initialFocusedButton;
		private DialogResult result;

		public AskDialog()
		{
			setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			setResizable(false);
			setModalityType(ModalityType.APPLICATION_MODAL);
			setLayout(new BorderLayout());

			JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
			contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

			iconLabel = new JLabel();
			contentPanel.add(iconLabel, BorderLayout.WEST);
			textLabel = new JLabel();
			contentPanel.add(textLabel, BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 4, 4, 4),
				BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
					BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE))));

			buttonYes = new JButton("Yes");
			buttonYes.setMnemonic(KeyEvent.VK_Y);
			buttonYes.addActionListener(new ButtonAction(DialogResult.YES));
			buttonPanel.add(buttonYes);

			buttonNo = new JButton("No");
			buttonNo.setMnemonic(KeyEvent.VK_N);
			buttonNo.addActionListener(new ButtonAction(DialogResult.NO));
			buttonPanel.add(buttonNo);

			buttonCancel = new JButton("Cancel");
			buttonCancel.setMnemonic(KeyEvent.VK_C);
			buttonCancel.addActionListener(new ButtonAction(DialogResult.CANCEL));
			buttonPanel.add(buttonCancel);

			fixButtonSize(80, 22, buttonYes, buttonNo, buttonCancel);

			add(contentPanel, BorderLayout.CENTER);
			add(buttonPanel, BorderLayout.SOUTH);

			initialFocusedButton = buttonCancel;
			addComponentListener(new ComponentAdapter()
			{
				public void componentShown(ComponentEvent e)
				{
					if (initialFocusedButton != null)
					{
						initialFocusedButton.requestFocusInWindow();
					}
				}
			});
		}

		private void setMessageIcon(MessageType type)
		{
			if (type == MessageType.PLAIN)
			{
				iconLabel.setVisible(false);
				iconLabel.setIcon(null);
			}
			else
			{
				iconLabel.setVisible(true);
				iconLabel.setIcon(getOptionPaneIcon(type));
			}
		}

		private void setContentText(String text)
		{
			textLabel.setText(text);
		}

		private void setButtonCaptions(String yes, String no, String cancel)
		{
			buttonYes.setText(yes);
			buttonNo.setText(no);
			buttonCancel.setText(cancel);
			fixButtonSize(80, 22, buttonYes, buttonNo, buttonCancel);
		}

		private void setInitialFocus(int buttonIndex)
		{
			switch (buttonIndex)
			{
			case 0:
				initialFocusedButton = buttonYes;
				break;
			case 1:
				initialFocusedButton = buttonNo;
				break;
			case 2:
				initialFocusedButton = buttonCancel;
				break;
			}
		}

		private DialogResult getDialogResult()
		{
			return result;
		}

		private class ButtonAction implements ActionListener
		{
			private DialogResult value;

			public ButtonAction(DialogResult value)
			{
				this.value = value;
			}

			public void actionPerformed(ActionEvent e)
			{
				result = value;
				dispose();
			}
		}
	}
}
