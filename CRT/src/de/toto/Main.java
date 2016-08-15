package de.toto;


import java.io.File;
import java.util.List;

import javax.swing.UIManager;

import de.toto.game.Game;
import de.toto.gui.AppFrame;
import de.toto.pgn.PGNReader;

public class Main {

	public static Game game;
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch (Exception ex) {
	       ex.printStackTrace();
		}
		
		File pgn = new File("C:/Users/080064/Downloads/Repertoire.pgn"); // C:/Users/Torsten/Documents/Repertoire.pgn
		List<Game> games = PGNReader.parse(pgn);
		int positionCount = 0;
		for (Game g : games) {
			positionCount += g.getAllPositions().size();
		}
		System.out.println(String.format("Successfully parsed %d games with %d positions", games.size(), positionCount));
		
		Game repertoire = games.get(0);
		games.remove(repertoire);
		for (Game game : games) {
			System.out.println("merging " + game);
			repertoire.mergeIn(game);
		}
		
		System.out.println(String.format("merged games to %d positions ", repertoire.getAllPositions().size()));
		
		game = repertoire;
		
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	showFrame();
            }
        });
	}
	
	private static void showFrame() {
		AppFrame frame = new AppFrame(game);		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
