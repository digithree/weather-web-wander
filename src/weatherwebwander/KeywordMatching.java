/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.util.Arrays;

/**
 *
 * @author simonkenny
 */
public class KeywordMatching {
    
    /*
    private String []keywordsFiles = {
        "climate-change-terms-acronyms.csv",
        "climate-change-terms-min.csv",
        "tulca-2015-keywords.csv"
    };
    */
    
    private final String keywordFileNormal = "climate-change-terms-min.csv";
    private final String keywordFileAcronyms = "climate-change-terms-acronyms.csv";
    
    private String []keywords;
    private String []acronyms;
    
    private int []keywordMatchCount;
    private int []acronymMatchCount;
    
    public KeywordMatching() {
        // relevant terms
        keywords = Utils.loadStringToArrayFromCSV(keywordFileNormal, keywords, getClass());
        keywordMatchCount = new int[keywords.length];
        Arrays.fill(keywordMatchCount, 0);
        acronyms = Utils.loadStringToArrayFromCSV(keywordFileAcronyms, acronyms, getClass());
        acronymMatchCount = new int[acronyms.length];
        Arrays.fill(acronymMatchCount, 0);
        if( keywords != null && acronyms != null ) {
            System.out.println("Failed to setup KeywordMatching");
        }
    }
    
    public int checkNumberOfWordsUsed(String text) {
        int count = 0;
        System.out.print("Looking for acronyms: ");
        String []parts = text.split("\\s+");
        for( String part : parts ) {
            for( int i = 0 ; i < acronyms.length ; i++ ) {
                if(part.equals(acronyms[i])) {
                    acronymMatchCount[i]++;
                    count++;
                }
            }
        }
        System.out.print("\nLooking for terms: ");
        for( int i = 0 ; i < keywords.length ; i++ ) {
            if( text.contains(keywords[i].toLowerCase()) ) {
                keywordMatchCount[i]++;
                count++;
            }
        }
        return count;
    }
    
    public void resetMatchCounts() {
        Arrays.fill(keywordMatchCount, 0);
        Arrays.fill(acronymMatchCount, 0);
    }
    
    // ---getters
    public String[] getKeywords() {
        return keywords;
    }

    public String[] getAcronyms() {
        return acronyms;
    }

    public int[] getKeywordMatchCount() {
        return keywordMatchCount;
    }

    public int[] getAcronymMatchCount() {
        return acronymMatchCount;
    }
}
