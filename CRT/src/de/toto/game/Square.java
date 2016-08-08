package de.toto.game;

import de.toto.game.Rules.Piece;

public class Square {
	
		public boolean isWhite;
		public int rank;
		public int file;
		public Piece piece;

		public Square(int rank, int file, boolean isWhite) {
			super();
			this.isWhite = isWhite;
			this.rank = rank;
			this.file = file;
		}

		// e.g. "f3"
		public String getName() {
			Character cFile = Character.valueOf((char)(file+96));
			return cFile.toString() + rank;
		}
		
		// e.g. "Nf3"
		public String getNameWithPieceSuffix() {
			String name = getName();
			if (piece != null) {
				name += piece.pgnChar;
				name.trim();
			}
			return name;
		}

		@Override
		public String toString() {
			return String.format("%s, %s, %s", getName(),
					isWhite ? "white" : "black", piece != null ? piece : "empty");
		}

	
}
