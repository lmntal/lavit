package lavit.util;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import lavit.Env;
import lavit.FrontEnd;
import lavit.editor.AutoStyledDocument;
import lavit.frame.ChildWindowListener;

public class UtilTextFrame extends JFrame{

	public UtilTextFrame(String title,String str){

		int width = FrontEnd.mainFrame.getWidth()/2;
		int height = FrontEnd.mainFrame.getHeight()/2;
		int x = FrontEnd.mainFrame.getX();
		int y = FrontEnd.mainFrame.getY();

		setSize(width,height);
		setLocation(x,y);
		setTitle(title);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		/*
		JTextArea text = new JTextArea(state);
		text.setLineWrap(true);
		text.setFont(new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE")));
		 */
		AutoStyledDocument doc = new AutoStyledDocument();
		JTextPane editor = new JTextPane();
		//editor.setEditorKit(new NoWrapEditorKit());
		editor.setDocument(doc);
		editor.setFont(new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE")));
		editor.setText(str);
		doc.colorChange();
		doc.end();

		add(new JScrollPane(editor));

		addWindowListener(new ChildWindowListener(this));

		setVisible(true);

	}
}
