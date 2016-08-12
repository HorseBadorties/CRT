package de.toto.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import de.toto.game.Game;
//import chesspresso.game.Game;
import de.toto.sound.Sounds;

@SuppressWarnings("serial")
public class AppFrame extends JFrame implements BoardListener {
	
	private List<Game> games = new ArrayList<Game>();
	private Game currentGame;
	private Board board;
	private JTextField txtFen;
	
	public AppFrame() throws HeadlessException {
		Game dummy = new Game();
		dummy.start();
		setGame(dummy);
		board = new Board();
		board.addBoardListener(this);		
		doUI();
	}

	public AppFrame(Game game) throws HeadlessException {
		this();
		games.add(game);
		setGame(game);
	}
	
	public AppFrame(List<Game> games) throws HeadlessException {
		this();
		this.games.addAll(games);
		setGame(games.get(0));
	}
	
	private void setGame(Game g) {
		currentGame = g;
		g.gotoStartPosition();
	}
	
	private void doUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().add(board, BorderLayout.CENTER);
		board.setPreferredSize(new Dimension(800, 800));
		
		Action actionNext = new AbstractAction("next") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentGame.gotoNextPosition() != null) {
//				if (game.goForward() != null) {
					updateBoard(true);
				} else {
					int i = games.indexOf(currentGame);
					if (i < games.size()-1) {
						currentGame = games.get(i+1);
						currentGame.gotoStartPosition();
						updateBoard(true);						
					} else {
						System.out.println("End of moves");
					}
				};
			}
		};		
		JButton btnNext = new JButton(actionNext);
		Action actionBack = new AbstractAction("back") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentGame.goBack() != null) {
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
		DefaultMutableTreeNode result = new DefaultMutableTreeNode(currentGame.toString()); 
		DefaultMutableTreeNode node = result; 
		currentGame.gotoStartPosition();
		
		
		return result;
	}
	
	private void updateBoard(boolean playSound) {		
		board.setCurrentPosition(currentGame.getPosition());
		txtFen.setText(currentGame.getPosition().getFen());
		if (playSound) {
			if (currentGame.getPosition().wasCapture()) {
				Sounds.capture();
			} else {
				Sounds.move();
			}
		}
	}

	@Override
	public void userMove(String move) {
		if (currentGame.getPosition().hasNext() && currentGame.getPosition().getNext().getMove().startsWith(move)) {
			currentGame.goForward();		
			updateBoard(true);
			new SwingWorker<Void,Void>() {

				@Override
				protected Void doInBackground() throws Exception {
					Thread.sleep(500);
					return null;
				}

				@Override
				protected void done() {
					currentGame.gotoNextPosition();		
					updateBoard(true);
				}
				
			}.execute();
		}
	}

}
