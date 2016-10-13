package de.toto.gui.swing;

import java.util.EventListener;

public interface BoardListener extends EventListener {
	
	public void userMove(String move);
	public void userClickedSquare(String squarename);
}
