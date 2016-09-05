package de.toto.game;

import java.util.EventObject;

@SuppressWarnings("serial")
public class DrillEvent extends EventObject {
	
	private int id;
	
	public static final int ID_DRILL_ENDED = 1;
	public static final int ID_WAS_CORRECT = 2;
	public static final int ID_WAS_INCORRECT = 3;
	public static final int ID_DRILLING_NEXT_VARIATION = 4;
		
	private String lastMove;

	public DrillEvent(int id, Object source, String lastMove) {
		super(source);
		this.id = id;
		this.lastMove = lastMove;
	}
	
	public String getLastMove() {
		return lastMove;
	}
	
	public int getID() {
		return id;
	}
	
	public Drill getDrill() {
		return (Drill)getSource();
	}

}
