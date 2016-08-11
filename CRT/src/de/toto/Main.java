package de.toto;


import java.io.File;
import java.util.List;

import de.toto.game.Game;
//import chesspresso.game.Game;
//import chesspresso.pgn.PGNReader;
//import chesspresso.pgn.PGNSimpleErrorHandler;
import de.toto.gui.AppFrame;
import de.toto.pgn.PGNReader;

public class Main {

	public static Game game;
	
	public static void main(String[] args) {
		
		File pgn = new File("C:/Users/080064/Downloads/test.pgn");
		List<Game> games = PGNReader.parse(pgn);
		if (!games.isEmpty()) {
			game = games.get(0);
		} else {
			game = new Game();
		}
		game.gotoStartPosition();
		
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
