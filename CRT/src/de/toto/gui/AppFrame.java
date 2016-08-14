package de.toto.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;

import javax.swing.*;

import de.toto.engine.Stockfish;
import de.toto.game.Game;
import de.toto.game.Position;
import de.toto.sound.Sounds;

@SuppressWarnings("serial")
public class AppFrame extends JFrame implements BoardListener {
	
	private List<Game> games = new ArrayList<Game>();
	private Game currentGame;
	private Board board;
	private JTextField txtFen;
	private JTextField txtComment;
	private Stockfish stockfish;
	
	private static final String PATH_TO_STOCKFISH = "C://Program Files//Stockfish//stockfish 7 x64.exe";
	
	public AppFrame() throws HeadlessException {
		Game dummy = new Game();
		dummy.start();
		setGame(dummy);
		board = new Board();
		board.addBoardListener(this);		
		doUI();
		addWindowListener(new WindowAdapter() {
				
			@Override
			public void windowClosing(WindowEvent e) {
				if (stockfish != null) {
					stockfish.stopEngine();
				}
			}
			
		});
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
		
		Action actionBack = new AbstractAction("back") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentGame.goBack() != null) {
					updateBoard(true);
				};
			}
		};
		
		Action actionFlip = new AbstractAction("flip") {
			@Override
			public void actionPerformed(ActionEvent e) {
				board.flip();
			}
		};
		
		Action actionBeginDrill = new AbstractAction("begin drill") {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentGame.beginDrill();
			}
		};
		
		Action actionEndDrill = new AbstractAction("end drill") {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentGame.endDrill();
			}
		};
		
		Action actionEval = new AbstractAction("get Stockfish eval") {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SwingWorker<String, String>() {

					@Override
					protected String doInBackground() throws Exception {
						if (stockfish == null) {
							stockfish = new Stockfish(PATH_TO_STOCKFISH);
						}
						return stockfish.getBestMove(currentGame.getPosition().getFen(), 5000);
					}

					@Override
					protected void done() {
						try {
							JOptionPane.showMessageDialog(AppFrame.this, this.get());
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					
					
					
				}.run();
			}
		};

		txtFen = new JTextField();
		txtFen.setEditable(false);
		txtFen.setColumns(50);
		
		txtComment = new JTextField();
		txtComment.setEditable(false);
		txtComment.setColumns(50);
		
		
		JPanel pnlSouth = new JPanel();
		pnlSouth.add(new JButton(actionBeginDrill));
		pnlSouth.add(new JButton(actionEndDrill));
		pnlSouth.add(new JButton(actionEval));
		pnlSouth.add(txtComment);
		getContentPane().add(pnlSouth, BorderLayout.PAGE_END);
		
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

	private void updateBoard(boolean playSound) {	
		Position p = currentGame.getPosition();
		board.setCurrentPosition(p);
		txtFen.setText(p.getFen());
		String comment = p.getMoveNotation();
		if (p.getComment() != null) {
			comment += " " + p.getComment();
		}
		txtComment.setText(comment);
		if (playSound) {
			if (p.wasCapture()) {
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
		} else if (currentGame.getPosition().hasNext()) {
			Sounds.wrong();
		}
	}

}
