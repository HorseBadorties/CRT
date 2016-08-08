package de.toto.game;

public class Rules {
	
	public enum PieceType {			
		KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
	}
	
	public enum Piece {			
		WHITE_KING(PieceType.KING, 'K', 'K'), 
		WHITE_QUEEN(PieceType.QUEEN, 'Q', 'Q'), 
		WHITE_ROOK(PieceType.ROOK, 'R', 'R'), 
		WHITE_BISHOP(PieceType.BISHOP, 'B', 'B'), 
		WHITE_KNIGHT(PieceType.KNIGHT, 'N', 'N'), 
		WHITE_PAWN(PieceType.PAWN, 'P', ' '), 
		BLACK_KING(PieceType.KING, 'k', 'K'), 
		BLACK_QUEEN(PieceType.QUEEN, 'q', 'Q'), 
		BLACK_ROOK(PieceType.ROOK, 'r', 'R'), 
		BLACK_BISHOP(PieceType.BISHOP, 'b', 'B'), 
		BLACK_KNIGHT(PieceType.KNIGHT, 'n', 'N'), 
		BLACK_PAWN(PieceType.PAWN, 'p', ' ');
		
		public PieceType type;
		public final char fenChar;
		public final char pgnChar;
		
		Piece(PieceType type, char fenChar, char pgnChar) {
			this.type = type;
			this.fenChar = fenChar;
			this.pgnChar = pgnChar;
		}
		
		public static Piece getByFenChar(char fenChar) {
			for (Piece p : Piece.values()) {
				if (p.fenChar == fenChar) return p;
			}
			throw new IllegalArgumentException("unknown Piece.fenChar: " + fenChar);
		}
		
	}
}
