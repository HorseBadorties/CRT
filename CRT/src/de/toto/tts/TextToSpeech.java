package de.toto.tts;

import java.util.Set;

import javax.sound.sampled.*;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.*;
import marytts.util.data.audio.AudioPlayer;

public class TextToSpeech {

	private MaryInterface marytts;
    private AudioPlayer ap;
	
    public TextToSpeech() throws MaryConfigurationException {
        marytts = new LocalMaryInterface();
        Set<String> voices = marytts.getAvailableVoices();
        if (!voices.isEmpty()) {
        	marytts.setVoice(marytts.getAvailableVoices().iterator().next());
        }
        ap = new AudioPlayer();   
        
    }
    
    public void setVoice(String voiceName) {
    	marytts.setVoice(voiceName);
    }
    
    public Set<String> getAvailableVoices() {
    	return marytts.getAvailableVoices();
    }

    public void say(String input) throws Exception {    	
    	AudioInputStream audio = marytts.generateAudio(input);    	
    	Clip clip = AudioSystem.getClip();
    	clip.open(audio);
    	clip.start();
    	clip.drain();
//    	ap.setAudio(audio);
//    	ap.start();       
    	System.out.println("I said: '" + input + "'");
    }
    
    /**
     * Announces a single Long Algebraic Notation chess move
     */
    
    public void sayChessMove(String move) {
    	try {
	    	StringBuilder input = new StringBuilder();
	    	if (move.startsWith("0-0-0")) {
	    		input.append("long castles");
	    	} else if (move.startsWith("0-0")) {
	    		input.append("short castles");	    		
	    	} else {
	    		String splitCharacter = move.contains("-") ? "-" : "x";
	    		String[] moveParts = move.split(splitCharacter);
	    		String from = moveParts[0];
	    		String to = moveParts[1];
	    		String piece = translatePiece(from.charAt(0));
	    		from = from.charAt(from.length()-2) + " " + from.charAt(from.length()-1);
	    		String toOrTakes = splitCharacter.equals("-") ? "to" : "takes";
	    		to = to.charAt(0) + " " + to.charAt(1);
	    		input.append(piece).append(" ").append(from).append(" ").append(toOrTakes).append(" ").append(to);
	    		
	    	}	    	
	    	promotion(move, input);
	    	checkOrMate(move, input);
	    	say(input.toString());
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    private void promotion(String move, StringBuilder input) {
    	if (move.contains("=")) {
    		Character promotionPiece = move.charAt(move.indexOf("=") + 1);
    		input.append(" and promotes to ").append(translatePiece(promotionPiece));
    	}
    	input.append(".");
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
			TextToSpeech tts = new TextToSpeech();
			System.out.println(tts.getAvailableVoices());
			tts.setVoice(tts.getAvailableVoices().iterator().next());
			tts.sayChessMove("0-0-0");
			tts.sayChessMove("0-0-0#");
			tts.sayChessMove("0-0+");
			tts.sayChessMove("Ng1xf3+");
			tts.sayChessMove("g7xh8=R+");
			tts.sayChessMove("g7-g8=Q#");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

	}

    
}
