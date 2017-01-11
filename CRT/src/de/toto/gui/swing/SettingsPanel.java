package de.toto.gui.swing;

import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class SettingsPanel extends JPanel {
	
	private JCheckBox cbShowBoard;
	private JCheckBox cbShowPieces;	
	private JCheckBox cbShowCoordinates;
	private JCheckBox cbShowArrows;
	private JCheckBox cbShowMetarialImbalance;
	private JCheckBox cbAnnounceMoves;
	
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
		cbShowMetarialImbalance = new JCheckBox(appFrame.actionShowMaterialImbalance);
		cbShowMetarialImbalance.setSelected(prefs.getBoolean(AppFrame.PREFS_SHOW_MATERIAL_IMBALANCE, false));		
		cbAnnounceMoves = new JCheckBox(appFrame.actionAnnounceMoves);
		cbAnnounceMoves.setSelected(prefs.getBoolean(AppFrame.PREFS_ANNOUNCE_MOVES, false));	
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(cbShowBoard);
		add(cbShowPieces);
		add(cbShowCoordinates);
		add(cbShowArrows);
		add(cbShowMetarialImbalance);
		add(cbAnnounceMoves);
	}
	
}
