package de.toto.gui.swing;

import java.util.*;

import javax.swing.table.*;

import de.toto.game.Game;

@SuppressWarnings("serial")
public class GameTableModel extends AbstractTableModel {
	
	private List<Game> games = new ArrayList<Game>();
	
	private static final String[] COLUMNS = { "White", "Black", "Result", "Date", "WhiteElo", "BlackElo", "ECO", "Event" };

	@Override
	public int getRowCount() {		
		return games.size();
	}

	@Override
	public int getColumnCount() {		
		return COLUMNS.length;
	}
 
	@Override
	public String getColumnName(int columnIndex) {
		return COLUMNS[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {		
		return String.class;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {		
		Game g = games.get(rowIndex);
		
		switch (getColumnName(columnIndex)) {
			case "White": return g.getTagValue("White");
			case "Black": return g.getTagValue("Black");
			case "Result": return g.getTagValue("Result");
			case "Date": return g.getTagValue("Date");
			case "WhiteElo": return g.getTagValue("WhiteElo");
			case "BlackElo": return g.getTagValue("BlackElo");
			case "ECO": return g.getTagValue("ECO");
			case "Event": return g.getTagValue("Event");
			default: return "";
		}
	}
	
	public void addGames(Game..._games) {
		if (_games.length == 0) return;
		int firstNewRow = games.size();
		games.addAll(Arrays.asList(_games));		
		fireTableRowsInserted(firstNewRow,  games.size()-1);
	}
	
	public void setGames(Game..._games) {		
		games.clear();
		games.addAll(Arrays.asList(_games));		
		fireTableDataChanged();
	}
	
	public Game getGameAt(int rowIndex) {
		return games.get(rowIndex);
	}

}
