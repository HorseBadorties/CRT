package de.toto.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.toto.engine.Stockfish;
import de.toto.game.DrillEvent;
import de.toto.game.Game;
import de.toto.game.Drill;
import de.toto.game.Drill.DrillStats;
import de.toto.game.DrillListener;
import de.toto.game.GameEvent;
import de.toto.game.GameListener;
import de.toto.game.Position;
import de.toto.pgn.PGNReader;
import de.toto.sound.Sounds;

@SuppressWarnings("serial")
public class AppFrame extends JFrame implements BoardListener, GameListener, DrillListener {
	
	private File pgn = null;
	private Game game;
	private Drill drill;
	private Board board;
	private JLabel txtComment;
	private JLabel txtStatus;
	private JTable tblMoves;
	private PositionTableModel modelMoves;
	private JList lstVariations;
	private JPanel pnlVariationsAndDrillStatus;
	private JPanel pnlVariations;
	private DefaultListModel modelVariations;
	private DrillStatusPanel drillStatus;
	private JCheckBox cbOnlyMainline;
	private JCheckBox cbShowComments;
	private JCheckBox cbRandomDrill;
	private JSplitPane splitCenter;
	private JSplitPane splitEast;
	private Stockfish stockfish;
	private Preferences prefs = Preferences.userNodeForPackage(AppFrame.class);
	
	private static Logger log = Logger.getLogger("AppFrame");
	
	private static final String PATH_TO_STOCKFISH = "C://Program Files//Stockfish//stockfish 7 x64.exe";
	private static final String PREFS_FRAME_WIDTH = "FRAME_WIDTH";
	private static final String PREFS_FRAME_HEIGHT = "FRAME_HEIGHT";
	private static final String PREFS_FRAME_EXTENDED_STATE = "FRAME_EXTENDED_STATE";
	private static final String PREFS_PGN_FILE = "PGN_FILE";	
	private static final String PREFS_WHITE_PERSPECTIVE = "WHITE_PERSPECTIVE";
	private static final String PREFS_SPLITTER_CENTER_POSITION = "SPLITTER_CENTER_POSITION";
	private static final String PREFS_SPLITTER_EAST_POSITION = "SPLITTER_EAST_POSITION";
	private static final String PREFS_FONT_SIZE = "FONT_SIZE";
	private static final String PREFS_ONLY_MAINLINE = "ONLY_MAINLINE";
	private static final String PREFS_SHOW_COMMENTS = "SHOW_COMMENTS";
	private static final String PREFS_RANDOM_DRILL = "RANDOM_DRILL";
	
	public AppFrame() throws HeadlessException {
		setIconImage(Toolkit.getDefaultToolkit().getImage(AppFrame.class.getResource("/images/icon/Knight50.png")));
		board = new Board();
		board.addBoardListener(this);		
		doUI();
		SwingUtilities.updateComponentTreeUI(this);
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
		prefs.putInt(PREFS_SPLITTER_CENTER_POSITION, splitCenter.getDividerLocation());
		prefs.putInt(PREFS_SPLITTER_EAST_POSITION, splitEast.getDividerLocation());
		prefs.putInt(PREFS_FONT_SIZE, lstVariations.getFont().getSize());
		prefs.putBoolean(PREFS_ONLY_MAINLINE, cbOnlyMainline.isSelected());
		prefs.putBoolean(PREFS_RANDOM_DRILL, cbRandomDrill.isSelected());
		prefs.putBoolean(PREFS_SHOW_COMMENTS, cbShowComments.isSelected());
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
		setTitle("Chess Repertoire Trainer: " + pgn.getName());
		txtStatus.setText(String.format("%s loaded with %d positions ", pgn, repertoire.getAllPositions().size()));
	}
	
	private void setGame(Game g) {
		if (game != null) {
			game.removeGameListener(this);
		}
		game = g;
		game.addGameListener(this);
		g.gotoStartPosition();
	}
	
	private Action actionNext = new AbstractAction("next") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (drill != null) {
				gotoNextDrillPosition();
			} else if (lstVariations.getSelectedIndex() >= 0) {
				Position p = (Position)modelVariations.get(lstVariations.getSelectedIndex());
				game.gotoPosition(p);						
			} else if (!modelVariations.isEmpty()) {
				Position p = (Position)modelVariations.get(0);
				game.gotoPosition(p);					
			} else {
				log.info("End of moves");
			};
		}
	};		
	
	private Action actionBack = new AbstractAction("back") {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (drill == null) {
				game.goBack();
			}
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
			if (drill == null) {				
				drill = new Drill(game.getPosition(), board.isOrientationWhite(), cbOnlyMainline.isSelected(), cbRandomDrill.isSelected());
				drill.addGameListener(AppFrame.this);
				drill.addDrillListener(AppFrame.this);
				modelVariations.clear();
				actionLoadPGN.setEnabled(false);				
				cbOnlyMainline.setEnabled(false);
				cbRandomDrill.setEnabled(false);
				drillStatus = new DrillStatusPanel(drill);
				setPanelVisible(drillStatus);
				this.putValue(Action.NAME, "end drill");
				drill.startDrill();
			} else {				
				drill.endDrill();				
			}			
		}
	};
	
	
	private Action actionShowComments = new AbstractAction("show comments / graphics comments?") {
		@Override
		public void actionPerformed(ActionEvent e) {
			board.setShowGraphicsComments(cbShowComments.isSelected());
		}
	};
	
	private Action actionChooseFont = new AbstractAction("choose font") {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFontChooser fc = new JFontChooser();
			if (fc.showDialog(AppFrame.this) == JFontChooser.OK_OPTION) {
				lstVariations.setFont(fc.getSelectedFont());
				tblMoves.setFont(fc.getSelectedFont());
			}
			
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
					return stockfish.getBestMove(game.getPosition().getFen(), 5000);
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
		JPanel pnlToolBar = new JPanel();
		JPanel pnlCenter = new JPanel(new BorderLayout());
		JPanel pnlEast = new JPanel(new BorderLayout());
		JPanel pnlSouth = new JPanel(new BorderLayout());
		
		splitCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlCenter, pnlEast);
		splitCenter.setDividerLocation(prefs.getInt(PREFS_SPLITTER_CENTER_POSITION, 650));
		
		pnlAll.add(pnlToolBar, BorderLayout.PAGE_START);		
		pnlAll.add(splitCenter, BorderLayout.CENTER);		
		pnlAll.add(pnlSouth, BorderLayout.PAGE_END);
		getContentPane().add(pnlAll, BorderLayout.CENTER);

		JButton btnLoadPGN = new JButton(actionLoadPGN);
		btnLoadPGN.putClientProperty("JComponent.sizeVariant", "large");
		pnlToolBar.add(btnLoadPGN);
		cbShowComments = new JCheckBox(actionShowComments);
		cbShowComments.setSelected(prefs.getBoolean(PREFS_SHOW_COMMENTS, false));
		actionShowComments.actionPerformed(null);
		pnlToolBar.add(cbShowComments);
		
		pnlToolBar.add(new JButton(actionBeginDrill));		
		cbOnlyMainline = new JCheckBox("accept main line only?");
		cbOnlyMainline.setSelected(prefs.getBoolean(PREFS_ONLY_MAINLINE, true));
		cbOnlyMainline.setEnabled(false);
		pnlToolBar.add(cbOnlyMainline);
		cbRandomDrill = new JCheckBox("drill positions in random order?");
		cbRandomDrill.setSelected(prefs.getBoolean(PREFS_RANDOM_DRILL, false));
		pnlToolBar.add(cbRandomDrill);		

		JPanel pnlBoard = new JPanel(new BorderLayout());
		pnlBoard.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 5));
		pnlBoard.add(board, BorderLayout.CENTER);
		txtComment = new JLabel();
		pnlCenter.add(pnlBoard, BorderLayout.CENTER);		
		pnlCenter.add(txtComment, BorderLayout.PAGE_END);
		pnlCenter.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
		
		JPopupMenu popUp = new JPopupMenu();
		popUp.add(actionChooseFont);
		JPanel pnlMoves = new JPanel(new BorderLayout());
		pnlMoves.setBorder(BorderFactory.createTitledBorder("Move List"));
		modelMoves = new PositionTableModel();
		tblMoves = new JTable(modelMoves) {
			@Override
			public void setFont(Font f) {
				super.setFont(f);
				setRowHeight(f.getSize() * 2);
			}
		};
		tblMoves.setEnabled(false);
		tblMoves.setFocusable(false);
		tblMoves.setTableHeader(null);
		tblMoves.setShowVerticalLines(false);
		tblMoves.setComponentPopupMenu(popUp);
		int fontSize = prefs.getInt(PREFS_FONT_SIZE, 12);
		tblMoves.setFont(new Font("Frutiger Standard", Font.PLAIN, fontSize));
		tblMoves.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = tblMoves.columnAtPoint(e.getPoint());
				int row = tblMoves.rowAtPoint(e.getPoint());
				if (column >= 0 && row >= 0) {
					Position p = modelMoves.getPositionAt(row, column);
					game.gotoPosition(p);					
				}				
			}			
		});
		pnlMoves.add(new JScrollPane(tblMoves));
		pnlMoves.setPreferredSize(new Dimension(150, 500));
		
		pnlVariationsAndDrillStatus = new JPanel(new BorderLayout());
		pnlVariations = new JPanel(new BorderLayout());
		pnlVariations.setBorder(BorderFactory.createTitledBorder("Variations"));
		modelVariations = new DefaultListModel();
		lstVariations = new JList(modelVariations);		
		lstVariations.setFocusable(false);
		lstVariations.setFont(new Font("Frutiger Standard", Font.PLAIN, fontSize));
		lstVariations.setComponentPopupMenu(popUp);
		lstVariations.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (lstVariations.getSelectedIndex() >= 0) {
					Position p = (Position)modelVariations.get(lstVariations.getSelectedIndex());
					game.gotoPosition(p);
				}
			}			
		});		
		pnlVariations.add(new JScrollPane(lstVariations));		
		pnlVariations.setPreferredSize(new Dimension(150, 200));
		pnlVariationsAndDrillStatus.add(pnlVariations);
		splitEast = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlVariationsAndDrillStatus, pnlMoves);
		splitEast.setBorder(null);
		int splitEastPosition = prefs.getInt(PREFS_SPLITTER_EAST_POSITION, 0);
		if (splitEastPosition > 0) {
			splitEast.setDividerLocation(splitEastPosition);
		}
		pnlEast.add(splitEast);
		
		txtStatus = new JLabel();
		txtStatus.setBorder(BorderFactory.createLoweredBevelBorder());	
		pnlSouth.add(txtStatus, BorderLayout.PAGE_END);
		
		
		KeyStroke keyNext = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		pnlAll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyNext, "next");
		pnlAll.getActionMap().put("next",actionNext);
		KeyStroke keyBack = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		pnlAll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyBack, "back");
		pnlAll.getActionMap().put("back",actionBack);		
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
		
		if (!prefs.getBoolean(PREFS_WHITE_PERSPECTIVE, true)) {
			board.flip();
		}
	}

	private void updateBoard(boolean playSound) {	
		Position p = drill != null ? drill.getPosition() : game.getPosition();
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
		if (drill == null) {
			for (Position variation : p.getVariations()) {
				modelVariations.addElement(variation);
			}
		}
	}

	@Override
	public void userMove(String move) {
		if (drill != null) {
			if (drill.isCorrectMove(move)) {
				drill.doMove(move);				
				waitAndLoadNextDrillPosition(drill.getPosition());
			} else if (drill.getPosition().hasNext()) {
				Sounds.wrong();
			}
		} else {
			if (game.isCorrectMove(move)) {
				game.doMove(move);				
			}
		}
	}
	
	@Override
	public void userClickedSquare(String squareName) {
		if (drill != null) {			
			if (drill.isCorrectSquare(squareName)) {
				drill.gotoPosition(drill.getPosition().getNext());				
				waitAndLoadNextDrillPosition(drill.getPosition());
			} else if (drill.getPosition().hasNext()) {
				Sounds.wrong();
			}
		} else {
			for (Position variation : game.getPosition().getVariations()) {
				if (variation.getMoveSquareNames()[1].equals(squareName)) {
					game.gotoPosition(variation);					
					break;
				}
			}
		}
	}
	
	private void waitAndLoadNextDrillPosition(final Position p) {
		new SwingWorker<Void,Void>() {
			
			@Override
			protected Void doInBackground() throws Exception {
				Thread.sleep(cbRandomDrill.isSelected() || p.hasNext() ? 500 : 500);
				return null;
			}

			@Override
			protected void done() {
				gotoNextDrillPosition();
			}
			
		}.execute();
	}
	
	private void gotoNextDrillPosition() {
		if (drill == null) return;
		drill.getNextDrillPosition();			
	}

	@Override
	public void positionChanged(GameEvent e) {
		updateBoard(true);
	}

	@Override
	public void drillEnded(DrillEvent e) {		
		DrillStats drillStats =  drill.getDrillStats();	
		drill = null;
		actionLoadPGN.setEnabled(true);		
		//cbOnlyMainline.setEnabled(true);
		cbRandomDrill.setEnabled(true);
		actionBeginDrill.putValue(Action.NAME, "begin drill");
		updateBoard(false);
		setPanelVisible(pnlVariations);
		
		JOptionPane.showMessageDialog(AppFrame.this,
				String.format("Drill ended for %d positions. It took %s.",
						drillStats.drilledPositions,
						drillStats.getFormattedDuration()), 
				"Drill ended", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void setPanelVisible(JPanel pnl) {
		pnlVariationsAndDrillStatus.removeAll();
		pnlVariationsAndDrillStatus.add(pnl);		
		pnlVariationsAndDrillStatus.revalidate();
		pnlVariationsAndDrillStatus.repaint();
	}

	@Override
	public void wasCorrect(DrillEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void wasIncorrect(DrillEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drillingNextVariation(DrillEvent e) {
		// TODO Auto-generated method stub
		
	}

}
