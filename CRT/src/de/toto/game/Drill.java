package de.toto.game;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
		
		public long drillBegin;
		public Position lastDrilledPosition;
		public int drilledPositions;
		public int correctPositions;
		
		public DrillStats() {
			drillBegin = System.currentTimeMillis();
		}
		
		public long getDuration() {
			return System.currentTimeMillis() - drillBegin;
		}
		
		public String getFormattedDuration() {
			StringBuilder result = new StringBuilder();
			long duration = getDuration();
			long hours = duration / 3600000;
			if (hours > 0) {
				result.append(hours).append(hours > 1 ? " hours" : " hour");
			
			}
			duration = duration % 3600000;
			long minutes = duration / 60000;
			if (minutes > 0) {
				if (result.length() > 0) result.append(", ");
				result.append(minutes).append(minutes > 1 ? " minutes" : " minute");
			
			}
			duration = duration % 60000;			
			if (duration > 0) {
				if (result.length() > 0) result.append(", ");
				long seconds = duration / 1000; 
				result.append(seconds).append(seconds > 1 ? " seconds" : " second");
			
			}
			return result.toString();
		}
		
	}
	
	private List<DrillListener> listener = new ArrayList<DrillListener>();
	
	public void addDrillListener(DrillListener l) {
		listener.add(l);		
	}
	
	public void removeDrillListener(DrillListener l) {
		listener.remove(l);
	}
	
	private void fireDrillEvent(DrillEvent e) {		
		for (DrillListener l : listener) {
			switch (e.getID()) {
				case DrillEvent.ID_DRILL_ENDED : l.drillEnded(e); break;
				case DrillEvent.ID_DRILLING_NEXT_VARIATION : l.drillingNextVariation(e); break;
				case DrillEvent.ID_WAS_CORRECT : l.wasCorrect(e); break;
				case DrillEvent.ID_WAS_INCORRECT : l.wasIncorrect(e); break;
			}
		}
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
			if (result) {
				drillStats.correctPositions++;
				fireDrillEvent(new DrillEvent(DrillEvent.ID_WAS_CORRECT, this, move));		
			} else {
				fireDrillEvent(new DrillEvent(DrillEvent.ID_WAS_INCORRECT, this, move));	
			}
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
				if (result) {
					drillStats.correctPositions++;
					fireDrillEvent(new DrillEvent(DrillEvent.ID_WAS_CORRECT, this, squareName));		
				} else {
					fireDrillEvent(new DrillEvent(DrillEvent.ID_WAS_INCORRECT, this, squareName));	
				}
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
			endDrill();
			return currentPosition;
		} else {
			currentPosition = drillPositions.remove(0);
			firePositionChangedEvent();
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
	
	public void startDrill() {
		currentPosition = getNextDrillPosition();
		firePositionChangedEvent();
	}
	
	
	public void endDrill() {		
		gotoPosition(drillStartingPosition);
		log.info(String.format("Drill ended - %d of %d positions correct", drillStats.correctPositions, drillStats.drilledPositions));
		fireDrillEvent(new DrillEvent(DrillEvent.ID_DRILL_ENDED, this, null));		
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
	
	public DrillStats getDrillStats() {
		return drillStats;
	}
	
	public int getPositionCount() {
		return drillPositions.size() + drillStats.drilledPositions;
	}
			
}
