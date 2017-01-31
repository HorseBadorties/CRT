package de.toto.gui.swing;

import java.awt.FlowLayout;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SettingsPanel extends JPanel {
	
	private JCheckBox cbShowBoard;
	private JCheckBox cbShowPieces;	
	private JCheckBox cbShowCoordinates;
	private JCheckBox cbShowArrows;
	private JCheckBox cbShowMetarialImbalance;
	private JCheckBox cbAnnounceMoves;
	private SpinnerModel spinnerModel;  
	private JSpinner spinnerDelayAfterMove;
	
	private Preferences prefs = Preferences.userNodeForPackage(AppFrame.class);
	
	public SettingsPanel(AppFrame appFrame) {
		
		cbShowBoard = createCheckBox(appFrame.actionShowBoard);
		cbShowBoard.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_BOARD, true));	
		cbShowPieces = createCheckBox(appFrame.actionShowPieces);
		cbShowPieces.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_PIECES, true));	
		cbShowCoordinates = createCheckBox(appFrame.actionShowCoordinates);
		cbShowCoordinates.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_COORDINATES, false));
		cbShowArrows = createCheckBox(appFrame.actionShowArrows);
		cbShowArrows.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_ARROWS, true));	
		cbShowMetarialImbalance = createCheckBox(appFrame.actionShowMaterialImbalance);
		cbShowMetarialImbalance.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_MATERIAL_IMBALANCE, false));		
		cbAnnounceMoves = createCheckBox(appFrame.actionAnnounceMoves);
		cbAnnounceMoves.setSelected(prefs.getBoolean(AppFrame.PREFS_ANNOUNCE_MOVES, false));	
		spinnerModel = new SpinnerNumberModel(prefs.getInt(AppFrame.PREFS_DELAY_AFTER_MOVE, 500), 0, 3000, 100);  
		spinnerModel.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {				
				int value = ((SpinnerNumberModel)spinnerModel).getNumber().intValue();
				prefs.putInt(AppFrame.PREFS_DELAY_AFTER_MOVE, value);
				
			}
		});
		spinnerDelayAfterMove = new JSpinner(spinnerModel);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(cbShowBoard);
		add(cbShowPieces);
		add(cbShowCoordinates);
		add(cbShowArrows);
		add(cbShowMetarialImbalance);
		add(cbAnnounceMoves);
		JPanel pnlDelayAfterMove = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,0));
		pnlDelayAfterMove.add(new JLabel("Delay in milliseconds after drill move: "));
		pnlDelayAfterMove.add(spinnerDelayAfterMove);
		pnlDelayAfterMove.setAlignmentX(cbAnnounceMoves.getAlignmentX());
		add(pnlDelayAfterMove);
	}
	
	private static JCheckBox createCheckBox(Action action) {
		JCheckBox result = new JCheckBox(action);
//		result.setHorizontalTextPosition(SwingConstants.LEADING);
		return result;
	}
	
}
