package de.toto.gui;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.toto.game.Drill;
import de.toto.game.DrillEvent;
import de.toto.game.DrillListener;

@SuppressWarnings("serial")
public class DrillStatusPanel extends JPanel implements DrillListener {
	
	private Drill drill;
	private JProgressBar pbPositionCount;

	public DrillStatusPanel(Drill drill) {
		super();
		this.drill = drill;
		pbPositionCount = new JProgressBar(JProgressBar.HORIZONTAL, 0, drill.getPositionCount());
		add(pbPositionCount);
		drill.addDrillListener(this);
		
	}

	@Override
	public void drillEnded(DrillEvent e) {
				
	}

	@Override
	public void wasCorrect(DrillEvent e) {
		pbPositionCount.setValue(drill.getDrillStats().drilledPositions);
		
	}

	@Override
	public void wasIncorrect(DrillEvent e) {
		pbPositionCount.setValue(drill.getDrillStats().drilledPositions);
		
	}

	@Override
	public void drillingNextVariation(DrillEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	

}
