package de.toto;

import javax.swing.UIManager;

import de.toto.gui.swing.AppFrame;

public class Main {

	
	public static void main(String[] args) {
//		try {
//			for (javax.swing.UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//		        if ("Nimbus".equals(info.getName())) {
//		            UIManager.setLookAndFeel(info.getClassName());
//		            break;
//		        }
//			}
//	    } catch (Exception ex) {
	    	try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    } catch (Exception ex2) {
		       ex2.printStackTrace();
			}
//	    }
		
		
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	showFrame();
            }
        });
	}
	
	private static void showFrame() {
		AppFrame frame = new AppFrame();	
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(frame));
		frame.setVisible(true);
	}

}
