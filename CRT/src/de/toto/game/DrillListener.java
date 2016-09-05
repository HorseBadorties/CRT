package de.toto.game;

public interface DrillListener {

	public void drillEnded(DrillEvent e);
	public void wasCorrect(DrillEvent e);
	public void wasIncorrect(DrillEvent e);
	public void drillingNextVariation(DrillEvent e);
	
}
