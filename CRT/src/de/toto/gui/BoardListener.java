package de.toto.gui;

import java.util.EventListener;

public interface BoardListener extends EventListener {
	
	public void userMove(String move);
	public void userClickedSquare(String squarename);
}
