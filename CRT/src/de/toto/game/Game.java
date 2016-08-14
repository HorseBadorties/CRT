package de.toto.game;

import java.util.*;

public class Game {
	
	private Position currentPosition;
	private Map<String, String> tags = new HashMap<String, String>();
	
	private Set<Position> drilledVariations = new HashSet<Position>();
	private boolean isDrilling = false;
	private boolean drillingWhite = true;
	
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
			System.out.println(p + " hasVariations " + p.getVariations());
			List<Position> candidates = new ArrayList<Position>(p.getVariations());
			candidates.removeAll(drilledVariations);
			if (candidates.isEmpty()) {
				Position previous = p.getPrevious();
				while (!previous.hasVariations() || previous.isWhiteToMove() == drillingWhite) {
					previous = previous.getPrevious();
					if (previous == null) {
						System.out.println("end of lines reached");
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
				System.out.println("returning " + result);
				return result;
			}
		} else if (p.hasNext()) {
			Position result = p.getNext();
			System.out.println(p + " no variations - returning next: " + result);
			return result;
		} else {
			System.out.println(p + " end of line");
			Position previous = p.getPrevious();
			while (!previous.hasVariations() || previous.isWhiteToMove() == drillingWhite) {
				previous = previous.getPrevious();
			}
			System.out.println("finding next for " + previous);
			return findNextPosition2(previous);
		}
	}
	
	public Position gotoNextPosition() {
		Position next = findNextPosition(currentPosition);
//		System.out.println(String.format("findNextPosition for %s: %s with previous %s", currentPosition, next, next.getPrevious()));
		if (next == null) {
			System.out.println("reached end of variation tree with last position " + currentPosition);
			return currentPosition;
		}
		if (currentPosition != next.getPrevious()) {
			System.out.println("jumpt to next variation with " + next);
		}
		currentPosition = next;
		return currentPosition;
	}
	
	public void beginDrill() {
		drilledVariations.clear();
		isDrilling = true;
		drillingWhite = getPosition().isWhiteToMove();
		System.out.println(String.format("Drilling %s now", drillingWhite ? "White" : "Black"));
	}
	
	public void endDrill() {
		isDrilling = false;
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
				System.out.println(String.format("merged %s as variation of %s", second, first));
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
	
	public static void main(String[] args) {
		Game g1 = new Game();
		g1.start();
		g1.addMove("d4"); g1.addMove("Nf6");
		Game g2 = new Game();
		g2.start();
		g2.addMove("e4"); g2.addMove("c5");
		g1.mergeIn(g2);
		System.out.println(g1.gotoStartPosition().getVariationCount());
	}
	
	
}
