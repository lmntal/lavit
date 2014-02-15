package extgui.util;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

public final class GUIUtils
{
	private GUIUtils() { }

	public static void fixButtonWidth(JButton ... buttons)
	{
		fixButtonWidth(0, buttons);
	}

	public static void fixButtonWidth(int padding, JButton ... buttons)
	{
		Dimension d = new Dimension();
		for (JButton b : buttons)
		{
			Dimension size = b.getPreferredSize();
			d.width = Math.max(size.width, d.width);
			d.height = Math.max(size.height, d.height);
		}
		d.width += 2 * padding;
		for (JButton b : buttons)
		{
			b.setPreferredSize(d);
		}
	}

	public static Border createDialogBottomBorder()
	{
		return BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5),
			BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE)));
	}
}
