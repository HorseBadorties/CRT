package de.toto.game;

import java.util.*;

public class Game {
	
	private Position currentPosition;
	private Map<String, String> tags;
	
	
	public Game() {
		tags = new HashMap<String, String>();
	}

	public void start() {
		currentPosition = new Position();
	}
	
	/**
	 * Starts a new variation for the last move
	 * 
	 */
	public Position newVariation(String move) {
		currentPosition = new Position(currentPosition.getPrevious(), move, null, true);		
		return currentPosition;
	}
	
	/**
	 * Go back to last move of parent variation 
	 */
	public Position endVariation() {
		int variationLevel = currentPosition.getVariationLevel();
		do {
			goBack();
		} while (variationLevel == currentPosition.getVariationLevel());
		goForward();
		return currentPosition;
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
	
	/**
	 * Go forward one move, not following variations
	 * @return
	 */
	public Position goForward() {
		return goForward(false);		
	}
	
	/**
	 * Go forward one move, possibly following variations
	 */
	public Position goForward(boolean followVariations) {
		if (!currentPosition.hasNext()) return null;
		if (!followVariations) {
			currentPosition = currentPosition.getNext();			
		} else {
			
		}
		return currentPosition; 
		
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
	
	public void addTag(String name, String value) {
		tags.put(name, value);
	}
	
	public String getTagValue(String tagName) {
		return tags.get(tagName);
	}
	
	@Override
	public String toString() {
		return getTagValue("Event"); 
	}
	
	
}
