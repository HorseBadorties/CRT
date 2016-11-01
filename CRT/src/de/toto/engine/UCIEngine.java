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
	private String fen;
	private String name;
	
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
	
	private void fireEngineMoved(String engineMove) {
		for (EngineListener l : listener) {
			l.engineMoved(engineMove);
		}
	}
	
	public void start() {
		if (isStarted()) return;
		try {
			process = new ProcessBuilder(pathToEngine).start();
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			writer = new OutputStreamWriter(process.getOutputStream());			
			sendCommand("uci");
			String line = null;
			boolean idReceived = false;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("id name")) {
					idReceived = true;
					name = line.substring(7, line.length()).trim();
					break;
				}
			}
			if (!idReceived) {
				throw new RuntimeException("process did not send an ID token - it's not a valid UCI engine");
			}
			sendCommand("setoption name Skill Level value 1");
			outputListener = new OutputReader(this);
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			try {
				stop();
			} catch (Exception ignore) {}
			if (!(e instanceof RuntimeException)) {
				throw new RuntimeException(e.getLocalizedMessage(), e);
			} else {
				throw (RuntimeException)e;
			}
		}	
	}
	
	public String getName() {
		return name;
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
			if (outputListener != null) outputListener.stop();
			reader.close();
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			process = null;
		}
		
	}
	
	public void setFEN(String newFEN) {
		if (!newFEN.equals(this.fen)) {
			this.fen = newFEN;
			sendCommand("stop");
			sendCommand("position fen " + newFEN);
			sendCommand("go infinite");
		}
	}
	
	public void setFENandMove(String newFEN) {
		if (!newFEN.equals(this.fen)) {
			this.fen = newFEN;
			//sendCommand("stop");
			sendCommand("position fen " + newFEN);
			sendCommand("go");
		}
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
					} else if (line.startsWith("bestmove")) {
						engine.fireEngineMoved(line.split(" ")[1]);
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	// C:\Program Files\Stockfish\\stockfish 7 x64.exe
	// C:\\Scid vs PC-4.12\\bin\\engines\\toga\\TogaII.exe	
	// C:\\Scid vs PC-4.12\\bin\\engines\\komodo-8_2d3f23\\Windows\\komodo-8-64bit.exe
	public static void main(String[] args) {
		UCIEngine engine = new UCIEngine("C:\\Program Files\\Stockfish\\stockfish 7 x64.exe");		
		try {
			engine.addEngineListener(new EngineListener() {

				@Override
				public void newEngineScore(Score s) {
					System.out.println("*** Score: *** " + s.toString());					
				}

				@Override
				public void engineMoved(String engineMove) {
					System.out.println("Engine moved: " + engineMove);		
					
				}
				

			});
			engine.start();
			engine.setFENandMove("r2qkb1r/pQnbpppp/8/2p5/3n4/2N3P1/PP1PPPBP/R1B1K1NR w KQkq - 1 9");
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
