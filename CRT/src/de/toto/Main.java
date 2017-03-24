package de.toto;

import javax.swing.UIManager;

import de.toto.google.GoogleDrive;
import de.toto.gui.swing.AppFrame;

public class Main {

	
	public static void main(String[] args) {
		if (NetworkConfig.atWork()) {
			try {
				GoogleDrive.downloadPGNs(new java.io.File("C:/Users/080064/Downloads"));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
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
