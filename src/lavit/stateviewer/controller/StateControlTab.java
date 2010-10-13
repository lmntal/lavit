package lavit.stateviewer.controller;

import javax.swing.JTabbedPane;

import lavit.Env;
import lavit.stateviewer.StatePanel;

public class StateControlTab extends JTabbedPane {

	public StatePanel statePanel;

	public StateButtonPanel buttonPanel;
	public StateSearchPanel searchPanel;
	public StateDynamicControlPanel dynamicPanel;

    StateControlTab(StatePanel statePanel){
    	this.statePanel = statePanel;

    	buttonPanel = new StateButtonPanel(statePanel);
		addTab("Control Button", buttonPanel);

		searchPanel = new StateSearchPanel(statePanel);
		addTab("Search", searchPanel);

    	dynamicPanel = new StateDynamicControlPanel(statePanel);
		addTab("Dynamic Controler",dynamicPanel);

    }

    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
    	dynamicPanel.setEnabled(enabled);
    	searchPanel.setEnabled(enabled);
		buttonPanel.setEnabled(enabled);
    }

}
