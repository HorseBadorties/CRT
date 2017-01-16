package de.toto.lichess;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.Gson;


public class Lichess {
		
	public void foo(String lichessUser, int gameNumber) {		 
		InputStream is = null;
		try {
			int nb = 10;
			URL url = new URL(String.format("https://en.lichess.org/api/user/%s/games?nb=%d&page=%d",
					lichessUser, nb, (int)(gameNumber /nb)));
			is = url.openStream();	
			Gson gson = new Gson();
			String result = gson.fromJson(new InputStreamReader(is, "UTF-8"), String.class);
			System.out.println(result);
		} catch (IOException ioEx) {
			throw new RuntimeException(ioEx);		
		} finally {
			try {
				if (is != null) is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new Lichess().foo("Horse_Badorties", 1);
	}

}
