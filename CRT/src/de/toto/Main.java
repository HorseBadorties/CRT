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
//		String[] moves = {"e2-e4", "d7-d5", "e4xd5", "e7-e6", "d5xe6", "Bf8-b4", "e6xf7+", "Ke8-e7", "f7xg8=N+"};
		String[] moves = {"Ng1-f3", "d7-d5", "c2-c4", "e7-e6", "g2-g3", "Ng8-f6", "Bf1-g2", "Bf8-e7", "0-0"};
//		String[] moves = new String[5000];
//		for (int i = 0; i < moves.length; i++) {
//			moves[i] = i % 2 != 0 ? "e4-e2" : "e2-e4";
//		}
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
