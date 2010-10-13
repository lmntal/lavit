package lavit.stateviewer.worker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import lavit.Env;
import lavit.Lang;
import lavit.frame.ChildWindowListener;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;

public class StateGraphSimpleMixAdjustWorker extends SwingWorker<Object,Object> {
	private StateGraphPanel panel;
	private StateNodeSet drawNodes;
	private boolean endFlag;

	public StateGraphSimpleMixAdjustWorker(StateGraphPanel panel){
		this.panel = panel;
		this.drawNodes = panel.getDrawNodes();
		this.endFlag = false;
	}

	public void waitExecute(){
		selectExecute();
		while(!endFlag){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
	}

	public void selectExecute(){
		if(drawNodes.size()<1000){
			atomic();
		}else{
			ready();
			execute();
		}
	}

	public void atomic(){
		ready();
		doInBackground();
		done();
	}

	public void ready(){
		panel.setActive(false);
	}

	public void end() {
		panel.autoCentering();
		panel.setActive(true);
		this.endFlag = true;
	}

	@Override
	protected Object doInBackground(){
		panel.statePanel.stateControlPanel.buttonPanel.exchangeDummyOnly.setSelected(false);
		Env.set("SV_CROSSREDUCTION_DUMMYONLY",false);

		drawNodes.removeDummy();

		drawNodes.setBackDummy();
		drawNodes.dummyCentering();
		drawNodes.updateNodeLooks();

		(new StateGraphAdjust2Worker(panel)).waitExecute();
		(new StateGraphExchangeWorker(panel)).waitExecute();

		if(panel.statePanel.isLtl()){
			panel.searchShortCycle();
		}

		drawNodes.updateNodeLooks();

		end();
		return null;
	}

	boolean rideOtherNode(ArrayList<StateNode> nodes,StateNode node){
		for(StateNode n : nodes){
			if(n==node) continue;
			double dy = node.getY()-n.getY();
			if(dy<0){ dy *= -1; }
			double r = node.getRadius()+n.getRadius()+7;
			if(dy<r){
				return true;
			}
		}
		return false;
	}

}
