package de.toto;


import de.toto.game.Game;
//import chesspresso.game.Game;
//import chesspresso.pgn.PGNReader;
//import chesspresso.pgn.PGNSimpleErrorHandler;
import de.toto.gui.AppFrame;

public class Main {

	public static Game game;
	
	public static void main(String[] args) {
		
		game = new Game();
		game.start();
		String[] moves = {"d2-d4", "Ng8-f6", "c2-c4", "c7-c5", "d4-d5", "b7-b5", "c4xb5", "a7-a6"};
		game.addMoves(moves);
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
