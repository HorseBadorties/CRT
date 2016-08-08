package de.toto.gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

import de.toto.game.Position;
import de.toto.game.Rules.Piece;
import de.toto.sound.Sounds;

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
		invalidate();
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
			Point topLeftOnBoard;
			boolean isDragSource = false;
			boolean isDragTarget = false;

			public Square(int rank, int file) {
				this.rank = rank;
				this.file = file;
			}

			// e.g. "a1"
			String getName() {
				Character cFile = Character.valueOf((char)(file+96));
				return cFile.toString() + rank;
			}
		}

		private Image boardImage, boardImageScaled;
		// unscaled
		private Image wK, wQ, wR, wB, wN, wP, bK, bQ, bR, bB, bN, bP; 
		// scaled
		private Image wKs, wQs, wRs, wBs, wNs, wPs, bKs, bQs, bRs, bBs, bNs, bPs; 
		private int scaleSize;
		
		private Color squareSelectionColor = new Color(.3f, .4f, .5f, .6f); //new Color(200, 255, 200);
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
					if (dropSquare.gameSquare.piece != null) {
						Sounds.capture();
						move += "x";
					} else {
						Sounds.move();
						move += "-";
					}
					move += dropSquare.getName();
					//Castles?
					if (dragSquare.gameSquare.piece.type == de.toto.game.Rules.PieceType.KING
							&& dragSquare.file == 5) 
					{
						if (dropSquare.file == 3) {
							move = "0-0-0";
						} else if (dropSquare.file == 7) {
							move = "0-0";
						}
					}
					board.fireUserMoved(move.trim());
//					dropSquare.gameSquare.piece = dragSquare.gameSquare.piece;
//					dragSquare.gameSquare.piece = null;
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
				boardImage = ImageIO.read(Board.class
						.getResource("/images/board/wood-1024.jpg"));
				wK = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_klt60.png"));
				wQ = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_qlt60.png"));
				wR = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_rlt60.png"));
				wB = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_blt60.png"));
				wN = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_nlt60.png"));
				wP = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_plt60.png"));
				bK = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_kdt60.png"));
				bQ = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_qdt60.png"));
				bR = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_rdt60.png"));
				bB = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_bdt60.png"));
				bN = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_ndt60.png"));
				bP = ImageIO.read(Board.class
						.getResource("/images/pieces/png/Chess_pdt60.png"));

			} catch (IOException e) {
				e.printStackTrace();
			}
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
			wKs = scaleImage(wK, squareSize);
			wQs = scaleImage(wQ, squareSize);
			wRs = scaleImage(wR, squareSize);
			wBs = scaleImage(wB, squareSize);
			wNs = scaleImage(wN, squareSize);
			wPs = scaleImage(wP, squareSize);
			bKs = scaleImage(bK, squareSize);
			bQs = scaleImage(bQ, squareSize);
			bRs = scaleImage(bR, squareSize);
			bBs = scaleImage(bB, squareSize);
			bNs = scaleImage(bN, squareSize);
			bPs = scaleImage(bP, squareSize);
			scaleSize = squareSize;
		}

		private Image getScaledPiece(Piece p) {
			Image result = null;
			switch (p) {
			case WHITE_KING:
				return wKs;
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
				return bKs;
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

			for (int rank = 1; rank <= 8; rank++) {
				for (int file = 1; file <= 8; file++) {
					Square square = getSquare(rank, file);
					// draw square background if no boardImage is loaded
					if (boardImageScaled == null) {
						g2.setColor(square.gameSquare.isWhite ? squareColorWhite : squareColorBlack);
						g2.fillRect(square.topLeftOnBoard.x, square.topLeftOnBoard.y, squareSize, squareSize);
					}					
					
					if (square.gameSquare.piece != null && !square.isDragSource) {
						g2.drawImage(getScaledPiece(square.gameSquare.piece),
								square.topLeftOnBoard.x,
								square.topLeftOnBoard.y, null);
					}
					
					// draw square coordinates?					
//					g2.setColor(Color.BLACK);
//					g2.drawString(square.getName(), square.topLeftOnBoard.x + 3, square.topLeftOnBoard.y+13);
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
				g2.drawImage(getScaledPiece(dragSquare.gameSquare.piece), cursorLocation.x
						- squareSize / 2, cursorLocation.y - squareSize / 2, null);
			}

		}
		
	}
}