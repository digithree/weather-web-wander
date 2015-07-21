/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import weatherwebwander.gradedstring.GradedString;
import weatherwebwander.gradedstring.GradedStringReverseOrderComparitor;

/**
 *
 * @author simonkenny
 */
public class BestURLLearner {
    
    private static final int MIN_WORD_LENGTH = 4;
    
    List<GradedString> gradedURLWords;
    
    public List<GradedString> getWords() {
        return gradedURLWords;
    }
    
    public BestURLLearner() {
        gradedURLWords = new ArrayList<>();
        //gradedURLWords.add(new GradedString("arstechnica",100000));
    }
    
    public void learnFromURL(String link, int adjustedRelevancy) {
        System.out.println("BestURLLearner: learnFromURL: link:"+link+", adjustedRelevancy:"+adjustedRelevancy);
        String []protocolSplit = link.split("[:]");
        if( protocolSplit.length < 2 ) {
            System.out.println("BestURLLearner: learnFromURL:  error when removing protocol");
            return;
        }
        String noProtocolLink = "";
        for( int i = 1 ; i < protocolSplit.length ; i++ ) {
            noProtocolLink += protocolSplit[i] + "-";
        }
        System.out.println("BestURLLearner: learnFromURL:   protocol removed link: "+noProtocolLink);
        String []parts = noProtocolLink.split("[\\W]");
        for( int j = 0 ; j < parts.length ; j++ ) {
            if( parts[j] != null ) {
                if( parts[j].length() >= MIN_WORD_LENGTH ) {
                    String part = parts[j].toLowerCase();
                    if( !part.equals("html") ) {
                        System.out.println("BestURLLearner: learnFromURL: word: "+parts[j]);
                        boolean match = false;
                        for( GradedString gradedWord : gradedURLWords ) {
                            if( gradedWord.getString().equals(parts[j]) ) {
                                gradedWord.addToGrade(adjustedRelevancy);
                                System.out.println("BestURLLearner: learnFromURL:   calc relevancy: "+gradedWord.getGrade());
                                match = true;
                                break;
                            }
                        }
                        if( !match ) {
                            gradedURLWords.add(new GradedString(parts[j],adjustedRelevancy));
                            System.out.println("BestURLLearner: learnFromURL:   new word relevancy: "+adjustedRelevancy);
                        }
                    }
                }
            }
        }
            
    }
    
    public List<String> processHyperlinks(String []links, int maxReturnLinks) {
        // get words from links, grade with relevancy
        List<GradedString> gradedLinks = new ArrayList<>();
        for( int i = 0 ; i < links.length ; i++ ) {
            String []parts = links[i].split("[\\W]");
            int grade = 0;
            for( int j = 0 ; j < parts.length ; j++ ) {
                if( parts[j] != null ) {
                    if( parts[j].length() >= MIN_WORD_LENGTH ) {
                        for( GradedString gradedWord : gradedURLWords ) {
                            if( gradedWord.getString().equals(parts[j]) ) {
                                grade += gradedWord.getGrade();
                                break;
                            }
                        }
                    }
                }
            }
            GradedString gradedLink = new GradedString(links[i], grade);
            gradedLinks.add(gradedLink);
        }
        // sort
        Collections.sort(gradedLinks, new GradedStringReverseOrderComparitor());
        List<String> returnLinks = new ArrayList<>();
        int numToReturn = maxReturnLinks;
        if( numToReturn > gradedLinks.size() ) {
            numToReturn = gradedLinks.size();
        }
        for( int i = 0 ; i < numToReturn ; i++ ) {
            returnLinks.add(gradedLinks.get(i).getString());
        }
        return returnLinks;
    }
}
