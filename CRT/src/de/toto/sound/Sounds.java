package de.toto.sound;

import java.net.URL;

import javax.sound.sampled.*;


public class Sounds {
	
	private static Clip move;
	private static Clip capture;
	private static Clip castle;
	private static Clip wrong;
	
	static {
		move = loadClip(Sounds.class.getResource("/sounds/lichess/Move.wav"));
		capture = loadClip(Sounds.class.getResource("/sounds/lichess/Capture.wav"));
		castle = loadClip(Sounds.class.getResource("/sounds/lichess/Move.wav"));
		wrong = loadClip(Sounds.class.getResource("/sounds/BlitzIn/ALERT.WAV"));
	}
	
	private static Clip loadClip(URL url) {
		try {
	        Clip clip = AudioSystem.getClip();
	        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
	        clip.open(ais);
	        return clip;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private static void playClip(Clip c) {
		if (c != null) {
			c.stop();
			c.setFramePosition(0);
			c.start();
		}
	}
	
	public static void move() {
		playClip(move);
	}
	
	public static void capture() {
		playClip(capture);
	}
	
	public static void castle() {
		playClip(castle);
	}
	
	public static void wrong() {
		playClip(wrong);
	}

}
