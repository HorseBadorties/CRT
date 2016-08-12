package de.toto;


import java.io.File;
import java.util.List;

import de.toto.game.Game;
import de.toto.game.Position;
//import chesspresso.game.Game;
//import chesspresso.pgn.PGNReader;
//import chesspresso.pgn.PGNSimpleErrorHandler;
import de.toto.gui.AppFrame;
import de.toto.pgn.PGNReader;

public class Main {

	public static Game game;
	
	public static void main(String[] args) {
		
		File pgn = new File("C:/Users/080064/Downloads/Repertoire.pgn"); //Repertoire.pgn");
		List<Game> games = PGNReader.parse(pgn);
		int positionCount = 0;
		for (Game g : games) {
			positionCount += g.getAllPositions().size();
		}
		System.out.println(String.format("Successfully parsed %d games with %d positions", games.size(), positionCount));
		
		Game repertoire = games.get(0);
		games.remove(repertoire);
		while (!games.isEmpty()) {
			Game game = games.get(0);
			System.out.println("merging " + game);
			game.gotoStartPosition(); 
			repertoire.gotoStartPosition();
			
			Position first = repertoire.getPosition();
			Position second = game.getPosition();
			
			for (;;) {
				second = second.getNext();
				if (second == null) break;
				if (!first.hasVariation(second)) {
					first.addVariation(second);
					System.out.println(String.format("merged %s as variation of %s", second, first));
					break;
				} else {
					first = first.getVariation(second);
				}				
			}
			games.remove(game);			
		}
		
//		System.out.println(String.format("merged games to %d positions ", repertoire.getAllPositions().size()));
		
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
