package de.toto.gui.swing;

public class DiagonalDrillPanel extends KnightMoveDrillPanel {
	
	public DiagonalDrillPanel(AppFrame appFrame) {
		super(appFrame);		
	}	
	
	protected void getRandomSquares() {
		firstSquare = allSquares.get(random.nextInt(64));
		secondSquare = allSquares.get(random.nextInt(64));
		while (firstSquare.rank == secondSquare.rank || firstSquare.file == secondSquare.file) {
			secondSquare = allSquares.get(random.nextInt(64));
		}
	}
	
	protected boolean doCheck() {
		return firstSquare.onDiagonalWith(secondSquare);
	}
		
}
