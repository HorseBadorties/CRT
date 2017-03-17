package de.toto.gui.swing;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.toto.game.Game;

public class GameListDialog extends JDialog {
	
	private GameTableModel gameTableModel = new GameTableModel();
	private JTable tblGames = new JTable(gameTableModel);
	private List<ActionListener> ourActionListener = new ArrayList<ActionListener>();	
	
	public GameListDialog(Frame owner) {
		super(owner, false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		tblGames.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int modelRow = tblGames.convertRowIndexToModel(tblGames.getSelectedRow());
				fireActionPerformed(gameTableModel.getGameAt(modelRow));
			}
		});
		tblGames.setRowSelectionAllowed(true);
		tblGames.setColumnSelectionAllowed(false);
		tblGames.setCellSelectionEnabled(false);
		tblGames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblGames.setAutoCreateRowSorter(true);
		getContentPane().add(new JScrollPane(tblGames));
		
		Preferences prefs = Preferences.userNodeForPackage(AppFrame.class);
		String fontName = prefs.get(AppFrame.PREFS_FONT_NAME, "Frutiger Standard");
		int fontSize = prefs.getInt(AppFrame.PREFS_FONT_SIZE, 12); 
		tblGames.setFont(new Font(fontName, Font.PLAIN, fontSize));
		
		
	}
	
	public void addActionListener(ActionListener e) {
		ourActionListener.add(e);
	}
	
	public void removeActionListener(ActionListener e) {
		ourActionListener.remove(e);
	}
	
	private void fireActionPerformed(Game g) {
		ActionEvent e = new ActionEvent(g, 0, "Game selected");
		for (ActionListener aListener : ourActionListener) {
			aListener.actionPerformed(e);
		}
	}
	
	public void addGame(Game g) {
		gameTableModel.addGames(g);
	}
	
	public void addGames(List<Game> _games) {
		for (Game g : _games) {
			gameTableModel.addGames(g);
		}
	}
	
	public void setGames(List<Game> _games) {
		gameTableModel.setGames(_games.toArray(new Game[0]));
	}
	
	public int getGamesCount() {
		return gameTableModel.getRowCount();
	}
	
}
