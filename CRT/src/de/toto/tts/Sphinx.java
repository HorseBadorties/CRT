package de.toto.tts;

import java.io.IOException;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

public class Sphinx {

	public static void main(String[] args) {
		Configuration configuration = new Configuration();
		 
		// Set path to acoustic model.
		configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		// Set path to dictionary.
		configuration.setDictionaryPath("resource:/de/toto/tts/chess_grammar.dic");
		// Set language model.
		configuration.setLanguageModelPath("resource:/de/toto/tts/chess_grammar.lm");
		
//		configuration.setGrammarPath("resource:/de/toto/tts");
//		configuration.setGrammarName("grammar"); 
//		configuration.setUseGrammar(true);
		
		try {
			LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
			// Start recognition process pruning previously cached data.
			recognizer.startRecognition(true);
			SpeechResult result;
		    while ((result = recognizer.getResult()) != null) {
		        System.out.format("Hypothesis: %s\n", result.getHypothesis());
		    }
		    recognizer.stopRecognition();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
