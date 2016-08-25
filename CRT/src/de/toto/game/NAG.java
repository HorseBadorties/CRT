package de.toto.game;


public enum NAG {
	
	// https://en.wikipedia.org/wiki/Numeric_Annotation_Glyphs
	
	GOOD_MOVE("$1", "!"),
	POOR_MOVE("$2", "?"),
	VERY_GOOD_MOVE("$3", "\u203C"),  
	VERY_POOR_MOVE("$4", "??"), // "\u2047"
	INTERESTING_MOVE("$5", "!?"), // "\u2049"
	DUBIOUS_MOVE("$6", "?!"), // "\u2048"
	FORCED_MOVE("$7", "\u25A1"),
	SINGULAR_MOVE("$8", "\u25A1"),
	EVEN_POSITION("$10", "="),
	EVEN_POSITION2("$11", "="),
	UNCLEAR_POSITION("$13", "\u221E"),
	SLIGHT_ADVANTAGE_WHITE("$14", "\u2A72"), //TODO
	SLIGHT_ADVANTAGE_BLACK("$15", "\u2A71"), //TODO
	ADVANTAGE_WHITE("$16", "\u00B1"),
	ADVANTAGE_BLACK("$17", "\u2213"),
	DECISIVE_ADVANTAGE_WHITE("$18", "+-"),
	DECISIVE_ADVANTAGE_BLACK("$19", "-+"),
	ZUGZWANG_WHITE("$22", "\u2A00"),
	ZUGZWANG_BLACK("$23", "\u2A00"),
	DEVELOPMENT_ADVANTAGE_WHITE("$32", "\u27F3"), //TODO
	DEVELOPMENT_ADVANTAGE_BLACK("$33", "\u27F3"), //TODO
	INITIATIVE_WHITE("$36", "\u2192"),
	INITIATIVE_BLACK("$37", "\u2192"),
	ATTACK_WHITE("$40", "\u2191"),
	ATTACK_BLACK("$41", "\u2191"),
	COMPENSATION_WHITE("$44", "="+"\u221E"), //Unicode symbol missing
	COMPENSATION_BLACK("$45", "="+"\u221E"), //Unicode symbol missing
	COUNTERPLAY_WHITE("$132", "\u21C6"),
	COUNTERPLAY_BLACK("$133", "\u21C6"),
	WITH_THE_IDEA("$140", "\u2206"),
	BETTER_IS("$142", "\u2313"),
	EDITORIAL_COMMENT("$145", "RR"),
	NOVELTY("$146", "N"),
	FILE("$239", "\u21D4"),
	DIAGONAL("$240", "\u21D7"),
	KING_SIDE("$242", "\u27EB"),
	QUEEN_SIDE("$243", "\u27EA"),
	WEAK_POINT("$244", "\u2715"),
	ENDING("$245", "\u22A5"),
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
