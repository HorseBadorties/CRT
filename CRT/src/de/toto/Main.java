package de.toto;


import de.toto.game.Game;
//import chesspresso.game.Game;
//import chesspresso.pgn.PGNReader;
//import chesspresso.pgn.PGNSimpleErrorHandler;
import de.toto.gui.AppFrame;

public class Main {

	public static Game game;
	
	public static void main(String[] args) {
		
//		String pgn = "1.e4 c5 2.Nc3 Nc6 3.g3 g6 4.Bg2 Bg7 5.d3 d6 6.Be3 e5 7.Qd2 Nge7 8.Bh6 Bxh6 9.Qxh6 Nd4 10.Qd2 Qa5 11.Nge2 Bg4 12.Nxd4 cxd4 13.Nd5 Qxd2+ 14.Kxd2 Nxd5 15.exd5 Bd7 16.c3 dxc3+ 17.bxc3 Kd8 18.Rhf1 f5 19.f4 Rc8 20.Rfe1 Re8 21.Re2 Rc5 22.Rb1 b6 23.Rb2 Ra5 24.c4 Kc7 25.Bf1 Ra3 26.Re3 h6 27.Kc1 g5 28.Rbe2 gxf4 29.gxf4 Rg8 30.Kb2 Ra4 31.fxe5 f4 32.exd6+ Kxd6 33.Re4 b5 34.Rxf4 bxc4 35.dxc4 Kc5 36.Re3 Rg1 37.Rf7 Rh1 38.Rxd7 Rxf1 39.Rc7+ Kd4 40.Re2 Rb4+ 41.Ka3 a5 42.Rb2 Kc3 43.Rg2 Rf6 44.Rg3+ Kd4 45.Rc6 Rxc6 46.dxc6 Rb8 47.Rg6 Kxc4 48.Rxh6 Kb5 49.c7 Rc8 50.Rh7 Kb6 51.h4 Kb7 52.Ka4 Rf8 53.h5 Rf5 54.Kb3 Rc5 55.h6 Rh5 56.Kc4 Kc8 57.Kd4 Rh2 58.Ke5 Kb7 59.Kf6 Rxa2 60.Rh8 Rf2+ 61.Ke5";
		String pgn = "1.e4 c6 2.e5 d5 3.exd6 Nf6 ";
		game = new Game();
		game.start();
//		String[] moves = {"e2-e4", "d7-d5", "e4xd5", "e7-e6", "d5xe6", "Bf8-b4", "e6xf7+", "Ke8-e7", "f7xg8=N+"};
//		String[] moves = {"Nf3", "d5", "c4", "e6", "g3", "Nf6", "Bg2", "Be7", "0-0"};
//		game.addMoves(moves);
		for (String move : pgn.split(" ")) {
			int dotPosition = move.indexOf(".");
			String m = dotPosition >= 0 ? move.substring(dotPosition+1, move.length()) : move;
			game.addMove(m);
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
