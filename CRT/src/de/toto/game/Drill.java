package de.toto.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Drill extends Game {
	
	private static Logger log = Logger.getLogger("Drill");
	
	private Position drillStartingPosition;
	private Set<Position> drilledVariations = new HashSet<Position>();
	private boolean drillingWhite;
	private boolean acceptOnlyMainline;
	private List<Position> drillPositions;
	private DrillStats drillStats;
	
	public static class DrillStats {
		public Position lastDrilledPosition;
		public int drilledPositions;
		public int correctPositions;
	}
	
	
	public Drill(Position startPosition, boolean drillingWhite, boolean acceptOnlyMainline, boolean randomDrill) {
		drillStats = new DrillStats();
		currentPosition = startPosition;
		drillStartingPosition = startPosition;
		this.drillingWhite = drillingWhite;
		this.acceptOnlyMainline = acceptOnlyMainline;		
		drillPositions = getAllDrillPositions();
		drilledVariations.clear();		
		if (randomDrill) Collections.shuffle(drillPositions);
		drillStats = new DrillStats();
		currentPosition = getNextDrillPosition();
		log.info(String.format("Drilling %s now for %d positions", drillingWhite ? "White" : "Black", drillPositions.size()));
	}	
	
	public boolean isCorrectMove(String move) {
		boolean result = false;
		if (currentPosition.hasNext()) {
			if (acceptOnlyMainline) {
				result = isCorrect(move, currentPosition.getNext());
			} else {
				for (Position p : currentPosition.getVariations()) {
					if (isCorrect(move, p)) {
						result = true;
						break;
					}				
				}
			}
		}
		if (currentPosition != drillStats.lastDrilledPosition) {
			if (result) drillStats.correctPositions++;
			drillStats.lastDrilledPosition = currentPosition;
		}		
		return result;
	}
	
	public boolean isCorrectSquare(String squareName) {
		boolean result = false;
		Position correctPosition = currentPosition.hasNext() ? currentPosition.getNext() : null;
		//TODO !acceptOnlyMainline
		if (correctPosition != null) {			
			if (correctPosition.getMoveSquareNames()[1].equals(squareName)) result = true;
			if (currentPosition != drillStats.lastDrilledPosition) {
				if (result) drillStats.correctPositions++;
				drillStats.lastDrilledPosition = currentPosition;
			}			
		}				
		return result;
	}
	
	private boolean isCorrect(String move, Position p) {
		if (p.getNagsAsString().startsWith("?")) return false; //don't accept "?" or "?!" moves
		if (move.equals("0-0")) {
			return p.getMove().startsWith("0-0") && !p.getMove().startsWith("0-0-0");
		} else {
			return p.getMove().startsWith(move);
		}
	}
	
	private Position findNextPosition(Position p) {
		if (p.hasVariations()) {
			if (p.isWhiteToMove() == drillingWhite) {
				Position next = p.getNext();
				if (next.equals(drillStartingPosition)) {
					log.info("drillStartingPosition reached - no more lines");
					return null;
				} else {
					return next;
				}
			}
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
						log.info("drillStartingPosition reached - no more lines");
						return null;
					}
					previous = previous.getPrevious();
					if (previous == null) {
						log.info("end of lines reached");
						return null;
					}
				}				
				return findNextPosition(previous);
			} else {
				if (candidates.size() > 1) {
					Collections.shuffle(candidates);
				}
				drilledVariations.add(candidates.get(0));
				Position result = candidates.get(0);
				log.info("returning " + result);				
				return result;
			}
		} else if (p.hasNext()) {
			Position result = p.getNext();
			log.info(p + " no variations - returning next: " + result);			
			return result;
		} else {
			log.info(p + " end of line");
			Position previous = p.getPrevious();
			if (previous == null) {
				log.info("end of lines reached");
				return null;
			}
			while (!previous.hasVariations() || previous.isWhiteToMove() == drillingWhite) {
				if (previous.equals(drillStartingPosition)) {
					log.info("drillStartingPosition reached - no more lines");
					return null;
				}
				previous = previous.getPrevious();
				if (previous == null) {
					log.info("end of lines reached");
					return null;
				}
			}
			log.info("finding next for " + previous);			
			return findNextPosition(previous);
		}
	}
	
	public Position getNextDrillPosition() {
		if (drillPositions.isEmpty()) {			
			return currentPosition;
		} else {
			currentPosition = drillPositions.remove(0);
			drillStats.drilledPositions++;
			return currentPosition;
		}
	}
	
	private Position gotoNextPosition() {
		Position next = findNextPosition(currentPosition);
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
	
	
	public DrillStats endDrill() {		
		log.info(String.format("Drill ended - %d of %d positions correct", drillStats.correctPositions, drillStats.drilledPositions));
		return drillStats;		
	}
	
	private List<Position> getAllDrillPositions() {
		List<Position> result = new ArrayList<Position>();		
		Position drillPosition = currentPosition;
		if (drillPosition.isWhiteToMove() != drillingWhite) {
			drillPosition = gotoNextPosition();
		}
		for(;;) { 
			Position repertoireAnswer = drillPosition.hasNext() ? drillPosition.getNext() : null;
			Position nextDrillPosition = null;			
			if (repertoireAnswer != null) {
				result.add(drillPosition);
				currentPosition = repertoireAnswer;
				nextDrillPosition = gotoNextPosition();
			} else {
				nextDrillPosition = gotoNextPosition();
			}
			if (nextDrillPosition.equals(drillPosition) 
					|| nextDrillPosition == null
					|| nextDrillPosition.equals(drillStartingPosition)
					|| nextDrillPosition.getMoveNumber() <= drillStartingPosition.getMoveNumber()) 
			{
				log.info("end of line");
				break;
			}
			drillPosition = nextDrillPosition;
		} 
		return result;
	}
		
}
