package lavit.multiedit.coloring;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.TextUI;

@SuppressWarnings("serial")
public class LmnTextPane extends JTextPane
{
	private LmnDocument _doc;
	
	public LmnTextPane()
	{
		setEditorKit(new LmnEditorKit());
		
		_doc = new LmnDocument();
		setDocument(_doc);
		
		setOpaque(false);
		
		addCaretListener(new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				int[] pospair = findParenPair(getCaretPosition());
				_doc.setParenPair(pospair);
				repaint();
			}
		});
		
		setFont(new Font("Consolas", Font.PLAIN, 12));
		
		InputMap im = getInputMap();
		im.put(KeyStroke.getKeyStroke("control Z"), "undo");
		im.put(KeyStroke.getKeyStroke("control Y"), "redo");
		
		ActionMap am = getActionMap();
		am.put("undo", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				_doc.undo();
			}
		});
		am.put("redo", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				_doc.redo();
			}
		});
	}
	
	public void setTabWidth(int spaces)
	{
		FontMetrics fm = getFontMetrics(getFont());
		int width = fm.charWidth('m') * spaces;
		_doc.setTabWidth(width);
	}
	
	public void clearUndo()
	{
		_doc.clearUndo();
	}
	
	// 右端で折り返さないようにする
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		Container container = getParent();
		TextUI ui = getUI();
		return ui.getPreferredSize(this).width < container.getWidth();
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		Rectangle bounds = g.getClipBounds();
		g.setColor(Color.WHITE);
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		super.paintComponent(g);
	}
	
	private int[] findParenPair(int caretPos)
	{
		String text = getText().replace("\r\n", "\n");
		int p0 = caretPos - 1, chpos;
		
		if (p0 < 0 || text.length() <= p0) return null;
		
		char c1 = text.charAt(p0);
		if ((chpos = "({[)}]".indexOf(text.charAt(p0))) != -1)
		{
			char c2 = ")}]({[".charAt(chpos);
			int dir = chpos < 3 ? 1 : -1;
			
			int p1 = p0 + dir;
			int level = 0;
			
			while (0 <= p1 && p1 < text.length())
			{
				char c = text.charAt(p1);
				if (c == c2)
				{
					if (level == 0)
					{
						return new int[] { p1, p0 };
					}
					else
					{
						level--;
					}
				}
				else if (c == c1)
				{
					level++;
				}
				p1 += dir;
			}
		}
		return null;
	}
}
