package de.toto.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.toto.game.Square;

public class SquareColorDrillPanel extends JPanel implements ActionListener {
	
	private List<Square> allSquares = new ArrayList<Square>(64);
	private Square currentSquare;
	private Random random = new Random();
	private JButton btnWhite, btnBlack;
	private JTextField txtSquarename;
	
	
	
	public SquareColorDrillPanel() {
		Square[][] squares8x8 = Square.createEmpty8x8();
		for (int rank = 1; rank <= 8; rank++) {				
			for (int file = 1; file <= 8; file++) {
				allSquares.add(squares8x8[rank - 1][file - 1]);
			}
		}
		btnWhite = new JButton("White");
		btnWhite.addActionListener(this);
		btnBlack = new JButton("Black");
		btnBlack.addActionListener(this);
		txtSquarename = new JTextField(10);
		txtSquarename.setEnabled(false);
		
		add(txtSquarename);
		add(btnWhite);
		add(btnBlack);	
		
		newRandomSquare();
		
	}
	
	
	private void newRandomSquare() {
		currentSquare = allSquares.get(random.nextInt(64));
		txtSquarename.setText(currentSquare.getName());		
		btnWhite.setEnabled(true);
		btnBlack.setEnabled(true);		
	}
	
	


	@Override
	public void actionPerformed(ActionEvent e) {
		btnWhite.setEnabled(false);
		btnBlack.setEnabled(false);
		boolean white = e.getSource() == btnWhite;
		boolean correct = (white && currentSquare.isWhite()) || (!white && !currentSquare.isWhite()) ;
		txtSquarename.setText(currentSquare.getName() +  (correct ? " is " : " is NOT ") + (white ? "white" : "black"));
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
	
	
	
}
