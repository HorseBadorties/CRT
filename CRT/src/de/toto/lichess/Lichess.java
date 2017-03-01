package de.toto.lichess;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.toto.NetworkConfig;


public class Lichess {
	
	static {
		NetworkConfig.doConfig();
	}
	
	public void foo(String lichessUser) {		
		
		InputStream is = null;
		try {
			int nb = 100;
			int page = 1;
			boolean hasNextPage = true;
			int i = 0;
			
			while (hasNextPage) { 
				URL url = new URL(String.format("https://lichess.org/api/user/%s/games?nb=%d&page=%d&with_moves=1",
						lichessUser, nb, page));
				JsonObject result = null;
				try {
					is = url.openStream();	
					Gson gson = new Gson();
					result = gson.fromJson(new InputStreamReader(is, "UTF-8"), JsonObject.class);
				} catch (IOException ex){
					if (ex.getMessage().startsWith("Server returned HTTP response code: 429")) {
						System.out.println("got a 429");
						pause(600);
						continue;
					} else {
						throw ex;
					}
				} finally {
					if (is != null) is.close();
				}
				System.out.printf("received %d games\n", nb * page);
				for (JsonElement e : result.getAsJsonArray("currentPageResults")) {				
					JsonObject game = e.getAsJsonObject();
//					System.out.printf("%d: %s; %s vs %s: %s\n",
//							i++,
//							get(game, "id"),
//							get(game, "players.white.userId"),
//							get(game, "players.black.userId"),
//							get(game, "moves"));					
				
				}
				hasNextPage = get(result, "nextPage") != null;
				page++;
				pause(10);
			}
			
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
	
	private static void pause(int seconds) {
		try {
			Date d = new Date(System.currentTimeMillis() + seconds * 1000);
			System.out.println("pausing until " + d);
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	private static String get(JsonObject o, String element) {		
		try {
			String[] token = element.split("\\.");		
			for (int i = 0; i < token.length; i++) {
				if (i == token.length-1) {
					return o.get(token[i]).toString();
				} else {
					// drill down
					o = o.getAsJsonObject(token[i]);
				}
			}
			return null;
		} catch (Exception ex) {
			return element;
		}
	}
	
	
	
	
	public static void main(String[] args) {
		new Lichess().foo("H_Badorties");
	}
	
	

}
