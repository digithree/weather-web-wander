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
 */
public class SearchTerm {
    
    private String searchURLPart1 = "http://www.google.ie/search?q=";
    //private String searchURLPart1 = "http://duckduckgo.com/?q=";
    private String searchURLPart2 = "";//"&ia=answer";
    
    /*
    private String []searchTermFiles = {
        "/Users/simonkenny/NetBeansProjects/WeatherWebWander-data/climate-change-terms.csv",
        "/Users/simonkenny/NetBeansProjects/WeatherWebWander-data/climate-change-keywords-2-1.csv",
        "/Users/simonkenny/NetBeansProjects/WeatherWebWander-data/climate-change-keywords-3.csv"
    };
    */
    private String []searchTermFiles = {
        "climate-change-terms.csv",
        "tulca-2015-keywords.csv"
    };
    private String [][]searchTerms;
    
    public SearchTerm() {
        System.out.println("Trying to initialise SearchTerm...");
        if( !init() ) {
            System.out.println("SearchTerm could not initialise");
        }
    }
    
    public int checkNumberOfWordsUsed(ArrayList<String> textLines) {
        int count = 0;
        for( String line : textLines ) {
            for( int i = 0 ; i < searchTerms.length ; i++ ) {
                for( String term : searchTerms[i] ) {
                    if( line.contains(term) ) {
                        System.out.print(term+", ");
                        count++;
                    }
                }
            }
        }
        System.out.println("[END] ");
        return count;
    }
    
    private boolean init() {
        searchTerms = new String[searchTermFiles.length][];
        int idx = 0;
        boolean error = false;
        for( String filename : searchTermFiles ) {
            try {
                CSVFileReader reader;
                InputStream is = getClass().getResourceAsStream(filename);
                reader = new CSVFileReader(new BufferedReader(new InputStreamReader(
                        is
                )));
                ArrayList<String> searchTermsList = reader.getAllTokens();
                is.close();
                searchTerms[idx] = new String[searchTermsList.size()];
                searchTerms[idx] = searchTermsList.toArray(searchTerms[idx]);
                idx++;
            } catch (IOException ex) {
                Logger.getLogger(SearchTerm.class.getName()).log(Level.SEVERE, null, ex);
                searchTerms[idx++] = null;
                error = true;
            }
        }
        return !error;
    }
    
    public String generateRandomSearchURL() {
        int firstIdx = (int)(Math.random()*(double)searchTerms.length);
        System.out.println("generateRandomSearchURL - firstIdx: "+firstIdx);
        int secondIdx = (int)(Math.random()*(double)searchTerms[firstIdx].length);
        System.out.println("generateRandomSearchURL - secondIdx: "+secondIdx);
        String searchTerm = searchTerms[firstIdx][secondIdx];
        String []parts = searchTerm.split("[ ]");
        if( parts != null ) {
            if( parts.length > 0 ) {
                String returnString = "" + searchURLPart1 + parts[0];
                if( (parts.length-1) > 0 ) {
                    returnString += "+";
                    for( int i = 1 ; i < (parts.length-1) ; i++ ) {
                        returnString += parts[i] + "+";
                    }
                    returnString += parts[parts.length-1];
                }
                returnString += searchURLPart2;
                return returnString;
            }
        }
        return null;
    }
}
