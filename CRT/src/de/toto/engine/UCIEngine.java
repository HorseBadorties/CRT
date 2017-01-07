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
	private boolean announcesBestMove = false;
	private int skillLevel = 8;
	private int multiPV = 1;
	private int threadCount = 1;
	private boolean gameStarted = false;
	private volatile boolean isThinking = false;
	
	private static final int[] MOVETIMES = {50, 100, 150, 200, 400, 800, 1600, 3200, 6400 };
	private static final int[] DEPTHS = {1, 3, 5, 8, 11, 15, 18, 22, 26};
	
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
			l.newEngineScore(this, s);
		}
	}
	
	private void fireEngineMoved(String fen, String engineMove) {
		for (EngineListener l : listener) {
			l.engineMoved(this, fen, engineMove);
		}
	}
	
	private void fireEngineStopped() {
		for (EngineListener l : listener) {
			l.engineStopped(this);
		}
	}
	
	public synchronized void start() {
		if (isStarted()) return;
		try {
			process = new ProcessBuilder(pathToEngine).start();
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			writer = new OutputStreamWriter(process.getOutputStream());			
			sendCommand("uci");
			String line = null;
			boolean idReceived = false;
			boolean uciokReceived = false;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("id name")) {
					idReceived = true;
					name = line.substring(7, line.length()).trim();
				} else if (line.startsWith("uciok")) {
					uciokReceived = true;
					break;
				}
			}
			if (!idReceived) {
				throw new RuntimeException("process did not send an ID token - it's not a valid UCI engine");
			}
			if (!uciokReceived) {
				throw new RuntimeException("process did not send 'uciok'");
			}
			sendCommand("isready");
			setMultiPV(multiPV);
			setThreadCount(threadCount);
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
	
	public synchronized boolean isStarted() {
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

	public synchronized void stop() {
		if (!isStarted()) return;
		try {
			sendCommand("stop");
			sendCommand("quit");
			if (outputListener != null) outputListener.stop();
			reader.close();
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			process = null;
			fireEngineStopped();
		}		
	}
	
	public synchronized void setFEN(String newFEN) {
		if (!newFEN.equals(this.fen)) {
			this.announcesBestMove = false;
			this.fen = newFEN;
			sendCommand("stop");
			sendCommand("position fen " + newFEN);
			sendCommand("go infinite");
		}
	}
	
	public synchronized String getFEN() {
		return fen;
	}
	
	public void setMultiPV(int value) {
		if (value != multiPV) {
			multiPV = value;
			sendCommand("setoption name MultiPV value " + value);
			if (isStarted() && fen != null) {
				String _fen = fen;
				fen = null;
				setFEN(_fen);
			}
		}
	}
	
	public int getMultiPV() {
		return multiPV;
	}
	
	public void setThreadCount(int value) {
		threadCount = value;
		sendCommand("setoption name Threads value " + value);
		if (isStarted() && fen != null) {
			String _fen = fen;
			fen = null;
			setFEN(_fen);
		}
	}
	
	public int getThreadCount() {
		return threadCount;
	}
		
	private int translateSkillLevel() {
		int result = skillLevel * 20 / 7;
		return result > 20 ? 20 : result;
	}
	
	public int[] getAllSkillLevel() {
		return new int[]{1,2,3,4,5,6,7,8,9};
	}
	
	public void startGame(int skillLevel, String startFEN) {
		this.skillLevel = skillLevel;				
		if (!isStarted()) {
			start();
		}
		sendCommand("stop");
		sendCommand("setoption name Skill Level value " + translateSkillLevel());
		sendCommand("setoption name PersonalityFile value C:\\Program Files\\engines\\Rodent_II\\rodent.txt");
		sendCommand("setoption name GuideBookFile value C:\\Program Files\\engines\\Rodent_II\\books\\guide\\solid.bin");
		sendCommand("setoption name MainBookFile value C:\\Program Files\\engines\\Rodent_II\\books\\rodent.bin");
		sendCommand("ucinewgame");
		sendCommand("isready");
		setMultiPV(multiPV);
		setThreadCount(threadCount);
		sendCommand("position fen " + startFEN);
		announcesBestMove = true;
		gameStarted = true;
	}
			
	public void endGame() {
		if (isStarted()) {
			sendCommand("stop");
			sendCommand("setoption name Skill Level value 20");
			sendCommand("ucinewgame");
			sendCommand("isready");
			announcesBestMove = false;
			gameStarted = false;
		}
	}	
	
	public void move(String startFEN, String moves, String fen) {		
		if (isThinking) return;
		this.fen = fen;
		String positionCommand = String.format("position %s moves %s", 
				startFEN != null ? "fen " + startFEN : "startpos", moves);	
		sendCommand(positionCommand);
		sendCommand(String.format("go depth %d movetime %d", DEPTHS[skillLevel-1], MOVETIMES[skillLevel-1]));
		isThinking = true;
	}
	
	public boolean isThinking() {
		return isThinking;
	}
				
	private static class OutputReader implements Runnable {
		
		private UCIEngine engine;
		private volatile boolean isAlive = true; 		
		
		public OutputReader(UCIEngine engine) {
			super();
			this.engine = engine;
			new Thread(this, engine.getName()).start();			
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
					//log.info(line);
					Score newScore = Score.parse(engine.getFEN(), line);
					if (newScore != null) {
						engine.fireNewScore(newScore);
					} else if (line.startsWith("bestmove")) {
						if (engine.announcesBestMove) {
							engine.isThinking = false;
							engine.fireEngineMoved(engine.getFEN(), line.split(" ")[1]);
						} 
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
}
