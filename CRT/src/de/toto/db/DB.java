package de.toto.db;

import java.sql.*;

public class DB {
	
	private Connection con;
	
	public DB(String pathToDB) {
		try {
			con = DriverManager.getConnection("jdbc:hsqldb:file:CRT", "SA", "");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int count() {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			ResultSet rslt = stmt.executeQuery("select count(*) from Position");
			if (rslt.next()) {
				return rslt.getInt(1);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();			
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {					
					e.printStackTrace();
				}
			}
		}
		return -1;
	}
	
}
