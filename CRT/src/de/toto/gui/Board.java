package de.toto.gui;

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
	
	private Position currentPosition = new Position();
	private BoardCanvas boardCanvas = new BoardCanvas(this);
	private boolean showGraphicsComments = true;
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
			boolean isWhite; //cache value rather than use de.toto.game.Square.isWhite() over and over again during paint()
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
				Character cFile = Character.valueOf((char)(file+96));
				return cFile.toString() + rank;
			}
		}

		private Image boardImage, boardImageScaled;
		private SVGIcon wK, wQ, wR, wB, wN, wP, bK, bQ, bR, bB, bN, bP; 
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
		
		
		private static final Color lightBlue = new Color(230, 245, 250);
		private static final Color darkBlue = new Color(150, 190, 200);
		private static final Color lightGreen = new Color(240, 250, 240);
		private static final Color darkGreen = new Color(113, 170, 85);
		private static final Color lightGray = new Color(240, 240, 240);
		private static final Color darkGray = Color.GRAY;		
		
		private static final Color squareColorWhite = lightGreen; 
		private static final Color squareColorBlack = darkGreen;
		
		private Font fontPositionEval = new Font("Frutiger Standard", Font.PLAIN, 200); 
		private static final Color colorPositionEval = new Color(1f, .0f, .0f, .6f); ;

		
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
					squares[rank-1][file-1].gameSquare = board.currentPosition.getSquare(rank, file);
				}
			}
		}

		private MouseAdapter mouseAdapter = new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (!isDragging) {
					dragSquare = getSquareAt(e.getPoint());					
					if (dragSquare.gameSquare.piece == null) return;
					dragSquare.isDragSource = true;
				}
				isDragging = true;
				cursorLocation = e.getPoint();
				Square newDragTarget = getSquareAt(e.getPoint());
				if (dragTarget != newDragTarget) {
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
				if (!isDragging)
					return;
				Square dropSquare = getSquareAt(e.getPoint());
				if (dropSquare != null && dropSquare != dragSquare) {
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
						// Castles?
						if (dragSquare.gameSquare.piece.type == de.toto.game.Rules.PieceType.KING
								&& dragSquare.file == 5) 
						{
							if (dropSquare.file == 3) {
								move = "0-0-0";
							} else if (dropSquare.file == 7) {
								move = "0-0";
							}
						}
						// TODO check, mate, promotion
						board.fireUserMoved(move.trim());
					}
				}
				isDragging = false;
				cursorLocation = null;
				dragSquare.isDragSource = false;
				dragSquare = null;
				if (dragTarget != null) {
					dragTarget.isDragTarget = false;
				}
				dragTarget = null;
				repaint();
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
					rescaleAll();
					repaint();
				}
			} 
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
				//maple.jpg wood-1024.jpg metal-1024.jpg
				boardImage = ImageIO.read(Board.class.getResource("/images/board/maple.jpg"));
				SVGUniverse svgUniverse = new SVGUniverse();
				String folder = "merida"; //"merida";
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
			int rank = 0;			
			int file = 0;
			if (isOrientationWhite) {
				rank = 8 - p.y / squareSize;			
				file = p.x >= 0 ? p.x / squareSize + 1 : 0;
			} else {
				rank = p.y / squareSize + 1;			
				file = p.x >= 0 ? 8 - p.x / squareSize : 0;
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
			BufferedImage result = new BufferedImage(size, size,
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = result.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(source, 0, 0, size, size, null);
			g.dispose();
			return result;
		}

		private void rescaleAll() {
			int squareSize = getSquareSize();
			for (int rank = 1; rank <= 8; rank++) {
				for (int file = 1; file <= 8; file++) {
					Square square = getSquare(rank, file);
					int x = 0, y = 0;
					if (isOrientationWhite) {
						x = (file - 1) * squareSize;
						y = getSize().height - ((rank - 1) * squareSize) - squareSize;
					} else {
						x = getSize().height - ((file - 1) * squareSize) - squareSize;
						y = (rank - 1) * squareSize;								
					}
					square.topLeftOnBoard = new Point(x, y);
				}
			}
			if (boardImage != null) {
				boardImageScaled = scaleImage(boardImage, getSize().height);
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
			scaleSize = squareSize;
			fontPositionEval = new Font("Frutiger Standard", Font.PLAIN, squareSize*4); 
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

		private int getSquareSize() {
			return getSize().height / 8;
		}

		@Override
		public void paint(Graphics g) {
			if (scaleSize != getSquareSize()) {
				rescaleAll();
			}

			Graphics2D g2 = (Graphics2D) g;
			if (boardImageScaled != null) {
				g2.drawImage(boardImageScaled, 0, 0, null);
			}

			int squareSize = getSquareSize();
			Position position = board.getCurrentPosition();
			
			// draw square background if no boardImage is loaded
			if (boardImageScaled == null) {
				for (int rank = 1; rank <= 8; rank++) {
					for (int file = 1; file <= 8; file++) {
						Square square = getSquare(rank, file);
						// draw square background if no boardImage is loaded
						if (boardImageScaled == null) {
							g2.setColor(square.isWhite ? squareColorWhite : squareColorBlack);
							g2.fillRect(square.topLeftOnBoard.x, square.topLeftOnBoard.y, squareSize, squareSize);
						}	
						// draw square coordinates?					
//						g2.setColor(Color.BLACK);
//						g2.drawString(square.getName(), square.topLeftOnBoard.x + 3, square.topLeftOnBoard.y+13);
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
			//draw square highlights
			if (!isDragging && board.showGraphicsComments) {				
				for (Position.GraphicsComment gc : graphicsComments) {
					if (gc.secondSquare == null) {
						Color c = highlightColorGreen;
						if (gc.color == Color.RED) c = highlightColorRed; 
						else if  (gc.color == Color.YELLOW) c = highlightColorYellow;  
						colorSquare(g2, getSquare(gc.firstSquare.rank, gc.firstSquare.file), c, squareSize);
					}
				}
			}
						
			for (int rank = 1; rank <= 8; rank++) {
				for (int file = 1; file <= 8; file++) {
					Square square = getSquare(rank, file);
					if (square.gameSquare.piece != null && !square.isDragSource) {
						getIconFor(square.gameSquare.piece).paintIcon(this, g2, 
								square.topLeftOnBoard.x, square.topLeftOnBoard.y);
						
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
				if (dragSquare.gameSquare.piece != null) {
					getIconFor(dragSquare.gameSquare.piece).paintIcon(this, g2, 
							cursorLocation.x - squareSize / 2, cursorLocation.y - squareSize / 2);
				}
			} 
			
			//draw arrows
			if (!isDragging && board.showGraphicsComments) {				
				for (Position.GraphicsComment gc : graphicsComments) {
					if (gc.secondSquare != null) {						
						drawArrow(g2, getSquare(gc.firstSquare.rank, gc.firstSquare.file),
								getSquare(gc.secondSquare.rank, gc.secondSquare.file), gc.color, squareSize);
					}
				}
			}
			
			//draw additionalGraphicsComment
			if (board.showGraphicsComments) {				
				for (Position.GraphicsComment gc : board.additionalGraphicsComment) {
					if (gc.secondSquare != null) {						
						drawArrow(g2, getSquare(gc.firstSquare.rank, gc.firstSquare.file),
								getSquare(gc.secondSquare.rank, gc.secondSquare.file), gc.color, squareSize);
					}
				}
			}
			
			/*
			//position eval						 
			if (positionEval != null && positionEval.length() > 0) {				
				g2.setColor(colorPositionEval);
				g2.setFont(fontPositionEval);				
				FontMetrics metrics = g2.getFontMetrics();
				int x = (getWidth() / 2) - (metrics.stringWidth(positionEval) / 2);
				int y = (getHeight() / 2) + (metrics.getHeight() / 4);
				g2.drawString(positionEval, x, y);
			}
			//*/
		}
		
		private void colorSquare(Graphics2D g2, Square s, Color color, int squareSize) {			
			g2.setColor(color);
			g2.fillRect(s.topLeftOnBoard.x, s.topLeftOnBoard.y, squareSize, squareSize);
		}
		
		private void drawArrow(Graphics2D g2, Square from, Square to, Color color, int squareSize) {			
			int x1 = from.topLeftOnBoard.x + squareSize/2;
			int y1 = from.topLeftOnBoard.y + squareSize/2;;
			int x2 = to.topLeftOnBoard.x + squareSize/2;
			int y2 = to.topLeftOnBoard.y + squareSize/2;
			
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
			g2.setPaint(new GradientPaint(x1, y1 ,gradientFrom,x2, y2, gradientTo));
			g2.fill(createArrowShape(new Point(x1,y1), new Point(x2,y2), squareSize));
		}
		
		public static Shape createArrowShape(Point fromPt, Point toPt, double squareSize) {
			double ptDistance = fromPt.distance(toPt);
			Point midPoint = new Point((int)((fromPt.x + toPt.x)/2.0), 
                    (int)((fromPt.y + toPt.y)/2.0));
			double rotate = Math.atan2(toPt.y - fromPt.y, toPt.x - fromPt.x);
			double arrowHeight = squareSize / 10;
			double arrowheadSide = squareSize / 2;
			double arrowheadLength = arrowheadSide; //TODO
			
			Path2D.Double path = new Path2D.Double();
			path.moveTo(-ptDistance/2, arrowHeight / 2);
			path.lineTo(ptDistance/2 - arrowheadLength, arrowHeight / 2);
			path.lineTo(ptDistance/2 - arrowheadLength, arrowheadSide / 2);
			path.lineTo(ptDistance/2, 0);
			path.lineTo(ptDistance/2 - arrowheadLength, -(arrowheadSide / 2));
			path.lineTo(ptDistance/2 - arrowheadLength, -(arrowHeight / 2));
			path.lineTo(-ptDistance/2, -(arrowHeight / 2));

		    AffineTransform transform = new AffineTransform();
		    transform.translate(midPoint.x, midPoint.y);
		    transform.rotate(rotate);

		    return transform.createTransformedShape(path);
		}

		
	}
}