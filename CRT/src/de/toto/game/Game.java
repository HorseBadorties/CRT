package de.toto.game;

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
	
	public Position getPosition() {
		return currentPosition;
	}
	
	public Position gotoStartPosition() {
		while (!currentPosition.isStartPosition()) {
			currentPosition = currentPosition.previous;
		}
		return currentPosition;
	}
	
	public Position goForward() {
		if (!currentPosition.next.isEmpty()) {
			currentPosition = currentPosition.next.get(0);
			return currentPosition;
		} else return null;
		
	}
	
	public Position goBack() {
		if (!currentPosition.isStartPosition()) {
			currentPosition = currentPosition.previous;
			return currentPosition;
		} else return null;
	}
	
	

}
