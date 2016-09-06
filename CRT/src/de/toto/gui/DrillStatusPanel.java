package de.toto.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.*;

import de.toto.game.*;
import de.toto.game.Drill.DrillStats;

@SuppressWarnings("serial")
public class DrillStatusPanel extends JPanel implements DrillListener {
	
	private Drill drill;
	private JProgressBar pbPositionCount;
	private JLabel lblLast;
	
	private Action actionShowMove = new AbstractAction("show correct move") {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (drill != null) {
				if (drill.getPosition().hasNext()) {
					JOptionPane.showMessageDialog(DrillStatusPanel.this, drill.getPosition().getNext());
				} else {
					JOptionPane.showMessageDialog(DrillStatusPanel.this, "repertoire move missing for current position");
				}
				
			}
		}
	};

	public DrillStatusPanel(Drill drill) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.drill = drill;
		pbPositionCount = new JProgressBar(JProgressBar.HORIZONTAL, 0, drill.getPositionCount());
		pbPositionCount.setStringPainted(true);
		add(pbPositionCount);
		drill.addDrillListener(this);
		updateProgress();		
		lblLast = new JLabel();
		add(lblLast);
		add(new JButton(actionShowMove));
		setBorder(BorderFactory.createTitledBorder("Drill Status"));
	}

	@Override
	public void drillEnded(DrillEvent e) {
				
	}

	@Override
	public void wasCorrect(DrillEvent e) {
		updateProgress();
		lblLast.setText("<html>" + e.getLastMove() + " was correct</html>");
	}

	@Override
	public void wasIncorrect(DrillEvent e) {
		updateProgress();
		lblLast.setText("<html><font color=red>" + e.getLastMove() + " was incorrect</font></html>");
	}

	@Override
	public void drillingNextVariation(DrillEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	private void updateProgress() {
		DrillStats stats = drill.getDrillStats();
		pbPositionCount.setValue(stats.drilledPositions);
		pbPositionCount.setString(String.format("%d of %d positions drilled", stats.drilledPositions, drill.getPositionCount()));
	}
	
}
