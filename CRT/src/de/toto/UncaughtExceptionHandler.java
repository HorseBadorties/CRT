package de.toto;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
	
	private Component parent;
	

	public UncaughtExceptionHandler(Component parent) {
		super();
		this.parent = parent;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		JTextArea ta = new JTextArea(sw.toString(), 20, 100);		
//        ta.setLineWrap(true);
        ta.setOpaque(false);
        ta.setBorder(null);
        ta.setEditable(false);
        ta.setFocusable(false);
		JOptionPane.showMessageDialog(parent, new JScrollPane(ta), 
				"uncaught exception on Thread " + t.getName(),
				JOptionPane.ERROR_MESSAGE);
		
	}

}
