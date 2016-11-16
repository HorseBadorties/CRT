package de.toto.gui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.toto.engine.EngineListener;
import de.toto.engine.Score;
import de.toto.engine.UCIEngine;

public class EnginePanel extends JPanel implements EngineListener, ChangeListener {
	
	private AppFrame parent;
	private UCIEngine engine;
	private JSpinner multiPV;
	private JSpinner threads;
	private JList<String> listBestlines;
	private DefaultListModel<String> bestlines;
	
	public EnginePanel(AppFrame parent, UCIEngine engine) {
		this.parent = parent;
		this.engine = engine;
		multiPV = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
		multiPV.addChangeListener(this);			
		threads = new JSpinner(new SpinnerNumberModel(1, 1, 2, 1));
		threads.addChangeListener(this);
		threads.setFocusable(false);
		bestlines = new DefaultListModel<String>();
		bestlines.setSize(engine.getMultiPV());
		listBestlines = new JList<String>(bestlines);
		setLayout(new BorderLayout());
		JPanel pnlNorth = new JPanel();
		pnlNorth.add(new JLabel("<html><b>" + engine.getName() + "      </b></html>"));
		pnlNorth.add(new JLabel("Lines: "));
		pnlNorth.add(multiPV);
		pnlNorth.add(new JLabel("Threads: "));
		pnlNorth.add(threads);
		add(pnlNorth, BorderLayout.PAGE_START);
		add(new JScrollPane(listBestlines), BorderLayout.CENTER);
		setNonFocusable(this);
	}
	
	private static void setNonFocusable(Container c) {
		c.setFocusable(false);
		for (Component child : c.getComponents()) {
			child.setFocusable(false);
			if (child instanceof Container) {
				setNonFocusable((Container)child);
			}
		}
	}

	@Override
	public void newEngineScore(UCIEngine e, Score s) {	
		if (bestlines.size() >= s.multiPV) {
			boolean whiteToMove = parent.getCurrentPosition().isWhiteToMove();
			boolean positiveScore = (whiteToMove && s.score >= 0) || (!whiteToMove && s.score < 0);	
			String scoreText = String.format("%d [%s%.2f] %s", 
					s.depth, positiveScore ? "+" : "-", Math.abs(s.score), s.bestLine);			
			bestlines.set(s.multiPV - 1, scoreText);
		}
	}

	@Override
	public void engineMoved(UCIEngine e, String engineMove) {}
	
	@Override
	public void engineStopped(UCIEngine e) {
		bestlines.set(0, "<engine stopped>");
		for (int i = 1; i < bestlines.size(); i++) {
			bestlines.set(i, "");
		}
		
	}

	// a spinner changed
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == multiPV) {
			int newMultiPV = ((SpinnerNumberModel)multiPV.getModel()).getNumber().intValue();
			if (newMultiPV != bestlines.size()) {
				engine.setMultiPV(newMultiPV);			
				bestlines.setSize(newMultiPV);
			}
		} else if (e.getSource() == threads) {			
			engine.setThreadCount(((SpinnerNumberModel)threads.getModel()).getNumber().intValue());
			
		}		
	}

	
	
	
}
