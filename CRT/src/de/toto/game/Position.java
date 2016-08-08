package de.toto.game;

import java.util.ArrayList;
import java.util.List;

import de.toto.game.Rules.Piece;

public class Position {
	
	int variationLevel = 0; //0 = main line, 1 = variation, 2 = variation of variation ...
	Position previous = null;
	List<Position> next = new ArrayList<Position>(); 
	String fen = null;
	String move = null;
	boolean whiteToMove = true;
	String comment = null; // may contain graphics token such as [%csl Ge5][%cal Ge5b2]
	List<String> nags = new ArrayList<String>(); // !, ?, ?? ...
	
	private Square[][] squares;
	
	
	// Startposition
	public Position() {		
		setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
	}

	public Position(Position previous, String move) {
		this(previous, move, null);
	}
	
	public Position(Position previous, String move, String fen) {
		this.previous = previous;
		previous.addNextPosition(this);
		this.whiteToMove = !previous.whiteToMove;
		this.move = move;
		this.variationLevel = previous.variationLevel;
		if (fen != null) {
			setFen(fen);
		} else if (move != null) {
			setMove(move);
		}
	}
	
	public boolean wasCapture() {
		return move != null && move.contains("x");
	}
	
	public boolean isCheck() {
		return move != null && move.endsWith("+");
	}
	
	public boolean isMate() {
		return move != null && move.endsWith("#");
	}
	
	public boolean isStartPosition() {
		return previous == null;
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
	
	private void addNextPosition(Position nextPosition) {
		next.add(nextPosition);
	}
	
	public String getFEN() {
		return fen;
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
			String[] fenFields = getFenFields(fen);
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
		
	private static String[] getFenFields(String fen) {
		String[] result = fen.split(" ");
		return result;
	}

	public String toFen() {
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
		return fen.toString();
	}
	
	
	private void setMove(String move) {
		initSquares();
		for (int rank = 1; rank <= 8; rank++) {
			for (int file = 1; file <= 8; file++) {
				squares[rank - 1][file - 1].piece = previous.getSquare(rank, file).piece;
			}
		}
		if ("0-0".equals(move)) {
			int rank = whiteToMove ? 8 : 1;
			getSquare(rank, 5).piece = null;
			getSquare(rank, 6).piece = previous.getSquare(rank, 8).piece;
			getSquare(rank, 7).piece = previous.getSquare(rank, 5).piece;
			getSquare(rank, 8).piece = null;
		} else if ("0-0-0".equals(move)) {
			int rank = whiteToMove ? 8 : 1;
			getSquare(rank, 5).piece = null;
			getSquare(rank, 4).piece = previous.getSquare(rank, 1).piece;
			getSquare(rank, 3).piece = previous.getSquare(rank, 5).piece;
			getSquare(rank, 1).piece = null;
		} else {
			String[] m = move.split("x|-");
			String from = m[0];
			if (from.length() > 2) {
				from = from.substring(from.length()-2, from.length());
			}
			String to = m[1];
			Piece piece = getSquare(from).piece;
			getSquare(from).piece = null;
			getSquare(to).piece = piece;
		}
		this.fen = toFen();
	}
	
	@Override
	public String toString() {
		return move + "; " + dumpSquares();
	}
	
	private String dumpSquares() {
		StringBuilder result = new StringBuilder();
		for (int rank = 0; rank < 8; rank++) {
			for (int file = 0; file < 8; file++) {
				result.append(squares[rank][file].getNameWithPieceSuffix()).append(" ");
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	public static void main(String[] args) {
		String fen = "r1bqkb1r/pp1ppp1p/2n2np1/8/1PpPP3/5N2/P1P1BPPP/RNBQK1R1 b Qkq - 1 6";
		Position p = new Position();
		p.setFen(fen);
		System.out.println(p.toFen());
	}
	
}
