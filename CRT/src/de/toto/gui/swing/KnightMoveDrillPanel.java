package de.toto.gui.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import de.toto.game.Square;

public class KnightMoveDrillPanel extends AbstractDrillPanel {
	
	protected Square firstSquare, secondSquare;
	private Action actionYes;	
	private Action actionNo;
	
	public KnightMoveDrillPanel(AppFrame appFrame) {
		super(appFrame);				
		newRandomSquares();		
	}	
	
	@Override
	public void doNewRandomSquares() {			
		getRandomSquares();
//		setText(firstSquare.getName() + " " + secondSquare.getName(), Color.BLACK);	
		showText(firstSquare.getName() + " " + secondSquare.getName());
		actionYes.setEnabled(true);
		actionNo.setEnabled(true);
		appFrame.announce(announceString(firstSquare) + ". " + announceString(secondSquare) + ".");
	}
	
	private static String announceString(Square s) {
		return " ." + s.getFileName().toUpperCase() + " " + s.rank;
	}
	
	protected boolean doCheck() {
		return firstSquare.isKnightMove(secondSquare);
	}
	
	protected void getRandomSquares() {
		firstSquare = allSquares.get(random.nextInt(64));
		secondSquare = allSquares.get(random.nextInt(64));
		int rankDiff = Math.abs(firstSquare.rank - secondSquare.rank);
		int fileDiff = Math.abs(firstSquare.file - secondSquare.file);
		while (rankDiff == 0 || rankDiff > 2 || fileDiff == 0 || fileDiff > 2) {
			secondSquare = allSquares.get(random.nextInt(64));
			rankDiff = Math.abs(firstSquare.rank - secondSquare.rank);
			fileDiff = Math.abs(firstSquare.file - secondSquare.file);
		}
	}
 		
	public void check(boolean yesAnswered) {		
		actionYes.setEnabled(false);
		actionNo.setEnabled(false);		
		boolean yes = doCheck();
		boolean correct = yes == yesAnswered ;
		if (!correct) {
			Sounds.wrong();
		}
		setText(String.format("%s (%d/%d)",				
				(correct ? "CORRECT" : "INCORRECT"),								
				(correct ? ++correctCounter : correctCounter),
				++counter), correct ? Color.BLACK : Color.RED);
		
		delay = 100;
		if (correct) {
			if (prefs.getBoolean(PREFS_SHOW_SUCCESS, true)) {
				highlightDiagonal(Color.GREEN, firstSquare, secondSquare, yes);
				delay = 1000;
			}
		} else if (prefs.getBoolean(PREFS_SHOW_ERROR, true)) {
			highlightDiagonal(Color.RED, firstSquare, secondSquare, yes);	
			delay = 1000;
		} 
		
		newRandomSquares();
		
	}
		

	@Override
	public int getFirstKeyCode() {		
		return KeyEvent.VK_Y;
	}


	@Override
	public int getSecondKeyCode() {
		return KeyEvent.VK_N;
	}


	@Override
	public Action getFirstAction() {
		if (actionYes == null) {
			actionYes = new AbstractAction("Yes") {
				@Override
				public void actionPerformed(ActionEvent e) {
					check(true);
				}
			};
		}
		return actionYes;
	}


	@Override
	public Action getSecondAction() {
		if (actionNo == null) {
			actionNo = new AbstractAction("No") {
				@Override
				public void actionPerformed(ActionEvent e) {
					check(false);
				}
			};
		}
		return actionNo;
	}

	
}
