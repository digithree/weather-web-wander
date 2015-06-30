/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Worker;

/**
 *
 * @author simonkenny
 */
public class WebpageManager {
    
    private WebpageNode webpageNodeHead;
    private WebpageNode currentNode;
    ArrayList<WebpageNode> allNodes = new ArrayList<WebpageNode>();
    
    private SearchTerm searchTermEngine;
    
    private final WebBrowser webBrowser;
    private final ForceDirectedGraphCanvas graph;
    
    private final NaturalLanguageProcessing nlp;
    
    private final int MAX_CONSECUTIVE_ERRORS = 10;
    private int consecutiveErrors = 0;
    
    private final int MAX_LINKS = 4;
    
    private final int MIN_RELEVANCY = 1;
    
    public WebpageManager(WebBrowser webBrowser, ForceDirectedGraphCanvas graph) {
        this.webBrowser = webBrowser;
        this.graph = graph;
        nlp = new NaturalLanguageProcessing();
        searchTermEngine = new SearchTerm();
        webpageNodeHead = createNode(WebpageNode.HEAD_NODE_STRING,null);
        currentNode = webpageNodeHead;
        // let graph displayer know about nodes
        graph.setNodesList(allNodes, webpageNodeHead);
        // setup for first time
        loadNextPage();
        startMainThread();
    }
    
    private WebpageNode createNode(String URL, WebpageNode parent) {
        int hashCode = URL.hashCode();
        for( WebpageNode node : allNodes ) {
            if( node.getHashcode() == hashCode ) {
                System.out.println("WebpageManager: Can't create node, same hash already exists");
                return null;
            }
        }
        WebpageNode node = new WebpageNode(URL);
        if( parent != null ) {
            node.addParent(parent);
        }
        allNodes.add(node);
        System.out.println("WebpageManager: Created node for URL "+URL);
        return node;
    }
    
    
    boolean runMainThread = true;
    private void startMainThread() {
        (new Thread() {
            public void run() {
                while(runMainThread) {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("NEW MAIN THREAD CYCLE: "+allNodes.size()+" nodes exist");
                    System.out.println("Waiting for webpage display to finish");
                    Worker.State webpageState = webBrowser.getWebpageState();
                    while( webpageState == Worker.State.RUNNING
                            || webpageState == Worker.State.SUCCEEDED ) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        webpageState = webBrowser.getWebpageState();
                        String str = "[NONE]";
                        if( webpageState == Worker.State.RUNNING ) {
                            str = "RUNNING";
                        } else if( webpageState == Worker.State.SUCCEEDED ) {
                            str = "SUCCEEDED";
                        }
                        System.out.println("Polling webpage display again... "+str);
                    }
                    System.out.println("Webpage display finished loading");
                    currentNode.setVisited(true);
                    // wait for metrics to be set
                    System.out.println("waiting for metrics to be set");
                    while(!currentNode.areMetricsSet()) {}
                    System.out.println("detected metrics are set");
                    // finished
                    if( webpageState == Worker.State.READY ) {
                        consecutiveErrors = 0;
                        System.out.println("WebpageManager: task completed");
                        System.out.println("addPageLinks");
                        Platform.runLater(() -> {
                            String []links = webBrowser.getAllHyperlinks();
                            // convert google search links to normal links
                            if( currentNode.getURL().toLowerCase().contains("google")
                                    && currentNode.getURL().toLowerCase().contains("search?") ) {
                                links = getGoogleSearchHyperlinks(links);
                            }
                            if(currentNode.getRelevancy() >= MIN_RELEVANCY) {
                                System.out.println("page "+currentNode.getURL()+" meets relevancy");
                                if( currentNode.getChildren().size() > 0 ) {
                                    System.out.println("Not adding children, already exist");
                                } else {
                                    System.out.println("Adding children to currentNode");
                                    if( links != null ) {
                                        System.out.println("Adding "+links.length+" links");
                                        int count = 0;
                                        for( String link : links ) {
                                            int linkHash = link.hashCode();
                                            boolean match = false;
                                            for( WebpageNode node : allNodes ) {
                                                if( node.getHashcode() == linkHash ) {
                                                    // link already exists, join
                                                    currentNode.addChild(node);
                                                    match = true;
                                                    break;
                                                }
                                            }
                                            if( !match ) {
                                                // link is new, create new node
                                                currentNode.addChild(createNode(link, currentNode));
                                                if( count++ >= MAX_LINKS ) {
                                                    // only add up to MAX_LINKS links
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                System.out.println("page "+currentNode.getURL()+" DOESN't meet relevancy");
                                System.out.println("going to parent");
                                ArrayList<WebpageNode> parents = currentNode.getParents();
                                if( parents.isEmpty() ) {
                                    // must be head node, reset
                                    System.out.println("No parent, must be head node [THIS SHOULDN'T HAPPEN]");
                                    currentNode = createNode(searchTermEngine.generateRandomSearchURL(), webpageNodeHead);
                                    webpageNodeHead.addChild(currentNode);
                                    currentNode.setMetrics(1, 0); //instead of loadBoilerPipe
                                    webBrowser.loadPage(currentNode.getURL());
                                    return;
                                }
                                if( parents.size() == 1 ) {
                                    currentNode = parents.get(0);
                                } else {
                                    currentNode = parents.get((int)(Math.random()*(double)parents.size()));
                                }
                                System.out.println("Chose parent: "+currentNode.getURL());
                            }
                            loadNextPage();
                            System.out.println("Loading next page...");
                        });
                    } else if( webpageState == Worker.State.CANCELLED 
                            || webpageState == Worker.State.FAILED ) {
                        System.out.println("WebpageManager LoadWorker: error");
                        consecutiveErrors++;
                        if( consecutiveErrors > MAX_CONSECUTIVE_ERRORS ) {
                            System.out.println("Fatal WebEngine errors");
                            runMainThread = false;
                        }
                        System.out.println("Loading next page...");
                        Platform.runLater(() -> {
                            loadNextPage();
                        });
                    }
                }
                Platform.exit();
            }
        }).start();
    }
    
    private void loadNextPage() {
        if( currentNode == webpageNodeHead ) {
            currentNode = createNode(searchTermEngine.generateRandomSearchURL(), webpageNodeHead);
            webpageNodeHead.addChild(currentNode);
            currentNode.setMetrics(1, 0); //instead of loadBoilerPipe
            webBrowser.loadPage(currentNode.getURL());
            return;
        }
        // else, not search page
        ArrayList<WebpageNode> children = currentNode.getChildren();
        for( WebpageNode node : children ) {
            if( !node.isVisited() ) {
                currentNode = node;
                System.out.println("selected new node: "+currentNode.getURL());
                webBrowser.loadPage(currentNode.getURL());
                loadBoilerpipe();
                return;
            }
        }
        // else no match
        System.out.println("No links, trying to go to parent of "+currentNode.getURL());
        ArrayList<WebpageNode> parents = currentNode.getParents();
        if( parents.isEmpty() ) {
            // must be head node, reset
            System.out.println("No parent, must be head node [THIS SHOULDN'T HAPPEN]");
            currentNode = createNode(searchTermEngine.generateRandomSearchURL(), webpageNodeHead);
            webpageNodeHead.addChild(currentNode);
            currentNode.setMetrics(1, 0); //instead of loadBoilerPipe
            webBrowser.loadPage(currentNode.getURL());
            return;
        }
        if( parents.size() == 1 ) {
            currentNode = parents.get(0);
        } else {
            currentNode = parents.get((int)(Math.random()*(double)parents.size()));
        }
        System.out.println("Chose parent: "+currentNode.getURL());
        // recurse with parent
        loadNextPage();
    }
    
    
    private void loadBoilerpipe() {
        System.out.println("loadBoilerpipe: "+currentNode.getURL());
        (new Thread() {
            @Override
            public void run() {
                int emotionalScore = 0;
                int relevancyScore = 0;
                try {
                    // boilerpipe
                    String text = ArticleExtractor.INSTANCE.getText(new URL(currentNode.getURL()));
                    String lettersOnlyText = text.toLowerCase().replaceAll("[^A-Za-z\\s]+", "");
                    emotionalScore = nlp.scoreText(lettersOnlyText);
                    System.out.println("Emotional score: "+emotionalScore);
                    String relevancyText = text.toLowerCase().replaceAll("[^A-Za-z0-9\\s-':()]+", "");
                    relevancyScore = searchTermEngine.checkNumberOfWordsUsed(relevancyText);
                    System.out.println("Relevancy score: "+relevancyScore);
                    
                    
                } catch (BoilerpipeProcessingException | MalformedURLException ex) {
                    Logger.getLogger(WebBrowser.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentNode.setMetrics(relevancyScore, emotionalScore);
                System.out.println("Set metrics for "+currentNode.getHashcode());
            }
        }).start();
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
    
}
