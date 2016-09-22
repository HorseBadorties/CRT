package de.toto.gui;

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
import de.toto.game.DrillListener;
import de.toto.game.GameEvent;
import de.toto.game.GameListener;
import de.toto.game.Position;
import de.toto.pgn.PGNReader;
import de.toto.sound.Sounds;

@SuppressWarnings("serial")
public class AppFrame extends JFrame implements BoardListener, GameListener, DrillListener, EngineListener {
	
	private File pgn = null;
	private Game game;
	private Drill drill;
	private Board board;
	private JLabel txtComment;
	private JLabel txtStatus;
	private JPanel pnlMoves;
	private JTable tblMoves;
	private PositionTableModel modelMoves;
	private JList lstVariations;
	private JPanel pnlVariationsAndDrillStatus;
	private JPanel pnlVariations;
	private DefaultListModel modelVariations;
	private DrillStatusPanel pnlDrillStatus;
	private JPanel pnlToolBar;
	private JCheckBox cbOnlyMainline;
	private JCheckBox cbShowComments;
	private JCheckBox cbRandomDrill;
	private JButton btnLoadPGN;
	private JButton btnDrill;
	private JButton btnEngine;
	private JButton btnBack;
	private JButton btnNext;
	private JButton btnFlip;	
	
	private JSplitPane splitCenter;
	private JSplitPane splitEast;
	private String pathToEngine;
	private UCIEngine engine;
	private Preferences prefs = Preferences.userNodeForPackage(AppFrame.class);
	
	private static Logger log = Logger.getLogger("AppFrame");

	private static final String PREFS_PATH_TO_ENGINE = "PREFS_PATH_TO_ENGINE";
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
				if (engine != null) {
					try {
						engine.stop();
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
		if (engine != null) {
			prefs.put(PREFS_PATH_TO_ENGINE, pathToEngine);
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
		if (game != null) {
			game.removeGameListener(this);
		}
		game = g;
		game.addGameListener(this);
		g.gotoStartPosition();
	}
	
	private Position getCurrentPosition() {
		Game g = drill != null ? drill : game;		
		return g.getPosition();
	}
	
	private Action actionNext = new AbstractAction("Next move") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (drill != null) {
				//gotoNextDrillPosition();
				drill.goForward();
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
	
	private Action actionBack = new AbstractAction("Move back") {
		@Override
		public void actionPerformed(ActionEvent e) {
			Game g = drill != null ? drill : game;
			g.goBack();			
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
	
	private Action actionBeginDrill = new AbstractAction("Begin Drill") {
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
				pnlDrillStatus = new DrillStatusPanel(drill);
				pnlDrillStatus.setFont(lstVariations.getFont());
				setPanelVisible(pnlDrillStatus);
				this.putValue(Action.NAME, "End Drill");
				btnDrill.setIcon(loadIcon("Make Decision red-32.png"));
				drill.startDrill();
			} else {				
				drill.endDrill();				
			}			
		}
	};
	
	
	private Action actionShowComments = new AbstractAction("Show arrows/colored squares?") {
		@Override
		public void actionPerformed(ActionEvent e) {
			board.setShowGraphicsComments(cbShowComments.isSelected());
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
	
	private Action actionEngine = new AbstractAction("Start Engine") {
		@Override
		public void actionPerformed(ActionEvent e) {			
			if (pathToEngine == null) {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("Please choose an UCI-compatible engine!");
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int ok = fc.showOpenDialog(AppFrame.this);
				if (ok == JFileChooser.APPROVE_OPTION) {
					pathToEngine = fc.getSelectedFile().getAbsolutePath();
				}
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
					btnEngine.setIcon(loadIcon("Robot-32.png"));
					txtStatus.setText("Engine stopped");
				} else {
					engine.start();
					engine.setFEN(getCurrentPosition().getFen());	
					this.putValue(Action.NAME, "Stop Engine");
					btnEngine.setIcon(loadIcon("Robot red-32.png"));
				}
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
	
	private void doUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel pnlAll = new JPanel(new BorderLayout());
		pnlToolBar = new JPanel();
		JPanel pnlCenter = new JPanel(new BorderLayout());
		JPanel pnlEast = new JPanel(new BorderLayout());
		JPanel pnlSouth = new JPanel(new BorderLayout());
		
		splitCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlCenter, pnlEast);
		splitCenter.setDividerLocation(prefs.getInt(PREFS_SPLITTER_CENTER_POSITION, 650));
		
		pnlAll.add(pnlToolBar, BorderLayout.PAGE_START);		
		pnlAll.add(splitCenter, BorderLayout.CENTER);		
		pnlAll.add(pnlSouth, BorderLayout.PAGE_END);
		getContentPane().add(pnlAll, BorderLayout.CENTER);
		
		pnlToolBar.add(btnLoadPGN = createButton(actionLoadPGN, "Open in Window-32.png", true));
		
		cbShowComments = new JCheckBox(actionShowComments);
		cbShowComments.setFocusable(false);
		cbShowComments.setSelected(prefs.getBoolean(PREFS_SHOW_COMMENTS, false));
		actionShowComments.actionPerformed(null);
		pnlToolBar.add(cbShowComments);
				
		pnlToolBar.add(btnDrill = createButton(actionBeginDrill, "Make Decision-32.png", true));		
		cbOnlyMainline = new JCheckBox("Accept main line only?");
		cbOnlyMainline.setSelected(prefs.getBoolean(PREFS_ONLY_MAINLINE, true));
		cbOnlyMainline.setFocusable(false);
		cbOnlyMainline.setEnabled(false);
		pnlToolBar.add(cbOnlyMainline);
		cbRandomDrill = new JCheckBox("Random position drill?");
		cbRandomDrill.setFocusable(false);
		cbRandomDrill.setSelected(prefs.getBoolean(PREFS_RANDOM_DRILL, false));
		pnlToolBar.add(cbRandomDrill);		

		JPopupMenu popUpFlipBoard = new JPopupMenu();
		popUpFlipBoard.add(actionFlip);
		board.setComponentPopupMenu(popUpFlipBoard);
		JPanel pnlBoard = new JPanel(new BorderLayout());
		pnlBoard.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 5));
		pnlBoard.add(board, BorderLayout.CENTER);
		JPanel pnlBoardControls = new JPanel();
		pnlBoardControls.add(btnBack = createButton(actionBack, "Circled Left 2-32.png", false));	
		pnlBoardControls.add(btnFlip = createButton(actionFlip, "Rotate Right-32.png", false));
		pnlBoardControls.add(btnNext = createButton(actionNext, "Circled Right 2-32.png", false));	
		pnlBoardControls.add(txtComment = new JLabel());
		pnlCenter.add(pnlBoard, BorderLayout.CENTER);		
		pnlCenter.add(pnlBoardControls, BorderLayout.PAGE_END);
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
		lstVariations.setComponentPopupMenu(popUpChooseFont);
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
		
		pnlToolBar.add(btnEngine = createButton(actionEngine, "Robot-32.png", true));	
		
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
		
		int fontSize = prefs.getInt(PREFS_FONT_SIZE, 12);
		String fontName = prefs.get(PREFS_FONT_NAME, "Frutiger Standard");
		setFonts(new Font(fontName, Font.PLAIN, fontSize));
		
		pathToEngine = prefs.get(PREFS_PATH_TO_ENGINE, null);
	}
	
	public static JButton createButton(Action action, String icon, boolean showText) {
		JButton btn = new JButton(action);
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
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(AppFrame.class.getResource("/images/icon/" + icon)));
	}

	private void updateBoard(boolean playSound) {	
		Position p = getCurrentPosition();
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
		tblMoves.scrollRectToVisible(tblMoves.getCellRect(modelMoves.getRowCount()-1, 0, false));
		modelVariations.clear();
		if (drill == null) {
			for (Position variation : p.getVariations()) {
				modelVariations.addElement(variation);
			}
		}		
		if (engine != null && engine.isStarted()) {
			engine.setFEN(p.getFen());
		} else {
			txtStatus.setText(p.getFen());
		}
		
		Game g = drill != null ? drill : game;
		actionNext.setEnabled(g.hasNext());
		actionBack.setEnabled(g.hasPrevious());		
	}

	@Override
	public void userMove(String move) {
		if (drill != null) {
			if (drill.isCurrentDrillPosition()) {
				if (drill.isCorrectMove(move)) {
					drill.doMove(move);				
					waitAndLoadNextDrillPosition(drill.getPosition());
				} else if (drill.getPosition().hasNext()) {
					Sounds.wrong();
				}
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
		actionBeginDrill.putValue(Action.NAME, "Begin Drill");
		btnDrill.setIcon(loadIcon("Make Decision-32.png"));
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
		lstVariations.setFont(f);
		tblMoves.setFont(f);	
		((javax.swing.border.TitledBorder)pnlVariations.getBorder()).setTitleFont(f);
		((javax.swing.border.TitledBorder)pnlMoves.getBorder()).setTitleFont(f);
		for (Component c : pnlToolBar.getComponents()) {
			c.setFont(f);
		}
		revalidate();
		repaint();		
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
	public void newEngineScore(final Score s) {
		SwingUtilities.invokeLater(new Runnable() {
						
			@Override
			public void run() {	
				boolean whiteToMove = getCurrentPosition().isWhiteToMove();
				boolean positiveScore = (whiteToMove && s.score >= 0) || (!whiteToMove && s.score < 0);
				String scoreText = String.format("%d [%s%.2f] %s", 
						s.depth, positiveScore ? "+" : "-", Math.abs(s.score), s.bestLine);
				txtStatus.setText(scoreText);
			}
			
		});
		
	}

}
