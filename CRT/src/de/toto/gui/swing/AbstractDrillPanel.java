package de.toto.gui.swing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.toto.game.Square;

public abstract class AbstractDrillPanel extends JPanel {
	
	protected AppFrame appFrame;
	protected List<Square> allSquares = new ArrayList<Square>(64);	
	protected Random random = new Random();
	protected JButton btnFirst, btnSecond;
	protected JTextField textfield;
	protected int counter, correctCounter;	
	
	public AbstractDrillPanel(AppFrame appFrame) {
		this.appFrame = appFrame;
		Square[][] squares8x8 = Square.createEmpty8x8();
		for (int rank = 1; rank <= 8; rank++) {				
			for (int file = 1; file <= 8; file++) {
				allSquares.add(squares8x8[rank - 1][file - 1]);
			}
		}
		btnFirst = new JButton(getFirstAction());
		btnSecond = new JButton(getSecondAction());
		textfield = new JTextField(15);
		textfield.setEditable(false);
		
		add(textfield);
		add(btnFirst);
		add(btnSecond);	
		
		KeyStroke keyW = KeyStroke.getKeyStroke(getFirstKeyCode(), 0);
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyW, "first");		
		this.getActionMap().put("first",getFirstAction());
		KeyStroke keyB = KeyStroke.getKeyStroke(getSecondKeyCode(), 0);
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyB, "second");		
		this.getActionMap().put("second", getSecondAction());
			
	}
	
	public void setText(String text, Color color) {
		textfield.setForeground(color);
		textfield.setText(text);
	}
	
	public abstract int getFirstKeyCode();
	public abstract int getSecondKeyCode();
	public abstract Action getFirstAction();
	public abstract Action getSecondAction();
	
}
