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

public class SquareColorDrillPanel extends JPanel {
	
	private AppFrame appFrame;
	private List<Square> allSquares = new ArrayList<Square>(64);
	private Square currentSquare;
	private Random random = new Random();
	private JButton btnWhite, btnBlack;
	private JTextField txtSquarename;
	private int counter, correctCounter;
	
	
	
	public SquareColorDrillPanel(AppFrame appFrame) {
		this.appFrame = appFrame;
		Square[][] squares8x8 = Square.createEmpty8x8();
		for (int rank = 1; rank <= 8; rank++) {				
			for (int file = 1; file <= 8; file++) {
				allSquares.add(squares8x8[rank - 1][file - 1]);
			}
		}
		btnWhite = new JButton(actionWhite);
		btnBlack = new JButton(actionBlack);
		txtSquarename = new JTextField(15);
		txtSquarename.setEditable(false);
		
		add(txtSquarename);
		add(btnWhite);
		add(btnBlack);	
		
		KeyStroke keyW = KeyStroke.getKeyStroke(KeyEvent.VK_W, 0);
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyW, "white");		
		this.getActionMap().put("white",actionWhite);
		KeyStroke keyB = KeyStroke.getKeyStroke(KeyEvent.VK_B, 0);
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyB, "black");		
		this.getActionMap().put("black",actionBlack);
		
		newRandomSquare();
		
	}
	
	
	private void newRandomSquare() {
		currentSquare = allSquares.get(random.nextInt(64));
		txtSquarename.setText(currentSquare.getName());		
		btnWhite.setEnabled(true);
		btnBlack.setEnabled(true);	
		appFrame.announce(currentSquare.getName());
	}
		
	public void check(boolean white) {
		btnWhite.setEnabled(false);
		btnBlack.setEnabled(false);		
		boolean correct = (white && currentSquare.isWhite()) || (!white && !currentSquare.isWhite()) ;
		if (!correct) {
			Sounds.wrong();
		}
		txtSquarename.setText(String.format("%s is %s %s (%d/%d)",
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
	
	private Action actionWhite = new AbstractAction("White") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			check(true);
		}
	};		
	
	private Action actionBlack = new AbstractAction("Black") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			check(false);
		}
	};
	
	
}
