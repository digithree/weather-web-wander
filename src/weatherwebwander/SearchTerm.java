/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

/**
 *
 * @author simonkenny
 */
public class SearchTerm {
    
    private String searchURLPart1 = "http://www.google.ie/search?q=";
    //private String searchURLPart1 = "http://duckduckgo.com/?q=";
    private String searchURLPart2 = "";//"&ia=answer";
    
    //private String searchTermFile = "climatechange-searchTerms.csv";
    private String searchTermFile = "climatechange-searchTerms-madek.csv";
    
    private String []searchTerms;
    
    public SearchTerm() {
        System.out.println("Trying to initialise SearchTerm...");
        searchTerms = Utils.loadStringToArrayFromCSV(searchTermFile, searchTerms, getClass());
        if( searchTerms == null ) {
            System.out.println("SearchTerm could not initialise");
        }
    }
    
    
    public String generateRandomSearchURL() {
        int idx = (int)(Math.random()*(double)searchTerms.length);
        System.out.println("generateRandomSearchURL - firstIdx: "+idx);
        String searchTerm = searchTerms[idx];
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
        //return searchURLPart1 + "tesco+damaging+the+environment" + searchURLPart2;
    }
}
