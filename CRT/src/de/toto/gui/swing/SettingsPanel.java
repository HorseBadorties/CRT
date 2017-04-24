package de.toto.gui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SettingsPanel extends JPanel implements ActionListener {
	
	private AppFrame appFrame;
	private JTabbedPane tabs;
	private JComboBox cmbBoard;
	private JComboBox cmbPieces;
	private JCheckBox cbShowBoard;
	private JCheckBox cbShowPieces;	
	private JCheckBox cbShowCoordinates;
	private JCheckBox cbShowArrows;
	private JCheckBox cbShowEngineArrows;
	private JCheckBox cbShowMaterialImbalance;
	private JCheckBox cbMoveSounds;
	private JCheckBox cbAnnounceMoves;
	private JCheckBox cbStealthMode;
	private SpinnerModel spinnerModel;  
	private JSpinner spinnerDelayAfterMove;
	private JCheckBox cbAcceptMainlineOnly;
	private JLabel txtEnginePath;
	private JButton btnPickEngine;
	private JLabel txtGameEnginePath;
	private JButton btnPickGameEngine;	
	
	private Preferences prefs = Preferences.userNodeForPackage(AppFrame.class);
	
	public SettingsPanel(AppFrame appFrame) {
		
		this.appFrame = appFrame;
		cmbBoard = new JComboBox<String>(Board.BOARD_NAMES);
		cmbBoard.setSelectedItem(prefs.get(AppFrame.PREFS_BOARD_NAME, "Brown"));
		cmbBoard.addActionListener(this);
		cmbPieces = new JComboBox<String>(Board.PIECES_NAMES);
		cmbPieces.setSelectedItem(prefs.get(AppFrame.PREFS_PIECES_NAME, "merida"));
		cmbPieces.addActionListener(this);
		cbShowBoard = new JCheckBox(appFrame.actionShowBoard);
		cbShowBoard.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_BOARD, true));	
		cbShowPieces = new JCheckBox(appFrame.actionShowPieces);
		cbShowPieces.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_PIECES, true));	
		cbShowCoordinates = new JCheckBox(appFrame.actionShowCoordinates);
		cbShowCoordinates.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_COORDINATES, false));
		cbShowArrows = new JCheckBox(appFrame.actionShowArrows);
		cbShowArrows.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_ARROWS, true));	
		cbShowEngineArrows = new JCheckBox(appFrame.actionShowEngineArrows);
		cbShowEngineArrows.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_ENGINE_ARROWS, true));	
		cbShowMaterialImbalance = new JCheckBox(appFrame.actionShowMaterialImbalance);
		cbShowMaterialImbalance.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_MATERIAL_IMBALANCE, false));		
		cbMoveSounds = new JCheckBox(appFrame.actionPlayMoveSound);
		cbMoveSounds.setSelected(prefs.getBoolean(AppFrame.PREFS_PLAY_MOVE_SOUNDS, true));		
		cbAnnounceMoves = new JCheckBox(appFrame.actionAnnounceMoves);
		cbAnnounceMoves.setSelected(prefs.getBoolean(AppFrame.PREFS_ANNOUNCE_MOVES, false));	
		cbStealthMode = new JCheckBox(appFrame.actionStealthMode);
		cbStealthMode.setSelected(prefs.getBoolean(AppFrame.PREFS_STEALTH_MODE, false));	
		
		spinnerModel = new SpinnerNumberModel(prefs.getInt(AppFrame.PREFS_DELAY_AFTER_MOVE, 500), 0, 3000, 100);  
		spinnerModel.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {				
				int value = ((SpinnerNumberModel)spinnerModel).getNumber().intValue();
				prefs.putInt(AppFrame.PREFS_DELAY_AFTER_MOVE, value);
				
			}
		});
		spinnerDelayAfterMove = new JSpinner(spinnerModel);
		cbAcceptMainlineOnly = new JCheckBox(appFrame.actionAcceptMainLineOnly);
		cbAcceptMainlineOnly.setSelected(prefs.getBoolean(AppFrame.PREFS_ONLY_MAINLINE, true));	
		txtEnginePath = new JLabel(prefs.get(AppFrame.PREFS_PATH_TO_ENGINE, ""));
		txtEnginePath.setBorder(BorderFactory.createLoweredBevelBorder());	
		btnPickEngine = new JButton("..");
		btnPickEngine.addActionListener(this);		
		txtGameEnginePath = new JLabel(prefs.get(AppFrame.PREFS_PATH_TO_GAME_ENGINE, ""));
		txtGameEnginePath.setBorder(BorderFactory.createLoweredBevelBorder());	
		btnPickGameEngine = new JButton("..");
		btnPickGameEngine.addActionListener(this);
		
		
		
		tabs = new JTabbedPane();
		JPanel pnlBoardOptions = createTabPanel();
		tabs.add("Board Options", pnlBoardOptions);
		JPanel pnlBoard = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,0));
		pnlBoard.add(new JLabel("Board: "));
		pnlBoard.add(cmbBoard);
		pnlBoard.setAlignmentX(LEFT_ALIGNMENT);
		pnlBoardOptions.add(pnlBoard);
		JPanel pnlPieces = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,0));
		pnlPieces.add(new JLabel("Pieces: "));
		pnlPieces.add(cmbPieces);
		pnlPieces.setAlignmentX(LEFT_ALIGNMENT);
		pnlBoardOptions.add(pnlPieces);
		pnlBoardOptions.add(cbShowBoard);
		pnlBoardOptions.add(cbShowPieces);
		pnlBoardOptions.add(cbShowCoordinates);
		pnlBoardOptions.add(cbShowArrows);
		pnlBoardOptions.add(cbShowEngineArrows);
		pnlBoardOptions.add(cbShowMaterialImbalance);
		pnlBoardOptions.add(cbMoveSounds);
		pnlBoardOptions.add(cbAnnounceMoves);
		pnlBoardOptions.add(cbStealthMode);
		
		JPanel pnlDrillOptions = createTabPanel();
		tabs.add("Drill Options", pnlDrillOptions);
		JPanel pnlDelayAfterMove = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,0));
		pnlDelayAfterMove.add(new JLabel("Delay in milliseconds after drill move: "));
		pnlDelayAfterMove.add(spinnerDelayAfterMove);
		pnlDelayAfterMove.setAlignmentX(LEFT_ALIGNMENT);
		pnlDelayAfterMove.setMaximumSize(pnlDelayAfterMove.getPreferredSize());
		pnlDrillOptions.add(pnlDelayAfterMove);
		pnlDrillOptions.add(cbAcceptMainlineOnly);
		
		
		JPanel pnlEngineOptions = createTabPanel();
		tabs.add("Engine Options", pnlEngineOptions);
		JPanel pnlEngine = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,0));
		pnlEngine.add(new JLabel("Analysis engine: "));
		pnlEngine.add(txtEnginePath);
		pnlEngine.add(btnPickEngine);
		pnlEngine.setAlignmentX(LEFT_ALIGNMENT);
		pnlEngine.setMaximumSize(pnlEngine.getPreferredSize());
		pnlEngineOptions.add(pnlEngine);
		JPanel pnlGameEngine = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,0));
		pnlGameEngine.add(new JLabel("Game engine: "));
		pnlGameEngine.add(txtGameEnginePath);
		pnlGameEngine.add(btnPickGameEngine);
		pnlGameEngine.setAlignmentX(LEFT_ALIGNMENT);
		pnlGameEngine.setMaximumSize(pnlGameEngine.getPreferredSize());
		pnlEngineOptions.add(pnlGameEngine);
		
		
		setLayout(new BorderLayout());
		add(tabs);		
		
	}
	
	private static JPanel createTabPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));
		result.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		result.setAlignmentX(LEFT_ALIGNMENT);
		//result.setAlignmentY(TOP_ALIGNMENT);
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cmbBoard) {
			prefs.put(AppFrame.PREFS_BOARD_NAME, cmbBoard.getSelectedItem().toString());
		} else if (e.getSource() == cmbPieces) {
			prefs.put(AppFrame.PREFS_PIECES_NAME, cmbPieces.getSelectedItem().toString());
		} else {
			String pref = e.getSource() == btnPickGameEngine ? AppFrame.PREFS_PATH_TO_GAME_ENGINE : AppFrame.PREFS_PATH_TO_ENGINE;
			JLabel textField = e.getSource() == btnPickGameEngine ? txtGameEnginePath : txtEnginePath; 
			
			String newPath = AppFrame.askForPathToEngine(this, prefs.get(pref, null));
			if (newPath != null && !newPath.equals(textField.getText())) {
				textField.setText(newPath);
				prefs.put(pref, newPath);			
			}
		}		
	}
	
	
}
