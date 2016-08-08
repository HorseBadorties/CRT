package de.toto;


import de.toto.game.Game;
//import chesspresso.game.Game;
//import chesspresso.pgn.PGNReader;
//import chesspresso.pgn.PGNSimpleErrorHandler;
import de.toto.gui.AppFrame;

public class Main {

	public static Game game;
	
	public static void main(String[] args) {
		
		// TODO remove
//		try {
//			String f = "C:\\Users\\Torsten\\Documents\\Adrian-Geisen.pgn"; //Adrian-Geisen.pgn
//			PGNReader reader = new PGNReader(f);
//            reader.setErrorHandler(new PGNSimpleErrorHandler(System.out));
//            game = reader.parseGame();
//            game.gotoStart();
//
//	
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		
		game = new Game();
		game.start();
		game.addMove("d4", "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq - 0 1");
		game.addMove("Nf6", "rnbqkb1r/pppppppp/5n2/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 2");
		game.addMove("c4", "rnbqkb1r/pppppppp/5n2/8/2PP4/8/PP2PPPP/RNBQKBNR b KQkq - 0 2");
		game.addMove("c5", "rnbqkb1r/pp1ppppp/5n2/2p5/2PP4/8/PP2PPPP/RNBQKBNR w KQkq - 0 3");
		game.addMove("d5", "rnbqkb1r/pp1ppppp/5n2/2pP4/2P5/8/PP2PPPP/RNBQKBNR b KQkq - 0 3");
		game.addMove("b5", "rnbqkb1r/p2ppppp/5n2/1ppP4/2P5/8/PP2PPPP/RNBQKBNR w KQkq - 0 4");
		game.addMove("cxb5", "rnbqkb1r/p2ppppp/5n2/1PpP4/8/8/PP2PPPP/RNBQKBNR b KQkq - 0 4");
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
