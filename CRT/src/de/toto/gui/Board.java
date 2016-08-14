package de.toto.gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;

import de.toto.game.Position;
import de.toto.game.Rules.Piece;
import de.toto.game.Rules.PieceType;
import de.toto.sound.Sounds;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

@SuppressWarnings("serial")
public class Board extends JPanel {
	
	private Position currentPosition = new Position();
	private BoardCanvas boardCanvas = new BoardCanvas(this);
	
	public Position getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Position currentPosition) {
		this.currentPosition = currentPosition;
		boardCanvas.positionChanged();
		repaint();		
	}
	
	public void flip() {
		boardCanvas.flip();
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
		
		private Color squareSelectionColor = new Color(.3f, .4f, .5f, .6f); //new Color(200, 255, 200);
		private Color squareHighlightColor = new Color(200, 255, 200);
		private Color squareColorWhite = Color.LIGHT_GRAY;
		private Color squareColorBlack = Color.GRAY;

		
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
			public void mouseReleased(MouseEvent e) {
				if (!isDragging)
					return;
				Square dropSquare = getSquareAt(e.getPoint());
				if (dropSquare != null && dropSquare != dragSquare) {
					String move = dragSquare.gameSquare.piece.pgnChar + dragSquare.getName();
					boolean isCapture = dropSquare.gameSquare.piece != null;
					// consider En Passant for pawn moves...
					if (dragSquare.gameSquare.piece.type == PieceType.PAWN) {
						isCapture = dragSquare.file != dropSquare.file;
					}
					if (isCapture) {
						Sounds.capture();
						move += "x";
					} else {
						Sounds.move();
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
				if (e.getClickCount() == 3) {
					flip();
				}
			}

		};
		
		public void flip() {
			setOrientationWhite(!isOrientationWhite);
		}
		
		public boolean isOrientationWhite() {
			return isOrientationWhite;
		}
		
		public void setOrientationWhite(boolean value) {
			if (value != isOrientationWhite) {
				isOrientationWhite = value;
				rescaleAll();
				repaint();
			} 
		}

		public BoardCanvas(Board board) {
			this.board = board;
			loadImages();
			initSquares();
			addMouseMotionListener(mouseAdapter);
			addMouseListener(mouseAdapter);
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
		}

		private SVGIcon getScaledPiece(Piece p) {
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
			String[] squareNames = board.getCurrentPosition().getMoveSquareNames();
			if (squareNames != null) {
				g2.setColor(squareSelectionColor);
				Square s = getSquare(squareNames[0]);
				g2.fillRect(s.topLeftOnBoard.x, s.topLeftOnBoard.y, squareSize, squareSize);
				s = getSquare(squareNames[1]);
				g2.fillRect(s.topLeftOnBoard.x, s.topLeftOnBoard.y, squareSize, squareSize);
				
			}
						
			for (int rank = 1; rank <= 8; rank++) {
				for (int file = 1; file <= 8; file++) {
					Square square = getSquare(rank, file);
					if (square.gameSquare.piece != null && !square.isDragSource) {
						getScaledPiece(square.gameSquare.piece).paintIcon(this, g2, 
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
							g2.setColor(squareSelectionColor);
							g2.fillRect(square.topLeftOnBoard.x, square.topLeftOnBoard.y, squareSize, squareSize);
						}
					}
				}
				getScaledPiece(dragSquare.gameSquare.piece).paintIcon(this, g2, 
						cursorLocation.x - squareSize / 2, cursorLocation.y - squareSize / 2);
			}
			
			//Line from c3 to e5;
//			int x1 = squareSize*2 + squareSize/3*2;
//			int y1 = squareSize*5 + squareSize/3;
//			int x2 = squareSize*4 + squareSize/3;
//			int y2 = squareSize*3 + squareSize/3*2;
//			g2.setPaint(new GradientPaint(x1,y1,Color.ORANGE,x2, y2,Color.RED));
//			g2.setStroke(new BasicStroke(squareSize/20));
//			g2.drawLine(x1, y1, x2, y2);

		}
		
	}
}