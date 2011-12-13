package lavit.multiedit.coloring;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.BadLocationException;

public class LmnTextPaneUI extends BasicTextPaneUI
{
	private JTextPane tc;

	public LmnTextPaneUI(JTextPane textPane)
	{
		tc = textPane;
		tc.addCaretListener(new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				tc.repaint();
			}
		});
	}

	public void paintBackground(Graphics g)
	{
		super.paintBackground(g);
		try
		{
			Rectangle rc0 = tc.modelToView(0);
			Rectangle rc = tc.modelToView(tc.getCaretPosition());
			
			if (rc0 == null || rc == null) return;
			
			int x = rc0.x;
			int y = rc.y;
			int w = tc.getWidth();
			int h = rc.height;
			
			g.setColor(new Color(240, 240, 255));
			g.fillRect(x, y, w, h);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}
}
