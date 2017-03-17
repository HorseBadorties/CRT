package de.toto.gui.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.toto.game.Game;

@SuppressWarnings("serial")
public class GameListDialog extends JDialog {
	
	private GameTableModel gameTableModel = new GameTableModel();
	private JTable tblGames = new JTable(gameTableModel);
	private List<ActionListener> ourActionListener = new ArrayList<ActionListener>();
	
	private Preferences prefs = Preferences.userNodeForPackage(AppFrame.class);
	
	private static final String PREFS_GAME_LIST_DIALOG_HEIGHT = "GAME_LIST_DIALOG_HEIGHT";
	private static final String PREFS_GAME_LIST_DIALOG_WIDTH = "GAME_LIST_DIALOG_WIDTH";
	
	
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
		tblGames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblGames.setAutoCreateRowSorter(true);
		getContentPane().add(new JScrollPane(tblGames));
		
		String fontName = prefs.get(AppFrame.PREFS_FONT_NAME, "Frutiger Standard");
		int fontSize = prefs.getInt(AppFrame.PREFS_FONT_SIZE, 12); 
		Font font = new Font(fontName, Font.PLAIN, fontSize);
		tblGames.setFont(font);
		tblGames.setRowHeight(fontSize + 6);
		
		setPreferredSize(new Dimension(prefs.getInt(PREFS_GAME_LIST_DIALOG_WIDTH, 500),
				(prefs.getInt(PREFS_GAME_LIST_DIALOG_HEIGHT, 400))));
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Dimension size = GameListDialog.this.getSize();
				prefs.putInt(PREFS_GAME_LIST_DIALOG_WIDTH, size.width);
				prefs.putInt(PREFS_GAME_LIST_DIALOG_HEIGHT, size.height);
			}
		});
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
