package de.toto.game;

import java.util.ArrayList;
import java.util.List;

import de.toto.game.Rules.Piece;
import de.toto.game.Rules.PieceType;

public class Square {
	
		public byte rank;
		public byte file;
		public Piece piece;

		public Square(byte rank, byte file) {
			this.rank = rank;
			this.file = file;
		
		}
		
		public static Square[][] createEmpty8x8() {
			Square[][] squares = new Square[8][8];			
			for (int rank = 1; rank <= 8; rank++) {				
				for (int file = 1; file <= 8; file++) {
					squares[rank - 1][file - 1] = new Square((byte)rank, (byte)file);
				}
			}
			return squares;
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
			return getFileName() + rank;
		}
		
		public String getFileName() {
			return Character.valueOf((char)(file+96)).toString();
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
			return name.trim();
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
				if (to.piece.isWhite && rank - to.rank == -2) {
					Square epSquare = getSquare(p, rank + 2, file -1);
					if (epSquare != null && epSquare.piece == Piece.BLACK_PAWN) return true;
					epSquare = getSquare(p, rank + 2, file +1);
					if (epSquare != null && epSquare.piece == Piece.BLACK_PAWN) return true;
				} else if (!to.piece.isWhite && rank - to.rank == 2) {
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
		public boolean attacks(Square other, Position p, Square ignore) {
			if (piece == null) return false;
			if (ignore == null && isPinned(p, other) && other.piece != (isWhite() ? Piece.BLACK_KING : Piece.WHITE_KING)) return false;
			switch (piece.type) {
				case KING: return kingAttacks(other, p);
				case QUEEN: return queenAttacks(other, p, ignore);
				case ROOK: return rookAttacks(other, p, ignore);
				case BISHOP: return bishopAttacks(other, p, ignore);
				case KNIGHT: return knightAttacks(other, p);
				case PAWN: return pawnAttacks(other, p);				
			}
			return false;
		}
		
		/**
		 * Is the piece on this Square attacked by an enemy piece on another square?
		 */
		public boolean isAttacked(Position p) {
			if (piece == null) return false;			
			for (Square squareWithEnemyPiece : p.getSquaresWithPiecesByColor(!piece.isWhite)) {
				if (squareWithEnemyPiece.attacks(this, p, null)) return true;
			}
			return false;
		}
		
		
		/**
		 * Can the piece on this Square move to the other square?
		 */
		public boolean canMoveTo(Square other, Position p, Square ignore) {
			if (piece == null) return false;
			if (ignore == null && isPinned(p, other)) return false;
			switch (piece.type) {
				case KING: return kingCanMoveTo(other, p);
				case QUEEN: return queenAttacks(other, p, ignore);
				case ROOK: return rookAttacks(other, p, ignore);
				case BISHOP: return bishopAttacks(other, p, ignore);
				case KNIGHT: return knightAttacks(other, p);
				case PAWN: return pawnCanMoveTo(other, p);				
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
		
		private boolean kingCanMoveTo(Square other, Position p) {
			if (kingAttacks(other, p)) return true;
			String[] castlingSquareNames = p.getPossibleCastlingSquareNames();
			if (other.getName().equals(castlingSquareNames[0]) || other.getName().equals(castlingSquareNames[1])) return true;
			return false;
		}
		
		private boolean queenAttacks(Square other, Position p, Square ignore) {
			return rookAttacks(other, p, ignore) || bishopAttacks(other, p, ignore);
		}
		
		private boolean rookAttacks(Square other, Position p, Square ignore) {
			int _rank = rank, _file = file;
			Square s = this;
			while (s != null) { //go up
				s = getSquare(p, ++_rank, _file);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null && s != ignore) break; 
			}
			s = this; _rank = rank; _file = file;
			while (s != null) { //go right
				s = getSquare(p, _rank, ++_file);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null && s != ignore) break;
			}
			s = this;  _rank = rank; _file = file;
			while (s != null) { //go down
				s = getSquare(p, --_rank, _file);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null && s != ignore) break;
			}
			s = this;  _rank = rank; _file = file;
			while (s != null) { //go left
				s = getSquare(p, _rank, --_file);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null && s != ignore) break;
			}
			return false;
		}
		
		private boolean bishopAttacks(Square other, Position p, Square ignore) {
			int _rank = rank, _file = file;
			Square s = this;
			while (s != null) { //go up-right
				s = getSquare(p, ++_rank, ++_file);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null && s != ignore) break;
			}
			s = this; _rank = rank; _file = file;
			while (s != null) { //go up-left
				s = getSquare(p, ++_rank, --_file);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null && s != ignore) break;
			}
			s = this;  _rank = rank; _file = file;
			while (s != null) { //go down-right
				s = getSquare(p, --_rank, ++_file);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null && s != ignore) break;
			}
			s = this;  _rank = rank; _file = file;
			while (s != null) { //go down-left
				s = getSquare(p, --_rank, --_file);
				if (other.equals(s)) return true;
				if (s != null && s.piece != null && s != ignore) break;
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
			int startRank = this.piece.isWhite ? 2 : 7;  
			// move one square
			Square s = getSquare(p, this.piece.isWhite ? rank+1 : rank-1, file);
			if (other.equals(s) && s.piece == null) return true;			
			if (rank == startRank && s.piece == null) {
				// move two squares
				s = getSquare(p, this.piece.isWhite ? rank+2 : rank-2, file);
				if (other.equals(s) && s.piece == null) return true;	
			}
			// try captures (with en passant)
			String fen = p.getFen() != null ? p.getFen() : p.getPrevious().getFen();
			String enPassantField = fen.split(" ")[3];  
			s = getSquare(p, this.piece.isWhite ? rank+1 : rank-1, file+1);
			if (other.equals(s) && ((s.piece != null && s.piece.isWhite != this.piece.isWhite) || s.getName().equals(enPassantField))) return true;	
			s = getSquare(p, this.piece.isWhite ? rank+1 : rank-1, file-1);
			if (other.equals(s) && ((s.piece != null && s.piece.isWhite != this.piece.isWhite) || s.getName().equals(enPassantField))) return true;	
			 
			return false;
		}
		
		private boolean pawnCanMoveTo(Square other, Position p) {
			return pawnAttacks(other, p);
		}
		
		
		public List<Square> getPossibleTargetSquares(Position p) {
			List<Square> result = new ArrayList<Square>();
			if (piece == null) return result;			
			switch (piece.type) {
				case KING: addPossibleTargetSquaresOfKing(result, p); break;
				case QUEEN: addPossibleTargetSquaresOfQueen(result, p); break;
				case ROOK: addPossibleTargetSquaresOfRook(result, p); break;
				case BISHOP: addPossibleTargetSquaresOfBishop(result, p); break;
				case KNIGHT: addPossibleTargetSquaresOfKnight(result, p); break;
				case PAWN: addPossibleTargetSquaresOfPawn(result, p); break;			
			}
			return result;
		}
		
		private boolean doAdd(List<Square> squares, Square s) {
			if (s != null && (s.piece == null || s.piece.isWhite != this.piece.isWhite)) {
				return squares.add(s);
			} else return false;
		}
		
		private void addPossibleTargetSquaresOfKing(List<Square> squares, Position p) {
			doAdd(squares, getSquare(p, rank+1, file));
			doAdd(squares, getSquare(p, rank+1, file-1));
			doAdd(squares, getSquare(p, rank+1, file+1));
			doAdd(squares, getSquare(p, rank, file-1));			
			doAdd(squares, getSquare(p, rank, file+1));
			doAdd(squares, getSquare(p, rank-1, file));
			doAdd(squares, getSquare(p, rank-1, file-1));
			doAdd(squares, getSquare(p, rank-1, file+1));
			// castling
			if (!p.isCheck()) {
				int rank = this.piece.isWhite ? 1 : 8;
				for (String castlingSquare : p.getPossibleCastlingSquareNames()) {
					if (castlingSquare != null && castlingSquare.startsWith("g")) {
						Square f = getSquare(p, rank, 6);
						Square g = getSquare(p, rank, 7);
						if (f.piece == null && !f.isAttacked(p) && g.piece == null && !g.isAttacked(p)) {
							squares.add(g);
						}
					} else if (castlingSquare != null && castlingSquare.startsWith("c")) { 
						Square d = getSquare(p, rank, 4);
						Square c = getSquare(p, rank, 3);
						if (d.piece == null && !d.isAttacked(p) && c.piece == null && !c.isAttacked(p)) {
							squares.add(c);
						}
					}
				}
			}
			
		}
		
		private void addPossibleTargetSquaresOfQueen(List<Square> squares, Position p) {
			addPossibleTargetSquaresOfRook(squares, p);
			addPossibleTargetSquaresOfBishop(squares, p);
		}
		
		private void addPossibleTargetSquaresOfRook(List<Square> squares, Position p) {			
			int _rank = rank, _file = file;			
			for (;;) { //go up
				Square s = getSquare(p, ++_rank, _file);
				if (!doAdd(squares, s)) break;
				if (s.piece != null) break;
			}
			_rank = rank; _file = file;
			for (;;) { //go right
				Square s = getSquare(p, _rank, ++_file);
				if (!doAdd(squares, s)) break; 
				if (s.piece != null) break;
			}
			_rank = rank; _file = file;
			for (;;) { //go down
				Square s = getSquare(p, --_rank, _file);
				if (!doAdd(squares,  s)) break;
				if (s.piece != null) break;
			}
			_rank = rank; _file = file;
			for (;;) { //go left
				Square s =  getSquare(p, _rank, --_file);
				if (!doAdd(squares, s)) break;
				if (s.piece != null) break;
			}			
		}
		
		private void addPossibleTargetSquaresOfBishop(List<Square> squares, Position p) {
			int _rank = rank, _file = file;			
			for (;;) { //go up-right
				Square s = getSquare(p, ++_rank,++ _file);
				if (!doAdd(squares, s)) break;
				if (s.piece != null) break;
			}
			_rank = rank; _file = file;
			for (;;) { //go down-right
				Square s = getSquare(p, --_rank, ++_file);
				if (!doAdd(squares, s)) break; 
				if (s.piece != null) break;
			}
			_rank = rank; _file = file;
			for (;;) { //go up-left
				Square s = getSquare(p, ++_rank, --_file);
				if (!doAdd(squares,  s)) break;
				if (s.piece != null) break;
			}
			_rank = rank; _file = file;
			for (;;) { //go down-left
				Square s =  getSquare(p, --_rank, --_file);
				if (!doAdd(squares, s)) break;
				if (s.piece != null) break;
			}		
		}
		
		private void addPossibleTargetSquaresOfKnight(List<Square> squares, Position p) {
			doAdd(squares, getSquare(p, rank+2, file+1));
			doAdd(squares, getSquare(p, rank+2, file-1));
			doAdd(squares, getSquare(p, rank+1, file+2));
			doAdd(squares, getSquare(p, rank+1, file-2));			
			doAdd(squares, getSquare(p, rank-2, file+1));
			doAdd(squares, getSquare(p, rank-2, file-1));
			doAdd(squares, getSquare(p, rank-1, file+2));
			doAdd(squares, getSquare(p, rank-1, file-2));
		}
		
		private void addPossibleTargetSquaresOfPawn(List<Square> squares, Position p) {
			int startRank = this.piece.isWhite ? 2 : 7;  
			// move one square
			Square s = getSquare(p, this.piece.isWhite ? rank+1 : rank-1, file);
			if (s != null && s.piece == null) squares.add(s);			
			if (rank == startRank && s.piece == null) {
				// move two squares
				s = getSquare(p, this.piece.isWhite ? rank+2 : rank-2, file);
				if (s != null && s.piece == null) squares.add(s);
			}
			// try captures (with en passant)
			String fen = p.getFen() != null ? p.getFen() : p.getPrevious().getFen();
			String enPassantField = fen.split(" ")[3];  
			s = getSquare(p, this.piece.isWhite ? rank+1 : rank-1, file+1);
			if (s != null && ((s.piece != null && s.piece.isWhite != this.piece.isWhite) || s.getName().equals(enPassantField))) squares.add(s);	
			s = getSquare(p, this.piece.isWhite ? rank+1 : rank-1, file-1);
			if (s != null && ((s.piece != null && s.piece.isWhite != this.piece.isWhite) || s.getName().equals(enPassantField))) squares.add(s);	
			 
		}
		
		private Square getSquare(Position p, int rank, int file) {
			if (rank < 1 || rank > 8 || file < 1 || file > 8) return null;
			return p.getSquare(rank, file);
		}
		
		//TODO isPinned
		public boolean isPinned(Position p, Square moveSquare) {			
			if (piece.type == PieceType.KING) return false;
			Square kingsSquare = p.findKing(this.piece.isWhite);
			Piece originalPieceOnMoveSquare = moveSquare.piece; 
			try {
				if (moveSquare != null) {
					//temporarily change the position by adding a "ghost pawn" on our moveSquare...
					moveSquare.piece = piece.isWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
				}
				List<Square> potentialAttackers = new ArrayList<Square>();
				Piece enemyQueen = piece.isWhite ? Piece.BLACK_QUEEN : Piece.WHITE_QUEEN;
				Piece enemyRook = piece.isWhite ? Piece.BLACK_ROOK : Piece.WHITE_ROOK;			
				// find potential pinning squares/pieces on same rank, file or diagonal behind us and our king...
				if (kingsSquare.rank == this.rank) {
					if (kingsSquare.file < this.file) {
						for (int _file = this.file+1; _file <= 8; _file++) {
							Square s = p.getSquare(rank, _file);
							if (s.piece == enemyQueen || s.piece == enemyRook) {
								potentialAttackers.add(s); break;							
							} else if (s.piece != null) break;
						}
					} else {
						for (int _file = this.file-1; _file >=1 ; _file--) {
							Square s = p.getSquare(rank, _file);
							if (s.piece == enemyQueen || s.piece == enemyRook) {
								potentialAttackers.add(s); break;
							} else if (s.piece != null) break;
						}
					}
				} else if (kingsSquare.file == this.file) {
					if (kingsSquare.rank < this.rank) {
						for (int _rank = this.rank+1; _rank <= 8; _rank++) {
							Square s = p.getSquare(_rank, this.file);
							if (s.piece == enemyQueen || s.piece == enemyRook) {
								potentialAttackers.add(s); break;
							} else if (s.piece != null) break;
						}
					} else {
						for (int _rank = this.rank-1; _rank >=1 ; _rank--) {
							Square s = p.getSquare(_rank, this.file);
							if (s.piece == enemyQueen || s.piece == enemyRook) {
								potentialAttackers.add(s); break;
							} else if (s.piece != null) break;
						}
					}
				} else { //check diagonals..
					Piece enemyBishop = piece.isWhite ? Piece.BLACK_BISHOP : Piece.WHITE_BISHOP;
					int _rank = kingsSquare.rank, _file = kingsSquare.file;
					Square s = kingsSquare;
					boolean foundMe = false;
					for(;;) { //go up-right
						s = getSquare(p, ++_rank, ++_file);
						if (s == null) break;
						if (foundMe && (s.piece == enemyQueen || s.piece == enemyBishop)) {						
							potentialAttackers.add(s); break;
						} else if (s == this) {
							foundMe = true;
						} else if (s.piece != null) break;
					}
					if (!foundMe) {
						s = kingsSquare; _rank = kingsSquare.rank; _file = kingsSquare.file;
						for(;;) { //go up-left
							s = getSquare(p, ++_rank, --_file);
							if (s == null) break;
							if (foundMe && (s.piece == enemyQueen || s.piece == enemyBishop)) {						
								potentialAttackers.add(s); break;
							} else if (s == this) {
								foundMe = true;
							} else if (s.piece != null) break;
						}
					}
					if (!foundMe) {
						s = kingsSquare;  _rank = kingsSquare.rank; _file = kingsSquare.file;
						for(;;) { //go down-right
							s = getSquare(p, --_rank, ++_file);
							if (s == null) break;
							if (foundMe && (s.piece == enemyQueen || s.piece == enemyBishop)) {						
								potentialAttackers.add(s); break;
							} else if (s == this) {
								foundMe = true;
							} else if (s.piece != null) break;
						}
					}
					if (!foundMe) {
						s = kingsSquare;  _rank = kingsSquare.rank; _file = kingsSquare.file;
						for(;;) { //go down-left
							s = getSquare(p, --_rank, --_file);
							if (s == null) break;
							if (foundMe && (s.piece == enemyQueen || s.piece == enemyBishop)) {						
								potentialAttackers.add(s); break;
							} else if (s == this) {
								foundMe = true;
							} else if (s.piece != null) break;
						}
					}
				}
				// does any potential attacker in fact pin us?
				for (Square potentialAttacker : potentialAttackers) {
					if (potentialAttacker.attacks(kingsSquare, p, this)) return true;
				}
				return false;
			} finally {				
				// restore original Position
				moveSquare.piece = originalPieceOnMoveSquare;
			}
		}
		
}
