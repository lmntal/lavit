package lavit.stateviewer.controller;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lavit.Env;
import lavit.stateviewer.StatePanel;

public class StateControlTab extends JTabbedPane {

	public StatePanel statePanel;

	public StateButtonPanel buttonPanel;
	public StateSearchPanel searchPanel;
	public StateDynamicControlPanel dynamicPanel;
	public StateOtherPanel otherPanel;
	public StateBetaPanel betaPanel;

    StateControlTab(StatePanel statePanel){
    	this.statePanel = statePanel;

    	buttonPanel = new StateButtonPanel(statePanel);
		addTab("Control Button", buttonPanel);

		searchPanel = new StateSearchPanel(statePanel);
		addTab("Search", searchPanel);

    	dynamicPanel = new StateDynamicControlPanel(statePanel);
		addTab("Dynamic", dynamicPanel);

		otherPanel = new StateOtherPanel(statePanel);
		addTab("Other", otherPanel);

		betaPanel = new StateBetaPanel(statePanel);
		addTab("Beta", betaPanel);

    }

    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
    	dynamicPanel.setEnabled(enabled);
    	searchPanel.setEnabled(enabled);
		buttonPanel.setEnabled(enabled);
		otherPanel.setEnabled(enabled);
		betaPanel.setEnabled(enabled);
    }

}