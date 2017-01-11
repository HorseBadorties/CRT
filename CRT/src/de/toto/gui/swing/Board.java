package de.toto.gui.swing;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.*;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;

import de.toto.game.Position;
import de.toto.game.Position.GraphicsComment;
import de.toto.game.Rules.Piece;
import de.toto.game.Rules.PieceType;

@SuppressWarnings("serial")
public class Board extends JPanel {

	private Position currentPosition;
	private BoardCanvas boardCanvas = new BoardCanvas(this);
	private boolean showBoard = true;
	private boolean showGraphicsComments = true;
	private boolean showPieces = true;
	private boolean showCoordinates = true;
	private boolean showMaterialImbalance = true;
	private java.util.List<GraphicsComment> additionalGraphicsComment = new ArrayList<GraphicsComment>();

	public Position getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Position currentPosition) {
		this.currentPosition = currentPosition;
		boardCanvas.positionChanged();
		repaint();
	}

	public void setShowGraphicsComments(boolean value) {
		showGraphicsComments = value;
	}

	public void setShowPieces(boolean value) {
		showPieces = value;
	}

	public void setShowBoard(boolean value) {
		showBoard = value;
	}

	public void setShowCoordinates(boolean value) {
		showCoordinates = value;
	}
	
	public void setShowMaterialImbalance(boolean value) {
		showMaterialImbalance = value;
		boardCanvas.rescale();
	}

	public void clearAdditionalGraphicsComment() {
		additionalGraphicsComment.clear();
	}

	public void addAdditionalGraphicsComment(GraphicsComment gc) {
		additionalGraphicsComment.add(gc);
	}

	public void flip() {
		boardCanvas.flip();
	}

	public boolean isOrientationWhite() {
		return boardCanvas.isOrientationWhite();
	}

	public Board() {
		super();
		add(boardCanvas);
		resizeBoardCanvas();
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				resizeBoardCanvas();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				resizeBoardCanvas();
			}

		});
	}

	private void resizeBoardCanvas() {
		Dimension pref = getSize();
		int canvasSize = Math.min(pref.height, pref.width);
		canvasSize = canvasSize / 8 * 8;
		boardCanvas.setPreferredSize(new Dimension(canvasSize, canvasSize));
		revalidate();
	}

	public void addBoardListener(BoardListener boardListener) {
		listenerList.add(BoardListener.class, boardListener);
	}

	public void removeBoardListener(BoardListener boardListener) {
		listenerList.remove(BoardListener.class, boardListener);
	}

	protected void fireUserMoved(String move) {
		for (BoardListener l : listenerList.getListeners(BoardListener.class)) {
			l.userMove(move);
		}
	}

	protected void fireUserClickedSquare(String squarename) {
		for (BoardListener l : listenerList.getListeners(BoardListener.class)) {
			l.userClickedSquare(squarename);
		}
	}

	public static class BoardCanvas extends JComponent {

		private static class Square {
			de.toto.game.Square gameSquare;
			int rank;
			int file;
			boolean isWhite; // cache value rather than use
								// de.toto.game.Square.isWhite() over and over
								// again during paint()
			Point topLeftOnBoard;
			boolean isDragSource = false;
			boolean isDragTarget = false;

			public Square(int rank, int file) {
				this.rank = rank;
				this.file = file;
				isWhite = (file % 2 == 0 && rank % 2 != 0) || (file % 2 != 0 && rank % 2 == 0);
			}

			// e.g. "a1"
			String getName() {
				Character cFile = Character.valueOf((char) (file + 96));
				return cFile.toString() + rank;
			}
		}

		private Image boardImage, boardImageScaled;
		private SVGIcon wK, wQ, wR, wB, wN, wP, bK, bQ, bR, bB, bN, bP;
		private SVGIcon wQs, wRs, wBs, wNs, wPs, bQs, bRs, bBs, bNs, bPs;
		private int scaleSize;

		private static final Color squareSelectionColor = new Color(.3f, .4f, .5f, .6f);
		private static final Color highlightColorGreen = new Color(0f, 1f, 0f, .4f);
		private static final Color highlightColorRed = new Color(1f, 0f, 0f, .4f);
		private static final Color highlightColorYellow = new Color(1f, 1f, 0f, .4f);
		private static final Color arrowColorGreen50Percent = new Color(0f, 1f, 0f, .5f);
		private static final Color arrowColorRed50Percent = new Color(1f, 0f, 0f, .5f);
		private static final Color arrowColorYellow50Percent = new Color(1f, 1f, 0f, .5f);
		private static final Color arrowColorBlack50Percent = new Color(0f, 0f, 0f, .5f);
		private static final Color arrowColorGreen20Percent = new Color(0f, 1f, 0f, .2f);
		private static final Color arrowColorRed20Percent = new Color(1f, 0f, 0f, .2f);
		private static final Color arrowColorYellow20Percent = new Color(1f, 1f, 0f, .2f);
		private static final Color arrowColorBlack20Percent = new Color(0f, 0f, 0f, .2f);

		private static final Color lightBlue = new Color(243, 243, 243);
		private static final Color darkBlue = new Color(115, 137, 182);
		private static final Color lightGreen = new Color(208, 217, 168);
		private static final Color darkGreen = new Color(81, 160, 104);
		private static final Color lightGray = new Color(223, 223, 223);
		private static final Color darkGray = new Color(128, 128, 128);
		private static final Color lighBrown = new Color(208, 192, 160);
		private static final Color darkBrown = new Color(160, 128, 80);

		private static final Color squareColorWhite = lighBrown;
		private static final Color squareColorBlack = darkBrown;
		
		private boolean isDragging = false;
		private Point cursorLocation;
		private Square dragSquare = null;
		private Square dragTarget = null;

		private Board board;
		private Square[][] squares = new Square[8][8];
		private boolean isOrientationWhite = true;

		private void positionChanged() {
			for (int rank = 1; rank <= 8; rank++) {
				for (int file = 1; file <= 8; file++) {
					squares[rank - 1][file - 1].gameSquare = board.currentPosition.getSquare(rank, file);
				}
			}
		}

		private MouseAdapter mouseAdapter = new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (!isDragging) {
					dragSquare = getSquareAt(e.getPoint());
					if (dragSquare == null || dragSquare.gameSquare.piece == null)
						return;
					dragSquare.isDragSource = true;
				}
				isDragging = true;
				cursorLocation = e.getPoint();
				Square newDragTarget = getSquareAt(e.getPoint());
				if (newDragTarget != null && dragTarget != newDragTarget) {
					if (dragTarget != null) {
						dragTarget.isDragTarget = false;
					}
					dragTarget = newDragTarget;
					if (dragTarget != null) {
						dragTarget.isDragTarget = true;
					}
				}
				repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
					return;
				}
				try {
					if (!isDragging || dragSquare.gameSquare.piece == null) {
						return;
					}
					boolean correctSideMoved = dragSquare.gameSquare.piece.isWhite == board.getCurrentPosition()
							.isWhiteToMove();
					Square dropSquare = getSquareAt(e.getPoint());
					if (correctSideMoved && dropSquare != null && dropSquare != dragSquare) {
						if (dragSquare.gameSquare.canMoveTo(dropSquare.gameSquare, board.getCurrentPosition(), null)) {
							String move = dragSquare.gameSquare.piece.pgnChar + dragSquare.getName();
							boolean isCapture = dropSquare.gameSquare.piece != null;
							// consider En Passant for pawn moves...
							if (dragSquare.gameSquare.piece.type == PieceType.PAWN) {
								isCapture = dragSquare.file != dropSquare.file;
							}
							if (isCapture) {
								move += "x";
							} else {
								move += "-";
							}
							move += dropSquare.getName();
							// Promotion?
							if (dragSquare.gameSquare.piece.type == PieceType.PAWN) {
								if ((dragSquare.gameSquare.piece.isWhite && dropSquare.rank == 8)
										|| (!dragSquare.gameSquare.piece.isWhite && dropSquare.rank == 1)) {
									move += "=Q"; // TODO underpromotion
								}
							}
							// Castles?
							if (dragSquare.gameSquare.piece.type == de.toto.game.Rules.PieceType.KING
									&& dragSquare.file == 5) {
								if (dropSquare.file == 3) {
									move = "0-0-0";
								} else if (dropSquare.file == 7) {
									move = "0-0";
								}
							}
							board.fireUserMoved(move.trim());
						}
					}
				} finally {
					isDragging = false;
					cursorLocation = null;
					if (dragSquare != null) {
						dragSquare.isDragSource = false;
						dragSquare = null;
					}
					if (dragTarget != null) {
						dragTarget.isDragTarget = false;
						dragTarget = null;
					}
					repaint();
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
					Square clickSquare = getSquareAt(e.getPoint());
					if (clickSquare != null) {
						board.fireUserClickedSquare(clickSquare.getName());
					}
				}
			}

		};

		private void showPopup(MouseEvent e) {
			JPopupMenu popup = getComponentPopupMenu();
			if (popup != null) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void flip() {
			setOrientationWhite(!isOrientationWhite);
		}

		public boolean isOrientationWhite() {
			return isOrientationWhite;
		}

		public void setOrientationWhite(boolean value) {
			if (value != isOrientationWhite) {
				isOrientationWhite = value;
				if (getSize().height > 0) {
					rescale();
				}
			}
		}
		
		public void rescale() {
			rescaleAll();
			repaint();
		}

		public BoardCanvas(Board board) {
			this.board = board;
			loadImages();
			initSquares();
			addMouseMotionListener(mouseAdapter);
			addMouseListener(mouseAdapter);
			setInheritsPopupMenu(true);
		}

		private void loadImages() {
			try {
				// maple.jpg wood-1024.jpg metal-1024.jpg
//				boardImage = ImageIO.read(Board.class.getResource("/images/board/maple.jpg"));
				SVGUniverse svgUniverse = new SVGUniverse();
				String folder = "merida"; // "cburnett", "merida", "pirouetti";
				wK = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wK.svg"));
				wQ = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wQ.svg"));
				wR = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wR.svg"));
				wB = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wB.svg"));
				wN = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wN.svg"));
				wP = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wP.svg"));
				bK = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bK.svg"));
				bQ = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bQ.svg"));
				bR = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bR.svg"));
				bB = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bB.svg"));
				bN = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bN.svg"));
				bP = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bP.svg"));
								
				wQs = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wQ.svg"));
				wRs = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wR.svg"));
				wBs = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wB.svg"));
				wNs = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wN.svg"));
				wPs = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/wP.svg"));
				bQs = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bQ.svg"));
				bRs = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bR.svg"));
				bBs = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bB.svg"));
				bNs = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bN.svg"));
				bPs = loadIcon(svgUniverse, Board.class.getResource("/images/pieces/" + folder + "/bP.svg"));

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private SVGIcon loadIcon(SVGUniverse svgUniverse, URL url) {
			SVGIcon result = new SVGIcon();
			result.setSvgURI(svgUniverse.loadSVG(url));
			result.setScaleToFit(true);
			result.setAntiAlias(true);
			return result;
		}

		private Square getSquareAt(Point p) {
			Square result = null;			
			int squareSize = getSquareSize();
			int borderSize = getBorderSize();
			if (p.x < borderSize || p.x > getSize().height-borderSize) return null;
			if (p.y < borderSize || p.y > getSize().height-borderSize) return null;
			int rank = 0;
			int file = 0;
			if (isOrientationWhite) {
				rank = 8 - (p.y - borderSize) / squareSize; 
				file = p.x >= 0 ? (p.x - borderSize) / squareSize + 1 : 0;
			} else {
				rank = (p.y - borderSize) / squareSize + 1;
				file = p.x >= 0 ? 8 - (p.x - borderSize) / squareSize : 0;
			}
			if (rank > 0 && rank <= 8 && file > 0 && file <= 8) {
				result = getSquare(rank, file);
			}
			return result;
		}

		private void initSquares() {
			for (int rank = 1; rank <= 8; rank++) {
				for (int file = 1; file <= 8; file++) {
					squares[rank - 1][file - 1] = new Square(rank, file);
				}
			}
		}

		private Square getSquare(int rank, int file) {
			return squares[rank - 1][file - 1];
		}

		// e.g. "a1"
		private Square getSquare(String squarename) {
			int file = squarename.charAt(0) - 96;
			int rank = Character.getNumericValue(squarename.charAt(1));
			return getSquare(rank, file);
		}

		private Image scaleImage(Image source, int size) {
			BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = result.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(source, 0, 0, size, size, null);
			g.dispose();
			return result;
		}

		private void rescaleAll() {			
			int borderSize = getBorderSize();
			int squareSize = getSquareSize();			
			int boardSize = getSize().height - borderSize*2;
			for (int rank = 1; rank <= 8; rank++) {
				for (int file = 1; file <= 8; file++) {
					Square square = getSquare(rank, file);
					int x = 0, y = 0;
					if (isOrientationWhite) {
						x = (file - 1) * squareSize + borderSize;
						y = boardSize - ((rank - 1) * squareSize) - squareSize + borderSize;
					} else {
						x = boardSize - ((file - 1) * squareSize) - squareSize + borderSize;
						y = (rank - 1) * squareSize + borderSize;
					}
					square.topLeftOnBoard = new Point(x, y);
				}
			}
			if (boardImage != null) {
				boardImageScaled = scaleImage(boardImage, squareSize * 8);
			}
			wK.setPreferredSize(new Dimension(squareSize, squareSize));
			wQ.setPreferredSize(new Dimension(squareSize, squareSize));
			wR.setPreferredSize(new Dimension(squareSize, squareSize));
			wB.setPreferredSize(new Dimension(squareSize, squareSize));
			wN.setPreferredSize(new Dimension(squareSize, squareSize));
			wP.setPreferredSize(new Dimension(squareSize, squareSize));
			bK.setPreferredSize(new Dimension(squareSize, squareSize));
			bQ.setPreferredSize(new Dimension(squareSize, squareSize));
			bR.setPreferredSize(new Dimension(squareSize, squareSize));
			bB.setPreferredSize(new Dimension(squareSize, squareSize));
			bN.setPreferredSize(new Dimension(squareSize, squareSize));
			bP.setPreferredSize(new Dimension(squareSize, squareSize));
			
			wQs.setPreferredSize(new Dimension(borderSize, borderSize));
			wRs.setPreferredSize(new Dimension(borderSize, borderSize));
			wBs.setPreferredSize(new Dimension(borderSize, borderSize));
			wNs.setPreferredSize(new Dimension(borderSize, borderSize));
			wPs.setPreferredSize(new Dimension(borderSize, borderSize));
			bQs.setPreferredSize(new Dimension(borderSize, borderSize));
			bRs.setPreferredSize(new Dimension(borderSize, borderSize));
			bBs.setPreferredSize(new Dimension(borderSize, borderSize));
			bNs.setPreferredSize(new Dimension(borderSize, borderSize));
			bPs.setPreferredSize(new Dimension(borderSize, borderSize));
			
			scaleSize = squareSize;
		}

		private SVGIcon getIconFor(Piece p) {
			SVGIcon result = null;
			switch (p) {
			case WHITE_KING:
				return wK;
			case WHITE_QUEEN:
				return wQ;
			case WHITE_ROOK:
				return wR;
			case WHITE_BISHOP:
				return wB;
			case WHITE_KNIGHT:
				return wN;
			case WHITE_PAWN:
				return wP;
			case BLACK_KING:
				return bK;
			case BLACK_QUEEN:
				return bQ;
			case BLACK_ROOK:
				return bR;
			case BLACK_BISHOP:
				return bB;
			case BLACK_KNIGHT:
				return bN;
			case BLACK_PAWN:
				return bP;
			}
			return result;
		}
		
		private SVGIcon getSmallIconFor(Piece p) {
			SVGIcon result = null;
			switch (p) {
			case WHITE_KING:
				return wK;
			case WHITE_QUEEN:
				return wQs;
			case WHITE_ROOK:
				return wRs;
			case WHITE_BISHOP:
				return wBs;
			case WHITE_KNIGHT:
				return wNs;
			case WHITE_PAWN:
				return wPs;
			case BLACK_KING:
				return bK;
			case BLACK_QUEEN:
				return bQs;
			case BLACK_ROOK:
				return bRs;
			case BLACK_BISHOP:
				return bBs;
			case BLACK_KNIGHT:
				return bNs;
			case BLACK_PAWN:
				return bPs;
			}
			return result;
		}

		private int getBorderSize() {			
			return board.showMaterialImbalance ? getSize().height / 24 : 0;
		}
		private int getSquareSize() {
			return (getSize().height - getBorderSize()*2) / 8;
		}

		@Override
		public void paint(Graphics g) {
			if (!board.showBoard)
				return;

			if (scaleSize != getSquareSize()) {
				rescaleAll();
			}
			int squareSize = getSquareSize();

			Graphics2D g2 = (Graphics2D) g;
			
//			g2.setColor(Color.YELLOW);
//			g2.fillRect(0, 0, getSize().width, getSize().height);
			
			if (boardImageScaled != null) {
				g2.drawImage(boardImageScaled, getBorderSize(), getBorderSize(), null);			
			}
			
			Position position = board.getCurrentPosition();

			// draw square background if no boardImage is loaded
			if (boardImageScaled == null) {
				Font font = new Font("Dialog", Font.PLAIN, squareSize / 7);
				float fontHeight = g2.getFontMetrics().getAscent();
				g2.setFont(font);
				int span = squareSize / 25;
				for (int rank = 1; rank <= 8; rank++) {
					for (int file = 1; file <= 8; file++) {
						Square square = getSquare(rank, file);
						// draw square background if no boardImage is loaded
						if (boardImageScaled == null) {
							g2.setColor(square.isWhite ? squareColorWhite : squareColorBlack);
							g2.fillRect(square.topLeftOnBoard.x, square.topLeftOnBoard.y, squareSize, squareSize);
							// draw square coordinates?
							if (board.showCoordinates && ((isOrientationWhite && (square.rank == 1 || square.file == 1))
									|| (!isOrientationWhite && (square.rank == 8 || square.file == 8)))) {
								g2.setColor(square.isWhite ? squareColorBlack : squareColorWhite);//
								g2.drawString(square.getName(), square.topLeftOnBoard.x + span,
										square.topLeftOnBoard.y + fontHeight + span);
							}
						}
					}
				}
			}

			// draw last move highlight
			String[] squareNames = position.getMoveSquareNames();
			if (squareNames != null) {
				colorSquare(g2, getSquare(squareNames[0]), squareSelectionColor, squareSize);
				colorSquare(g2, getSquare(squareNames[1]), squareSelectionColor, squareSize);
			}

			java.util.List<Position.GraphicsComment> graphicsComments = position.getGraphicsComments();
			// draw square highlights
			if (!isDragging && board.showGraphicsComments) {
				for (Position.GraphicsComment gc : graphicsComments) {
					if (gc.secondSquare == null) {
						Color c = highlightColorGreen;
						if (gc.color == Color.RED)
							c = highlightColorRed;
						else if (gc.color == Color.YELLOW)
							c = highlightColorYellow;
						colorSquare(g2, getSquare(gc.firstSquare.rank, gc.firstSquare.file), c, squareSize);
					}
				}
			}

			// draw pieces
			if (board.showPieces) {
				for (int rank = 1; rank <= 8; rank++) {
					for (int file = 1; file <= 8; file++) {
						Square square = getSquare(rank, file);
						if (square.gameSquare.piece != null && !square.isDragSource) {
							getIconFor(square.gameSquare.piece).paintIcon(this, g2, square.topLeftOnBoard.x,
									square.topLeftOnBoard.y);

						}
					}
				}
			}

			// Drag&Drop decoration
			if (isDragging) {
				for (int rank = 1; rank <= 8; rank++) {
					for (int file = 1; file <= 8; file++) {
						Square square = getSquare(rank, file);
						if (square.isDragSource || square.isDragTarget) {
							colorSquare(g2, square, squareSelectionColor, squareSize);
						}
					}
				}
				if (board.showPieces && dragSquare.gameSquare.piece != null) {
					getIconFor(dragSquare.gameSquare.piece).paintIcon(this, g2, cursorLocation.x - squareSize / 2,
							cursorLocation.y - squareSize / 2);
				}
			}

			// draw arrows
			if (!isDragging && board.showGraphicsComments) {
				for (Position.GraphicsComment gc : graphicsComments) {
					if (gc.secondSquare != null) {
						drawArrow(g2, getSquare(gc.firstSquare.rank, gc.firstSquare.file),
								getSquare(gc.secondSquare.rank, gc.secondSquare.file), gc.color, squareSize);
					}
				}
			}

			// draw additionalGraphicsComment
			if (board.showGraphicsComments) {
				for (Position.GraphicsComment gc : board.additionalGraphicsComment) {
					if (gc.secondSquare != null) {
						drawArrow(g2, getSquare(gc.firstSquare.rank, gc.firstSquare.file),
								getSquare(gc.secondSquare.rank, gc.secondSquare.file), gc.color, squareSize);
					}
				}
			}
			
			// draw material imbalance
			if (board.showMaterialImbalance) {
				java.util.List<Piece> imbalancePieces = position.getMaterialImbalance(!isOrientationWhite); 
				int count = imbalancePieces.size();
				for (int i = 0, x = getSize().width - getBorderSize(), y = getSize().height / 2 - getBorderSize() * count / 2;
						i < count; i++, y += getBorderSize()) {
					getSmallIconFor(imbalancePieces.get(i)).paintIcon(this, g2, x,  y);
				}				
			}
			
		}

		private void colorSquare(Graphics2D g2, Square s, Color color, int squareSize) {
			g2.setColor(color);
			g2.fillRect(s.topLeftOnBoard.x, s.topLeftOnBoard.y, squareSize, squareSize);
		}

		private void drawArrow(Graphics2D g2, Square from, Square to, Color color, int squareSize) {
			int x1 = from.topLeftOnBoard.x + squareSize / 2;
			int y1 = from.topLeftOnBoard.y + squareSize / 2;
			;
			int x2 = to.topLeftOnBoard.x + squareSize / 2;
			int y2 = to.topLeftOnBoard.y + squareSize / 2;

			Color gradientFrom = arrowColorBlack20Percent;
			Color gradientTo = arrowColorBlack50Percent;
			if (color.equals(Color.GREEN)) {
				gradientFrom = arrowColorGreen20Percent;
				gradientTo = arrowColorGreen50Percent;
			} else if (color.equals(Color.YELLOW)) {
				gradientFrom = arrowColorYellow20Percent;
				gradientTo = arrowColorYellow50Percent;
			} else if (color.equals(Color.RED)) {
				gradientFrom = arrowColorRed20Percent;
				gradientTo = arrowColorRed50Percent;
			}
			g2.setPaint(new GradientPaint(x1, y1, gradientFrom, x2, y2, gradientTo));
			g2.fill(createArrowShape(new Point(x1, y1), new Point(x2, y2), squareSize));
		}

		public static Shape createArrowShape(Point fromPt, Point toPt, double squareSize) {
			double ptDistance = fromPt.distance(toPt);
			Point midPoint = new Point((int) ((fromPt.x + toPt.x) / 2.0), (int) ((fromPt.y + toPt.y) / 2.0));
			double rotate = Math.atan2(toPt.y - fromPt.y, toPt.x - fromPt.x);
			double arrowHeight = squareSize / 10;
			double arrowheadSide = squareSize / 3;
			double arrowheadLength = arrowheadSide; // TODO

			Path2D.Double path = new Path2D.Double();
			path.moveTo(-ptDistance / 2, arrowHeight / 2);
			path.lineTo(ptDistance / 2 - arrowheadLength, arrowHeight / 2);
			path.lineTo(ptDistance / 2 - arrowheadLength, arrowheadSide / 2);
			path.lineTo(ptDistance / 2, 0);
			path.lineTo(ptDistance / 2 - arrowheadLength, -(arrowheadSide / 2));
			path.lineTo(ptDistance / 2 - arrowheadLength, -(arrowHeight / 2));
			path.lineTo(-ptDistance / 2, -(arrowHeight / 2));

			AffineTransform transform = new AffineTransform();
			transform.translate(midPoint.x, midPoint.y);
			transform.rotate(rotate);

			return transform.createTransformedShape(path);
		}

	}
}