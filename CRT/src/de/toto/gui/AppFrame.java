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
		board.setPreferredSize(new Dimension(800, 800));
		
		Action actionNext = new AbstractAction("next") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (game.goForward() != null) {
					updateBoard(true);
				};
			}
		};		
		JButton btnNext = new JButton(actionNext);
		Action actionBack = new AbstractAction("back") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (game.goBack() != null) {
					updateBoard(true);
				};
			}
		};
		JButton btnBack = new JButton(actionBack);
		
		JPanel pnlSouth = new JPanel();		
		pnlSouth.add(btnBack);
		pnlSouth.add(btnNext);
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
		board.setCurrentPosition(game.getPosition());
		System.out.println(game.getPosition());
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
		game.addMove(move);
		System.out.println(game.getPosition());
		updateBoard(true);
	}

}
