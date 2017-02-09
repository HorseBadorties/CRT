package de.toto.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.toto.game.Game;
import de.toto.game.Position;
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
		currentSquare = allSquares.get(random.nextInt(64));
		textfield.setText(currentSquare.getName());		
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
		textfield.setText(String.format("%s is %s %s (%d/%d)",
				currentSquare.getName(),
				(correct ? "" : "NOT"),
				(white ? "white" : "black"),				
				(correct ? ++correctCounter : correctCounter),
				++counter));
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
