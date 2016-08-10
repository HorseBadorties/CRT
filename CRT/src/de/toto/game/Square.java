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
		
		public boolean isWhite() {
			return (file % 2 == 0 && rank % 2 != 0) || (file % 2 != 0 && rank % 2 == 0);
		}

		// e.g. "f3"
		public String getName() {
			Character cFile = Character.valueOf((char)(file+96));
			return cFile.toString() + rank;
		}
		
		// e.g. "Nf3"
		public String getNameWithPieceSuffix() {
			String name = "";
			if (piece != null) {
				name += piece.fenChar;
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
		
		public boolean attacks(Square other, Position p) {
			//TODO attacks
			return false;
		}
		
	
}
