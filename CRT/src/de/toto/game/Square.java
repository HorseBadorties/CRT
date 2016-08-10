package de.toto.game;

import de.toto.game.Rules.Piece;

public class Square {
	
		public byte rank;
		public byte file;
		public Piece piece;

		public Square(byte rank, byte file) {
			this.rank = rank;
			this.file = file;
		
		}
		
		/**
		 * returns 1 for "a" and so forth
		 */		
		public static int filenumberForName(String fileName) {
			return fileName.charAt(0) - 96;
		}
		
		public boolean isWhite() {
			return (file % 2 == 0 && rank % 2 != 0) || (file % 2 != 0 && rank % 2 == 0);
		}

		/**
		 * e.g. "f3"
		 */	
		public String getName() {
			Character cFile = Character.valueOf((char)(file+96));
			return cFile.toString() + rank;
		}
		
		/**
		 * e.g. "Nf3"
		 */	
		public String getNameWithPieceSuffix() {
			String name = "";
			if (piece != null) {
				name += piece.pgnChar;
				name.trim();
			}
			name += getName();
			return name;
		}

		@Override
		public String toString() {
			return String.format("%s, %s, %s", getName(),
					isWhite() ? "white" : "black", piece != null ? piece : "empty");
		}
		
		
		
		/**
		 * A Square equals another Square is the have the same coordinates.
		 */		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Square)) return false;
			Square other = (Square)obj;
			return this.rank == other.rank && this.file == other.file;
		}
		
		public boolean isEnPassantPossible(Square to, Position p) {			
			try {							
				if (p.whiteMoved() && rank - to.rank == -2) {
					Square epSquare = getSquare(p, rank + 2, file -1);
					if (epSquare != null && epSquare.piece == Piece.BLACK_PAWN) return true;
					epSquare = getSquare(p, rank + 2, file +1);
					if (epSquare != null && epSquare.piece == Piece.BLACK_PAWN) return true;
				} else if (!p.whiteMoved() && rank - to.rank == 2) {
					Square epSquare = getSquare(p, rank - 2, file -1);
					if (epSquare != null && epSquare.piece == Piece.WHITE_PAWN) return true;
					epSquare = getSquare(p, rank - 2, file +1);
					if (epSquare != null && epSquare.piece == Piece.WHITE_PAWN) return true;
				}
			} catch (Exception ignore) {}
			return false;
			
		}

		/**
		 * Does the piece on this Square attack the other square?
		 */
		public boolean attacks(Square other, Position p) {
			if (piece == null) return false;
			if (isPinned(p)) return false;
			switch (piece.type) {
				case KING: return kingAttacks(other, p);
				case QUEEN: return queenAttacks(other, p);
				case ROOK: return rookAttacks(other, p);
				case BISHOP: return bishopAttacks(other, p);
				case KNIGHT: return knightAttacks(other, p);
				case PAWN: return pawnAttacks(other, p);				
			}
			return false;
		}
		
		private boolean kingAttacks(Square other, Position p) {
			if (other.equals(getSquare(p, rank+1, file))) return true;
			if (other.equals(getSquare(p, rank+1, file-1))) return true;
			if (other.equals(getSquare(p, rank+1, file+1))) return true;
			if (other.equals(getSquare(p, rank, file-1))) return true;			
			if (other.equals(getSquare(p, rank, file+1))) return true;
			if (other.equals(getSquare(p, rank-1, file))) return true;
			if (other.equals(getSquare(p, rank-1, file-1))) return true;
			if (other.equals(getSquare(p, rank-1, file+1))) return true;
			return false;
		}
		
		private boolean queenAttacks(Square other, Position p) {
			return rookAttacks(other, p) || bishopAttacks(other, p);
		}
		
		private boolean rookAttacks(Square other, Position p) {
			int _rank = rank, _file = file;
			Square s = this;
			while (s != null) { //go up
				s = getSquare(p, _rank+1, _file);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null) break;
				_rank++; 
			}
			s = this; _rank = rank; _file = file;
			while (s != null) { //go right
				s = getSquare(p, _rank, _file+1);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null) break;
				_file++;
			}
			s = this;  _rank = rank; _file = file;
			while (s != null) { //go down
				s = getSquare(p, _rank-1, _file);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null) break;
				_rank--;
			}
			s = this;  _rank = rank; _file = file;
			while (s != null) { //go left
				s = getSquare(p, _rank, _file-1);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null) break;
				_file--;
			}
			return false;
		}
		
		private boolean bishopAttacks(Square other, Position p) {
			int _rank = rank, _file = file;
			Square s = this;
			while (s != null) { //go up-right
				s = getSquare(p, _rank+1, _file+1);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null) break;
				_rank++; _file++;
			}
			s = this; _rank = rank; _file = file;
			while (s != null) { //go up-left
				s = getSquare(p, _rank+1, _file-1);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null) break;
				_rank++; _file--;
			}
			s = this;  _rank = rank; _file = file;
			while (s != null) { //go down-right
				s = getSquare(p, _rank-1, _file+1);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null) break;
				_rank--; _file++;
			}
			s = this;  _rank = rank; _file = file;
			while (s != null) { //go down-left
				s = getSquare(p, _rank-1, _file-1);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null) break;
				_rank--; _file--;
			}
			return false;
		}
		
		private boolean knightAttacks(Square other, Position p) {
			if (other.equals(getSquare(p, rank+2, file+1))) return true;
			if (other.equals(getSquare(p, rank+2, file-1))) return true;
			if (other.equals(getSquare(p, rank+1, file+2))) return true;
			if (other.equals(getSquare(p, rank+1, file-2))) return true;			
			if (other.equals(getSquare(p, rank-1, file+2))) return true;
			if (other.equals(getSquare(p, rank-1, file-2))) return true;
			if (other.equals(getSquare(p, rank-2, file+1))) return true;
			if (other.equals(getSquare(p, rank-2, file-1))) return true;
			return false;
		}
				
		private boolean pawnAttacks(Square other, Position p) {
			int startRank = p.whiteMoved() ? 2 : 7;  
			// move one square
			Square s = getSquare(p, p.whiteMoved() ? rank+1 : rank-1, file);
			if (other.equals(s)) return true;			
			if (rank == startRank && s.piece == null) {
				// move two squares
				s = getSquare(p, p.whiteMoved() ? rank+2 : rank-2, file);
				if (other.equals(s)) return true;	
			}
			// try captures
			s = getSquare(p, p.whiteMoved() ? rank+1 : rank-1, file+1);
			if (other.equals(s) && s.piece != null && s.piece.isWhite != p.whiteMoved()) return true;	
			s = getSquare(p, p.whiteMoved() ? rank+1 : rank-1, file-1);
			if (other.equals(s) && s.piece != null && s.piece.isWhite != p.whiteMoved()) return true;	
			// en passant
			String enPassantField = p.getPrevious().getFen().split(" ")[3]; 
			if (!"-".equals(enPassantField)) {
				if (other.getName().equals(enPassantField)) return true;
			} 
			return false;
		}
		
		private Square getSquare(Position p, int rank, int file) {
			if (rank < 1 || rank > 8 || file < 1 || file > 8) return null;
			return p.getSquare(rank, file);
		}
		
		//TODO isPinned
		public boolean isPinned(Position p) {			
			return false;
		}
				
		
	
}
