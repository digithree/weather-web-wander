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
    
    private final int GOOD_PAGE_LOAD_WAIT = 14000;
    private final int PAGE_LOAD_MAX_WAIT = 20000;
    
    private final int PAGE_SCROLL_PX = 1;
    private final int PAGE_SCROLL_DELAY = 30;
    
    private String blackListFile = "blacklist.csv";
    private String []blacklist;
    
    private Timeline timeline;
    
    private javafx.concurrent.Worker.State webpageState = javafx.concurrent.Worker.State.CANCELLED;

    
    public WebBrowser() {
        setupBlacklist();
        getStyleClass().add("browser");
        // add load worker callback
        webEngine.getLoadWorker().stateProperty().addListener(
        new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue ov, State oldState, State newState) {
                webpageState = newState;
                if( newState == State.READY ) {
                    System.out.println("WebEngine LoadWorker: ready");
                } else if( newState == State.RUNNING || newState == State.SCHEDULED ) {
                    System.out.println("WebEngine LoadWorker: doing task");
                } else if( newState == State.SUCCEEDED ) { //newState == State.READY
                    System.out.println("WebEngine LoadWorker: task completed");
                    if( timeline != null ) {
                        timeline.stop();
                    }
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
                    Task task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            System.out.println("WebEngine: waiting for page scroll");
                            try {
                                Thread.sleep(GOOD_PAGE_LOAD_WAIT);
                            } catch (InterruptedException interrupted) {
                                // do nothing
                            }
                            webpageState = State.READY;
                            System.out.println("WebEngine: set to ready");
                            // load next page
                            return null;
                        }
                    };
                    new Thread(task).start();
                } else if( newState == State.CANCELLED ) {
                    System.out.println("WebEngine LoadWorker: cancelled");
                } else if( newState == State.FAILED ) {
                    System.out.println("WebEngine LoadWorker: failed");    
                }
            }
        });
        //add the web view to the scene
        getChildren().add(browser);
    }
    
    public State getWebpageState() {
        return webpageState;
    }
    
    
    public void loadPage(String URL) {
        
        System.out.println("Loading URL: "+URL);
        // TODO : stop web engine worker?
        // stop javascript scroll if not on search page
        String content = 
                "function stopPos(){doPosMove=false;} stopPos();"
        ;
        // replace problematic characters
        content = content.replace("'", "\\'");
        content = content.replace(System.getProperty("line.separator"), "\\n");
        content = content.replace("\n", "\\n");
        content = content.replace("\r", "\\n");
        webEngine.executeScript(content);
        
        webEngine.load(URL);
        timeline = new Timeline(new KeyFrame(
                Duration.millis(PAGE_LOAD_MAX_WAIT),
                ae -> {
                    System.out.println("TIMER RAN OUT, RESETING WORKER AND LOAD");
                    System.out.println("WORKER -IS"
                            +(webEngine.getLoadWorker().isRunning()?"":" NOT")
                            +"- RUNNING");
                    //webEngine.getLoadWorker().
                    webpageState = State.FAILED;
                }));
        timeline.play();
    }
    
    /*
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
        }
        int numWordsUsed = 0;
        if( paragraphs.size() > 0 ) {
            numWordsUsed = searchTermEngine.checkNumberOfWordsUsed(paragraphs);
        }
        return numWordsUsed;
    }
    */
    
    private void setupBlacklist() {
        try // set up blacklist
        {
            CSVFileReader reader;
            InputStream is = getClass().getResourceAsStream(blackListFile);
            reader = new CSVFileReader(new BufferedReader(new InputStreamReader(
                    is
            )));
            ArrayList<String> list = reader.getAllTokens(true);
            is.close();
            blacklist = new String[list.size()];
            blacklist = list.toArray(blacklist);
        } catch (IOException ex) {
            Logger.getLogger(WebBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String[] getAllHyperlinks() {
        Document document = webEngine.getDocument();
        if( document == null ) { return null; }
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
