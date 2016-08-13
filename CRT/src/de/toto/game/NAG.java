package de.toto.game;


public enum NAG {
	
	// https://en.wikipedia.org/wiki/Numeric_Annotation_Glyphs
	
	GOOD_MOVE("$1", "!"),
	POOR_MOVE("$2", "?"),
	VERY_GOOD_MOVE("$3", "!!"),
	VERY_POOR_MOVE("$4", "??"),
	INTERESTING_MOVE("$5", "!?"),
	DUBIOUS_MOVE("$6", "?!"),
	FORCED_MOVE("$7", "□"),
	EVEN_POSITION("$10", "="),
	UNCLEAR_POSITION("$13", "∞"),
	SLIGHT_ADVANTAGE_WHITE("$14", "⩲"),
	SLIGHT_ADVANTAGE_BLACK("$15", "⩱"),
	ADVANTAGE_WHITE("$16", "±"),
	ADVANTAGE_BLACK("$17", "∓"),
	DECISIVE_ADVANTAGE_WHITE("$18", "+-"),
	DECISIVE_ADVANTAGE_BLACK("$19", "-+"),
	ZUGZWANG_WHITE("$22", "⨀"),
	ZUGZWANG_BLACK("$23", "⨀"),
	INITIATIVE_WHITE("$36", "→"),
	INITIATIVE_BLACK("$37", "→"),
	ATTACK_WHITE("$40", "↑"),
	ATTACK_BLACK("$41", "↑"),
	COUNTERPLAY_WHITE("$132", "⇆"),
	COUNTERPLAY_BLACK("$133", "⇆"),
	WITH_THE_IDEA("$140", "∆"),
	BETTER_IS("$142", "⌓"),
	EDITORIAL_COMMENT("$145", "RR"),
	NOVELTY("$146", "N"),
	FILE("$239", "⇔"),
	DIAGONAL("$240", "⇗"),
	KING_SIDE("$241", "⟫"),
	QUEEN_SIDE("$242", "⟪"),
	WEAK_POINT("$243", "✕"),
	ENDING("$244", "⊥"),
	UNKNOWN("<?>", "<unknown NAG>");
	public final String nag;
	public final String pgn;
	
	NAG(String nag, String pgn) {
		this.nag = nag;
		this.pgn = pgn;
	}
	
	@Override
	public String toString() {
		return pgn;
	}

	public static NAG getByNag(String nag) {
		for (NAG n : NAG.values()) {
			if (n.nag.equals(nag)) return n;
		}
		return UNKNOWN;
	}

}
