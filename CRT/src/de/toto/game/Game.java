package de.toto.game;

import java.util.*;
import java.util.logging.Logger;

public class Game {
	
	private static Logger log = Logger.getLogger("Game");
	
	protected Position currentPosition;
	private Map<String, String> tags = new HashMap<String, String>();
	
	private List<GameListener> listener = new ArrayList<GameListener>();
	
	public void addGameListener(GameListener l) {
		listener.add(l);
	}
	
	public void removeGameListener(GameListener l) {
		listener.remove(l);
	}
	
	public Game() {
		
	}
	
	public Game(Position startPosition) {
		currentPosition = startPosition;
		firePositionChangedEvent();
	}
	
	protected void firePositionChangedEvent() {
		GameEvent e = new GameEvent(this);
		for (GameListener l : listener) {
			l.positionChanged(e);
		}
	}
	
	public void start() {
		currentPosition = new Position();
		firePositionChangedEvent();
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
		firePositionChangedEvent();
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
	
	
	private Position findStartPosition() {
		Position p = currentPosition;
		while (p.hasPrevious()) {
			p = p.getPrevious();
		}
		return p;
	}
	
	public Position gotoStartPosition() {
		currentPosition = findStartPosition();
		firePositionChangedEvent();
		return currentPosition;
	}
	
	public Position gotoPosition(Position p) {		
		currentPosition = p;
		firePositionChangedEvent();
		return currentPosition;
	}
	
	/**
	 * Go forward one move, following the main line 
	 */
	public void goForward() {
		if (!currentPosition.hasNext()) return;
		currentPosition = currentPosition.getNext();
		firePositionChangedEvent();	
	}
	
	public boolean hasNext() {
		return currentPosition.hasNext();
	}
		
	public void goBack() {
		if (currentPosition.hasPrevious()) {
			currentPosition = currentPosition.getPrevious();
			firePositionChangedEvent();
		}
	}
	
	public boolean hasPrevious() {
		return currentPosition.hasPrevious();
	}
		
	public void addTag(String name, String value) {
		tags.put(name, value);
	}
	
	public String getTagValue(String tagName) {
		return tags.get(tagName);
	}
	
	public Set<Position> getAllPositions() {
		Set<Position> result = new HashSet<Position>();
		Position p = gotoStartPosition();		
		p = findNextPosition(p);
		while (p != null) {
			result.add(p);
			p = findNextPosition(p);
		}		
		return result;
	}
	
	
	
	public Position doMove(String move) {
		if (getPosition().hasNext()) {
			for (Position p : getPosition().getVariations()) {				
				if (p.getMove().startsWith(move)) {
					currentPosition = p;
					firePositionChangedEvent();
					return currentPosition;
				}
			}
		}
		return null;
	}
	
	public boolean isCorrectMove(String move) {
		if (getPosition().hasNext()) {			
			for (Position p : getPosition().getVariations()) {
				if (move.equals("0-0")) {
					if (p.getMove().startsWith("0-0") && !p.getMove().startsWith("0-0-0")) {
						return true;
					}
				} else {
					if (p.getMove().startsWith(move)) {
						return true;
					}				
				}
			}
		}		
		return false;
	}
	
	private Position findNextPosition(Position p) {		
		if (p.hasVariations()) {
			// enter first variation 
			return p.getVariations().get(1);
		} else if (p.hasNext()) {
			return p.getNext();
		} else {
			if (p.getVariationLevel() == 0) {
				// end of game
				return null;
			} else {
				// end of variation - go back to start of variation and look for the next
				Position headOfVariation = p;
				Position previous = p.getPrevious();
				while (previous.getVariationLevel() != p.getVariationLevel()-1) {
					if (previous.getVariationLevel() == p.getVariationLevel()) headOfVariation = previous;
					previous = previous.getPrevious();
				}
				// now look for the next variation
				List<Position> variations = previous.getVariations();
				int indexOfHeadOfVariation = variations.indexOf(headOfVariation);
				if (indexOfHeadOfVariation < variations.size()-1) {
					return variations.get(indexOfHeadOfVariation+1);
				} else {
					return previous.getNext();
				}				
			}			 
		}
	}
	
	
	public void mergeIn(Game other) {
		other.gotoStartPosition(); 
		gotoStartPosition();
		
		Position first = getPosition();
		Position second = other.getPosition();
		
		for (;;) {
			if (!second.hasNext()) break;
			second = second.getNext();
			if (second == null) break;
			if (!first.hasVariation(second)) {
				first.addVariation(second);
				second.setComment(other.toString());
				log.info(String.format("merged %s as variation of %s", second, first));
				break;
			} else {
				first = first.getVariation(second);
			}				
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s - %s: %s", getTagValue("White"), getTagValue("Black"), getTagValue("Event")); 
	}
	
	public Position findNovelty1(Game other) {
		Position currentPositionBackup = currentPosition;
		try {
			other.gotoStartPosition(); 
			gotoStartPosition();
			
			Position first = getPosition();
			Position second = other.getPosition();
			
			for (;;) {
				if (!second.hasNext()) return null;
				second = second.getNext();
				if (second == null) return null;
				if (!first.hasVariation(second)) {					
					log.info(String.format("findNovelty in %s: %s", other, second));
					return second;					
				} else {
					first = first.getVariation(second);
				}				
			}
		} finally {
			currentPosition = currentPositionBackup;
		}
	}
	
	public Position findNovelty(Game other) {
		Position otherPosition = other.gotoStartPosition();
		
		//go to other's last Position 
		while (otherPosition.hasNext()) {
			otherPosition = otherPosition.getNext();
		}
		
		//find first match 
		while (otherPosition != null) {
			if (this.contains(otherPosition)) break;
			otherPosition = otherPosition.getPrevious();
		}
		
		//novelty is the next move after our match
		Position novelty = null;
		if (otherPosition == null) {
			novelty = other.gotoStartPosition();			
		} else if (otherPosition.hasNext()) {
			novelty = otherPosition.getNext();
		} else {
			novelty = otherPosition;
		}
		log.info(String.format("findNovelty in %s: %s", other, novelty));
		return novelty;
		
	}
	
	public boolean contains(Position aPosition) {
		for (Position p : getAllPositions()) {
			if (p.isSamePositionAs(aPosition)) return true;
		}
		return false;
	}
	
	public String getUCIEngineMoves() {
		StringBuilder result = new StringBuilder();
		Position position = findStartPosition();
		while (position.hasNext()) {
			position = position.getNext();
			result.append(position.getMoveAsEngineMove()).append(" ");			
		}
		return result.toString();
	}
	
	public String getUCIStartFEN() {
		Position startPosition = findStartPosition();
		if (startPosition.isStartPosition()) {
			return null;
		} else {
			return startPosition.getFen();
		}
	}
		
	
}
