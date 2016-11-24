package de.toto.engine;

public interface EngineListener {
	public void newEngineScore(UCIEngine e, Score s);
	public void engineMoved(UCIEngine e, String fen, String engineMove);
	public void engineStopped(UCIEngine e);
}
