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
		String[] moves = {"d2-e4", "d7-d5", "e4xd5", "e7-e6", "d5xe6", "Bf8-b4", "e6xf7+", "Ke8-e7", "f7xg8=N+"};
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
