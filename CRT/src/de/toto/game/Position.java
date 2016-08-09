package de.toto.game;

import java.util.ArrayList;
import java.util.List;

import de.toto.game.Rules.Piece;

public class Position {
	
	private Square[][] squares;
	private boolean whiteToMove = true;
	private Position previous = null;
	private List<Position> next = new ArrayList<Position>(); 
	private int variationLevel = 0; //0 = main line, 1 = variation, 2 = variation of variation ...
	private String fen = null;
	private String move = null;
	private String comment = null; // may contain graphics token such as [%csl Ge5][%cal Ge5b2]
	private List<String> nags = new ArrayList<String>(); // !, ?, ?? ...
		
	// Startposition
	public Position() {		
		setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		whiteToMove = true;
	}

	public Position(Position previous, String move) {
		this(previous, move, null);
	}
	
	public Position(Position previous, String move, String fen) {
		this(previous, move, fen, false);
	}
	
	public Position(Position previous, String move, String fen, boolean asVariation) {
		this.previous = previous;
		previous.addNextPosition(this, asVariation);
		this.whiteToMove = !previous.whiteToMove;
		this.move = move;
		this.variationLevel = asVariation ? previous.variationLevel+1 : previous.variationLevel;
		if (fen != null) {
			setFen(fen);
		} else if (move != null) {
			setMove(move);
		}
	}	
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getVariationLevel() {
		return variationLevel;
	}

	public Position getPrevious() {
		return previous;
	}
	
	public boolean hasPrevious() {
		return previous == null;
	}

	public Position getNext() {
		return next.get(0);
	}
	
	public boolean hasNext() {
		return !next.isEmpty();
	}
	
	public int getVariationCount() {
		return next.isEmpty() ? 0 : next.size() - 1;
	}
	
	public boolean hasVariations() {
		return getVariationCount() > 0;
	}
	
	public String getFen() {
		return fen;
	}

	public String getMove() {
		return move;
	}

	public boolean isWhiteToMove() {
		return whiteToMove;
	}

	public Square[][] getSquares() {
		return squares;
	}

	public boolean wasCapture() {
		return move != null && move.contains("x");
	}
	
	public boolean wasCastling() {
		return move != null && move.startsWith("0-0");
	}
	
	public boolean wasPromotion() {
		return move != null && move.contains("=");
	}
	
	private Piece getPromotionPiece() {
		if (wasPromotion()) {
			boolean checkOrMate = isCheck() || isMate();
			int promotionPiecePosition = checkOrMate ? move.length() - 2 : move.length() - 1;
			String promotionPiece = move.substring(promotionPiecePosition, move.length());
			if (whiteToMove) promotionPiece.toLowerCase();
			return Piece.getByFenChar(promotionPiece.charAt(0));			
		}		
		return null;
	}
	
	public boolean isCheck() {
		return move != null && move.endsWith("+");
	}
	
	public boolean isMate() {
		return move != null && move.endsWith("#");
	}
	
	public int getMoveNumber() {
		int moveCount = 0;
		Position halfMove = this;
		do  {
			if (!halfMove.whiteToMove) moveCount++;
			halfMove = halfMove.previous;
		} while (halfMove != null);
		return moveCount;
		
	}
		
	private void addNextPosition(Position nextPosition, boolean asVariation) {
		next.add(asVariation ? next.size() : 0, nextPosition);			
	}
		
	private void initSquares() {
		squares = new Square[8][8];
		boolean isWhite;
		for (int rank = 1; rank <= 8; rank++) {
			isWhite = rank % 2 == 0 ? true : false;
			for (int file = 1; file <= 8; file++) {
				squares[rank - 1][file - 1] = new Square(rank, file, isWhite);
				isWhite = !isWhite;
			}
		}
	}
	
	public Square getSquare(int rank, int file) {
		return squares[rank - 1][file - 1];
	}

	// e.g. "a1"
	private Square getSquare(String squarename) {
		int file = squarename.charAt(0) - 96;
		int rank = Character.getNumericValue(squarename.charAt(1));
		return getSquare(rank, file);
	}
	
	private void setFen(String fen) {
		this.fen = fen;
		try {
			String[] fenFields = fen.split(" ");
			initSquares();
			int rank = 8;
			int file = 1;			
			for (int i = 0; i < fenFields[0].length(); i++) {
				char fenChar = fen.charAt(i);			
				if ('/' == fenChar) {
					rank--;
					file = 1;
					continue;
				}
				int numericValue = Character.getNumericValue(fenChar);
				if (numericValue > 0 && numericValue <= 8) {
					file += numericValue;
					continue;
				}
				Piece piece = Piece.getByFenChar(fenChar);
				if (piece != null) {
					getSquare(rank, file).piece = piece;
					file++;
				} else {
					throw new IllegalArgumentException("failed to parse FEN: " + fen);
				}				
			}			
			whiteToMove = "w".equals(fenFields[1]);
			//TODO use other fields...
		} catch (Exception ex) {
			throw new IllegalArgumentException("failed to parse FEN: " + fen, ex);
		}
	}

	private void createFen() {
		StringBuilder fen = new StringBuilder();
		for (int rank = 8; rank >= 1; rank--) {
			int emptySquareCounter = 0;
			for (int file = 1; file <= 8; file++) {
				Square s = getSquare(rank, file);
				if (s.piece != null) {
					if (emptySquareCounter > 0) fen.append(emptySquareCounter);
					emptySquareCounter = 0;
					fen.append(s.piece.fenChar);
				} else emptySquareCounter++;
			}
			if (emptySquareCounter > 0) fen.append(emptySquareCounter);
			if (rank > 1) fen.append("/");	
		}
		fen.append(" ");
		fen.append(whiteToMove ? "w" : "b");
		// TODO Castle field
		fen.append(" -");
		// TODO En passant square field
		fen.append(" -");
		// TODO Halfmove clock field
		fen.append(" 0");
		// TODO Fullmove number field
		fen.append(" 0");
		this.fen = fen.toString();
	}
	
	// move in LAN, e.g. "Ng1-f3"
	private void setMove(String move) {
		initSquares();
		for (int rank = 1; rank <= 8; rank++) {
			for (int file = 1; file <= 8; file++) {
				squares[rank - 1][file - 1].piece = previous.getSquare(rank, file).piece;
			}
		}
		if (move != null) {
			if (wasCastling()) {
				String[] castlingSquareNames = getCastlingSquareNames();				
				getSquare(castlingSquareNames[0]).piece = null;
				getSquare(castlingSquareNames[1]).piece = previous.getSquare(castlingSquareNames[3]).piece;
				getSquare(castlingSquareNames[2]).piece = previous.getSquare(castlingSquareNames[0]).piece;
				getSquare(castlingSquareNames[3]).piece = null;		
			} else {
				String[] m = move.split("x|-");
				String from = m[0];
				if (from.length() > 2) {
					from = from.substring(from.length()-2, from.length());
				}
				String to = m[1];
				Piece piece = getSquare(from).piece;
				getSquare(from).piece = null;
				if (wasPromotion()) {
					getSquare(to).piece = getPromotionPiece();
				} else {
					getSquare(to).piece = piece;
				}
			}
		}
		createFen();
	}
	
	public String[] getMoveSquareNames() {
		String[] result = null;
		if (move != null) {
			if (wasCastling()) {
				String[] castlingSquareNames = getCastlingSquareNames();
				result = new String[2];
				result[0] = castlingSquareNames[0];
				result[1] = castlingSquareNames[2];
			} else {
				result = move.split("x|-");		
				if (result[0].length() > 2) { //e.g. "Ng1"
					result[0] = result[0].substring(result[0].length()-2, result[0].length());
				}
				if (result[1].length() > 2) { //e.g. "d8=Q"
					result[1] = result[1].substring(0, 2);
				}
			}
		}	
		return result;
	}
	
	/*
	 * result[0] = king position before castling
	 * result[1] = rook position after castling
	 * result[2] = king position after castling
	 * result[3] = rook position before castling
	 * 
	 */
	private String[] getCastlingSquareNames() {
		String[] result = new String[4];
		int rank = whiteToMove ? 8 : 1;
		boolean longCastles = move.startsWith("0-0-0");
		result[0] = "e" + rank;
		result[1] = (longCastles ? "d" : "f") + rank;
		result[2] = (longCastles ? "c" : "g") + rank;
		result[3] = (longCastles ? "a" : "h") + rank;		
		return result;
	}
	
	@Override
	public String toString() {
		return move + "\n" + dumpSquares();
	}
	
	private String dumpSquares() {
		StringBuilder result = new StringBuilder();
		for (int rank = 7; rank >= 0; rank--) {
			for (int file = 0; file < 8; file++) {
				String s = squares[rank][file].getNameWithPieceSuffix();
				if (s.length() == 2) {
					result.append(" ");
				}
				result.append(s).append(" ");
				
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	public static void main(String[] args) {
		String fen = "r1bqkb1r/pp1ppp1p/2n2np1/8/1PpPP3/5N2/P1P1BPPP/RNBQK1R1 b Qkq - 1 6";
		Position p = new Position();
		p.setFen(fen);
		p.createFen();
		System.out.println(p.getFen());
	}
	
}
