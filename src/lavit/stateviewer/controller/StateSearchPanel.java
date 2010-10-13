package lavit.stateviewer.controller;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lavit.Env;
import lavit.FrontEnd;
import lavit.stateviewer.StatePanel;
import lavit.util.CommonFontUser;

public class StateSearchPanel extends JPanel implements ActionListener,CommonFontUser {

	private StatePanel statePanel;

	private JPanel matchPanel = new JPanel();
	private JLabel matchLabel = new JLabel();
	private JPanel matchHeadPanel = new JPanel();
	private JTextField matchHeadField = new JTextField();
	private JPanel matchGuardPanel = new JPanel();
	private JTextField matchGuardField = new JTextField();
    private JButton matchButton = new JButton("Match");

	private JPanel findPanel = new JPanel();
	private JLabel findLabel = new JLabel();
	private JTextField findField = new JTextField();
    private JButton findButton = new JButton("Find");

    StateSearchPanel(StatePanel statePanel){

    	this.statePanel = statePanel;
    	setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

    	matchButton.addActionListener(this);
		matchHeadField.addActionListener(this);
		matchGuardField.addActionListener(this);
		matchHeadPanel.setLayout(new BorderLayout());
		matchHeadPanel.add(new JLabel(" Head:"),BorderLayout.WEST);
		matchHeadPanel.add(matchHeadField, BorderLayout.CENTER);
		matchGuardPanel.setLayout(new BorderLayout());
		matchGuardPanel.add(new JLabel(" Guard:"),BorderLayout.WEST);
		matchGuardPanel.add(matchGuardField, BorderLayout.CENTER);
		JPanel l = new JPanel(new GridLayout(1,2));
		l.add(matchHeadPanel);
		l.add(matchGuardPanel);
		matchPanel.setLayout(new BorderLayout());
		matchPanel.add(matchLabel,BorderLayout.WEST);
		matchPanel.add(l, BorderLayout.CENTER);
		matchPanel.add(matchButton, BorderLayout.EAST);
		add(matchPanel);


		findButton.addActionListener(this);
		findField.addActionListener(this);
		findPanel.setLayout(new BorderLayout());
		findPanel.add(findLabel,BorderLayout.WEST);
		findPanel.add(findField, BorderLayout.CENTER);
		findPanel.add(findButton, BorderLayout.EAST);
		add(findPanel);

		loadFont();
		FrontEnd.addFontUser(this);
    }

    public void loadFont(){
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));
		matchHeadField.setFont(font);
		matchGuardField.setFont(font);
		findField.setFont(font);
		revalidate();
	}

    public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		matchHeadField.setEnabled(enabled);
		matchGuardField.setEnabled(enabled);
		matchButton.setEnabled(enabled);
		findField.setEnabled(enabled);
		findButton.setEnabled(enabled);
	}

    public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if(src==matchButton||src==matchHeadField||src==matchGuardField){
			matchLabel.setText("matching...");
			(new Thread(new Runnable() { public void run() {
				final int match = statePanel.stateGraphPanel.stateMatch(matchHeadField.getText(),matchGuardField.getText());
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					if(match>=0){
						matchLabel.setText(" match : "+match+" state ");
					}else{
						matchLabel.setText(" error! ");
					}
				}});
			}})).start();
		}else if(src==findButton||src==findField){
			findLabel.setText("finding...");
			(new Thread(new Runnable() { public void run() {
				final int match = statePanel.stateGraphPanel.stateFind(findField.getText());
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					findLabel.setText(" \""+findField.getText()+"\" match : "+match+" state ");
				}});
			}})).start();
		}
	}

}
