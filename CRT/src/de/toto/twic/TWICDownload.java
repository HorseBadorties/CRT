package de.toto.twic;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.toto.game.Game;
import de.toto.pgn.PGNReader;

public class TWICDownload {

	private static final String TWIC_URL = "http://www.theweekinchess.com/zips"; 
	//"http://www.theweekinchess.com/zips/twic1167g.zip"
	
	public static void main(String[] args) throws Exception {
		System.out.println(downloadIssue("1167"));
		
	}
	
	public static List<Game> downloadIssue(String issueNumber) throws Exception {
		URL url = new URL(TWIC_URL + "/twic" + issueNumber + "g.zip");
		InputStream in = url.openStream();
		try {
			ZipInputStream zip = new ZipInputStream(in);
			ZipEntry entry = zip.getNextEntry();
			if (entry == null) return null;			
            return PGNReader.parse(zip);	
		} finally {
			if (in != null) in.close();
		}
		
	}
}
