package de.toto.gui.swing;

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
		if (p != null) {
			return p.getMoveNotation(p.whiteMoved());
		} else {
			if (columnIndex == 1) {
				return "";
			} else {
				Position blackMove = getPositionAt(rowIndex, 1);
				return blackMove != null ? blackMove.getMoveNumber()+"..." : "";
			}
		}
	}
	
	public Position getPositionAt(int rowIndex, int columnIndex) {		
		List<Position> l = columnIndex == 0 ? white : black;
		return l.size() <= rowIndex ? null : l.get(rowIndex);
	}
	
	public void setPosition(Position p) {
		white.clear();
		black.clear();
		List<Position> moves = new ArrayList<Position>();		
		for (;;) {
			if (p.getMoveNumber() > 0) {
				moves.add(0, p);
			}
			if (p.hasPrevious()) {
				p = p.getPrevious();
			} else {
				break;
			}
		}		
		//if the first move is a black move add a null Position into white
		if (!moves.isEmpty() && !moves.get(0).whiteMoved()) {
			white.add(null);
		}
		for (Position pos : moves) {
			List<Position> l = pos.whiteMoved() ? white : black;
			l.add(pos);			
		}
		fireTableDataChanged();
	}
	

}
