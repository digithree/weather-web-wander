/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simonkenny
 * 
 * TODO : consider splitting words into alphabetised arrays
 */
public class NaturalLanguageProcessing {
    
    private String tableFile = "EmotionLookupTable.csv";
    
    private String []words;
    private int []scores;
    
    public NaturalLanguageProcessing() {
        try {
            CSVFileReader reader;
            InputStream is = getClass().getResourceAsStream(tableFile);
            reader = new CSVFileReader(new BufferedReader(new InputStreamReader(
                    is
            )));
            ArrayList<String> tokensList = reader.getAllTokens(false);
            is.close();
            System.out.println("tokenList size: "+tokensList.size());
            words = new String[tokensList.size()/2];
            scores = new int[tokensList.size()/2];
            int idx = 0;
            for( int i = 0 ; i < words.length ; i++ ) {
                words[i] = tokensList.get(idx++).replace("*", "");
                scores[i] = Integer.parseInt(tokensList.get(idx++));
            }
        } catch (IOException ex) {
            Logger.getLogger(SearchTerm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error building Natural Language Proceesing");
        }
    }
    
    public int scoreText(String text) {
        int score = 0;
        String wordsDescription = "Words: ";
        String []textParts = text.split("\\s+");
        for( String part : textParts ) {
            int idx = 0;
            for( String word : words ) {
                if( part.equals(word.toLowerCase())) {
                    wordsDescription = wordsDescription.concat(word+"("+scores[idx]+")");
                    score += scores[idx];
                    break;
                }
                idx++;
            }
        }
        System.out.println(wordsDescription);
        return score;
    }
}
