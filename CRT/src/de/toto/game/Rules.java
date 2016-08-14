package de.toto.game;

public class Rules {
	
	public enum PieceType {			
		KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
	}
	
	public enum Piece {			
		WHITE_KING(PieceType.KING, true, 'K', 'K'), 
		WHITE_QUEEN(PieceType.QUEEN, true, 'Q', 'Q'), 
		WHITE_ROOK(PieceType.ROOK, true, 'R', 'R'), 
		WHITE_BISHOP(PieceType.BISHOP, true, 'B', 'B'), 
		WHITE_KNIGHT(PieceType.KNIGHT, true, 'N', 'N'), 
		WHITE_PAWN(PieceType.PAWN, true, 'P', ' '), 
		BLACK_KING(PieceType.KING, false, 'k', 'K'), 
		BLACK_QUEEN(PieceType.QUEEN, false, 'q', 'Q'), 
		BLACK_ROOK(PieceType.ROOK, false, 'r', 'R'), 
		BLACK_BISHOP(PieceType.BISHOP, false, 'b', 'B'), 
		BLACK_KNIGHT(PieceType.KNIGHT, false, 'n', 'N'), 
		BLACK_PAWN(PieceType.PAWN, false, 'p', ' ');
		
		public PieceType type;
		public final boolean isWhite; 
		public final char fenChar;
		public final char pgnChar;
		
		
		Piece(PieceType type, boolean isWhite, char fenChar, char pgnChar) {
			this.type = type;
			this.isWhite = isWhite;
			this.fenChar = fenChar;
			this.pgnChar = pgnChar;
		}
		
		public static Piece getByFenChar(char fenChar) {
			for (Piece p : Piece.values()) {
				if (p.fenChar == fenChar) return p;
			}
			throw new IllegalArgumentException("unknown Piece.fenChar: " + fenChar);
		}
		
		public String getFigurine() {
			switch (type) {
				case KING: return "♔";
				case QUEEN: return "♕";
				case ROOK: return "♖";
				case BISHOP: return "♗";
				case KNIGHT: return "♘";
				case PAWN: return "♙";
			}
			return null;
		}
		
		public String getColoredFigurine() {
			switch (type) {
				case KING: return isWhite ? "♔" : "♚";
				case QUEEN: return isWhite ? "♕" : "♛";
				case ROOK: return isWhite ? "♖" : "♜";
				case BISHOP: return isWhite ? "♗" : "♝";
				case KNIGHT: return isWhite ? "♘" : "♞";
				case PAWN: return isWhite ? "♙" : "♟";
			}
			return null;
		}
	}
}
