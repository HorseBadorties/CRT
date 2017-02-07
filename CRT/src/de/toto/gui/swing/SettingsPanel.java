package de.toto.gui.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SettingsPanel extends JPanel implements ActionListener {
	
	private JCheckBox cbShowBoard;
	private JCheckBox cbShowPieces;	
	private JCheckBox cbShowCoordinates;
	private JCheckBox cbShowArrows;
	private JCheckBox cbShowMaterialImbalance;
	private JCheckBox cbAnnounceMoves;
	private SpinnerModel spinnerModel;  
	private JSpinner spinnerDelayAfterMove;
	private JTextField txtEnginePath;
	private JLabel txtGameEnginePath;
	private JButton btnPickGameEngine;	
	
	private Preferences prefs = Preferences.userNodeForPackage(AppFrame.class);
	
	public SettingsPanel(AppFrame appFrame) {
		
		cbShowBoard = new JCheckBox(appFrame.actionShowBoard);
		cbShowBoard.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_BOARD, true));	
		cbShowPieces = new JCheckBox(appFrame.actionShowPieces);
		cbShowPieces.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_PIECES, true));	
		cbShowCoordinates = new JCheckBox(appFrame.actionShowCoordinates);
		cbShowCoordinates.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_COORDINATES, false));
		cbShowArrows = new JCheckBox(appFrame.actionShowArrows);
		cbShowArrows.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_ARROWS, true));	
		cbShowMaterialImbalance = new JCheckBox(appFrame.actionShowMaterialImbalance);
		cbShowMaterialImbalance.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_MATERIAL_IMBALANCE, false));		
		cbAnnounceMoves = new JCheckBox(appFrame.actionAnnounceMoves);
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
		txtGameEnginePath = new JLabel(prefs.get(AppFrame.PREFS_PATH_TO_GAME_ENGINE, ""));
		txtGameEnginePath.setBorder(BorderFactory.createLoweredBevelBorder());	
		btnPickGameEngine = new JButton("..");
		btnPickGameEngine.addActionListener(this);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(cbShowBoard);
		add(cbShowPieces);
		add(cbShowCoordinates);
		add(cbShowArrows);
		add(cbShowMaterialImbalance);
		add(cbAnnounceMoves);
		JPanel pnlDelayAfterMove = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,0));
		pnlDelayAfterMove.add(new JLabel("Delay in milliseconds after drill move: "));
		pnlDelayAfterMove.add(spinnerDelayAfterMove);
		pnlDelayAfterMove.setAlignmentX(cbAnnounceMoves.getAlignmentX());
		add(pnlDelayAfterMove);
		JPanel pnlGameEngine = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,0));
		pnlGameEngine.add(new JLabel("Game engine: "));
		pnlGameEngine.add(txtGameEnginePath);
		pnlGameEngine.setAlignmentX(cbAnnounceMoves.getAlignmentX());
		pnlGameEngine.add(btnPickGameEngine);
		add(pnlGameEngine);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnPickGameEngine) {
			String newPath = AppFrame.askForPathToEngine(this);
			if (newPath != null && !newPath.equals(txtGameEnginePath.getText())) {
				txtGameEnginePath.setText(newPath);
				prefs.put(AppFrame.PREFS_PATH_TO_GAME_ENGINE, newPath);
			}
		}
		
	}
	
	
}
