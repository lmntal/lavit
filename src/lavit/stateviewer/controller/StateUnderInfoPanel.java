package lavit.stateviewer.controller;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StatePanel;

public class StateUnderInfoPanel extends JPanel {

	public StatePanel statePanel;

	private JLabel zoomNum = new JLabel();
	private JLabel stateNum = new JLabel();

	StateUnderInfoPanel(StatePanel statePanel){
		this.statePanel = statePanel;

		setLayout(new GridLayout(1,2));

		zoomNum.setHorizontalAlignment(JLabel.LEFT);
		add(zoomNum);

		stateNum.setHorizontalAlignment(JLabel.RIGHT);
		add(stateNum);

		setDrawInfo(0,0);
		setStateInfo(0,0,0);
	}

	public void updateInfo(){
		StateGraphPanel p = statePanel.stateGraphPanel;
		setDrawInfo(p.getZoom(),p.getDrawTime());
		setStateInfo(p.getDepth()-1,p.getAllNum(),p.getEndNum());
	}

	private void setDrawInfo(double zoom,double drawTime){
		String z = ""+(int)(zoom*100);
		if(zoom<0.01){
			z = ""+(((int)(zoom*10000))/100.0);
		}
		zoomNum.setText(" Zoom : "+z+"%, DrawTime : "+(drawTime/1000)+"s");
	}

	private void setStateInfo(int depth,int num,int end){
		stateNum.setText(" Depth : "+depth+", State : "+num+", (End : "+end+") ");
	}

}
