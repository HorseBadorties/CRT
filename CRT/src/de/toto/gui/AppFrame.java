package de.toto.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.toto.engine.Stockfish;
import de.toto.game.Game;
import de.toto.game.Game.DrillStats;
import de.toto.game.Position;
import de.toto.pgn.PGNReader;
import de.toto.sound.Sounds;

@SuppressWarnings("serial")
public class AppFrame extends JFrame implements BoardListener {
	
	private File pgn = null;
	private List<Game> games = new ArrayList<Game>();
	private Game currentGame;
	private Board board;
	private JLabel txtComment;
	private JTextField txtStatus;
	private JTable tblMoves;
	private PositionTableModel modelMoves;
	private JList lstVariations;
	private DefaultListModel modelVariations;
	private Stockfish stockfish;
	private Preferences prefs = Preferences.userNodeForPackage(AppFrame.class);
	
	private static Logger log = Logger.getLogger("AppFrame");
	
	private static final String PATH_TO_STOCKFISH = "C://Program Files//Stockfish//stockfish 7 x64.exe";
	private static final String PREFS_FRAME_WIDTH = "FRAME_WIDTH";
	private static final String PREFS_FRAME_HEIGHT = "FRAME_HEIGHT";
	private static final String PREFS_FRAME_EXTENDED_STATE = "FRAME_EXTENDED_STATE";
	private static final String PREFS_PGN_FILE = "PGN_FILE";
	private static final String PREFS_WHITE_PERSPECTIVE = "WHITE_PERSPECTIVE";
	
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
				savePrefs();
				if (stockfish != null) {
					try {
						stockfish.stopEngine();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			
		});
	}
	
	private void savePrefs() {
		boolean maximized = getExtendedState() == JFrame.MAXIMIZED_BOTH;
		if (!maximized) {
			prefs.putInt(PREFS_FRAME_WIDTH, getSize().width);
			prefs.putInt(PREFS_FRAME_HEIGHT, getSize().height);
		} 
		prefs.putBoolean(PREFS_FRAME_EXTENDED_STATE, maximized);
		if (pgn != null) {
			prefs.put(PREFS_PGN_FILE, pgn.getAbsolutePath());
		}
		prefs.putBoolean(PREFS_WHITE_PERSPECTIVE, board.isOrientationWhite());
	}
	
	
	private void loadPgn(File pgn) {
		List<Game> games = PGNReader.parse(pgn);
		int positionCount = 0;
		for (Game g : games) {
			positionCount += g.getAllPositions().size();
		}
		log.info(String.format("Successfully parsed %d games with %d positions", games.size(), positionCount));
		
		Game repertoire = games.get(0);
		games.remove(repertoire);
		for (Game game : games) {
			log.info("merging " + game);
			repertoire.mergeIn(game);
		}		
		log.info(String.format("merged games to %d positions ", repertoire.getAllPositions().size()));
		this.pgn = pgn;
		
		setGame(repertoire);	
		updateBoard(false);
		txtStatus.setText(String.format("%s loaded with %d positions ", pgn, repertoire.getAllPositions().size()));
	}
	
	private void setGame(Game g) {
		currentGame = g;
		g.gotoStartPosition();
	}
	
	private Action actionNext = new AbstractAction("next") {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (lstVariations.getSelectedIndex() >= 0) {
				Position p = (Position)modelVariations.get(lstVariations.getSelectedIndex());
				currentGame.gotoPosition(p);
				updateBoard(true);		
			} else if (!modelVariations.isEmpty()) {
				Position p = (Position)modelVariations.get(0);
				currentGame.gotoPosition(p);
				updateBoard(true);	
			} else {
				int i = games.indexOf(currentGame);
				if (i < games.size()-1) {
					currentGame = games.get(i+1);
					currentGame.gotoStartPosition();
					updateBoard(true);						
				} else {
					log.info("End of moves");
				}
			};
		}
	};		
	
	private Action actionBack = new AbstractAction("back") {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (currentGame.goBack() != null) {
				updateBoard(true);
			};
		}
	};
	
	private Action actionUp = new AbstractAction("up") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (lstVariations.getSelectedIndex() <= 0) {
				lstVariations.setSelectedIndex(modelVariations.size()-1);
			} else {
				lstVariations.setSelectedIndex(lstVariations.getSelectedIndex()-1);
			}
		}
	};
	
	private Action actionDown = new AbstractAction("down") {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (lstVariations.getSelectedIndex() == modelVariations.size()-1) {
				lstVariations.setSelectedIndex(0);
			} else if (lstVariations.getSelectedIndex() == -1 && modelVariations.size() > 1) {
				lstVariations.setSelectedIndex(1);
			} else {
				lstVariations.setSelectedIndex(lstVariations.getSelectedIndex()+1);
			}			
		}
	};
	
	
	
	private Action actionFlip = new AbstractAction("flip") {
		@Override
		public void actionPerformed(ActionEvent e) {
			board.flip();
		}
	};
	
	private Action actionBeginDrill = new AbstractAction("begin drill") {
		@Override
		public void actionPerformed(ActionEvent e) {
			currentGame.beginDrill();
		}
	};
	
	private Action actionEndDrill = new AbstractAction("end drill") {
		@Override
		public void actionPerformed(ActionEvent e) {
			currentGame.endDrill();
		}
	};
	
	private Action actionEval = new AbstractAction("get Stockfish eval") {
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
	
	private Action actionLoadPGN = new AbstractAction("load PGN") {
		@Override
		public void actionPerformed(ActionEvent e) {
			File lastDir = pgn != null ? pgn.getParentFile() : null;
			JFileChooser fc = new JFileChooser(lastDir);
			fc.setDialogTitle("Please choose a PGN file that contains your repertoire lines!");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setFileFilter(new FileNameExtensionFilter("*.pgn", "pgn"));
			int ok = fc.showOpenDialog(AppFrame.this);
			if (ok == JFileChooser.APPROVE_OPTION) {
				loadPgn(fc.getSelectedFile());
			}
		}
	};
	
	private void doUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel pnlAll = new JPanel(new BorderLayout());
		JPanel pnlNorth = new JPanel();
		JPanel pnlCenter = new JPanel(new BorderLayout());
		JPanel pnlEast = new JPanel(new BorderLayout());
		JPanel pnlSouth = new JPanel(new BorderLayout());
		
		pnlAll.add(pnlNorth, BorderLayout.PAGE_START);		
		pnlAll.add(pnlCenter, BorderLayout.CENTER);
		pnlAll.add(pnlEast, BorderLayout.LINE_END);
		pnlAll.add(pnlSouth, BorderLayout.PAGE_END);
		getContentPane().add(pnlAll, BorderLayout.CENTER);
		
				
		pnlNorth.add(new JButton(actionBeginDrill));
		pnlNorth.add(new JButton(actionEndDrill));
		pnlNorth.add(new JButton(actionLoadPGN));
		

		JPanel pnlBoard = new JPanel(new BorderLayout());
		pnlBoard.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 5));
		pnlBoard.add(board, BorderLayout.CENTER);
		txtComment = new JLabel();
		pnlCenter.add(pnlBoard, BorderLayout.CENTER);		
		pnlCenter.add(txtComment, BorderLayout.PAGE_END);
		pnlCenter.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
		
		JPanel pnlMoves = new JPanel(new BorderLayout());
		pnlMoves.setBorder(BorderFactory.createTitledBorder("Move List"));
		modelMoves = new PositionTableModel();
		tblMoves = new JTable(modelMoves);
		tblMoves.setEnabled(false);
		tblMoves.setTableHeader(null);
		tblMoves.setShowVerticalLines(false);
		tblMoves.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = tblMoves.columnAtPoint(e.getPoint());
				int row = tblMoves.rowAtPoint(e.getPoint());
				if (column >= 0 && row >= 0) {
					Position p = modelMoves.getPositionAt(row, column);
					currentGame.gotoPosition(p);
					updateBoard(true);
				}				
			}			
		});
		pnlMoves.add(new JScrollPane(tblMoves));
		pnlMoves.setPreferredSize(new Dimension(150, 500));
		
		JPanel pnlVariations = new JPanel(new BorderLayout());
		pnlVariations.setBorder(BorderFactory.createTitledBorder("Variations"));
		modelVariations = new DefaultListModel();
		lstVariations = new JList(modelVariations);		
		lstVariations.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (lstVariations.getSelectedIndex() >= 0) {
					String move = modelVariations.get(lstVariations.getSelectedIndex()).toString();
					currentGame.doMove(move);
					updateBoard(true);
				}
			}			
		});
		pnlVariations.add(new JScrollPane(lstVariations));		
		pnlVariations.setPreferredSize(new Dimension(150, 200));
		pnlEast.add(pnlMoves, BorderLayout.CENTER);
		pnlEast.add(pnlVariations, BorderLayout.PAGE_END);
		
		txtStatus = new JTextField();
		txtStatus.setEditable(false);
		txtStatus.setBorder(BorderFactory.createLoweredBevelBorder());	
		pnlSouth.add(txtStatus, BorderLayout.PAGE_END);
		
		
		KeyStroke keyNext = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		pnlAll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyNext, "next");
		pnlAll.getActionMap().put("next",actionNext);
		KeyStroke keyBack = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		pnlAll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyBack, "back");
		pnlAll.getActionMap().put("back",actionBack);
		InputMap im = (InputMap) UIManager.get("List.focusInputMap");
		im.remove(keyNext);
		im.remove(keyBack);
		KeyStroke keyUp = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
		pnlAll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyUp, "up");
		pnlAll.getActionMap().put("up",actionUp);
		KeyStroke keyDown = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		pnlAll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyDown, "down");
		pnlAll.getActionMap().put("down",actionDown);		
		KeyStroke keyControlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
		pnlAll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyControlF, "flip");
		pnlAll.getActionMap().put("flip",actionFlip);
		
		Dimension prefSize = new Dimension(prefs.getInt(PREFS_FRAME_WIDTH, 800), prefs.getInt(PREFS_FRAME_HEIGHT, 800));		
		this.setPreferredSize(prefSize);
		pack();
		if (prefs.getBoolean(PREFS_FRAME_EXTENDED_STATE, false)) {
			setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			setLocationRelativeTo(null);
		}
		
		String lastPGN = prefs.get(PREFS_PGN_FILE, null);
		if (lastPGN != null) {
			File f = new File(lastPGN);
			if (f.exists()) {
				loadPgn(f);
			}			
		} 
		if (pgn == null) {
			actionLoadPGN.actionPerformed(null);
		}	
		
		updateBoard(false);
		if (!prefs.getBoolean(PREFS_WHITE_PERSPECTIVE, true)) {
			board.flip();
		}
	}

	private void updateBoard(boolean playSound) {	
		Position p = currentGame.getPosition();
		board.setCurrentPosition(p);
		String comment = " ";
		if (p != null && p.getComment() != null) {
			comment = "<html>Move comment: <b>" + p.getComment() + "</b></html>";
		}
		txtComment.setText(comment);
		if (playSound) {
			if (p.wasCapture()) {
				Sounds.capture();
			} else {
				Sounds.move();
			}
		}
		modelMoves.setPosition(p);
		modelVariations.clear();
		if (!currentGame.isDrilling()) {
			for (Position variation : p.getVariations()) {
				modelVariations.addElement(variation);
			}
		}
	}

	@Override
	public void userMove(String move) {
		if (currentGame.isDrilling()) {
			if (currentGame.hasNextPosition(move)) {
				currentGame.doMove(move);		
				updateBoard(true);
				new SwingWorker<Void,Void>() {
	
					@Override
					protected Void doInBackground() throws Exception {
						Thread.sleep(500);
						return null;
					}
	
					@Override
					protected void done() {
						Position current = currentGame.getPosition();
						Position newPosition = currentGame.gotoNextPosition();
						if (current == newPosition) {
							DrillStats drillStats = currentGame.endDrill();
							JOptionPane.showMessageDialog(AppFrame.this, String.format("Drill ended for %d positions", drillStats.drilledPositions));
							currentGame.gotoStartPosition();
						} 
						updateBoard(true);
					}
					
				}.execute();
			} else if (currentGame.getPosition().hasNext()) {
				Sounds.wrong();
			}
		} else {
			if (currentGame.hasNextPosition(move)) {
				currentGame.doMove(move);		
				updateBoard(true);
			}
		}
	}

}
