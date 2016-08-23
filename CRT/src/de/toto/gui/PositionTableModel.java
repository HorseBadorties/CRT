package de.toto.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.toto.game.Position;

@SuppressWarnings("serial")
public class PositionTableModel extends AbstractTableModel {
	
	private List<Position> white = new ArrayList<Position>();
	private List<Position> black = new ArrayList<Position>();

	@Override
	public int getRowCount() {
		return white.size();
	}

	@Override
	public int getColumnCount() {		
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {		
		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {		
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {		
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {		
		Position p = getPositionAt(rowIndex, columnIndex);
		return p != null ? p.getMoveNotation(p.whiteMoved()) : "";
	}
	
	public Position getPositionAt(int rowIndex, int columnIndex) {		
		List<Position> l = columnIndex == 0 ? white : black;
		return l.size() <= rowIndex ? null : l.get(rowIndex);
	}
	
	public void setPosition(Position p) {
		white.clear();
		black.clear();
		Position _p = p;
		while (_p != null) {
			putPosition(_p);
			_p = _p.getPrevious();
		}
		fireTableDataChanged();
	}
	
	private void putPosition(Position p) {
		if (p.getPrevious() == null) return;
		List<Position> l = p.whiteMoved() ? white : black;
		if (l.isEmpty()) {
			l.add(p);
		} else {
			l.add(0, p);
		}
	}
	

}
