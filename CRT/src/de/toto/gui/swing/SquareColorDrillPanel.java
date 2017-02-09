package de.toto.gui.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import de.toto.game.Square;

public class SquareColorDrillPanel extends AbstractDrillPanel {
	
	private Square currentSquare;
	private Action actionWhite;	
	private Action actionBlack;
	
	public SquareColorDrillPanel(AppFrame appFrame) {
		super(appFrame);				
		newRandomSquare();		
	}	
	
	private void newRandomSquare() {
		Square newSquare = allSquares.get(random.nextInt(64));
		while (newSquare.equals(currentSquare)) {
			newSquare = allSquares.get(random.nextInt(64));
		}
		currentSquare = newSquare;
		setText(currentSquare.getName(), Color.BLACK);		
		btnFirst.setEnabled(true);
		btnSecond.setEnabled(true);	
		appFrame.announce(currentSquare.getName());
	}
		
	public void check(boolean white) {
		btnFirst.setEnabled(false);
		btnSecond.setEnabled(false);		
		boolean correct = (white && currentSquare.isWhite()) || (!white && !currentSquare.isWhite()) ;
		if (!correct) {
			Sounds.wrong();
		}
		setText(String.format("%s is %s %s (%d/%d)",
				currentSquare.getName(),
				(correct ? "" : "NOT"),
				(white ? "white" : "black"),				
				(correct ? ++correctCounter : correctCounter),
				++counter), correct ? Color.BLACK : Color.RED);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				newRandomSquare();				
			}
			
		});
	}
	
	

	@Override
	public int getFirstKeyCode() {		
		return KeyEvent.VK_W;
	}


	@Override
	public int getSecondKeyCode() {
		return KeyEvent.VK_B;
	}


	@Override
	public Action getFirstAction() {
		if (actionWhite == null) {
			actionWhite = new AbstractAction("White") {
				@Override
				public void actionPerformed(ActionEvent e) {
					check(true);
				}
			};
		}
		return actionWhite;
	}


	@Override
	public Action getSecondAction() {
		if (actionBlack == null) {
			actionBlack = new AbstractAction("Black") {
				@Override
				public void actionPerformed(ActionEvent e) {
					check(false);
				}
			};
		}
		return actionBlack;
	}
	
	
}
