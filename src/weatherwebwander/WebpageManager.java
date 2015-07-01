/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.text.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author simonkenny
 */
public class WebpageManager {
    
    private WebpageNode webpageNodeHead;
    private WebpageNode currentNode;
    private final ArrayList<WebpageNode> allNodes = new ArrayList<WebpageNode>();
    
    private SearchTerm searchTermEngine;
    
    private final ForceDirectedGraphCanvas graph;
    
    private final NaturalLanguageProcessing nlp;
    
    private final int MAX_CONSECUTIVE_ERRORS = 10;
    private int consecutiveErrors = 0;
    
    private final int MAX_LINKS = 4;
    
    private final int MIN_RELEVANCY = 3;
    
    private String blackListFile = "blacklist.csv";
    private String []blacklist;
    
    private final int NEXT_PAGE_WAIT = 3000;
    
    private final int MAX_WANDER_UNTIL_NEXT_SEARCH = 35;
    private int lastSearchCount = MAX_WANDER_UNTIL_NEXT_SEARCH;
    
    private Text text;
    
    public WebpageManager(ForceDirectedGraphCanvas graph, Text text) {
        this.graph = graph;
        this.text = text;
        setupBlacklist();
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
    WebpageNode lastNode = null;
    private void startMainThread() {
        (new Thread() {
            public void run() {
                while(runMainThread) {
                    System.out.println("NEW MAIN THREAD CYCLE: "+allNodes.size()+" nodes exist");
                    //System.out.println("Draw nodes: "+graph.getAllNodes().size()+" nodes");
                    System.out.println("Waiting for a few seconds...");
                    try {
                        Thread.sleep(NEXT_PAGE_WAIT);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Finish waiting");
                    String []links = getAllHyperlinks();
                    // convert google search links to normal links
                    if( links != null ) {
                        if( currentNode.getURL().toLowerCase().contains("google")
                                && currentNode.getURL().toLowerCase().contains("search?") ) {
                            links = getGoogleSearchHyperlinks(links);
                        }
                        System.out.println("num actual links: "+links.length);
                    } else {
                        System.out.println("num actual links: NULL");
                    }
                    // wait for metrics to be set
                    System.out.println("waiting for metrics to be set");
                    while(!currentNode.areMetricsSet()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println("waiting for metrics to be set...");
                    }
                    System.out.println("detected metrics are set");
                    //consecutiveErrors = 0;
                    System.out.println("WebpageManager: task completed");
                    // finished
                    currentNode.setVisited(true);
                    System.out.println("addPageLinks");
                    //Platform.runLater(() -> {
                        if(currentNode.getRelevancy() >= MIN_RELEVANCY) {
                            System.out.println("page "+currentNode.getURL()+" meets relevancy");
                            if( currentNode.getChildren().size() > 0 ) {
                                System.out.println("Not adding children, already exist");
                            } else {
                                System.out.println("Adding children to currentNode");
                                if( links != null ) {
                                    System.out.println("Adding "+links.length+" links");
                                    if( links.length > 0 ) {
                                        boolean []usedLinks = new boolean[links.length];
                                        for( int i = 0 ; i < usedLinks.length ; i++ ) {
                                            usedLinks[i] = false;
                                        }
                                        int MAX_TRIES = 10;
                                        for( int i = 0 ; i < MAX_LINKS ; i++ ) {
                                            boolean foundLink = false;
                                            for( int j = 0 ; j < MAX_TRIES ; j++ ) {
                                                int idx = (int)(Math.random()*(double)links.length);
                                                if( !usedLinks[idx] ) {
                                                    foundLink = true;
                                                    String link = links[idx];
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
                                                    }
                                                    usedLinks[idx] = true;
                                                    break;
                                                }
                                            }
                                            if( !foundLink ) {
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
                                purgeAllBadNodes();
                                currentNode = createNode(searchTermEngine.generateRandomSearchURL(), webpageNodeHead);
                                Platform.runLater(() -> {
                                    text.setText(currentNode.getURL());
                                });
                                lastSearchCount = MAX_WANDER_UNTIL_NEXT_SEARCH;
                                webpageNodeHead.addChild(currentNode);
                                currentNode.setMetrics(MIN_RELEVANCY, 0); //instead of loadBoilerPipe
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
                        
                    //});
                    /*
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
                    */
                }
                Platform.exit();
            }
        }).start();
    }
    
    private void purgeAllBadNodes() {
        synchronized(allNodes) {
            WebpageNode []allNodesCopy = new WebpageNode[allNodes.size()];
            allNodesCopy = allNodes.toArray(allNodesCopy);
            boolean []toRemove = new boolean[allNodesCopy.length];
            toRemove[0] = false;
            for( int i = 1 ; i < allNodesCopy.length ; i++ ) {
                toRemove[i] = (!allNodesCopy[i].isVisited()) || allNodesCopy[i].getRelevancy() < this.MIN_RELEVANCY;
            }
            for( int i = 0 ; i < allNodesCopy.length ; i++ ) {
                for( int j = 1 ; j < allNodesCopy.length ; j++ ) {
                    if( toRemove[j] ) {
                        allNodesCopy[i].removeNodeReferences(allNodesCopy[j]);
                    }
                }
            }
            System.out.println("Purging nodes from "+allNodes.size()+"...");
            allNodes.clear();
            for( int i = 0 ; i < allNodesCopy.length ; i++ ) {
                if( !toRemove[i] ) {
                    allNodes.add(allNodesCopy[i]);
                }
            }
            System.out.println("...to "+allNodes.size());
        }
    }
    
    private void loadNextPage() {
        lastSearchCount--;
        if( lastSearchCount <= 0 ) {
            System.out.println("------ WANDERED FAR ENOUGH, GOING BACK TO SEARCH");
            currentNode = webpageNodeHead;
            System.out.println("------ PURGED ALL UNVISITED NODES");
        }
        if( currentNode == webpageNodeHead ) {
            purgeAllBadNodes();
            currentNode = createNode(searchTermEngine.generateRandomSearchURL(), webpageNodeHead);
            Platform.runLater(() -> {
                text.setText(currentNode.getURL());
            });
            lastSearchCount = MAX_WANDER_UNTIL_NEXT_SEARCH;
            webpageNodeHead.addChild(currentNode);
            currentNode.setMetrics(MIN_RELEVANCY, 0); //instead of loadBoilerPipe
            return;
        }
        // else, not search page
        ArrayList<WebpageNode> children = currentNode.getChildren();
        for( WebpageNode node : children ) {
            if( !node.isVisited() ) {
                currentNode = node;
                Platform.runLater(() -> {
                    text.setText(currentNode.getURL());
                });
                System.out.println("selected new node: "+currentNode.getURL());
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
            purgeAllBadNodes();
            currentNode = createNode(searchTermEngine.generateRandomSearchURL(), webpageNodeHead);
            Platform.runLater(() -> {
                text.setText(currentNode.getURL());
            });
            lastSearchCount = MAX_WANDER_UNTIL_NEXT_SEARCH;
            webpageNodeHead.addChild(currentNode);
            currentNode.setMetrics(MIN_RELEVANCY, 0); //instead of loadBoilerPipe
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
                    Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentNode.setMetrics(relevancyScore, emotionalScore);
                System.out.println("Set metrics for "+currentNode.getHashcode());
            }
        }).start();
    }
    
    
    // utils
    
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
            Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String[] getAllHyperlinks() {
        // Based on example from http://jsoup.org/cookbook/extracting-data/example-list-links
        Document doc;
        try {
            doc = Jsoup.connect(currentNode.getURL()).userAgent("Chrome").get();
            Elements links = doc.select("a[href]");
            if( links.size() > 0 ) {
                System.out.println("getAllHyperlinks: Links: "+ links.size());
                ArrayList<String> allLinks = new ArrayList<String>();
                int numBlacklisted = 0;
                for (Element link : links) {
                    String href = link.attr("abs:href");
                    if( href.contains("http://") || href.contains("https://")) {
                        boolean containsBlacklistedTerm = false;
                        if( blacklist != null ) {
                            for( String term : blacklist ) {
                                if( href.contains(term) || href.contains("#")) {
                                    //System.out.println(href+" contains "+term);
                                    containsBlacklistedTerm = true;
                                    break;
                                }
                            }
                            if( !containsBlacklistedTerm ) {
                                allLinks.add(href);
                                //System.out.println("New link: "+href);
                            } else {
                                numBlacklisted++;
                            }
                        }
                    }
                }
                if( numBlacklisted > 0 ) {
                    System.out.println("Number of URLs with blacklisted terms: "+numBlacklisted);
                }
                String []linksArray = new String[allLinks.size()];
                linksArray = allLinks.toArray(linksArray);
                return linksArray;
            }
        } catch (IOException ex) {
            Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("getAllHyperlinks: NONE!");
        return null;
    }
    
    private final String searchUrlCode = "/url?q=http";
    private String[] getGoogleSearchHyperlinks(String[] uncleanedLinks) {
        // /url
        ArrayList<String> links = new ArrayList<String>();
        for( String link : uncleanedLinks ) {
            if( link.length() > searchUrlCode.length() ) {
                if( link.contains(searchUrlCode) && !link.contains("webcache") ) {
                    int idx = link.indexOf(searchUrlCode);
                    if( idx != -1 ) {
                        String []parts = link.substring(idx+7).split("[&]");;
                        //System.out.println("Cleaned link: "+parts[0]);
                        links.add(parts[0]);
                    }
                }
                /*
                String subStr = link.substring(0, searchUrlCode.length());
                if( subStr.equals(searchUrlCode) && !link.contains("webcache") ) {
                    String []parts = link.substring(searchUrlCode.length()).split("[&]");
                    links.add(parts[0]);
                    //links.add(link);
                }
                        */
            }
        }
        String []linksArray = new String[links.size()];
        linksArray = links.toArray(linksArray);
        return linksArray;
    }
    
}
