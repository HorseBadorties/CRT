package de.toto.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import de.toto.game.Game;
//import chesspresso.game.Game;
import de.toto.sound.Sounds;

@SuppressWarnings("serial")
public class AppFrame extends JFrame implements BoardListener {
	
	private Game game;
	private Board board;
	
	public AppFrame(Game game) throws HeadlessException {
		this.game = game;
		board = new Board();
		board.addBoardListener(this);
		doUI();
	}
	
	private void doUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().add(board, BorderLayout.CENTER);
		board.setPreferredSize(new Dimension(1200, 1200));
		
		
		final JTextField txt = new JTextField(50);
		ActionListener actionLoadFen = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				board.boardCanvas.fen(txt.getText());
			}
		}; 
		txt.addActionListener(actionLoadFen);
		Action actionNext = new AbstractAction("next") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (game.goForward() != null) {
					updateBoard(true);
				};
			}
		};
		
		JButton btn = new JButton(actionNext);
		Action actionBack = new AbstractAction("back") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (game.goBack() != null) {
					updateBoard(true);
				};
			}
		};
		
		JPanel pnlSouth = new JPanel();
		pnlSouth.add(txt);
		pnlSouth.add(btn);
		getContentPane().add(pnlSouth, BorderLayout.PAGE_END);
		
		KeyStroke keyNext = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		pnlSouth.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyNext, "next");
		pnlSouth.getActionMap().put("next",actionNext);
		KeyStroke keyBack = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		pnlSouth.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyBack, "back");
		pnlSouth.getActionMap().put("back",actionBack);
		
		updateBoard(false);
	}
	
	private void updateBoard(boolean playSound) {
		board.boardCanvas.fen(game.getPosition().getFEN());
		if (playSound) {
			if (game.getPosition().wasCapture()) {
				Sounds.capture();
			} else {
				Sounds.move();
			}
		}
	}

	@Override
	public void userMove(String move) {
		System.out.println("User moved " + move);
	}

}
