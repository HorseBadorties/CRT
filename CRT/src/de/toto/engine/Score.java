package de.toto.engine;

import java.util.ArrayList;
import java.util.List;

import de.toto.game.Game;
import de.toto.game.Position;

public class Score {
	
	public String fen;
	public int multiPV;
	public int depth;
	public int mate;
	public int time;
	public float score;	
	public String bestLine;
	public String bestMove;
	
	private static final String TOKEN_INFO = "info";
	private static final String TOKEN_SCORE_CP = "score cp";
	private static final String TOKEN_SCORE_MATE = "score mate";
	private static final String TOKEN_PV = "pv";
	private static final String TOKEN_DEPTH = "depth";
	private static final String TOKEN_MULTIPV = "multipv";
	private static final String TOKEN_TIME = "time";
	
	public static Score parse(String fen, String outputLine) {
		Score result = null;
		if (outputLine != null && outputLine.startsWith(TOKEN_INFO) 
				&& (outputLine.indexOf(TOKEN_SCORE_CP) > 0 || outputLine.indexOf(TOKEN_SCORE_MATE) > 0)) {
			result = new Score();
			result.fen = fen;
			result.multiPV = readTokenValue(outputLine, TOKEN_MULTIPV, 1);
			result.mate = readTokenValue(outputLine, TOKEN_SCORE_MATE, 0);
			if (result.mate == 0) {
				result.score = (float)readTokenValue(outputLine, TOKEN_SCORE_CP, 0) / 100;
			}
			result.depth = readTokenValue(outputLine, TOKEN_DEPTH, 0);
			result.time = readTokenValue(outputLine, TOKEN_TIME, 0);
			List<String> bestLineToken = null;
			for (String aToken : outputLine.split(" ")) {
				if (TOKEN_PV.equals(aToken)) {
					bestLineToken = new ArrayList<String>(); 
					continue;
				}
				//Assumes that TOKEN_PV is the last token...!?
				if (bestLineToken != null) {					
					bestLineToken.add(aToken);
				}
			}
			if (bestLineToken != null && !bestLineToken.isEmpty()) {
				try {
					Game g = new Game(new Position(null, "--", fen));				
					StringBuilder bestLineBuilder = new StringBuilder();
					int count = 0;				
					for (String move : bestLineToken) {
						count++;
						String lanMove = g.getPosition().translateMove(move);
						if (lanMove == null) {
							// current engine FEN has already changed
							break;
						}
						Position p = g.addMove(g.getPosition().translateMove(move));
						if (count == 1) result.bestMove = move;
						bestLineBuilder.append(p.getMoveNotation(count == 1 || p.whiteMoved())).append(" ");
						//if (count >= 10) break;
					};
					result.bestLine = bestLineBuilder.toString();
				} catch (Exception ex) {
					// can happen if the current position does no longer match the engine position
					result.bestLine = "";
				}
			}
		}
		return result;
	}
	
	private static int readTokenValue(String outputLine, String token, int defaultValue) {
		if (outputLine.indexOf(token) < 0) return defaultValue;
		String lineAfterToken = outputLine.substring(outputLine.indexOf(token) + token.length(), outputLine.length()).trim();
		String[] allToken = lineAfterToken.split(" ");
		return Integer.parseInt(allToken[0]);
	}
	
	@Override
	public String toString() {
		if (mate > 0) {
			return String.format("%d: %d [M%d] %s", multiPV, depth, mate, bestLine);			
		} else {
			return String.format("%d: %d [%.2f] %s", multiPV, depth, score, bestLine);
		}
	}
	
	
}
