package lavit.stateviewer.controller;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lavit.stateviewer.*;

public class StateGeneralControlPanel extends JPanel implements ActionListener {

	private StateGraphPanel graphPanel;

	JButton rootButton = new JButton("root");
	JButton upButton = new JButton("up");
	JLabel ancestors = new JLabel();

	public StateGeneralControlPanel(StateGraphPanel graphPanel){
		this.graphPanel = graphPanel;

		setLayout(new FlowLayout(FlowLayout.LEFT));
		setOpaque(false);

		rootButton.addActionListener(this);
		add(rootButton);

		upButton.addActionListener(this);
		add(upButton);

		add(ancestors);

	}

	public void updateLabel(StateNodeSet nodes){
		StateNode node = nodes.parent;
		String str = "";
		while(node!=null){
			str = node.id + (str.length()>0?" -> ":"") + str;
			node = node.parent.parent;
		}
		ancestors.setText(str);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if(src==rootButton){
			graphPanel.generationReset();
		}else if(src==upButton){
			graphPanel.generationUp();
		}
	}

}
