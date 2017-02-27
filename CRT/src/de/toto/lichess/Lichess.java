package de.toto.lichess;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class Lichess {
		
	//{currentPage=1.0, maxPerPage=10.0, currentPageResults=[{id=I8q8rbFf, rated=true, variant=standard, speed=bullet, perf=bullet, createdAt=1.483218057415E12, lastMoveAt=1.483218290415E12, turns=55.0, color=black, status=outoftime, clock={initial=120.0, increment=1.0, totalTime=160.0}, players={white={userId=horse_badorties, rating=1500.0, ratingDiff=139.0, provisional=true}, black={userId=hogardi, rating=1419.0, ratingDiff=-6.0}}, winner=white, url=https://lichess.org/I8q8rbFf/white}, {id=iLtsE9s9, rated=true, variant=standard, speed=blitz, perf=blitz, createdAt=1.482847727763E12, lastMoveAt=1.482848286163E12, turns=91.0, color=black, status=resign, clock={initial=300.0, increment=0.0, totalTime=300.0}, players={white={userId=tebe_privet, rating=2051.0, ratingDiff=11.0}, black={userId=horse_badorties, rating=2079.0, ratingDiff=-13.0}}, winner=white, url=https://lichess.org/iLtsE9s9/black}, {id=gII947so, rated=true, variant=standard, speed=blitz, perf=blitz, createdAt=1.482847432052E12, lastMoveAt=1.482847666552E12, turns=34.0, color=white, status=resign, clock={initial=300.0, increment=0.0, totalTime=300.0}, players={white={userId=horse_badorties, rating=2084.0, ratingDiff=-5.0}, black={userId=bad-minton, rating=2275.0, ratingDiff=6.0}}, winner=black, url=https://lichess.org/gII947so/black}, {id=UQpthiyq, rated=true, variant=standard, speed=blitz, perf=blitz, createdAt=1.482420106127E12, lastMoveAt=1.482420359027E12, turns=46.0, color=white, status=resign, clock={initial=300.0, increment=0.0, totalTime=300.0}, players={white={userId=elpaisa007, rating=1889.0, ratingDiff=-5.0, analysis={inaccuracy=3.0, mistake=3.0, blunder=1.0, acpl=55.0}}, black={userId=horse_badorties, rating=2078.0, ratingDiff=6.0, analysis={inaccuracy=1.0, mistake=0.0, blunder=1.0, acpl=27.0}}}, winner=black, url=https://lichess.org/UQpthiyq/black}, {id=Noi2r3TY, rated=true, variant=standard, speed=blitz, perf=blitz, createdAt=1.482401596365E12, lastMoveAt=1.482402135765E12, turns=101.0, color=black, status=resign, clock={initial=300.0, increment=0.0, totalTime=300.0}, players={white={userId=horse_badorties, rating=2064.0, ratingDiff=14.0, analysis={inaccuracy=3.0, mistake=0.0, blunder=0.0, acpl=14.0}}, black={userId=exrich, rating=2181.0, ratingDiff=-14.0, analysis={inaccuracy=4.0, mistake=4.0, blunder=1.0, acpl=35.0}}}, winner=white, url=https://lichess.org/Noi2r3TY/black}, {id=q70e01aZ, rated=true, variant=standard, speed=blitz, perf=blitz, createdAt=1.482400805165E12, lastMoveAt=1.482401356065E12, turns=147.0, color=black, status=outoftime, clock={initial=300.0, increment=0.0, totalTime=300.0}, players={white={userId=gigibignotti, rating=1980.0, ratingDiff=2.0, analysis={inaccuracy=10.0, mistake=4.0, blunder=4.0, acpl=41.0}}, black={userId=horse_badorties, rating=2066.0, ratingDiff=-2.0, analysis={inaccuracy=7.0, mistake=5.0, blunder=0.0, acpl=27.0}}}, url=https://lichess.org/q70e01aZ/black}, {id=vhPwDYLu, rated=true, variant=standard, speed=blitz, perf=blitz, createdAt=1.482399919996E12, lastMoveAt=1.482400536496E12, turns=95.0, color=black, status=outoftime, clock={initial=300.0, increment=0.0, totalTime=300.0}, players={white={userId=wahyuchao, rating=1949.0, ratingDiff=32.0, analysis={inaccuracy=4.0, mistake=7.0, blunder=7.0, acpl=90.0}}, black={userId=horse_badorties, rating=2081.0, ratingDiff=-15.0, analysis={inaccuracy=5.0, mistake=3.0, blunder=4.0, acpl=68.0}}}, winner=white, url=https://lichess.org/vhPwDYLu/black}, {id=O1ysSkCG, rated=true, variant=standard, speed=blitz, perf=blitz, createdAt=1.482317763263E12, lastMoveAt=1.482318016063E12, turns=51.0, color=black, status=resign, clock={initial=300.0, increment=0.0, totalTime=300.0}, players={white={userId=horse_badorties, rating=2073.0, ratingDiff=8.0, analysis={inaccuracy=1.0, mistake=0.0, blunder=0.0, acpl=15.0}}, black={userId=yasinking, rating=1987.0, ratingDiff=-9.0, analysis={inaccuracy=1.0, mistake=4.0, blunder=1.0, acpl=54.0}}}, winner=white, url=https://lichess.org/O1ysSkCG/white}, {id=mrcbZD1L, rated=false, variant=standard, speed=classical, perf=classical, createdAt=1.482269129473E12, lastMoveAt=1.482270836373E12, turns=127.0, color=black, status=resign, clock={initial=900.0, increment=5.0, totalTime=1100.0}, players={white={userId=superspeed, rating=2219.0}, black={userId=horse_badorties, rating=1500.0, provisional=true}}, winner=white, url=https://lichess.org/mrcbZD1L/white}, {id=9qcSC42G, rated=true, variant=standard, speed=blitz, perf=blitz, createdAt=1.48222704533E12, lastMoveAt=1.48222749623E12, turns=128.0, color=white, status=resign, clock={initial=300.0, increment=0.0, totalTime=300.0}, players={white={userId=boogieknight, rating=1850.0, ratingDiff=-4.0, analysis={inaccuracy=6.0, mistake=6.0, blunder=2.0, acpl=51.0}}, black={userId=horse_badorties, rating=2068.0, ratingDiff=5.0, analysis={inaccuracy=3.0, mistake=6.0, blunder=1.0, acpl=34.0}}}, winner=black, url=https://lichess.org/9qcSC42G/black}], nbResults=1765.0, previousPage=null, nextPage=2.0, nbPages=177.0}

	public void foo(String lichessUser, int gameNumber) {		 
		InputStream is = null;
		try {
			int nb = 10;
			URL url = new URL(String.format("https://lichess.org/api/user/%s/games?nb=%d&page=%d",
					lichessUser, nb, (int)(gameNumber /nb)));
			is = url.openStream();	
			Gson gson = new Gson();
			JsonObject result = gson.fromJson(new InputStreamReader(is, "UTF-8"), JsonObject.class);
			for (JsonElement e : result.getAsJsonArray("currentPageResults")) {
//				String s = e.getAsString();
			}
			
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
