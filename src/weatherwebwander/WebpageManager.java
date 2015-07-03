/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import net.sf.image4j.codec.ico.ICODecoder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author simonkenny
 */
public class WebpageManager {
    
    private final WebpageNode webpageNodeHead;
    private WebpageNode currentNode;
    private final ArrayList<WebpageNode> allNodes = new ArrayList<WebpageNode>();
    
    private final SearchTerm searchTermEngine;
    
    private KeywordMatching keywordMatching;
    private final ForceDirectedGraphCanvas graph;
    
    private final NaturalLanguageProcessing nlp;
    
    private final int MAX_CONSECUTIVE_ERRORS = 10;
    private int consecutiveErrors = 0;
    
    private final int BASE_LINKS_TO_EXTRACT = 4;
    private final int MAX_LINKS = 15;
    
    private final int MIN_RELEVANCY = 3;
    
    private final String blackListFile = "blacklist.csv";
    private String []blacklist;
    
    private final int NEXT_PAGE_WAIT = 1000;
    
    private final int MAX_WANDER_UNTIL_NEXT_SEARCH = 5000;  //really big
    private final int PURGE_IRRELEVANT_MOD = 19;
    private int lastSearchCount = MAX_WANDER_UNTIL_NEXT_SEARCH;
    
    private final int MAX_DISTANCE_FROM_HEAD_NODE = 4;
    
    private final Text text;
    
    boolean runMainThread = true;
    
    private final int SEARCH_PAGE_RELEVANCY = 6;
    
    
    public WebpageManager(KeywordMatching keywordMatching, ForceDirectedGraphCanvas graph, Text text) {
        this.keywordMatching = keywordMatching;
        this.graph = graph;
        this.text = text;
        setupBlacklist();
        nlp = new NaturalLanguageProcessing();
        searchTermEngine = new SearchTerm();
        keywordMatching = new KeywordMatching();
        webpageNodeHead = createNode(WebpageNode.HEAD_NODE_STRING,null);
        currentNode = webpageNodeHead;
        // let graph displayer know about nodes
        graph.setNodesList(allNodes, webpageNodeHead);
        // setup for first time
        loadNextPage();
        startMainThread();
    }
    
    public void prepareForExit() {
        runMainThread = false;
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
    
    private void startMainThread() {
        (new Thread() {
            public void run() {
                while(runMainThread) {
                    System.out.println("NEW MAIN THREAD CYCLE: "+allNodes.size()+" nodes exist");
                    if( currentNode == null ) {
                        System.out.println("currentnode is null, waiting...");
                        while(currentNode == null ) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            System.out.println("currentnode is null, waiting...");
                        }
                    }
                    String []links = getAllHyperlinks();
                    System.out.println("Waiting for a few seconds...");
                    try {
                        Thread.sleep(NEXT_PAGE_WAIT);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Finish waiting");
                    // convert google search links to normal links
                    if( links != null ) {
                        if( currentNode.getURL().toLowerCase().contains("google")
                                && currentNode.getURL().toLowerCase().contains("search?") ) {
                            links = getGoogleSearchHyperlinks(links);
                        }
                        System.out.println("num actual links: "+links.length);
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
                    } else {
                        System.out.println("num actual links: NULL");
                    }
                    //consecutiveErrors = 0;
                    System.out.println("WebpageManager: task completed");
                    // finished
                    currentNode.setVisited(true);
                    if(links != null && currentNode.getRelevancy() >= MIN_RELEVANCY) {
                        System.out.println("addPageLinks");
                        System.out.println("page "+currentNode.getURL()+" meets relevancy");
                        if( currentNode.getChildren().size() > 0 || currentNode.getLevel() >= MAX_DISTANCE_FROM_HEAD_NODE ) {
                            System.out.println("Not adding children, already exist");
                        } else {
                            if( links.length > 0 ) {
                                System.out.println("Adding children to currentNode");
                                boolean []usedLinks = new boolean[links.length];
                                for( int i = 0 ; i < usedLinks.length ; i++ ) {
                                    usedLinks[i] = false;
                                }
                                int MAX_TRIES = 10;
                                int numLinksToExtract = BASE_LINKS_TO_EXTRACT
                                        + (currentNode.getRelevancy() - MIN_RELEVANCY);
                                if( numLinksToExtract > MAX_LINKS ) {
                                    numLinksToExtract = MAX_LINKS;
                                }
                                if( numLinksToExtract > links.length ) {
                                    numLinksToExtract = links.length;
                                }
                                System.out.println("Adding "+numLinksToExtract+" links");
                                for( int i = 0 ; i < numLinksToExtract ; i++ ) {
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
                    } else {
                        System.out.println("page "+currentNode.getURL()+" DOESN't meet relevancy");
                        System.out.println("going to parent");
                        ArrayList<WebpageNode> parents = currentNode.getParents();
                        if( parents.isEmpty() ) {
                            // must be head node, reset
                            System.out.println("No parent, must be head node [THIS SHOULDN'T HAPPEN]");
                            currentNode = webpageNodeHead;
                            loadNextPage();
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
                }
                Platform.exit();
            }
        }).start();
    }
    
    private void purgeIrrelevantNodes() {
        synchronized(allNodes) {
            WebpageNode []allNodesCopy = new WebpageNode[allNodes.size()];
            allNodesCopy = allNodes.toArray(allNodesCopy);
            boolean []toRemove = new boolean[allNodesCopy.length];
            toRemove[0] = false;
            for( int i = 1 ; i < allNodesCopy.length ; i++ ) {
                toRemove[i] = allNodesCopy[i].isVisited() && allNodesCopy[i].getRelevancy() < this.MIN_RELEVANCY;
            }
            for( int i = 0 ; i < allNodesCopy.length ; i++ ) {
                for( int j = 1 ; j < allNodesCopy.length ; j++ ) {
                    if( toRemove[j] ) {
                        allNodesCopy[i].removeNodeReferences(allNodesCopy[j]);
                    }
                }
            }
            System.out.println("Purging irrelevant nodes from "+allNodes.size()+"...");
            allNodes.clear();
            for( int i = 0 ; i < allNodesCopy.length ; i++ ) {
                if( !toRemove[i] ) {
                    allNodes.add(allNodesCopy[i]);
                }
            }
            System.out.println("...to "+allNodes.size());
        }
    }
    
    private void purgeUnvisitedNodes() {
        synchronized(allNodes) {
            WebpageNode []allNodesCopy = new WebpageNode[allNodes.size()];
            allNodesCopy = allNodes.toArray(allNodesCopy);
            boolean []toRemove = new boolean[allNodesCopy.length];
            toRemove[0] = false;
            for( int i = 1 ; i < allNodesCopy.length ; i++ ) {
                toRemove[i] = !allNodesCopy[i].isVisited();
            }
            for( int i = 0 ; i < allNodesCopy.length ; i++ ) {
                for( int j = 1 ; j < allNodesCopy.length ; j++ ) {
                    if( toRemove[j] ) {
                        allNodesCopy[i].removeNodeReferences(allNodesCopy[j]);
                    }
                }
            }
            System.out.println("Purging irrelevant nodes from "+allNodes.size()+"...");
            allNodes.clear();
            for( int i = 0 ; i < allNodesCopy.length ; i++ ) {
                if( !toRemove[i] ) {
                    allNodes.add(allNodesCopy[i]);
                }
            }
            System.out.println("...to "+allNodes.size());
        }
    }
    
    private void purgeAllBadNodes() {
        purgeIrrelevantNodes();
        //purgeUnvisitedNodes();
    }
    
    private void takeScreenShot() {
        System.out.println("Waiting for graph to correct itself...");
        Platform.runLater(() -> {
            text.setText("Waiting for graph to correct itself...");
        });
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Saving graph to file...");
        Platform.runLater(() -> {
            text.setText("Saving graph to file...");
            graph.saveFile("/Users/simonkenny/Desktop/crawler_scrshots/");
        });
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        Platform.runLater(() -> {
            text.setText("Finished saving!");
        });
    }
    
    private void resetNodes() {
        synchronized(allNodes) {
            currentNode = null;
            webpageNodeHead.getChildren().clear();
            allNodes.clear();
            allNodes.add(webpageNodeHead);
        }
        currentNode = webpageNodeHead;
    }
    
    private void loadNextPage() {
        lastSearchCount--;
        if( lastSearchCount <= 0 ) {
            System.out.println("------ WANDERED FAR ENOUGH, GOING BACK TO SEARCH");
            currentNode = webpageNodeHead;
        }
        if( currentNode == webpageNodeHead ) {
            if( allNodes.size() > 1 ) {
                if( lastSearchCount > 0 ) {
                    // make sure that no unvisited nodes
                    System.out.println("Looking for unvisited nodes...");
                    WebpageNode unvisitedNode = null;
                    int highestLevel = 0;
                    for( WebpageNode node : allNodes ) {
                        if( !node.isVisited() ) {
                            if( node.getLevel() > highestLevel ) {
                                highestLevel = node.getLevel();
                                unvisitedNode = node;
                                System.out.println("Found unvisited node at level "+highestLevel);
                            }
                        }
                    }
                    if( highestLevel > 0 ) {
                        currentNode = unvisitedNode;
                        loadNextPage();
                        return;
                    }
                    // else, no unvisited nodes
                }
                System.out.println("no unvisited nodes, prepare for reset");
                purgeAllBadNodes();
                takeScreenShot();
                resetNodes();
            }
            currentNode = createNode(searchTermEngine.generateRandomSearchURL(), webpageNodeHead);
            lastSearchCount = MAX_WANDER_UNTIL_NEXT_SEARCH;
            webpageNodeHead.addChild(currentNode);
            currentNode.setMetrics(SEARCH_PAGE_RELEVANCY, 0); //instead of loadBoilerPipe
            return;
        } else {
            // purge irrelevant every mod
            if( lastSearchCount % PURGE_IRRELEVANT_MOD == 0 ) {
                System.out.println("------ PURGED ALL IRRELEVANT NODES");
                purgeIrrelevantNodes();
            }
        }
        // else, not search page
        ArrayList<WebpageNode> children = currentNode.getChildren();
        for( WebpageNode node : children ) {
            if( !node.isVisited() ) {
                currentNode = node;
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
            // just make sure
            currentNode = webpageNodeHead;
            loadNextPage();
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
                    relevancyScore = keywordMatching.checkNumberOfWordsUsed(relevancyText);
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
    
    private final int STRING_TRIM_LEN = 60;
    private String trimString(String str, int len) {
        if( str.length() > len ) {
            return str.substring(0, len).concat("...");
        }
        return str;
    }
    
    private String[] getAllHyperlinks() {
        // Based on example from http://jsoup.org/cookbook/extracting-data/example-list-links
        Document doc;
        try {
            doc = Jsoup.connect(currentNode.getURL()).userAgent("Chrome").get();
            Platform.runLater(() -> {
                text.setText(trimString(doc.title(),STRING_TRIM_LEN)
                        +"   ["+trimString(currentNode.getURL(),STRING_TRIM_LEN)+"]");
            });
            // get fav icon
            {
                String []parts = currentNode.getURL().split("[/]");
                String absFavIconURL = parts[0] + "//" + parts[2] + "/favicon.ico";
                System.out.println("Loading favicon: "+absFavIconURL);
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is = new URL(absFavIconURL).openStream ();
                List<BufferedImage> images = ICODecoder.read(is);
                if( images.size() > 0 ) {
                    for( BufferedImage image : images ) {
                        Image fxImage = SwingFXUtils.toFXImage(image, null);
                        System.out.println("Adding image to current node");
                        currentNode.setFavIconImage(fxImage);
                    }
                } else {
                    System.out.println("Couldn't get ICO file");
                }
            }
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
        } catch (IOException ex ) {
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
            }
        }
        String []linksArray = new String[links.size()];
        linksArray = links.toArray(linksArray);
        return linksArray;
    }
    
}
