package lavit.stateviewer.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import lavit.Env;
import lavit.FrontEnd;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.StateTransitionEm;
import lavit.util.CommonFontUser;
import lavit.util.FixFlowLayout;

public class StateSearchPanel extends JPanel implements ActionListener {

	private StatePanel statePanel;

	private JPanel resultPanel = new JPanel();
	private JLabel resultLabel = new JLabel();
	private JButton resultButton = new JButton("Clear");

	private JPanel matchPanel = new JPanel();
	private JPanel matchHeadPanel = new JPanel();
	private JTextField matchHeadField = new JTextField();
	private JPanel matchGuardPanel = new JPanel();
	private JTextField matchGuardField = new JTextField();
    private JButton matchButton = new JButton("Search");

	private JPanel findPanel = new JPanel();
	private JTextField findField = new JTextField();
    private JButton findButton = new JButton("Search");

    private JPanel transitionPanel = new JPanel();
    private JButton ruleWindowButton = new JButton("Rule Name Search");

    StateSearchPanel(StatePanel statePanel){

    	this.statePanel = statePanel;
    	setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

    	resultPanel.setLayout(new BorderLayout());
    	resultPanel.setBorder(new TitledBorder("Result"));
    	resultPanel.setMaximumSize(new Dimension(1000,50));
    	resultLabel.setHorizontalAlignment(JLabel.LEFT);
    	resultLabel.setText("");
    	resultPanel.add(resultLabel, BorderLayout.CENTER);
    	resultButton.addActionListener(this);
    	resultPanel.add(resultButton, BorderLayout.EAST);
    	add(resultPanel);

    	matchPanel.setLayout(new BorderLayout());
		matchPanel.setBorder(new TitledBorder("LMNtal Syntax Search"));
		matchPanel.setMaximumSize(new Dimension(1000,50));
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
		matchPanel.add(l, BorderLayout.CENTER);
		matchButton.addActionListener(this);
		matchPanel.add(matchButton, BorderLayout.EAST);
		add(matchPanel);

		findPanel.setLayout(new BorderLayout());
		findPanel.setBorder(new TitledBorder("String Search"));
		findPanel.setMaximumSize(new Dimension(1000,50));
		findButton.addActionListener(this);
		findPanel.add(findButton, BorderLayout.EAST);
		findField.addActionListener(this);
		findPanel.add(findField, BorderLayout.CENTER);
		add(findPanel);

		transitionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		ruleWindowButton.addActionListener(this);
		transitionPanel.add(ruleWindowButton);
		add(transitionPanel);
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

		if(src==resultButton){
			resultLabel.setText("");
			matchHeadField.setText("");
			matchGuardField.setText("");
			findField.setText("");
			statePanel.stateGraphPanel.searchReset();
		}else if(src==matchButton||src==matchHeadField||src==matchGuardField){
			resultLabel.setText("searching...");
			(new Thread(new Runnable() { public void run() {
				final int match = statePanel.stateGraphPanel.stateMatch(matchHeadField.getText(),matchGuardField.getText());
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					if(match>=0){
						resultLabel.setText(" match : "+match+" state ");
					}else{
						resultLabel.setText(" error! ");
					}
				}});
			}})).start();
		}else if(src==findButton||src==findField){
			resultLabel.setText("searching...");
			(new Thread(new Runnable() { public void run() {
				final int match = statePanel.stateGraphPanel.stateFind(findField.getText());
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					resultLabel.setText(" \""+findField.getText()+"\" match : "+match+" state ");
				}});
			}})).start();
		}else if(src==ruleWindowButton){
			resultLabel.setText("");
			statePanel.stateGraphPanel.searchReset();
			new SelectStateTransitionRuleFrame(statePanel.stateGraphPanel,new StateTransitionEm(statePanel.stateGraphPanel));
		}
	}

}
