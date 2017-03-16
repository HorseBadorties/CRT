package de.toto.pgn;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import de.toto.game.*;

public class PGNReader {
		
	private BufferedReader reader;
	
	private static final boolean DEBUG = false;
	
	private static Logger log = Logger.getLogger("PGNReader");
	
	private static DateFormat PGN_DATE_FOMATTER = new SimpleDateFormat("yyyy.MM.dd");	
	public static String toPGNTimestamp(String millis) {
		return toPGNTimestamp(Long.valueOf(millis));
	}
	public static String toPGNTimestamp(long millis) {
		return PGN_DATE_FOMATTER.format(new Date(millis));
	}
	
	public PGNReader(File pgn) {
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(pgn), "UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException("opening PGN file failed", e);		
		}
	}
	
	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);	
			}
		}
	}
	
	public Game readNextGame() {
		try {
			Game result = readGame(reader);
			if (result == null) {
				reader.close();
				reader = null;
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);	
		}
	}
		
	public static List<Game> parse(InputStream stream) {
		try {
			List<Game> result = new ArrayList<Game>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			try {
				Game g = readGame(reader);
				while (g != null) {
					result.add(g);
					g = readGame(reader);
				}
			} finally {
				reader.close();
			}	
			return result;
		} catch (Exception ex) {
			//TODO better error handling
			throw new RuntimeException("parsing PGN file failed", ex);
		}	
	}
	
	public static List<Game> parse(File pgn) {
		try {
			return parse(new FileInputStream(pgn));
		} catch (FileNotFoundException ex) {			
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
		
	public static List<Game> parse(URL url) {		
		try {
			return parse(url.openStream());
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	public static List<Game> parse(String pgn) {
		try {
			return parse(new ByteArrayInputStream(pgn.getBytes("UTF-8")));
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}		
	}
		
	private static Game readGame(BufferedReader reader) throws IOException {
		if (reader == null) return null;
		String line = reader.readLine();
		while (line != null && StringUtils.isBlank(line)) {
			line = reader.readLine();
		}		
		if (line == null) { // EOF
			return null;
		}
		// Handle first 3 ChessBase special characters		
		if (line.indexOf('[') >= 0) {
			line = line.substring(line.indexOf('['));
		}

		StringBuilder movetext = new StringBuilder();
		Game game = new Game();
		game.start();
		
		while (line != null) {			
			try {
				line = line.trim();
				if (StringUtils.isBlank(line)) {
					line = reader.readLine();
					continue;
				}				
				if (line.startsWith("[") && movetext.length() == 0) {
					line = line.replaceAll("\\[|\\]", ""); //strip "[" and "]"
					String name = line.substring(0, line.indexOf(" "));
					String value = line.substring(line.indexOf(" "), line.length()).trim().replace("\"", "");
					game.addTag(name, value);
					if ("FEN".equals(name)) {
						game.addMove("--", value);
					}
				} else {
					movetext.append(line).append(" ");
					String gameResult = game.getTagValue("Result");
					if (line.endsWith(gameResult)) {
						/*  
						 *  TODO The Result String might be inside a Comment at a line end...
						 *  This would break our parser.
						 */
						parseMovetext(movetext.toString(), game, gameResult);
						if (DEBUG) log.info("parsed game " + game);
						return game;
					}
				}
			} catch (RuntimeException ex) {
				System.err.println("parsing error at line: " + line);
				throw ex;
			}
			line = reader.readLine();
		}
		// EOF
		return null;
	}
	
	private static void parseMovetext(String movetext, Game game, String expectedGameResult) {
		boolean insideComment = false;
		StringBuilder moveComment = null;
		boolean startVariation = false;
		int endVariation = 0;
		for (String token : movetext.split(" ")) {			
			if (!insideComment && token.startsWith("(")) {
				startVariation = true;
				token = token.substring(1);
			}
			if (token.isEmpty()) continue;
			if (token.startsWith("{")) {
				insideComment = true;
				token = token.substring(1);
				moveComment = new StringBuilder();			
			}
//			when insideComment, check if we really are at an "})" or just inside the comment without an "}"
			while (token.endsWith(")") && (!insideComment || token.contains("}"))) {
				endVariation++;
				token = token.substring(0, token.length()-1);
			}
			if (token.endsWith("}")) {
				if (!insideComment) throw new RuntimeException("comment-end token found, but comment-start missing");
				token = token.substring(0, token.length()-1);
				moveComment.append(token);
				insideComment = false;
				if (DEBUG && moveComment != null && moveComment.length() > 0) {
					log.info("adding comment " + moveComment + " at move " + game.getPosition().getMoveNumber());
				}
				game.getPosition().setComment( moveComment == null ? null : moveComment.toString());
				for (int i = 0; i < endVariation; i++) {				
					if (DEBUG) log.info("ending variation" + " at move " + game.getPosition().getMoveNumber());
					game.endVariation();					
				}
				endVariation = 0;
				continue;
			} 
			if (insideComment) {
				moveComment.append(token).append(" ");
				continue;
			}
			// !insideComment
			if (token.equals(expectedGameResult)) break;			
			if (token.endsWith(".")) {				
				continue;
			}
			 
			if (token.startsWith("$")) {
				if (DEBUG) log.info("adding nag " + token + " at move " + game.getPosition().getMoveNumber());
				game.getPosition().addNag(token);
				for (int i = 0; i < endVariation; i++) {				
					if (DEBUG) log.info("ending variation" + " at move " + game.getPosition().getMoveNumber());
					game.endVariation();					
				}
				endVariation = 0;
				continue;
			}			
			//actual move
			if (startVariation) {
				if (DEBUG) log.info("adding variation " + token + " at move " + game.getPosition().getMoveNumber());
				game.newVariation(stripPossibleMoveNumber(token));
				startVariation = false;
			} else if (!token.isEmpty()) {
				if (DEBUG) log.info("adding move " + token + " at move " + game.getPosition().getMoveNumber());
				game.addMove(stripPossibleMoveNumber(token));
			}			
			for (int i = 0; i < endVariation; i++) {				
				if (DEBUG) log.info("ending variation" + " at move " + game.getPosition().getMoveNumber());
				game.endVariation();					
			}
			endVariation = 0;		
		}
		
	}
	
	// Scid vs. PC exports PGNs like "1.d4" or "5...e5" rather than ChessBase's "1. d4" or "5... e5" ...
	private static String stripPossibleMoveNumber(String move) {
		int lastIndexOfDot = move.lastIndexOf('.');		
		return lastIndexOfDot == -1 ? move : move.substring(lastIndexOfDot+1, move.length());
	}
	
		
}
