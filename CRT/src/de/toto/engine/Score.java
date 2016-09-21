package de.toto.engine;

import java.util.ArrayList;
import java.util.List;

public class Score {
	
	public int depth;
	public float score;	
	public List<String> bestLine = new ArrayList<String>();
	
	private static final String TOKEN_INFO = "info";
	private static final String TOKEN_SCORE_CP = "score cp";
	private static final String TOKEN_PV = "pv";
	private static final String TOKEN_DEPTH = "depth";
	
	public static Score parse(String outputLine) {
		Score result = null;
		if (outputLine != null && outputLine.startsWith(TOKEN_INFO) && outputLine.indexOf(TOKEN_SCORE_CP) > 0) {
			result = new Score();
			result.score = (float)readTokenValue(outputLine, TOKEN_SCORE_CP) / 100;
			result.depth = readTokenValue(outputLine, TOKEN_DEPTH);
			boolean bestLineFound = false; 
			for (String aToken : outputLine.split(" ")) {
				if (TOKEN_PV.equals(aToken)) {
					bestLineFound = true;
					continue;
				}
				//Assumes that TOKEN_PV is the last token...!?
				if (bestLineFound) {
					result.bestLine.add(aToken);
				}
			}
		}
		return result;
	}
	
	private static int readTokenValue(String outputLine, String token) {
		String lineAfterToken = outputLine.substring(outputLine.indexOf(token) + token.length(), outputLine.length()).trim();
		String[] allToken = lineAfterToken.split(" ");
		return Integer.parseInt(allToken[0]);
	}
	
	@Override
	public String toString() {
		return String.format("%d [%.2f] %s", depth, score, bestLine); 
	}
	
	
}
