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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author simonkenny
 */
public class WebBrowser extends Region {
    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();
    
    private SearchTerm searchTermEngine;
    private boolean broswerIsSearching = true;
    
    private String blackListFile = "blacklist.csv";
    private String []blacklist;
    
    private final int MAX_CONSECUTIVE_ERRORS = 10;
    private int consecutiveErrors = 0;
    
    private final int GOOD_PAGE_LOAD_WAIT = 14000;
    private final int PAGE_LOAD_MAX_WAIT = 20000;
    
    private final int PAGE_SCROLL_PX = 1;
    private final int PAGE_SCROLL_DELAY = 30;
    
    private Timeline timeline;
     
    public WebBrowser() {
        {
            try // set up blacklist
            {
                CSVFileReader reader;
                InputStream is = getClass().getResourceAsStream(blackListFile);
                reader = new CSVFileReader(new BufferedReader(new InputStreamReader(
                        is
                )));
                ArrayList<String> list = reader.getAllTokens();
                is.close();
                blacklist = new String[list.size()];
                blacklist = list.toArray(blacklist);
            } catch (IOException ex) {
                Logger.getLogger(WebBrowser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        startPageLoad(true);
        
        getStyleClass().add("browser");
        // add load worker callback
        webEngine.getLoadWorker().stateProperty().addListener(
        new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue ov, State oldState, State newState) {
                if( newState == State.READY ) {
                    System.out.println("WebEngine LoadWorker: ready");
                } else if( newState == State.RUNNING || newState == State.SCHEDULED ) {
                    System.out.println("WebEngine LoadWorker: doing task");
                } else if( newState == State.SUCCEEDED ) { //newState == State.READY
                    if( timeline != null ) {
                        timeline.stop();
                    }
                    
                    consecutiveErrors = 0;
                    System.out.println("WebEngine LoadWorker: task completed");
                    if( !broswerIsSearching ) {
                        System.out.println("WebEngine: injecting webpage scroll javascript function");
                        String content = 
                                "var doPosMove = true; "
                                + "function toPos(yPos){"
                                + "window.scrollTo(0, yPos); "
                                + "if(doPosMove){setTimeout(function() { toPos(yPos + "+PAGE_SCROLL_PX+"); }, "+PAGE_SCROLL_DELAY+");} "
                                + "}"
                                + "toPos(0);"
                        ;
                        // replace problematic characters
                        content = content.replace("'", "\\'");
                        content = content.replace(System.getProperty("line.separator"), "\\n");
                        content = content.replace("\n", "\\n");
                        content = content.replace("\r", "\\n");
                        webEngine.executeScript(content);
                    }
                    // wait now
                    Task task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            // Now block the thread for a short time, but be sure
                            // to check the interrupted exception for cancellation!
                            Platform.runLater(() -> {
                                int numTerms = getNumTermsInDoc();
                                if( numTerms > 0 ) {
                                    System.out.println("Num terms in paragraph text: "+numTerms);
                                } else {
                                    System.out.println("No terms used in paragraph");
                                }
                                
                                Task task2 = new Task<Void>() {
                                    @Override
                                    protected Void call() throws Exception {
                                        if( !broswerIsSearching && numTerms > 0 ) {
                                            try {
                                                Thread.sleep(GOOD_PAGE_LOAD_WAIT);
                                            } catch (InterruptedException interrupted) {
                                                // do nothing
                                            }
                                        }
                                        Platform.runLater(() -> {
                                            startPageLoad(false);
                                        });
                                        return null;
                                    }
                                };
                                new Thread(task2).start();
                            });
                            
                            // load next page
                            return null;
                        }
                    };
                    new Thread(task).start();
                } else if( newState == State.CANCELLED || newState == State.FAILED ) {
                    consecutiveErrors++;
                    if( consecutiveErrors > MAX_CONSECUTIVE_ERRORS ) {
                        System.out.println("Fatal WebEngine errors");
                        Platform.exit();
                    }
                    System.out.println("WebEngine LoadWorker: error");
                    Platform.runLater(() -> {
                        String searchTermString = searchTermEngine.generateRandomSearchURL();
                        System.out.println("searchTerm: "+searchTermString);
                        broswerIsSearching = true;
                        webEngine.load(searchTermString);
                    });
                }
            }
        });
        //add the web view to the scene
        getChildren().add(browser);
 
    }
    
    private void startPageLoad(boolean firstTime) {
        if( firstTime ) {
            // set up search term engine and load page
            searchTermEngine = new SearchTerm();
            String searchTermString = searchTermEngine.generateRandomSearchURL();
            System.out.println("searchTerm: "+searchTermString);
            broswerIsSearching = true;
            webEngine.load(searchTermString);
            timeline = new Timeline(new KeyFrame(
                    Duration.millis(PAGE_LOAD_MAX_WAIT),
                    ae -> {
                        // taking too long timer
                        Platform.runLater(() -> {
                            System.out.println("TIMER RAN OUT, RESETING WORKER AND LOAD");
                        });
                        webEngine.getLoadWorker().cancel();
                        startPageLoad(false);
                    }));
            timeline.play();
        } else {
            // stop position mover
            {
            String content = 
                    "function stopPos(){doPosMove=false;} stopPos();"
            ;
            // replace problematic characters
            content = content.replace("'", "\\'");
            content = content.replace(System.getProperty("line.separator"), "\\n");
            content = content.replace("\n", "\\n");
            content = content.replace("\r", "\\n");
            webEngine.executeScript(content);
            }
            int numTerms = getNumTermsInDoc();
            System.out.println("getting links");
            String []links;
            boolean forceSearchAgain = false;
            if( broswerIsSearching ) {
                links = getGoogleSearchHyperlinks(getAllHyperlinks());
            } else {
                links = getAllHyperlinks();
                if( numTerms == 0 ) {
                    forceSearchAgain = true;
                }
            }
            System.out.println("got "+links.length+" links");

            if( links.length > 0 && !forceSearchAgain ) {
                String chosenLink = links[(int)(Math.random()*(double)links.length)];
                System.out.println("loading new page: "+chosenLink);
                broswerIsSearching = false;
                webEngine.getLoadWorker().cancel();
                webEngine.load(chosenLink);
            } else {
                String searchTermString = searchTermEngine.generateRandomSearchURL();
                System.out.println("searchTerm: "+searchTermString);
                broswerIsSearching = true;
                webEngine.getLoadWorker().cancel();
                webEngine.load(searchTermString);
            }
        }
    }
    
    private final String searchUrlCode = "/url?q=";
    private String[] getGoogleSearchHyperlinks(String[] uncleanedLinks) {
        // /url
        ArrayList<String> links = new ArrayList<String>();
        for( String link : uncleanedLinks ) {
            if( link.length() > searchUrlCode.length() ) {
                //System.out.println("Link: "+link);
                String subStr = link.substring(0, searchUrlCode.length());
                if( subStr.equals(searchUrlCode) && !link.contains("webcache") ) {
                    String []parts = link.substring(searchUrlCode.length()).split("[&]");
                    links.add(parts[0]);
                    //links.add(link);
                }
            }
        }
        String []linksArray = new String[links.size()];
        linksArray = links.toArray(linksArray);
        return linksArray;
    }
    
    private int getNumTermsInDoc() {
        Document document = webEngine.getDocument();
        NodeList nodeList = document.getElementsByTagName("*");
        ArrayList<String> paragraphs = new ArrayList<String>();
        for( int i = 0 ; i < nodeList.getLength() ; i++ ) {
            org.w3c.dom.Node node = nodeList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element)node;
                if( element.getTagName().toLowerCase().equals("p") 
                        || element.getTagName().toLowerCase().equals("td")
                        || element.getTagName().toLowerCase().equals("li")
                        || element.getTagName().toLowerCase().equals("h1")
                        || element.getTagName().toLowerCase().equals("h2")
                        || element.getTagName().toLowerCase().equals("h3")
                        ) {
                    NodeList childElements = element.getChildNodes();
                    //TEXT_NODE
                    for( int j = 0 ; j < childElements.getLength() ; j++ ) {
                        org.w3c.dom.Node childNode = childElements.item(j);
                        if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                            paragraphs.add(childNode.getNodeValue());
                        }
                    }
                }
            }
            /*
            if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                paragraphs.add(node.getNodeValue());
            }
                    */
        }
        int numWordsUsed = 0;
        if( paragraphs.size() > 0 ) {
            numWordsUsed = searchTermEngine.checkNumberOfWordsUsed(paragraphs);
        }
        return numWordsUsed;
    }
    
    private String[] getAllHyperlinks() {
        Document document = webEngine.getDocument();
        NodeList nodeList = document.getElementsByTagName("*");
        ArrayList<String> links = new ArrayList<String>();
        int numBlacklisted = 0;
        for( int i = 0 ; i < nodeList.getLength() ; i++ ) {
            org.w3c.dom.Node node = nodeList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element)node;
                String href = element.getAttribute("href");
                if( href != null ) {
                    if( !href.isEmpty() ) {
                        if( href.contains("http://") || href.contains("https://")) {
                            boolean containsBlacklistedTerm = false;
                            if( blacklist != null ) {
                                for( String term : blacklist ) {
                                    if( href.contains(term) ) {
                                        containsBlacklistedTerm = true;
                                        break;
                                    }
                                }
                                if( !containsBlacklistedTerm ) {
                                    links.add(href);
                                } else {
                                    numBlacklisted++;
                                }
                            }
                        }
                    }
                }
                
            }
        }
        if( numBlacklisted > 0 ) {
            System.out.println("Number of URLs with blacklisted terms: "+numBlacklisted);
        }
        String []linksArray = new String[links.size()];
        linksArray = links.toArray(linksArray);
        return linksArray;
    }
    
    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
 
    @Override protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }
 
    @Override protected double computePrefWidth(double height) {
        return 750;
    }
 
    @Override protected double computePrefHeight(double width) {
        return 500;
    }
}
