package de.toto.pgn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.toto.game.*;

public class PGNReader {
		
	private static final boolean DEBUG = true;
	
	public static List<Game> parse(File pgn) {	
		List<String> pgnLines = new ArrayList<String>();
		try {
			LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(pgn)));
			try {
				String line = null;
				for(;;) {
					line = reader.readLine();
					if (line != null) {
						pgnLines.add(line);
					} else break;
				}				
			} finally {
				reader.close();
			}
			return parse(pgnLines);
		} catch (Exception ex) {
			//TODO better error handling
			throw new RuntimeException("parsing PGN file failed", ex);
		}	
	}
	
	public static List<Game> parse(String pgn) {
		return parse(Arrays.asList(pgn.split("\\R")));
	}
	
	private static List<Game> parse(List<String> pgnLines) {
		List<Game> result = new ArrayList<Game>();
		if (pgnLines == null || pgnLines.isEmpty()) return result;
		// Handle first 3 ChessBase special characters
		if (pgnLines.get(0).startsWith("﻿")) {
			pgnLines.set(0, pgnLines.get(0).substring(3,pgnLines.get(0).length()));
		}
		boolean isBeginOfGame = true;
		StringBuilder movetext = null;
		Game currentGame = null;
		for (String _line : pgnLines) {			
			String line = _line.trim();
			if (line.isEmpty()) {
				continue;
			}
			if (isBeginOfGame) {				
				if (currentGame != null) {
					if (DEBUG) System.out.println("adding game " + currentGame);
					result.add(currentGame);
				}
				currentGame = new Game();
				currentGame.start();
				movetext = new StringBuilder();
				isBeginOfGame = false;
			}
			
			if (line.startsWith("[") && movetext.length() == 0) {
				line = line.replaceAll("\\[|\\]", ""); //strip "[" and "]"
				String name = line.substring(0, line.indexOf(" "));
				String value = line.substring(line.indexOf(" "), line.length()).trim().replace("\"", "");
				currentGame.addTag(name, value);							
			} else {
				movetext.append(line).append(" ");
				if (line.endsWith(currentGame.getTagValue("Result"))) {
					parseMovetext(movetext.toString(), currentGame);
					isBeginOfGame = true;
				}
			}
		}
		//add last game
		if (currentGame != null) {
			if (DEBUG) System.out.println("adding game " + currentGame);
			result.add(currentGame);
		}
		return result;
	}
	
	private static void parseMovetext(String movetext, Game game) {
		String expectedGameResult = game.getTagValue("Result");
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
			while (token.endsWith(")")) {
				endVariation++;
				token = token.substring(0, token.length()-1);
			}
			if (token.endsWith("}")) {
				if (!insideComment) throw new RuntimeException("comment-end token found, but comment-start missing");
				token = token.substring(0, token.length()-1);
				moveComment.append(token);
				insideComment = false;
				if (DEBUG && moveComment != null && moveComment.length() > 0) {
					System.out.println("adding comment " + moveComment + " at move " + game.getPosition().getMoveNumber());
				}
				game.getPosition().setComment( moveComment == null ? null : moveComment.toString());
				for (int i = 0; i < endVariation; i++) {				
					System.out.println("ending variation" + " at move " + game.getPosition().getMoveNumber());
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
				if (DEBUG) System.out.println("adding nag " + token + " at move " + game.getPosition().getMoveNumber());
				game.getPosition().addNag(token);
				for (int i = 0; i < endVariation; i++) {				
					System.out.println("ending variation" + " at move " + game.getPosition().getMoveNumber());
					game.endVariation();					
				}
				endVariation = 0;
				continue;
			}			
			//actual move
			if (startVariation) {
				if (DEBUG) System.out.println("adding variation " + token + " at move " + game.getPosition().getMoveNumber());
				game.newVariation(stripPossibleMoveNumber(token));
				startVariation = false;
			} else if (!token.isEmpty()) {
				if (DEBUG) System.out.println("adding move " + token + " at move " + game.getPosition().getMoveNumber());
				game.addMove(stripPossibleMoveNumber(token));
			}			
			for (int i = 0; i < endVariation; i++) {				
				System.out.println("ending variation" + " at move " + game.getPosition().getMoveNumber());
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
	
	public static void main(String[] args) {		
		File pgn = new File("C:/Users/080064/Downloads/Repertoire.pgn"); //Repertoire.pgn");
		List<Game> games = PGNReader.parse(pgn);
		System.out.println(games);
	}
	
	
}
