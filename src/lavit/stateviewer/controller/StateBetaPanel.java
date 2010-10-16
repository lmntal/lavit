package lavit.stateviewer.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lavit.Env;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.StateTransitionAbstraction;

public class StateBetaPanel extends JPanel implements ActionListener {
	private StatePanel statePanel;

	private JComboBox simpleModeBox;
	private String[] simpleModeBoxItems = {"auto","true","false"};

	private JComboBox startupResetBox;
	private String[] startupResetBoxItems = {"none","PositionReset","AdjustReset","AdjustBackReset","AdjustFindReset","SimpleMixAdjust","DummyMixAdjust"};

	StateBetaPanel(StatePanel statePanel){
		this.statePanel = statePanel;

		add(new JLabel("Simple Mode:"));
		simpleModeBox = new JComboBox(simpleModeBoxItems);
		simpleModeBox.addActionListener(this);
		add(simpleModeBox);
		for(String str : simpleModeBoxItems){
			if(str.equals(Env.get("SV_SIMPLE_MODE"))){
				simpleModeBox.setSelectedItem(str);
				break;
			}
		}

		add(new JLabel("Startup Reset:"));
		startupResetBox = new JComboBox(startupResetBoxItems);
		startupResetBox.addActionListener(this);
		add(startupResetBox);
		for(String str : startupResetBoxItems){
			if(str.equals(Env.get("SV_STARTUP_RESET_TYPE"))){
				startupResetBox.setSelectedItem(str);
				break;
			}
		}

	}

	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		simpleModeBox.setEnabled(enabled);
		startupResetBox.setEnabled(enabled);
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if(src==simpleModeBox){
			Env.set("SV_SIMPLE_MODE",(String)simpleModeBox.getSelectedItem());
			statePanel.stateGraphPanel.updateSimpleMode();
			statePanel.stateGraphPanel.update();
		}else if(src==startupResetBox){
			Env.set("SV_STARTUP_RESET_TYPE",(String)startupResetBox.getSelectedItem());
		}
	}

}
