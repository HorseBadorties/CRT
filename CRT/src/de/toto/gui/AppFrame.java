package de.toto.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import de.toto.game.Game;
//import chesspresso.game.Game;
import de.toto.sound.Sounds;

@SuppressWarnings("serial")
public class AppFrame extends JFrame implements BoardListener {
	
	private Game game;
	private Board board;
	private JTextField txtFen;
	
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
				if (game.gotoNextPosition() != null) {
//				if (game.goForward() != null) {
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
		
		Action actionFlip = new AbstractAction("flip") {
			@Override
			public void actionPerformed(ActionEvent e) {
				board.flip();
			}
		};
		
		txtFen = new JTextField();
		txtFen.setEditable(false);
		txtFen.setColumns(50);
		
		JPanel pnlSouth = new JPanel();
		pnlSouth.add(btnBack);
		pnlSouth.add(btnNext);
		pnlSouth.add(txtFen);
		getContentPane().add(pnlSouth, BorderLayout.PAGE_END);
		
//		JTree tree = new JTree(createMoveTree());
//		getContentPane().add(new JScrollPane(tree), BorderLayout.LINE_START);
		
		KeyStroke keyNext = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		pnlSouth.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyNext, "next");
		pnlSouth.getActionMap().put("next",actionNext);
		KeyStroke keyBack = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		pnlSouth.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyBack, "back");
		pnlSouth.getActionMap().put("back",actionBack);
		KeyStroke keyControlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
		pnlSouth.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyControlF, "flip");
		pnlSouth.getActionMap().put("flip",actionFlip);
		
		updateBoard(false);
	}
	
	private TreeNode createMoveTree() {
		DefaultMutableTreeNode result = new DefaultMutableTreeNode(game.toString()); 
		DefaultMutableTreeNode node = result; 
		game.gotoStartPosition();
		
		
		return result;
	}
	
	private void updateBoard(boolean playSound) {
		board.setCurrentPosition(game.getPosition());
		txtFen.setText(game.getPosition().getFen());
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
		if (game.getPosition().hasNext() && game.getPosition().getNext().getMove().startsWith(move)) {
			game.goForward();		
			updateBoard(true);
			new SwingWorker<Void,Void>() {

				@Override
				protected Void doInBackground() throws Exception {
					Thread.sleep(500);
					return null;
				}

				@Override
				protected void done() {
					game.goForward();		
					updateBoard(true);
				}
				
			}.execute();
		}
//		game.addMove(move);		
//		updateBoard(true);
	}

}
