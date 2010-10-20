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

public class StateBetaPanel extends JPanel {
	private StatePanel statePanel;

	private SimpleModeChanger simpleModeChanger;
	private StartupResetChanger startupResetChanger;
	private GraphDrawChanger graphDrawChanger;

	StateBetaPanel(StatePanel statePanel){
		this.statePanel = statePanel;

		simpleModeChanger = new SimpleModeChanger();
		add(simpleModeChanger);

		startupResetChanger = new StartupResetChanger();
		add(startupResetChanger);

		graphDrawChanger = new GraphDrawChanger();
		add(graphDrawChanger);

	}

	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		simpleModeChanger.setEnabled(enabled);
		startupResetChanger.setEnabled(enabled);
	}

	class SimpleModeChanger extends JPanel implements ActionListener{
		private JComboBox box;
		private String[] boxItems = {"auto","true","false"};

		SimpleModeChanger(){
			add(new JLabel("Simple Mode:"));
			box = new JComboBox(boxItems);
			box.addActionListener(this);
			add(box);
			for(String str : boxItems){
				if(str.equals(Env.get("SV_SIMPLE_MODE"))){
					box.setSelectedItem(str);
					break;
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if(src==box){
				Env.set("SV_SIMPLE_MODE",(String)box.getSelectedItem());
				statePanel.stateGraphPanel.updateSimpleMode();
				statePanel.stateGraphPanel.update();
			}
		}

		public void setEnabled(boolean enabled){
			super.setEnabled(enabled);
			box.setEnabled(enabled);
		}
	}

	class StartupResetChanger extends JPanel implements ActionListener{
		private JComboBox box;
		private String[] boxItems = {"none","PositionReset","AdjustReset","AdjustBackReset","AdjustFindReset","SimpleMixAdjust","DummyMixAdjust"};

		StartupResetChanger(){
			add(new JLabel("Startup Reset:"));
			box = new JComboBox(boxItems);
			box.addActionListener(this);
			add(box);
			for(String str : boxItems){
				if(str.equals(Env.get("SV_STARTUP_RESET_TYPE"))){
					box.setSelectedItem(str);
					break;
				}
			}
		}

		public void setEnabled(boolean enabled){
			super.setEnabled(enabled);
			box.setEnabled(enabled);
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if(src==box){
				Env.set("SV_STARTUP_RESET_TYPE",(String)box.getSelectedItem());
			}
		}
	}

	class GraphDrawChanger extends JPanel implements ActionListener{
		private JComboBox box;
		private String[] boxItems = {"BASIC","BLACK"};

		GraphDrawChanger(){
			add(new JLabel("Graph Draw Mode:"));
			box = new JComboBox(boxItems);
			box.addActionListener(this);
			add(box);
			for(String str : boxItems){
				if(str.equals(Env.get("SV_GRAPH_DRAW"))){
					box.setSelectedItem(str);
					break;
				}
			}
		}

		public void setEnabled(boolean enabled){
			super.setEnabled(enabled);
			box.setEnabled(enabled);
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if(src==box){
				Env.set("SV_GRAPH_DRAW",(String)box.getSelectedItem());
				statePanel.stateGraphPanel.updateDraw();
				statePanel.stateGraphPanel.update();
			}
		}
	}

}
