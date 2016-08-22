package de.toto;

import javax.swing.UIManager;

import de.toto.gui.AppFrame;

public class Main {

	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch (Exception ex) {
	       ex.printStackTrace();
		}
		
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	showFrame();
            }
        });
	}
	
	private static void showFrame() {
		AppFrame frame = new AppFrame();		
		frame.setVisible(true);
	}

}
