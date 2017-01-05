package de.toto.tts;

import java.util.Set;

import de.toto.game.Position;

public interface TextToSpeach {

	void setVoice(String voiceName);
	
	Set<String> getAvailableVoices();

	void say(String input) throws Exception;

	/**
	 * Announces a single Long Algebraic Notation chess move
	 */

	void announceChessMove(String move);

	/**
	 * Announces a single Short Algebraic Notation chess move
	 */

	void announcePosition(Position p);

}