package de.toto.game;

import java.util.ArrayList;
import java.util.List;

public class Game {
	private Position currentPosition;
	
	public void start() {
		currentPosition = new Position();
	}
	
	public Position startNewVariation() {
		return null;
	}
	
	public Position addMove(String move) {
		return addMove(move, null);
	}
	
	public Position addMove(String move, String fen) {
		currentPosition = new Position(currentPosition, move, fen);
		return currentPosition;
	}
	
	public Position addMoves(List<String> moves) {
		return addMoves(moves.toArray(new String[0]));		
	}
	
	public Position addMoves(String[] moves) {
		for (String move : moves) {
			addMove(move);
		}
		return currentPosition;
	}
	
	public Position getPosition() {
		return currentPosition;
	}
	
	public Position gotoStartPosition() {
		while (!currentPosition.hasPrevious()) {
			currentPosition = currentPosition.getPrevious();
		}
		return currentPosition;
	}
	
	public Position goForward() {
		if (currentPosition.hasNext()) {
			currentPosition = currentPosition.getNext();
			return currentPosition;
		} else return null;
		
	}
	
	public Position goBack() {
		if (!currentPosition.hasPrevious()) {
			currentPosition = currentPosition.getPrevious();
			return currentPosition;
		} else return null;
	}
	
	public List<String> dumpMoves() {
		List<String> result = new ArrayList<String>();
		Position p = currentPosition;
		while (!p.hasPrevious()) {
			p = p.getPrevious();
		}
		for(;;) {
			result.add(p.getMove());
			if (!p.hasNext()) break;
			p = p.getNext();
		}
		return result;
	}
	
	@Override
	public String toString() {
		return dumpMoves().toString(); 
	}
	
}
