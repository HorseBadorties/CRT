package de.toto.tts;

import java.util.*;

import javax.sound.sampled.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.toto.game.Position;
import de.toto.game.Rules.PieceType;
import de.toto.game.Square;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.*;

public class MaryTTS implements TextToSpeach {
	
	static {
		Logger.getRootLogger().getLoggerRepository().resetConfiguration();
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(Level.WARN);
		console.activateOptions();
		Logger.getRootLogger().addAppender(console);
	}

	private MaryInterface marytts;
	
    public MaryTTS() throws MaryConfigurationException {
        marytts = new LocalMaryInterface();
        try {
        	setVoice("dfki-prudence-hsmm");
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
    
    /* (non-Javadoc)
	 * @see de.toto.tts.TextToSpeach#setVoice(java.lang.String)
	 */
    @Override
	public void setVoice(String voiceName) {
    	marytts.setVoice(voiceName);
    }
    
    //"dfki-prudence-hsmm", "cmu-slt-hsmm"
    /* (non-Javadoc)
	 * @see de.toto.tts.TextToSpeach#getAvailableVoices()
	 */
    @Override
	public Set<String> getAvailableVoices() {
    	return marytts.getAvailableVoices();
    }

    /* (non-Javadoc)
	 * @see de.toto.tts.TextToSpeach#say(java.lang.String)
	 */
    @Override
	public void say(String input) throws Exception {    	
    	AudioInputStream audio = marytts.generateAudio(input);    	
    	Clip clip = AudioSystem.getClip();
    	clip.open(audio);
    	clip.start();
    	clip.drain();     
    	System.out.println("I said: '" + input + "'");
    }
    
    /* (non-Javadoc)
	 * @see de.toto.tts.TextToSpeach#announceChessMove(java.lang.String)
	 */    
    @Override
	public void announceChessMove(String move) {
    	try {
	    	StringBuilder input = new StringBuilder();
	    	if (move.startsWith("0-0-0")) {
	    		input.append("long castles");
	    	} else if (move.startsWith("0-0")) {
	    		input.append("short castles");	    		
	    	} else {
	    		for (int i = 0; i < move.length(); i++) {
	    			char c = move.charAt(i);
	    			if (i == 0) {
	    				if (c >= 'A' && c <= 'Z') {	    			
	    					input.append(translatePiece(c)).append(" ");
	    				} else {
	    					input.append(c).append(" ");
	    				}
	    			} else if ((c >= 'a' && c <= 'h') ||  (c >= '1' && c <= '8')) {	    				
	    				input.append(c).append(" ");	    					    				
	    			} else if (c == 'x') {
    					input.append("takes ");
    				} 
	    		}	    		
	    	}	    	
	    	promotion(move, input);
	    	checkOrMate(move, input);
	    	say(input.toString());
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    /* (non-Javadoc)
	 * @see de.toto.tts.TextToSpeach#announcePosition(de.toto.game.Position)
	 */
    
    @Override
	public void announcePosition(Position p) {
    	try {
	    	StringBuilder input = new StringBuilder();
	    	// Who's to move?
	    	input.append(p.isWhiteToMove() ? "White" : "Black").append(" to move.\n");
	    	// Pawn and piece count
	    	input.append(String.format("White has %d pawns and %d pieces.\n",	    			
	    			countPawns(p, true), countPieces(p, true)));
	    	input.append(String.format("Black has %d pawns and %d pieces.\n",	    			
	    			countPawns(p, false), countPieces(p, false)));
	    	// White's Pawns
	    	input.append("White pawns on ");	    
	    	for (Square s : getSquaresWithPiecesByColor(p, true, PieceType.PAWN)) {
	    		input.append(s.getName() + ", ");	
	    	}
	    	input.replace(input.length()-2, input.length()-1, ".\n");
	    	// White's Pieces
	    	for (Square s : getSquaresWithPiecesByColor(p, true, 
	    			new PieceType[] {PieceType.KING, PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT})) {
	    		input.append("White " + translatePiece(s.piece.pgnChar) + " on " + s.getName() + ".\n");	
	    	}
	    	input.append("\n");
	    	// Black's Pawns
	    	input.append("Black pawns on ");	    
	    	for (Square s : getSquaresWithPiecesByColor(p, false, PieceType.PAWN)) {
	    		input.append(s.getName() + ", ");	
	    	}
	    	input.replace(input.length()-2, input.length()-1, ".\n");
	    	// Black's Pieces
	    	for (Square s : getSquaresWithPiecesByColor(p, false, 
	    			new PieceType[] {PieceType.KING, PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT})) {
	    		input.append("White " + translatePiece(s.piece.pgnChar) + " on " + s.getName() + ".\n");	
	    	}
	    	say(input.toString());
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    private int countPawns(Position p, boolean white) {
    	return getSquaresWithPiecesByColor(p, white, new PieceType[] {PieceType.PAWN}).size();
    }
    
    private int countPieces(Position p, boolean white) {
    	return getSquaresWithPiecesByColor(p, white, 
    			new PieceType[] {PieceType.KING, PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT}).size();
    }
    
    private List<Square> getSquaresWithPiecesByColor(Position p, boolean white, PieceType... pieceTypes) {
    	List<Square> result = new ArrayList<Square>();
    	for (Square s : p.getSquaresWithPiecesByColor(white)) {
    		if (Arrays.asList(pieceTypes).contains(s.piece.type)) result.add(s);
    	}
    	return result;
    }
    
    private void promotion(String move, StringBuilder input) {
    	if (move.contains("=")) {
    		Character promotionPiece = move.charAt(move.indexOf("=") + 1);
    		input.append(" promotes to ").append(translatePiece(promotionPiece));
    	}
    	input.append("!");
    }
    
    private String translatePiece(Character pieceCharacter) {
    	if ('K' == pieceCharacter) {
			return "King";	  
    	} else if ('Q' == pieceCharacter) {
			return "Queen";	  
		} else if ('R' == pieceCharacter) {
			return "Rook";	  
		} else if ('B' == pieceCharacter) {
			return "Bishop";	   
		} else if ('N' == pieceCharacter) {
			return "Knight";	   
		} else return "Pawn";
    }
    
    private void checkOrMate(String move, StringBuilder input) {
    	if (move.endsWith("#")) {
    		input.append(" Mate!");
    	} else if (move.endsWith("+")) {
    		input.append(" Check.");
    	} 
    }
        
    public static void main(String[] args) {    	
    	try {
			TextToSpeach tts = new MaryTTS();
			System.out.println(tts.getAvailableVoices());
			tts.setVoice(tts.getAvailableVoices().iterator().next());
//			tts.announceChessMove("0-0-0");
//			tts.announceChessMove("0-0-0#");
//			tts.announceChessMove("0-0+");
			tts.announceChessMove("Nf3");
			tts.announceChessMove("Nxf3+");
			tts.announceChessMove("gxh8=R+");
			tts.announceChessMove("g8=Q#");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

	}

    
}
