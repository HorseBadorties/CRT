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

public class DiagonalDrillPanel extends AbstractDrillPanel {
	
	private Square firstSquare, secondSquare;
	private Action actionYes;	
	private Action actionNo;
	
	public DiagonalDrillPanel(AppFrame appFrame) {
		super(appFrame);				
		newRandomSquare();		
	}	
	
	private void newRandomSquare() {
		firstSquare = allSquares.get(random.nextInt(64));
		secondSquare = allSquares.get(random.nextInt(64));
		while (firstSquare.equals(secondSquare) || firstSquare.rank == secondSquare.rank || firstSquare.file == secondSquare.file) {
			secondSquare = allSquares.get(random.nextInt(64));
		}
		textfield.setText(firstSquare.getName() + " " + secondSquare.getName());		
		btnFirst.setEnabled(true);
		btnSecond.setEnabled(true);	
		appFrame.announce(firstSquare.getName() + ". " + secondSquare.getName() + ".");
	}
		
	public void check(boolean yes) {
		btnFirst.setEnabled(false);
		btnSecond.setEnabled(false);		
		boolean correct = firstSquare.onDiagonalWith(secondSquare) == yes ;
		if (!correct) {
			Sounds.wrong();
		}
		textfield.setText(String.format("%s (%d/%d)",				
				(correct ? "CORRECT" : "INCORRECT"),								
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
