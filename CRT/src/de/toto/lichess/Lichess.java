package de.toto.lichess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.toto.NetworkConfig;
import de.toto.game.*;
import de.toto.pgn.PGNReader;


public class Lichess {
	
	static {
		NetworkConfig.doConfig();
	}
	
	public static List<Game> downloadGames(String lichessUser) {
		return downloadGames(lichessUser, null, null, null, true, true, (String[])null, (String[])null);
	}
			
	// speed=bullet|blitz|classical|unlimited
	public static List<Game> downloadGames(String lichessUser, String upToID, Date from, Date to, boolean whiteGames, boolean blackGames, String[] speed, String[] movesStartWith) {		
		System.out.println("starting at " + new Date());
		List<Game> result = new ArrayList<>();
		if (!whiteGames && !blackGames) {
			System.out.println("neither white nor black games requested");
			return result;
		}
		InputStream is = null;		
		try {			
			int nb = 100;
			int page = 1;
			
			while_loop:
			while (page > 0) { 
				URL url = new URL(String.format("https://lichess.org/api/user/%s/games?nb=%d&page=%d&with_moves=1",
						lichessUser, nb, page));
				JsonObject json = null;
				try {
					System.out.println("opening " + url);
					is = url.openStream();	
					Gson gson = new Gson();
					json = gson.fromJson(new InputStreamReader(is, "UTF-8"), JsonObject.class);
				} catch (IOException ex){
					if (ex.getMessage().startsWith("Server returned HTTP response code: 429")) {
						System.out.println("got a 429");
						pause(70);
						continue;
					} else {
						throw ex;
					}
				} finally {
					if (is != null) is.close();
				}				
				for (JsonElement e : json.getAsJsonArray("currentPageResults")) {	
					JsonObject jsonGame = e.getAsJsonObject();
					//matches the search criteria?
					Date createdAt = new Date(Long.valueOf(get(jsonGame, "createdAt")));
					if (from != null && createdAt.before(from)) {
						break while_loop;
					}	
					if (to != null && createdAt.after(to)) continue;
					if (whiteGames && !blackGames) {
						if (!lichessUser.equalsIgnoreCase(get(jsonGame, "players.white.userId"))) continue;
					} 
					if (!whiteGames && blackGames) {
						if (!lichessUser.equalsIgnoreCase(get(jsonGame, "players.black.userId"))) continue;
					}
					if (speed != null && !Arrays.asList(speed).contains(get(jsonGame, "speed"))) {
						continue;
					}
					if (movesStartWith != null) {
						String moves = get(jsonGame, "moves");	
						boolean match = false;
						for (String requestedMoves : movesStartWith) {
							if (moves.startsWith(requestedMoves)) {
								match = true; 
								break;
							}
						}
						if (!match) continue;
					}
					
					Game g = toGame(jsonGame);
					if (g != null) {
						if (get(jsonGame, "id").equals(upToID)) {
							break while_loop;
						}
						result.add(g);						
					}
				}				
				if (get(json, "nextPage") != null) {
					page++;
					pause(2);
				} else {
					page = -1;
				}
			}			
			System.out.println("finished at " + new Date());
		} catch (IOException ioEx) {
			throw new RuntimeException(ioEx);		
		} finally {
			try {
				if (is != null) is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}		
			
		}
		return result;
	}
	
	private static void pause(int seconds) {
		try {
			Date d = new Date(System.currentTimeMillis() + seconds * 1000);
			System.out.println("pausing until " + d);
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	private static String get(JsonObject o, String element) {		
		try {
			String[] token = element.split("\\.");		
			for (int i = 0; i < token.length; i++) {
				if (i == token.length-1) {
					JsonElement el = o.get(token[i]); 
					return el == null || el.isJsonNull() ? null : el.getAsString();
				} else {
					// drill down
					o = o.getAsJsonObject(token[i]);
				}
			}
			return null;
		} catch (Exception ex) {
			return element;
		}
	}
	
	private static Game toGame(JsonObject lichessGame) {
		if (!"standard".equalsIgnoreCase(get(lichessGame, "variant"))) return null;
		try {
			Game result = new Game(new Position());
			String moves = get(lichessGame, "moves");	
			if (moves == null || moves.trim().isEmpty()) return null; //ignore empty games
			for (String move : moves.split(" ")) {
				result.addMove(move);
			}		
			result.addTag("Event", toPGNEvent(get(lichessGame, "rated")));
			result.addTag("Site", "https://lichess.org/" + get(lichessGame, "id"));
			result.addTag("Date", PGNReader.toPGNTimestamp(get(lichessGame, "lastMoveAt")));
			result.addTag("White", get(lichessGame, "players.white.userId"));
			result.addTag("Black", get(lichessGame, "players.black.userId"));
			result.addTag("Result", toPGNResult(lichessGame));
			result.addTag("WhiteElo", get(lichessGame, "players.white.rating"));
			result.addTag("BlackElo", get(lichessGame, "players.black.rating"));
			result.addTag("PlyCount", get(lichessGame, "turns"));
			result.addTag("TimeControl", get(lichessGame, "clock.initial") + "+" + get(lichessGame, "clock.increment"));
			result.addTag("Termination", toPGNTermination(get(lichessGame, "status")));
			result.addTag("LichessID", get(lichessGame, "id"));
			return result;
		} catch (Exception ex) {
			System.err.println("failed to parse game " + get(lichessGame, "url"));
			return null;
		}
	}
	
	
	private static String toPGNEvent(String rated) {
		if ("true".equalsIgnoreCase(rated)) return "Rated game";		
		return "unrated game";
	}
	
	
	
	private static String toPGNResult(JsonObject lichessGame) {
		String winner = get(lichessGame, "winner");
		String status = get(lichessGame, "status");
		if ("white".equalsIgnoreCase(winner)) return "1-0";
		if ("black".equalsIgnoreCase(winner)) return "0-1";
		if ("draw".equalsIgnoreCase(status)
			|| "outoftime".equalsIgnoreCase(status)
			|| "timeout".equalsIgnoreCase(status)) return "1/2-1/2";
		return "*";
	}
	
	private static String toPGNTermination(String status) {
		if ("timeout".equalsIgnoreCase(status)) return "time forfeit";
		if ("outoftime".equalsIgnoreCase(status)) return "time forfeit";
		return "normal";
	}
	
	public static void main(String[] args) throws Exception {
		
		String lichessUser = "madi1693";
		List<Game> games = Lichess.downloadGames(lichessUser,
				null,				
				new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), // from )
				null, // to
				false, // whiteGames
				true, // blackGames
				new String[] {"blitz","classical","unlimited"}, // speed
				new String[] {"Nf3", "c4"}); // moves  //new String[] {"e4 e6", "e3"}
		
		Game.saveToFile(new File(System.getProperty("user.home") + "/Downloads", lichessUser + ".pgn"), 
				true, games);
			
	}
	
	

}
