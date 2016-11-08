package de.toto.gui.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.toto.UncaughtExceptionHandler;
import de.toto.engine.EngineListener;
import de.toto.engine.Score;
import de.toto.engine.UCIEngine;
import de.toto.game.DrillEvent;
import de.toto.game.Game;
import de.toto.game.Drill;
import de.toto.game.Drill.DrillStats;
import de.toto.game.Position.GraphicsComment;
import de.toto.game.Square;
import de.toto.game.DrillListener;
import de.toto.game.GameEvent;
import de.toto.game.GameListener;
import de.toto.game.Position;
import de.toto.pgn.PGNReader;

@SuppressWarnings("serial")
public class AppFrame extends JFrame 
implements BoardListener, GameListener, DrillListener, EngineListener, AWTEventListener 
{
	
	private File pgn = null;
	private Game repertoire;
	private Drill drill;
	private Game tryVariation;
	private Game gameAgainstTheEngine;
	private Board board;
	private JLabel txtComment;
	private JLabel txtStatus;
	private JPanel pnlMoves;
	private JTable tblMoves;
	private PositionTableModel modelMoves;
	private JList<Position> lstVariations;
	private JPanel pnlVariationsAndDrillStatus;
	private JPanel pnlVariations;	
	private DefaultListModel<Position> modelVariations;
	private DrillStatusPanel pnlDrillStatus;
	private JPanel pnlDrillHistory;
	private JLabel lblDrillHistory;	
	private JPanel pnlTryVariation;
	private JLabel lblTryVariation;
	private JPanel pnlToolBar;
	private JCheckBox cbOnlyMainline;
	private JCheckBox cbShowComments;
	private JCheckBox cbShowPieces;
	private JCheckBox cbShowBoard;
	private JCheckBox cbShowCoordinates;
	private JCheckBox cbRandomDrill;
	private AbstractButton btnLoadPGN;
	private AbstractButton btnDrill;
	private AbstractButton btnTryVariation;
	private AbstractButton btnEngine;
	private AbstractButton btnBack;
	private AbstractButton btnNext;
	private AbstractButton btnFlip;
	private AbstractButton btnBackToCurrentDrillPosition;
	private AbstractButton btnGameAgainstTheEngine;
	
	private JSplitPane splitCenter;
	private JSplitPane splitEast;
	private String pathToEngine;
	private UCIEngine engine;
	private String pathToGameEngine;
	private UCIEngine gameEngine;
	private String enginesBestMove;
	private Preferences prefs = Preferences.userNodeForPackage(AppFrame.class);
	private String keysTyped = "";
	
	private static Logger log = Logger.getLogger("AppFrame");

	private static final String PREFS_PATH_TO_ENGINE = "PATH_TO_ENGINE";
	private static final String PREFS_PATH_TO_GAME_ENGINE = "PATH_TO_GAME_ENGINE";
	private static final String PREFS_FRAME_WIDTH = "FRAME_WIDTH";
	private static final String PREFS_FRAME_HEIGHT = "FRAME_HEIGHT";
	private static final String PREFS_FRAME_EXTENDED_STATE = "FRAME_EXTENDED_STATE";
	private static final String PREFS_PGN_FILE = "PGN_FILE";	
	private static final String PREFS_WHITE_PERSPECTIVE = "WHITE_PERSPECTIVE";
	private static final String PREFS_SPLITTER_CENTER_POSITION = "SPLITTER_CENTER_POSITION";
	private static final String PREFS_SPLITTER_EAST_POSITION = "SPLITTER_EAST_POSITION";
	private static final String PREFS_FONT_SIZE = "FONT_SIZE";
	private static final String PREFS_FONT_NAME = "FONT_NAME";
	private static final String PREFS_ONLY_MAINLINE = "ONLY_MAINLINE";
	private static final String PREFS_SHOW_COMMENTS = "SHOW_COMMENTS";
	private static final String PREFS_SHOW_PIECES = "SHOW_PIECES";
	private static final String PREFS_SHOW_BOARD = "SHOW_BOARD";
	private static final String PREFS_SHOW_COORDINATES = "SHOW_COORDINATES";
	private static final String PREFS_RANDOM_DRILL = "RANDOM_DRILL";
	
	public AppFrame() throws HeadlessException {
		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
		setIconImage(Toolkit.getDefaultToolkit().getImage(AppFrame.class.getResource("/images/icon/Knight50.png")));
		board = new Board();
		board.addBoardListener(this);		
		doUI();
		SwingUtilities.updateComponentTreeUI(this);
		addWindowListener(new WindowAdapter() {
				
			@Override
			public void windowClosing(WindowEvent e) {
				savePrefs();
				if (engine != null) {
					try {
						engine.stop();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				if (gameEngine != null) {
					try {
						gameEngine.stop();
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
		prefs.put(PREFS_FONT_NAME, lstVariations.getFont().getName());
		prefs.putBoolean(PREFS_ONLY_MAINLINE, cbOnlyMainline.isSelected());
		prefs.putBoolean(PREFS_RANDOM_DRILL, cbRandomDrill.isSelected());
		prefs.putBoolean(PREFS_SHOW_COMMENTS, cbShowComments.isSelected());
		prefs.putBoolean(PREFS_SHOW_PIECES, cbShowPieces.isSelected());
		prefs.putBoolean(PREFS_SHOW_BOARD, cbShowBoard.isSelected());
		prefs.putBoolean(PREFS_SHOW_COORDINATES, cbShowCoordinates.isSelected());
		if (engine != null) {
			prefs.put(PREFS_PATH_TO_ENGINE, pathToEngine);
		}
		if (gameEngine != null) {
			prefs.put(PREFS_PATH_TO_GAME_ENGINE, pathToGameEngine);
		}
	}	
	
	private void loadPgn(final File pgn) {
		try {
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
		} catch (Exception ex) {			
			Game dummy = new Game();
			dummy.start();
			setGame(dummy);		
			setTitle("Chess Repertoire Trainer");
			txtStatus.setText(String.format("Loading PGN %s failed", pgn));
			new UncaughtExceptionHandler(this).uncaughtException(Thread.currentThread(), ex);
		}
	}
	
	private void setGame(Game g) {
		if (repertoire != null) {
			repertoire.removeGameListener(this);
		}
		repertoire = g;
		repertoire.addGameListener(this);
		g.gotoStartPosition();
	}
	
	private Position getCurrentPosition() {	
		return getCurrentGame().getPosition();
	}
	
	private Game getCurrentGame() {
		Game g = repertoire;
		if (gameAgainstTheEngine != null) {
			g = gameAgainstTheEngine;
		} else if (tryVariation != null) {
			g = tryVariation;
		} else if (drill != null) {
			g = drill;
		}
		return g;
	}
	
	private Action actionNext = new AbstractAction("Next move") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			Game g = getCurrentGame();
			if (g == tryVariation || g == gameAgainstTheEngine) {
				g.goForward();
			} else if (g == drill) {
				//gotoNextDrillPosition();
				drill.goForward();
			} else if (lstVariations.getSelectedIndex() >= 0) {
				Position p = (Position)modelVariations.get(lstVariations.getSelectedIndex());
				getCurrentGame().gotoPosition(p);						
			} else if (!modelVariations.isEmpty()) {
				Position p = (Position)modelVariations.get(0);
				getCurrentGame().gotoPosition(p);					
			} else {
				log.info("End of moves");
			};
		}
	};		
	
	private Action actionBack = new AbstractAction("Move back") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			getCurrentGame().goBack();			
		}
	};
	
	private Action actionUp = new AbstractAction("Up") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (lstVariations.getSelectedIndex() <= 0) {
				lstVariations.setSelectedIndex(modelVariations.size()-1);
			} else {
				lstVariations.setSelectedIndex(lstVariations.getSelectedIndex()-1);
			}
		}
	};
	
	private Action actionDown = new AbstractAction("Down") {
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
	
	private Action actionFlip = new AbstractAction("Flip board") {
		@Override
		public void actionPerformed(ActionEvent e) {
			board.flip();
		}
	};
	
	private Action actionDrill = new AbstractAction("Begin Drill") {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (drill == null) {				
				drill = new Drill(repertoire.getPosition(), board.isOrientationWhite(), cbOnlyMainline.isSelected(), cbRandomDrill.isSelected());
				drill.addGameListener(AppFrame.this);
				drill.addDrillListener(AppFrame.this);
				modelVariations.clear();
				actionLoadPGN.setEnabled(false);
				actionGameAgainstTheEngine.setEnabled(false);
				cbOnlyMainline.setEnabled(false);
				cbRandomDrill.setEnabled(false);
				pnlDrillStatus = new DrillStatusPanel(drill);
				pnlDrillStatus.setFont(lstVariations.getFont());
				setPanelVisible(pnlDrillStatus);
				this.putValue(Action.NAME, "End Drill ");
				btnDrill.setIcon(loadIcon("Make Decision red2"));
				drill.startDrill();
			} else {				
				drill.endDrill();				
			}			
		}
	};
	
	private Action actionShowComments = new AbstractAction("Show arrows/colored squares?") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (e != null && e.getSource() != cbShowComments) {
				cbShowComments.setSelected(!cbShowComments.isSelected());
			}
			board.setShowGraphicsComments(cbShowComments.isSelected());
			if (getCurrentGame() != null) updateBoard(false);
		}
	};
	
	private Action actionShowPieces = new AbstractAction("Show pieces?") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (e != null && e.getSource() != cbShowPieces) {
				cbShowPieces.setSelected(!cbShowPieces.isSelected());
			}
			board.setShowPieces(cbShowPieces.isSelected());
			if (getCurrentGame() != null) updateBoard(false);
		}
	};
	
	private Action actionShowBoard = new AbstractAction("Show board?") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (e != null && e.getSource() != cbShowBoard) {
				cbShowBoard.setSelected(!cbShowBoard.isSelected());
			}
			board.setShowBoard(cbShowBoard.isSelected());
			if (getCurrentGame() != null) updateBoard(false);
		}
	};
	
	private Action actionShowCoordinates = new AbstractAction("Show coordinates?") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (e != null && e.getSource() != cbShowCoordinates) {
				cbShowCoordinates.setSelected(!cbShowCoordinates.isSelected());
			}
			board.setShowCoordinates(cbShowCoordinates.isSelected());
			if (getCurrentGame() != null) updateBoard(false);
		}
	};
	
	private Action actionChooseFont = new AbstractAction("Choose Font") {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFontChooser fc = new JFontChooser();
			fc.setSelectedFont(lstVariations.getFont());
			if (fc.showDialog(AppFrame.this) == JFontChooser.OK_OPTION) {				
				setFonts(fc.getSelectedFont());
			}
			
		}
	};
	
	private String askForPathToEngine() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Please choose an UCI-compatible engine!");
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ok = fc.showOpenDialog(AppFrame.this);
		if (ok == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile().getAbsolutePath();
		} else {
			return null;
		}
		
	}
	
	private Action actionEngine = new AbstractAction("Start Engine") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (pathToEngine == null) {				
				pathToEngine = askForPathToEngine();				
			}
			if (pathToEngine == null) return;
			
			try {
				if (engine == null) {				
					engine = new UCIEngine(pathToEngine);
					engine.addEngineListener(AppFrame.this);
				}				
				if (engine.isStarted()) {
					engine.stop();
					this.putValue(Action.NAME, "Start Engine");
					btnEngine.setIcon(loadIcon("Superman"));
					btnEngine.setToolTipText("Start Engine");
					txtStatus.setText("Engine stopped");
					enginesBestMove = null;
				} else {
					engine.start();					
					engine.setFEN(getCurrentPosition().getFen());	
					this.putValue(Action.NAME, "Stop Engine");
					btnEngine.setToolTipText(engine.getName());
					btnEngine.setIcon(loadIcon("Superman red"));
				}
				updateBoard(false);
			} catch (RuntimeException ex) {
				engine = null;
				pathToEngine = null;
				throw ex;
			}
			
		}
	};
	
	private Action actionLoadPGN = new AbstractAction("Load PGN") {
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
	
	private Action actionTryVariation = new AbstractAction("Try Variation") {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (tryVariation != null) {
				tryVariation.removeGameListener(AppFrame.this);
				tryVariation = null;				
				updateBoard(false);
				btnTryVariation.setIcon(loadIcon("Microscope"));
				this.putValue(Action.NAME, "Try Variation");				
				
			} else {
				Position start = getCurrentPosition();
				tryVariation = new Game(new Position(null, start.getMove(), start.getFen()));				
				tryVariation.addGameListener(AppFrame.this);
				updateBoard(false);
				btnTryVariation.setIcon(loadIcon("Microscope red"));
				this.putValue(Action.NAME, "End Variation");				
			}
		}
	};
	
	private Action actionGameAgainstTheEngine = new AbstractAction("Training Game") {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (gameAgainstTheEngine != null) {
				gameAgainstTheEngine.removeGameListener(AppFrame.this);
				gameAgainstTheEngine = null;
				if (gameEngine != null) {
					gameEngine.endGame();
				}
				updateBoard(false);
				btnGameAgainstTheEngine.setIcon(loadIcon("Robot"));
				this.putValue(Action.NAME, "Training Game");				
				
			} else {
				if (pathToGameEngine == null) {				
					pathToGameEngine = askForPathToEngine();				
				}
				if (pathToGameEngine == null) return;				
				if (gameEngine == null) {				
					gameEngine = new UCIEngine(pathToGameEngine);
					gameEngine.addEngineListener(AppFrame.this);
				}
				Position start = getCurrentPosition();
				gameAgainstTheEngine = new Game(new Position(null, start.getMove(), start.getFen()));				
				gameAgainstTheEngine.addGameListener(AppFrame.this);
				int[] allSkillLevel = gameEngine.getAllSkillLevel();
				Integer[] levels = new Integer[allSkillLevel.length];
				for (int i = 0; i < levels.length; i++) {
					levels[i] = allSkillLevel[i];
				}
				Integer result = (Integer) JOptionPane.showInputDialog(AppFrame.this, "Engine Skill Level", 
						"Skill Level", JOptionPane.QUESTION_MESSAGE, null, 
						levels, levels[2]);
				if (result != null) {
					gameEngine.startGame(result);				
					updateBoard(false);
					btnGameAgainstTheEngine.setIcon(loadIcon("Robot red"));
					this.putValue(Action.NAME, "End Game");
				}
			}
		}
	};
	
	private Action actionBackToCurrentDrillPosition = new AbstractAction("Back to current drill position") {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (drill != null && drill.isInDrillHistory()) {
				drill.goToCurrentDrillPosition();
			}
		}
	};
	
	private Action actionShowNovelties = new AbstractAction("Show Novelties") {
		@Override
		public void actionPerformed(ActionEvent e) {
			File lastDir = pgn != null ? pgn.getParentFile() : null;
			JFileChooser fc = new JFileChooser(lastDir);
			fc.setDialogTitle("Please choose a PGN file that contains your games!");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setFileFilter(new FileNameExtensionFilter("*.pgn", "pgn"));
			int ok = fc.showOpenDialog(AppFrame.this);
			if (ok == JFileChooser.APPROVE_OPTION) {
				for (Game g : PGNReader.parse(fc.getSelectedFile())) {
					repertoire.findNovelty(g);
				}
			}		
		}
	};
	
	private void doUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension defaultFrameSize = new Dimension(screenSize.width/3*2, screenSize.height/3*2);
		
		JPanel pnlAll = new JPanel(new BorderLayout());
		pnlToolBar = new JPanel();
		pnlToolBar.setLayout(new BoxLayout(pnlToolBar, BoxLayout.LINE_AXIS));
		JPanel pnlCenter = new JPanel(new BorderLayout());
		JPanel pnlEast = new JPanel(new BorderLayout());
		JPanel pnlSouth = new JPanel(new BorderLayout());
		
		splitCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlCenter, pnlEast);
		splitCenter.setDividerLocation(prefs.getInt(PREFS_SPLITTER_CENTER_POSITION, defaultFrameSize.width/3*2));
		
		pnlAll.add(pnlToolBar, BorderLayout.PAGE_START);		
		pnlAll.add(splitCenter, BorderLayout.CENTER);		
		pnlAll.add(pnlSouth, BorderLayout.PAGE_END);
		getContentPane().add(pnlAll, BorderLayout.CENTER);
		
		
		
		cbShowComments = new JCheckBox(actionShowComments);
		cbShowComments.setFocusable(false);
		cbShowComments.setSelected(prefs.getBoolean(PREFS_SHOW_COMMENTS, false));
		actionShowComments.actionPerformed(null);		
		
		cbShowPieces = new JCheckBox(actionShowPieces);
		cbShowPieces.setFocusable(false);
		cbShowPieces.setSelected(prefs.getBoolean(PREFS_SHOW_PIECES, true));
		actionShowPieces.actionPerformed(null);		
		
		cbShowBoard = new JCheckBox(actionShowBoard);
		cbShowBoard.setFocusable(false);
		cbShowBoard.setSelected(prefs.getBoolean(PREFS_SHOW_BOARD, false));
		actionShowBoard.actionPerformed(null);		
		
		cbShowCoordinates = new JCheckBox(actionShowCoordinates);
		cbShowCoordinates.setFocusable(false);
		cbShowCoordinates.setSelected(prefs.getBoolean(PREFS_SHOW_COORDINATES, false));
		actionShowCoordinates.actionPerformed(null);		
						
		cbOnlyMainline = new JCheckBox("Accept main line only?");
		cbOnlyMainline.setSelected(prefs.getBoolean(PREFS_ONLY_MAINLINE, true));
		cbOnlyMainline.setFocusable(false);
		cbOnlyMainline.setEnabled(false);		
		cbRandomDrill = new JCheckBox("Random position drill?");
		cbRandomDrill.setFocusable(false);
		cbRandomDrill.setSelected(prefs.getBoolean(PREFS_RANDOM_DRILL, false));
		
		JPanel pnlBoard = new JPanel(new BorderLayout());
		pnlBoard.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 5));
		pnlBoard.add(board, BorderLayout.CENTER);
		JPanel pnlCenterSouth = new JPanel(new BorderLayout());
		JPanel pnlMoveComments = new JPanel();
		pnlMoveComments.setLayout(new BoxLayout(pnlMoveComments, BoxLayout.LINE_AXIS));
		txtComment = new JLabel();
//		pnlMoveComments.add(txtComment);
//		pnlMoveComments.add(Box.createHorizontalGlue());
		pnlMoveComments.add(cbShowBoard);
		pnlMoveComments.add(cbShowPieces);
		pnlMoveComments.add(cbShowCoordinates);
		pnlMoveComments.add(cbShowComments);
		pnlCenterSouth.add(pnlMoveComments, BorderLayout.PAGE_START);
		JPanel pnlBoardControls = new JPanel();
		pnlBoardControls.setLayout(new BoxLayout(pnlBoardControls, BoxLayout.LINE_AXIS));
		pnlBoardControls.add(Box.createHorizontalGlue());
		pnlBoardControls.add(btnBack = createButton(actionBack, "Circled Left 2", false, false));	
		pnlBoardControls.add(btnFlip = createButton(actionFlip, "Available Updates", false, false)); //Rotate Right-64.png
		pnlBoardControls.add(btnNext = createButton(actionNext, "Circled Right 2", false, false));	
		pnlBoardControls.add(Box.createHorizontalGlue());
		pnlCenterSouth.add(pnlBoardControls, BorderLayout.CENTER);
		pnlCenter.add(pnlBoard, BorderLayout.CENTER);		
		pnlCenter.add(pnlCenterSouth, BorderLayout.PAGE_END);
		pnlCenter.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
		
		JPopupMenu popUpChooseFont = new JPopupMenu();
		popUpChooseFont.add(actionChooseFont);
		pnlMoves = new JPanel(new BorderLayout());
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
		tblMoves.setComponentPopupMenu(popUpChooseFont);		
		tblMoves.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = tblMoves.columnAtPoint(e.getPoint());
				int row = tblMoves.rowAtPoint(e.getPoint());
				if (column >= 0 && row >= 0) {
					Position p = modelMoves.getPositionAt(row, column);
					getCurrentGame().gotoPosition(p);					
				}				
			}			
		});
		pnlMoves.add(new JScrollPane(tblMoves));
//		pnlMoves.setPreferredSize(new Dimension(150, 500));
		
		pnlTryVariation = new JPanel(new BorderLayout());
		lblTryVariation = new JLabel("Trying Variation");
		lblTryVariation.setForeground(Color.RED);
		lblTryVariation.setHorizontalAlignment(SwingConstants.CENTER);
		pnlTryVariation.add(lblTryVariation);
		pnlVariationsAndDrillStatus = new JPanel(new BorderLayout());
		pnlVariations = new JPanel(new BorderLayout());
		pnlVariations.setBorder(BorderFactory.createTitledBorder("Repertoire Variations"));
		modelVariations = new DefaultListModel<Position>();
		lstVariations = new JList<Position>(modelVariations);		
		lstVariations.setFocusable(false);		
		lstVariations.setComponentPopupMenu(popUpChooseFont);
		lstVariations.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (lstVariations.getSelectedIndex() >= 0) {
					Position p = (Position)modelVariations.get(lstVariations.getSelectedIndex());
					repertoire.gotoPosition(p);
				}
			}			
		});		
		pnlVariations.add(new JScrollPane(lstVariations));		
//		pnlVariations.setPreferredSize(new Dimension(150, 200));
		pnlVariationsAndDrillStatus.add(pnlVariations);
		
		pnlDrillHistory = new JPanel();
		pnlDrillHistory.setLayout(new BoxLayout(pnlDrillHistory, BoxLayout.PAGE_AXIS));
		lblDrillHistory = new JLabel("Browsing Drill History");
		lblDrillHistory.setForeground(Color.RED);		
		btnBackToCurrentDrillPosition = createButton(actionBackToCurrentDrillPosition, "Make Decision red2", true, false);
		btnBackToCurrentDrillPosition.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblDrillHistory.setAlignmentX(Component.CENTER_ALIGNMENT);
		pnlDrillHistory.add(Box.createRigidArea(new Dimension(0,10)));
		pnlDrillHistory.add(lblDrillHistory);
		pnlDrillHistory.add(Box.createRigidArea(new Dimension(0,10)));
		pnlDrillHistory.add(btnBackToCurrentDrillPosition);
		splitEast = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlVariationsAndDrillStatus, pnlMoves);
		splitEast.setBorder(null);
		int splitEastPosition = prefs.getInt(PREFS_SPLITTER_EAST_POSITION, 0);
		if (splitEastPosition > 0) {
			splitEast.setDividerLocation(splitEastPosition);
		}
		pnlEast.add(splitEast);
		

		pnlToolBar.add(Box.createHorizontalStrut(10));
		pnlToolBar.add(btnLoadPGN = createButton(actionLoadPGN, "Open in Popup", true, false));
		pnlToolBar.add(createButton(actionShowNovelties, "Open in Popup", true, false));
//		pnlToolBar.add(cbShowComments);
		pnlToolBar.add(Box.createHorizontalGlue());
		pnlToolBar.add(btnDrill = createButton(actionDrill, "Make Decision", true, true));		
		//pnlToolBar.add(cbOnlyMainline);
		pnlToolBar.add(cbRandomDrill);
		pnlToolBar.add(Box.createHorizontalGlue());
		pnlToolBar.add(btnEngine = createButton(actionEngine, "Superman", true, true)); //"Robot-64.png
		pnlToolBar.add(btnTryVariation = createButton(actionTryVariation, "Microscope", true, true));
		pnlToolBar.add(btnGameAgainstTheEngine = createButton(actionGameAgainstTheEngine, "Robot", true, true));
		pnlToolBar.add(Box.createHorizontalStrut(10));
		
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
		
		
		Dimension prefSize = new Dimension(prefs.getInt(PREFS_FRAME_WIDTH, defaultFrameSize.width), 
				prefs.getInt(PREFS_FRAME_HEIGHT, defaultFrameSize.height));		
		this.setPreferredSize(prefSize);
		pack();
		if (prefs.getBoolean(PREFS_FRAME_EXTENDED_STATE, false)) {
			setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			setLocationRelativeTo(null);
		}
		
		final String lastPGN = prefs.get(PREFS_PGN_FILE, null);
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
		
		int fontSize = prefs.getInt(PREFS_FONT_SIZE, isUltraHighResolution() ? 24 : 12);
		String fontName = prefs.get(PREFS_FONT_NAME, "Frutiger Standard");
		setFonts(new Font(fontName, Font.PLAIN, fontSize));
		
		pathToEngine = prefs.get(PREFS_PATH_TO_ENGINE, null);
		pathToGameEngine = prefs.get(PREFS_PATH_TO_GAME_ENGINE, null);
	}
	
	public static AbstractButton createButton(Action action, String icon, boolean showText, boolean toggleButton) {
		AbstractButton btn = toggleButton ? new JToggleButton(action) : new JButton(action);
		if (icon != null) {
			btn.setIcon(loadIcon(icon));
			btn.setVerticalTextPosition(SwingConstants.BOTTOM);
			btn.setHorizontalTextPosition(SwingConstants.CENTER);
			btn.setToolTipText(btn.getText());
			if (!showText) {
				btn.setText("");				
			}
		}
		btn.setFocusable(false);
		btn.putClientProperty("JComponent.sizeVariant", "large");
		return btn;
	}
	
	private static ImageIcon loadIcon(String icon) {
		String suffix = isUltraHighResolution() ? "-64.png" : "-32.png"; 
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(AppFrame.class.getResource("/images/icon/" + icon + suffix)));
	}
	
	private void updateBoard(boolean playSound) {	
		Position p = getCurrentPosition();
		board.setCurrentPosition(p);
		String comment = p != null ? p.getCommentText() : null;
		if (comment != null && comment.trim().length() > 0) {
			comment = "<html>Move comment: <b>" + comment + "</b></html>";
		} else {
			comment = " ";
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
		tblMoves.scrollRectToVisible(tblMoves.getCellRect(modelMoves.getRowCount()-1, 0, false));
		modelVariations.clear();
		if (drill == null) {
			for (Position variation : p.getVariations()) {
				modelVariations.addElement(variation);
			}
		}
		if (getCurrentGame() == gameAgainstTheEngine) {
			if (board.isOrientationWhite() != getCurrentPosition().isWhiteToMove()) {
				if (!gameAgainstTheEngine.getPosition().hasNext()) {
					gameEngine.setFENandMove(p.getFen());
				}
			}
		}
		if (engine != null && engine.isStarted()) {
			engine.setFEN(p.getFen());			
		} else {
			txtStatus.setText(p.getFen());
		}
		
		if (gameAgainstTheEngine != null || tryVariation != null) {
			setPanelVisible(pnlTryVariation);
		} else if (drill != null) {			
			if (drill.isInDrillHistory()) {
				setPanelVisible(pnlDrillHistory);
			} else {
				setPanelVisible(pnlDrillStatus);
			}
		} else {
			setPanelVisible(pnlVariations);
		}
		
		Game g = getCurrentGame();
		actionNext.setEnabled(g.hasNext());
		actionBack.setEnabled(g.hasPrevious());	
		
		// Engine move
		board.clearAdditionalGraphicsComment();	
		if (enginesBestMove != null) {
			Square from = p.getSquare(enginesBestMove.substring(0, 2));
			Square to = p.getSquare(enginesBestMove.substring(2, 4));								
			board.addAdditionalGraphicsComment(new GraphicsComment(from, to, Color.BLACK));
		}
	}

	@Override
	public void userMove(String move) {
		if (gameAgainstTheEngine != null) {
			try {
				gameAgainstTheEngine.addMove(move);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(AppFrame.this, String.format("Move '%s' is not legal", move), 
						"Illegal move", JOptionPane.WARNING_MESSAGE);
			}
		} else if (tryVariation != null) {
			tryVariation.addMove(move);
		} else if (drill != null) {
			if (drill.isInDrillHistory()) {
				if (drill.isCorrectMove(move)) {
					drill.doMove(move);				
				}				
			} else {				
				if (drill.isCorrectMove(move)) {
					drill.doMove(move);				
					waitAndLoadNextDrillPosition(drill.getPosition());
				} else if (drill.getPosition().hasNext()) {
					Sounds.wrong();
				}
			}
		} else {
			if (repertoire.isCorrectMove(move)) {
				repertoire.doMove(move);				
			}
		}
	}
	
	@Override
	public void userClickedSquare(String squareName) {
		Game g = getCurrentGame();
		if (g instanceof Drill) {			
			if (drill.isInDrillHistory()) {
				if (drill.isCorrectSquare(squareName)) {
					drill.gotoPosition(drill.getPosition().getNext());	
				}				
			} else {
				if (drill.isCorrectSquare(squareName)) {
					drill.gotoPosition(drill.getPosition().getNext());				
					waitAndLoadNextDrillPosition(drill.getPosition());
				} else if (drill.getPosition().hasNext()) {
					Sounds.wrong();
				}
			}
		} else {
			for (Position variation : g.getPosition().getVariations()) {
				if (variation.getMoveSquareNames()[1].equals(squareName)) {
					g.gotoPosition(variation);					
					break;
				}
			}
		}
	}
	
	private void userTypedSquare(String squareName) {
		userClickedSquare(squareName);
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
		actionGameAgainstTheEngine.setEnabled(true);
		//cbOnlyMainline.setEnabled(true);
		cbRandomDrill.setEnabled(true);
		actionDrill.putValue(Action.NAME, "Begin Drill");
		btnDrill.setIcon(loadIcon("Make Decision"));
		btnDrill.setSelected(false);
		updateBoard(false);
		setPanelVisible(pnlVariations);
		
		showMessageDialog(String.format("Drill ended for %d positions. It took %s.",
						drillStats.drilledPositions,
						drillStats.getFormattedDuration()), 
				"Drill ended");
	}
	
	private void showMessageDialog(String text, String title) {
		JOptionPane.showMessageDialog(AppFrame.this,
				text, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void setPanelVisible(JPanel pnl) {
		pnlVariationsAndDrillStatus.removeAll();
		pnlVariationsAndDrillStatus.add(pnl);		
		pnlVariationsAndDrillStatus.revalidate();
		pnlVariationsAndDrillStatus.repaint();
	}
	
	private void setFonts(Font f) {	
		lblTryVariation.setFont(f);
		lblDrillHistory.setFont(f);
		btnBackToCurrentDrillPosition.setFont(f);
		lstVariations.setFont(f);
		tblMoves.setFont(f);	
		((javax.swing.border.TitledBorder)pnlVariations.getBorder()).setTitleFont(f);
		((javax.swing.border.TitledBorder)pnlMoves.getBorder()).setTitleFont(f);
		for (Component c : pnlToolBar.getComponents()) {
			c.setFont(f);
		}
		txtComment.setFont(f);
		txtStatus.setFont(f);		
		revalidate();
		repaint();		
		resizeToolbarButtons();		
	}
	
	private void resizeToolbarButtons() {
		Dimension dim = new Dimension();
		for (Component c : pnlToolBar.getComponents()) {
			if (c instanceof AbstractButton && !(c instanceof JCheckBox)) {
				c.doLayout();
				dim.height = Math.max(dim.height, c.getPreferredSize().height);
				dim.width = Math.max(dim.width, c.getPreferredSize().width);
			}
		}
		for (Component c : pnlToolBar.getComponents()) {
			if (c instanceof AbstractButton && !(c instanceof JCheckBox)) {
				c.setPreferredSize(dim);
				c.setMinimumSize(dim);
				c.setMaximumSize(dim);
			}
		}
		
		
	}

	@Override
	public void wasCorrect(DrillEvent e) {
	}

	@Override
	public void wasIncorrect(DrillEvent e) {
	}

	@Override
	public void drillingNextVariation(DrillEvent e) {
	}

	@Override
	public void newEngineScore(final UCIEngine e, final Score s) {
		if (e == engine) {
			SwingUtilities.invokeLater(new Runnable() {
							
				@Override
				public void run() {	
					Position currentPosition = getCurrentPosition();
				
					boolean whiteToMove = currentPosition.isWhiteToMove();
					boolean positiveScore = (whiteToMove && s.score >= 0) || (!whiteToMove && s.score < 0);	
					String scoreText = String.format("%d [%s%.2f] %s", 
							s.depth, positiveScore ? "+" : "-", Math.abs(s.score), s.bestLine);
					txtStatus.setText(scoreText);
					
					//draw move arrow
					if (!s.bestLine.isEmpty()) {
						if (!s.bestLine.get(0).equals(enginesBestMove)) {
							enginesBestMove = s.bestLine.get(0);
							updateBoard(false);
						}
					}
				}
				
			});
		}
		
	}
	
	@Override
	public void engineMoved(UCIEngine e, String engineMove) {
		if (e == gameEngine) {
			if (!"(none)".equals(engineMove)) {				
				gameAgainstTheEngine.addMove(getCurrentPosition().translateMove(engineMove));
			}
		}
	}

	private static boolean isUltraHighResolution() {
		return Toolkit.getDefaultToolkit().getScreenSize().width >= 1600;
	}
	
	@Override
	public void eventDispatched(AWTEvent event) {
		if (event instanceof KeyEvent) {
			KeyEvent keyEvent = (KeyEvent)event;			
			if (keyEvent.getID() == KeyEvent.KEY_TYPED) {				
				char c = keyEvent.getKeyChar();
				if (getCurrentGame() == gameAgainstTheEngine) {
					if (c == '\n') {
						String move = keysTyped.trim();
						keysTyped = "";
						userMove(move);
					} else if (c == 27) { //ESC
						keysTyped = "";
					} else if (c == 8) { //BACKSPACE
						if (keysTyped.length() > 0)  {
							keysTyped = keysTyped.substring(0, keysTyped.length() - 1);
						}
					} else {
						keysTyped = keysTyped + String.valueOf(c);
					}
					
				} else {
					if (c == 'K' || c == 'Q' || c == 'B'|| c == 'N'|| c == 'R') {
						keysTyped = String.valueOf(c);
					} else if (c == 'x' && keysTyped.length() == 1 && keysTyped.charAt(0) >= 'B') {
						keysTyped = keysTyped + String.valueOf(c);
					} else if ((c >= 'a' && c <= 'h') || (c >= '1' && c <= '8')) {
						keysTyped = keysTyped + String.valueOf(c);
					} else {
						keysTyped = "";
					}
					if (getCurrentGame() == drill) {
						pnlDrillStatus.setLast(keysTyped);
					}
					if (keysTyped.length() >= 2 && Character.isDigit(keysTyped.charAt(keysTyped.length()-1))) {
						userTypedSquare(keysTyped.substring(keysTyped.length()-2, keysTyped.length()));
						keysTyped = "";
					}
				}
				txtStatus.setText(keysTyped.length() == 0 ? " " : keysTyped);
			}
		}
    }
	
	
	
}
