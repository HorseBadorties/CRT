package de.toto.game;

import java.util.*;
import java.util.logging.Logger;

public class Game {
	
	private static Logger log = Logger.getLogger("Game");
	
	private Position currentPosition;
	private Map<String, String> tags = new HashMap<String, String>();
	
	private Position drillStartingPosition;
	private Set<Position> drilledVariations = new HashSet<Position>();
	private boolean isDrilling = false;
	private boolean drillingWhite = true;
	private boolean acceptOnlyMainline = true;
	private boolean randomDrill = true;
	private List<Position> drillPositions;
	private DrillStats drillStats;
	
	public static class DrillStats {
		public int drilledPositions;
		public int correctPositions;
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
	
	public Position gotoPosition(Position p) {		
		currentPosition = p;		
		return currentPosition;
	}
	
	/**
	 * Go forward one move, following the main line 
	 */
	public Position goForward() {
		if (!currentPosition.hasNext()) return null;
		currentPosition = currentPosition.getNext();		
		return currentPosition; 	
	}
		
	public Position goBack() {
		if (!currentPosition.hasPrevious()) {
			currentPosition = currentPosition.getPrevious();
			return currentPosition;
		} else return null;
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
		result.add(p);
		p = findNextPosition(p);
		while (p != null) {
			result.add(p);
			p = findNextPosition(p);
		}		
		return result;
	}
	
	public boolean hasNextPosition(String move) {
		boolean result = false;
		if (getPosition().hasNext()) {
			if (isDrilling() && acceptOnlyMainline) {
				result = isCorrect(move, getPosition().getNext());
			} else {
				for (Position p : getPosition().getVariations()) {
					if (isCorrect(move, p)) {
						result = true;
						break;
					}				
				}
			}
		}
		if (isDrilling && result) drillStats.correctPositions++;
		return result;
	}
	
	private boolean isCorrect(String move, Position p) {
		if (isDrilling && p.getNagsAsString().startsWith("?")) return false; //don't accept "?" or "?!" moves
		if (move.equals("0-0")) {
			return p.getMove().startsWith("0-0") && !p.getMove().startsWith("0-0-0");
		} else {
			return p.getMove().startsWith(move);
		}
	}
	
	public Position doMove(String move) {
		if (getPosition().hasNext()) {
			for (Position p : getPosition().getVariations()) {				
				if (p.getMove().startsWith(move)) {
					currentPosition = p;
					return currentPosition;
				}
			}
		}
		return null;
	}
	
	private Position findNextPosition(Position p) {
		if (isDrilling) return findNextPosition2(p);
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
	
	private Position findNextPosition2(Position p) {
		if (p.hasVariations()) {
			if (p.isWhiteToMove() == drillingWhite) return p.getNext();
			log.info(p + " hasVariations " + p.getVariations());
			List<Position> candidates = new ArrayList<Position>(p.getVariations());
			candidates.removeAll(drilledVariations);
			if (candidates.isEmpty()) {
				Position previous = p.getPrevious();
				if (previous == null) {
					log.info("end of lines reached");
					return null;
				}
				while (!previous.hasVariations() || previous.isWhiteToMove() == drillingWhite) {
					if (previous.equals(drillStartingPosition)) {
						log.info("end of lines reached");
						return null;
					}
					previous = previous.getPrevious();
					if (previous == null) {
						log.info("end of lines reached");
						return null;
					}
				}
				return findNextPosition2(previous);
			} else {
				if (candidates.size() > 1) {
					Collections.shuffle(candidates);
				}
				drilledVariations.add(candidates.get(0));
				Position result = candidates.get(0);
				log.info("returning " + result);
				drillStats.drilledPositions++;
				return result;
			}
		} else if (p.hasNext()) {
			Position result = p.getNext();
			log.info(p + " no variations - returning next: " + result);
			drillStats.drilledPositions++;
			return result;
		} else {
			log.info(p + " end of line");
			Position previous = p.getPrevious();
			if (previous == null) {
				log.info("end of lines reached");
				return null;
			}
			while (!previous.hasVariations() || previous.isWhiteToMove() == drillingWhite) {
				previous = previous.getPrevious();
				if (previous == null) {
					log.info("end of lines reached");
					return null;
				}
			}
			log.info("finding next for " + previous);			
			return findNextPosition2(previous);
		}
	}
	
	public Position gotoNextPosition() {
		if (randomDrill) {
			if (drillPositions.isEmpty()) {
				log.info("reached end of random drill");
				return currentPosition;
			} else {
				currentPosition = drillPositions.remove(0);
				return currentPosition;
			}
		}
		Position next = findNextPosition(currentPosition);
//		log.info(String.format("findNextPosition for %s: %s with previous %s", currentPosition, next, next.getPrevious()));
		if (next == null) {
			log.info("reached end of variation tree with last position " + currentPosition);
			return currentPosition;
		}
		if (currentPosition != next.getPrevious()) {
			log.info("jumped to next variation with " + next);
		}
		currentPosition = next;
		return currentPosition;
	}
	
	public void beginDrill(boolean white, boolean acceptOnlyMainline) {
		randomDrill = false;
		drillStartingPosition = getPosition();
		drilledVariations.clear();
		drillStats = new DrillStats();
		isDrilling = true;
		drillingWhite = white;
		this.acceptOnlyMainline = acceptOnlyMainline;
		if (currentPosition.isWhiteToMove() != drillingWhite) {
			gotoNextPosition();
		}
		drillPositions = getAllDrillPositions();
		drilledVariations.clear();
		Collections.shuffle(drillPositions);
		randomDrill = true;
		log.info(String.format("Drilling %s now for %d positions", drillingWhite ? "White" : "Black", drillPositions.size()));
		
	}
	
	public DrillStats endDrill() {
		isDrilling = false;
		log.info(String.format("Drill ended - %d of %d positions correct", drillStats.correctPositions, drillStats.drilledPositions));
		return drillStats;
		
	}
	
	private List<Position> getAllDrillPositions() {
		List<Position> result = new ArrayList<Position>();
		//drill has just begun - we are on our first drill prosition already
		Position drillPosition = getPosition();
		for(;;) { 
			Position repertoireAnswer = drillPosition.hasNext() ? drillPosition.getNext() : null;
			Position nextDrillPosition = null;
			if (repertoireAnswer != null) {
				result.add(getPosition());
				gotoPosition(repertoireAnswer);
				nextDrillPosition = gotoNextPosition();
			} else {
				nextDrillPosition = gotoNextPosition();
			}
			if (nextDrillPosition.equals(drillPosition) || nextDrillPosition == null) break;
			drillPosition = nextDrillPosition;
		} 
		gotoPosition(drillStartingPosition);
		return result;
	}
	
	public boolean isDrilling() {
		return isDrilling;
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
		
	
}
