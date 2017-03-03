package de.toto.db;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.toto.game.Game;
import de.toto.game.Position;

public class DB {
	
	private Connection con;
	
	public DB(String dbName) {
		try {
			con = DriverManager.getConnection(String.format("jdbc:hsqldb:file:%s", dbName), "SA", "");
			con.setAutoCommit(false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void close() {
		if (con != null) {
			try {
				try (Statement stmt = con.createStatement()) {
					stmt.execute("SHUTDOWN");
				}
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<Integer> listGames() {
		List<Integer> result = new ArrayList<Integer>();
		try (Statement stmt = con.createStatement()) {
			ResultSet rslt = stmt.executeQuery("select id from GAME");
			while (rslt.next()) {
				result.add(Integer.valueOf(rslt.getInt("ID")));
			} 
		} catch (SQLException ex) {
			ex.printStackTrace();
		}		
		return result;
	}
	
	public Game loadGame(int id) {
		Game result = null;
		try (Statement stmt = con.createStatement()) {
			ResultSet rslt = stmt.executeQuery(String.format("select * from GAME where ID = %d", id));
			if (rslt.next()) {
				PositionResult pr = loadPosition(rslt.getInt("STARTPOSITION"), null);
				result = new Game(pr.position);
				result.setDbId(rslt.getInt("ID"));
				result.addTag("White", rslt.getString("WHITE"));
				result.addTag("Black", rslt.getString("BLACK"));
				result.addTag("Result", rslt.getString("RESULT"));
				result.addTag("Event", rslt.getString("EVENT"));
				while (pr != null) {
					pr = loadPosition(pr.nextID, pr.position);
				}
			} 
		} catch (SQLException ex) {
			ex.printStackTrace();
		}		
		return result;
	}
		
	public void saveGame(Game g) {
		if (g.getDbId() > 0) {
			// game already saved
			return;
		}
		String insert = "INSERT INTO GAME (ID, DATE, WHITE, BLACK, RESULT, EVENT, STARTPOSITION) VALUES (?,?,?,?,?,?,?)";
		try (PreparedStatement stmt = con.prepareStatement(insert)) {
			// set dbID for all Positions
			int newPositionID = getNextID("POSITION", "ID");
			Position p = g.gotoStartPosition();
			while (p != null) {
				p.setDbId(newPositionID++);
				p = p.hasNext() ? p.getNext() : null;
			}
			// save all Positions
			p = g.gotoStartPosition();
			while (p != null) {
				savePosition(p);
				p = p.hasNext() ? p.getNext() : null;
			}
		
			int newID = getNextID("GAME", "ID");
			stmt.setInt(1, newID);
			stmt.setDate(2, fromPGNDate(g.getTagValue("Date")));
			stmt.setString(3, g.getTagValue("White"));
			stmt.setString(4, g.getTagValue("Black"));
			stmt.setString(5, g.getTagValue("Result"));
			stmt.setString(6, g.getTagValue("Event"));
			stmt.setInt(7, g.gotoStartPosition().getDbId());
			if (stmt.executeUpdate() == 1) {
				g.setDbId(newID);
			};	
			con.commit();
		} catch (SQLException ex) {
			try {
				con.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ex.printStackTrace();
		}
	}
	
	private static final class PositionResult {
		public Position position;
		public int nextID;
	}
	
	private PositionResult loadPosition(int id, Position previous) {		
		PositionResult result = new PositionResult();
		try (Statement stmt = con.createStatement()) {
			ResultSet rslt = stmt.executeQuery(String.format("select * from POSITION where ID = %s", id));
			if (rslt.next()) {
				result.position = new Position(previous, 
						rslt.getString("SAN"), previous != null ? null : rslt.getString("FEN"));
				result.position.setDbId(rslt.getInt("ID"));
				result.position.setComment(rslt.getString("COMMENT"));	
				result.nextID = rslt.getInt("NEXT");
			} else {
				result = null;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();			
		}
		return result;
	}
	
	private void savePosition(Position p) {		
		String insert = "INSERT INTO POSITION (ID, PREVIOUS, NEXT, SAN, FEN, COMMENT) VALUES (?,?,?,?,?,?)";
		try (PreparedStatement stmt = con.prepareStatement(insert)) {	
			stmt.setInt(1, p.getDbId());
			stmt.setInt(2, p.hasPrevious() ? p.getPrevious().getDbId() : -1);
			stmt.setInt(3, p.hasNext() ? p.getNext().getDbId() : -1);
			stmt.setString(4, p.getMoveAsSan());
			stmt.setString(5, p.getFen());
			stmt.setString(6, p.getComment());
			stmt.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();			
		}
	}
	
	private int getNextID(String tableName, String idColumnName) throws SQLException {
		try (Statement stmt = con.createStatement()) {			
			ResultSet rslt = stmt.executeQuery(String.format("select max(%s) from %s", idColumnName, tableName));
			if (rslt.next()) {
				return rslt.getInt(1) + 1; 
			}
		}
		return -1;
	}
	
	private static DateFormat PGN_DATE_FOMATTER = new SimpleDateFormat("yyyy.MM.dd");
	
	private static java.sql.Date fromPGNDate(String pgnDate) {
		try {
			return new java.sql.Date(PGN_DATE_FOMATTER.parse(pgnDate).getTime());
		} catch (ParseException e) {
			return null;
		}
	}
	
}
