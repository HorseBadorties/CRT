package de.toto.game;

import java.util.ArrayList;
import java.util.List;

public class Position {
	
	int variationLevel = 0; //0 = main line, 1 = variation, 2 = variation of variation ...
	Position previous = null;
	List<Position> next = new ArrayList<Position>(); 
	String fen = null;
	String move = null;
	boolean whiteToMove = true;
	String comment = null; // may contain graphics token such as [%csl Ge5][%cal Ge5b2]
	List<String> nags = new ArrayList<String>(); // !, ?, ?? ...
	
	
	// Startposition
	public Position() {
		fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"; 
		whiteToMove = true;
	}

	public Position(Position previous, String move) {
		this(previous, move, null);
	}
	
	public Position(Position previous, String move, String fen) {
		this.previous = previous;
		previous.addNextPosition(this);
		this.whiteToMove = !previous.whiteToMove;
		this.move = move;
		this.variationLevel = previous.variationLevel;
		this.fen = fen;
	}
	
	public boolean wasCapture() {
		return move != null && move.contains("x");
	}
	
	public boolean isCheck() {
		return move != null && move.endsWith("+");
	}
	
	public boolean isMate() {
		return move != null && move.endsWith("#");
	}
	
	public boolean isStartPosition() {
		return previous == null;
	}
	
	public int getMoveNumber() {
		int moveCount = 0;
		Position halfMove = this;
		do  {
			if (!halfMove.whiteToMove) moveCount++;
			halfMove = halfMove.previous;
		} while (halfMove != null);
		return moveCount;
		
	}
	
	public String getFEN() {
		return fen;
	}
	
	private void addNextPosition(Position nextPosition) {
		next.add(nextPosition);
	}
	
	
}
