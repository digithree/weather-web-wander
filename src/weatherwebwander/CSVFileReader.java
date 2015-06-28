/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simonkenny
 */
public class CSVFileReader {
    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
    private final BufferedReader bufferedReader;
    private String currentToken = null;
    
    CSVFileReader( BufferedReader bufferedReader ) {
        this.bufferedReader = bufferedReader;
    }
    
    public String getNextToken() throws InterruptedException, IOException {
        // check if head exists
        currentToken = queue.poll();
        if( currentToken == null ) {
            String line = bufferedReader.readLine();
            if( line != null ) {
                String []parts = line.split("[,]");
                queue.addAll(Arrays.asList(parts));
                currentToken = queue.poll();
            }
        }
        return currentToken;
    }
    
    public String getCurrentToken() {
        return currentToken;
    }
    
    
    private final int MAX_ITEMS_TO_READ = 30000;
    public ArrayList<String> getAllTokens(boolean throwAwayShortWords) {
        ArrayList<String> tokensList = new ArrayList<String>();
        int itemsRead = 0;
        try {
            while( itemsRead < MAX_ITEMS_TO_READ ) {
                String token = getNextToken();
                if( token == null ) {
                    break;
                }
                token = token.replace("\"", "");
                //System.out.println("Token: "+token);
                if( throwAwayShortWords ) {
                    if( token.length() >= 2 ) {
                        tokensList.add(token);
                    }
                } else {
                    tokensList.add(token);
                }
            }
        } catch( InterruptedException | IOException ex ) {
            Logger.getLogger(SearchTerm.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Finished adding "+tokensList.size()+" tokens");
        return tokensList;
    }
}
