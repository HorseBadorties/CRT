package de.toto.gui.swing;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.toto.game.Game;

public class GameListDialog extends JDialog {
	
	private List<ActionListener> ourActionListener = new ArrayList<ActionListener>();	
//	private List<Game> games = new ArrayList<Game>();
	private DefaultListModel<Game> games = new DefaultListModel<Game>();
	private JList<Game> gameList = new JList<Game>(games);
	
	public GameListDialog(Frame owner) {
		super(owner, false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);		
		gameList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				fireActionPerformed(gameList.getSelectedValue());
			}
		});
		getContentPane().add(new JScrollPane(gameList));
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
		games.addElement(g);
	}
	
	public void addGames(List<Game> _games) {
		for (Game g : _games) {
			games.addElement(g);
		}
	}
	
	public void setGames(List<Game> _games) {
		games.clear();
		for (Game g : _games) {
			games.addElement(g);
		}
	}
	
	public int getGamesCount() {
		return games.getSize();
	}
	
}
