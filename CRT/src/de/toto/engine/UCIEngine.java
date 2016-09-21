package de.toto.engine;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UCIEngine {

	private String pathToEngine;
	private Process process;
	private BufferedReader reader;
	private OutputStreamWriter writer;	
	private OutputReader outputListener;
	private List<EngineListener> listener = new ArrayList<EngineListener>();
	
	private static Logger log = Logger.getLogger("UCIEngine");

	public UCIEngine(String pathToEngine) {
		this.pathToEngine = pathToEngine;
	}
	
	public void addEngineListener(EngineListener newListener) {
		if (!listener.contains(newListener)) {
			listener.add(newListener);
		}		
	}
	
	public void removeEngineListener(EngineListener newListener) {
		listener.remove(newListener);				
	}

	private void fireNewScore(Score s) {
		for (EngineListener l : listener) {
			l.newEngineScore(s);
		}
	}
	
	public void start() {
		if (isStarted()) return;
		try {
			process = new ProcessBuilder(pathToEngine).start();
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			writer = new OutputStreamWriter(process.getOutputStream());	
			outputListener = new OutputReader(this);	
			sendCommand("uci");
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			if (!(e instanceof RuntimeException)) {
				throw new RuntimeException(e.getLocalizedMessage(), e);
			} else {
				throw (RuntimeException)e;
			}
		}	
	}
	
	public boolean isStarted() {
		return process != null;
	}

	/**
	 * Takes in any valid UCI command and executes it
	 * 
	 * @param command
	 */
	private void sendCommand(String command) {
		try {
			writer.write(command + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		if (!isStarted()) return;
		try {
			sendCommand("quit");
			outputListener.stop();
			reader.close();
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			process = null;
		}
		
	}
	
	public void setFEN(String fen) {
		sendCommand("stop");
		sendCommand("position fen " + fen);
		sendCommand("go infinite");
	}
	
	private static class OutputReader implements Runnable {
		
		private UCIEngine engine;
		private volatile boolean isAlive = true; 
		
		public OutputReader(UCIEngine engine) {
			super();
			this.engine = engine;
			new Thread(this).start();			
		}
		
		public void stop() {
			isAlive = false;
		}

		@Override
		public void run() {
			while (isAlive) {
				readEngineOutput();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
		}
		
		private void readEngineOutput() {			
			try {
				String line = null;
				while (isAlive && (line = engine.reader.readLine()) != null) {
					Score newScore = Score.parse(line);
					if (newScore != null) {
						engine.fireNewScore(newScore);
					}										
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	// C:\\Scid vs PC-4.12\\bin\\engines\\stockfish\\stockfish 7 x64.exe
	// C:\\Scid vs PC-4.12\\bin\\engines\toga\\TogaII.exe	
	public static void main(String[] args) {
		UCIEngine engine = new UCIEngine("C:\\Scid vs PC-4.12\\bin\\engines\\stockfish\\stockfish 7 x64.exe");		
		try {
			engine.addEngineListener(new EngineListener() {

				@Override
				public void newEngineScore(Score s) {
					System.out.println(s.toString());					
				}
				
			});
			engine.start();
			engine.setFEN("r2qkb1r/pQnbpppp/8/2p5/3n4/2N3P1/PP1PPPBP/R1B1K1NR w KQkq - 1 9");
			try {
				Thread.sleep(50000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} finally {
			if (engine != null) engine.stop();
		}
	}
}
