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
		
//		game = new Game();
//		game.start();
//		game.addMove("nn", "6k1/8/8/8/8/5PR1/8/1rn1R1K1 w - - 0 1"); //"6k1/5ppp/8/8/8/6R1/5PPP/1r2R1K1 w - - 0 1"); 
//		game.addMove("Re3"); //Ne4 
//		game.addMove("nn", "rnbqk1nr/pppp1ppp/8/4p3/1b6/2N5/PPP2PPP/R1BQKBNR w KQkq - 0 1"); 
//		game.addMove("Ne4");
		
		System.out.println("Number of positions: " + game.getAllPositions().size());
		
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
