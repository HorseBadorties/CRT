package de.toto.gui;

import java.awt.Component;
import java.awt.Dimension;
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
		this.drill = drill;
		drill.addDrillListener(this);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));		
		add(Box.createRigidArea(new Dimension(0,5)));
		pbPositionCount = new JProgressBar(JProgressBar.HORIZONTAL, 0, drill.getPositionCount());
		pbPositionCount.setStringPainted(true);
		pbPositionCount.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(pbPositionCount);
		
		add(Box.createRigidArea(new Dimension(0,10)));	
		lblLast = new JLabel();
		lblLast.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblLast.setHorizontalAlignment(JLabel.CENTER);
		add(lblLast);
		add(Box.createRigidArea(new Dimension(0,10)));
		add(Box.createVerticalGlue());
		JButton btnShowMove = AppFrame.createButton(actionShowMove);
		btnShowMove.putClientProperty("JComponent.sizeVariant", "large");
		btnShowMove.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(btnShowMove);
		add(Box.createRigidArea(new Dimension(0,5)));
		setBorder(BorderFactory.createTitledBorder("Drill Status"));
		updateProgress();	
	}

	@Override
	public void drillEnded(DrillEvent e) {
				
	}

	@Override
	public void wasCorrect(DrillEvent e) {
		updateProgress();
		lblLast.setText("<html><font color=green>" + e.getLastMove() + " was the correct repertoire answer</font></html>");
	}

	@Override
	public void wasIncorrect(DrillEvent e) {
		updateProgress();
		lblLast.setText("<html><font color=red>" + e.getLastMove() + " wasn't the correct repertoire answer</font></html>");
	}

	@Override
	public void drillingNextVariation(DrillEvent e) {
		
	}
	
	private void updateProgress() {
		DrillStats stats = drill.getDrillStats();
		pbPositionCount.setValue(stats.drilledPositions);
		pbPositionCount.setString(String.format("%d of %d positions drilled", stats.drilledPositions, drill.getPositionCount()));
	}
	
}
