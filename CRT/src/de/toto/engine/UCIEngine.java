package de.toto.engine;

import java.io.*;
import java.util.logging.Logger;

public class UCIEngine {

	private String pathToEngine;
	private Process process;
	private BufferedReader reader;
	private OutputStreamWriter writer;	
	private EngineListener engineListener;
	
	private static Logger log = Logger.getLogger("UCIEngine");

	public UCIEngine(String pathToEngine) {
		this.pathToEngine = pathToEngine;
	}

	
	public void start() {
		if (isStarted()) return;
		try {
			process = new ProcessBuilder(pathToEngine).start();
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			writer = new OutputStreamWriter(process.getOutputStream());	
			engineListener = new EngineListener(this);			
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
	public void sendCommand(String command) {
		try {
			writer.write(command + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is generally called right after 'sendCommand' for getting the raw
	 * output from Stockfish
	 * 
	 * @param waitTime
	 *            Time in milliseconds for which the function waits before
	 *            reading the output. Useful when a long running command is
	 *            executed
	 * @return Raw output from Stockfish
	 */
	public String getOutput(int waitTime) {
		StringBuffer buffer = new StringBuffer();
		try {
			Thread.sleep(waitTime);
			sendCommand("isready");
			while (true) {
				String text = reader.readLine();
				if (text.equals("readyok"))
					break;
				else
					buffer.append(text + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}
	
	public void stop() {
		if (!isStarted()) return;
		try {
			sendCommand("quit");
			engineListener.stop();
			reader.close();
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			process = null;
		}
		
	}
	
	public void setFEN(String fen) {
		
	}

	/**
	 * Get the evaluation score of a given board position
	 * 
	 * @param fen
	 *            Position string
	 * @param waitTime
	 *            in milliseconds
	 * @return evalScore
	 */
	public float getEvalScore(String fen, int waitTime) {
		sendCommand("position fen " + fen);
		sendCommand("go movetime " + waitTime);

		float evalScore = 0.0f;
		String[] dump = getOutput(waitTime + 20).split("\n");
		for (int i = dump.length - 1; i >= 0; i--) {
			if (dump[i].startsWith("info depth ")) {
				try {
					evalScore = Float.parseFloat(dump[i].split("score cp ")[1].split(" nodes")[0]);
				} catch (Exception e) {
					evalScore = Float.parseFloat(dump[i].split("score cp ")[1].split(" upperbound nodes")[0]);
				}
			}
		}
		return evalScore / 100;
	}
	
	private static class EngineListener implements Runnable {
		
		private UCIEngine engine;
		private volatile boolean isAlive = true; 
		
		public EngineListener(UCIEngine engine) {
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
					if (!line.startsWith("info")) continue;
					int tokenScore = line.indexOf(" score cp ");
					if (tokenScore >= 0) {
						String value = line.substring(tokenScore, line.length()).split(" ")[3];
						System.out.println(Integer.parseInt(value) / 100);
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
			engine.start();
			engine.sendCommand("uci");
//			System.out.println(engine.getOutput(0));	
//			engine.sendCommand("ucinewgame");
			engine.sendCommand("position fen r2qkb1r/pQnbpppp/8/2p5/3n4/2N3P1/PP1PPPBP/R1B1K1NR w KQkq - 1 9");
//			System.out.println(engine.getOutput(1000));	
			engine.sendCommand("go infinite");
			try {
				Thread.sleep(50000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			System.out.println(engine.getOutput(1000));	
		} finally {
			if (engine != null) engine.stop();
		}
	}
}
