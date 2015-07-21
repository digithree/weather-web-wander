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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
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
    
    private final WebpageNode webpageNodeHead;
    private final ArrayList<WebpageNode> allNodes = new ArrayList<>();
    
    private final SearchTerm searchTermEngine;
    
    private final KeywordMatching keywordMatching;
    private final ForceDirectedGraphCanvas graph;
    
    //private final NaturalLanguageProcessing nlp;
    
    private final DomainData domainData;
    
    private final int MAX_CONSECUTIVE_ERRORS = 10;
    private int consecutiveErrors = 0;
    
    private final int BASE_LINKS_TO_EXTRACT = 3;
    private final int MAX_LINKS = 15;
    
    public static final int MIN_RELEVANCY = 3;
    
    private final String blackListFile = "blacklist.csv";
    private String []blacklist;
    
    private final int NEXT_PAGE_WAIT = 1000;
    
    private final int MAX_WANDER_UNTIL_NEXT_SEARCH = 5000;  //really big
    private final int PURGE_IRRELEVANT_MOD = 19;
    private int lastSearchCount = MAX_WANDER_UNTIL_NEXT_SEARCH;
    
    private final int MIN_NODES_FOR_GOOD_GRAPH = 15;
    
    private final Text text;
    
    private boolean runMainThread = true;
    
    private Date startTime;
    
    // Tweek parameters
    private final int SEARCH_PAGE_RELEVANCY = MIN_RELEVANCY + 1;
    private final int MAX_DISTANCE_FROM_HEAD_NODE = 5;
    
    //private final int LOADED_WEBPAGE_WAIT_TIME = 5000;
    private final int LOADED_WEBPAGE_WAIT_TIME = 50;
    
    private final BestURLLearner bestURLLearner;
    
    private final String screenShotSavePath;
    
    
    public WebpageManager(KeywordMatching keywordMatching, ForceDirectedGraphCanvas graph,
            Text text, DomainData domainData, BestURLLearner bestURLLearner,
            String screenShotSavePath) {
        this.keywordMatching = keywordMatching;
        this.graph = graph;
        this.text = text;
        this.domainData = domainData;
        this.bestURLLearner = bestURLLearner;
        this.screenShotSavePath = screenShotSavePath;
        setupBlacklist();
        //nlp = new NaturalLanguageProcessing();
        searchTermEngine = new SearchTerm();
        keywordMatching = new KeywordMatching();
        bestURLLearner = new BestURLLearner();
        webpageNodeHead = createNode(WebpageNode.HEAD_NODE_STRING,null);
        graph.setCurrentNode(webpageNodeHead);
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
                    if( graph.getCurrentNode() == null ) {
                        System.out.println("currentnode is null, waiting...");
                        while(graph.getCurrentNode() == null ) {
                            try {
                                Thread.sleep(LOADED_WEBPAGE_WAIT_TIME);
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
                        if( graph.getCurrentNode().getURL().toLowerCase().contains("google")
                                && graph.getCurrentNode().getURL().toLowerCase().contains("search?") ) {
                            links = getGoogleSearchHyperlinks(links);
                        }
                        System.out.println("num actual links: "+links.length);
                        // wait for metrics to be set
                        System.out.println("waiting for metrics to be set");
                        while(!graph.getCurrentNode().areMetricsSet()) {
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
                    graph.getCurrentNode().setVisited(true);
                    if(links != null && graph.getCurrentNode().getRelevancy() >= MIN_RELEVANCY) {
                        // add domain to list (also sets fav icon)
                        // get fav icon
                        try {
                            // cut up URL
                            String domainURL = Utils.getDomainFromURL(graph.getCurrentNode().getURL());
                            // remove domain front and replace with WWW
                            if( domainURL != null ) {
                                int idx = domainData.addDomain(domainURL, graph.getCurrentNode().getRelevancy());
                                graph.getCurrentNode().setFillCol(domainData.getColorForIdx(idx));
                                graph.setCurrentFavicon(domainData.getFavicon(domainURL));
                                //graph.setCurrentPageDomain(domainURL);
                                /*
                                try {
                                    graph.setCurrentFavicon(Utils.getFavIcon(domainURL));
                                } catch (IOException ex) {
                                    Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
                                    System.out.println("Couldn't get ICO file");
                                }
                                        */
                            } else {
                                System.out.println("DOMAIN URL IS NULL!");
                            }
                        } catch (MalformedURLException e ) {
                            System.out.println("Malformed URL: "+e.getLocalizedMessage());
                        }
                        System.out.println("addPageLinks");
                        System.out.println("page "+graph.getCurrentNode().getURL()+" meets relevancy");
                        if( graph.getCurrentNode().getChildren().size() > 0 ) {
                            System.out.println("Not adding children, already exist");
                        } else if( graph.getCurrentNode().getLevel() >= MAX_DISTANCE_FROM_HEAD_NODE ) { 
                            System.out.println("Not adding children, at level limit: "+graph.getCurrentNode().getLevel());
                        } else {
                            if( links.length > 0 ) {
                                System.out.println("Adding children to currentNode");
                                int numLinksToExtract = BASE_LINKS_TO_EXTRACT
                                        + (graph.getCurrentNode().getRelevancy() - MIN_RELEVANCY);
                                if( numLinksToExtract > MAX_LINKS ) {
                                    numLinksToExtract = MAX_LINKS;
                                }
                                if( numLinksToExtract > links.length ) {
                                    numLinksToExtract = links.length;
                                }
                                System.out.println("Num links target is "+numLinksToExtract);
                                List<String> bestLinks = bestURLLearner.processHyperlinks(links,numLinksToExtract);
                                System.out.println("Num best links is"+bestLinks.size());
                                for( String link : bestLinks ) {
                                    int linkHash = link.hashCode();
                                    boolean match = false;
                                    for( WebpageNode node : allNodes ) {
                                        if( node.getHashcode() == linkHash ) {
                                            // link already exists, join
                                            graph.getCurrentNode().addChild(node);
                                            match = true;
                                            break;
                                        }
                                    }
                                    if( !match ) {
                                        // link is new, create new node
                                        graph.getCurrentNode().addChild(createNode(link, graph.getCurrentNode()));
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("page "+graph.getCurrentNode().getURL()+" DOESN't meet relevancy");
                        System.out.println("going to parent");
                        ArrayList<WebpageNode> parents = graph.getCurrentNode().getParents();
                        if( parents.isEmpty() ) {
                            // must be head node, reset
                            System.out.println("No parent, must be head node [THIS SHOULDN'T HAPPEN]");
                            graph.setCurrentNode(webpageNodeHead);
                            loadNextPage();
                            return;
                        }
                        if( parents.size() == 1 ) {
                            graph.setCurrentNode(parents.get(0));
                        } else {
                            graph.setCurrentNode(parents.get((int)(Math.random()*(double)parents.size())));
                        }
                        System.out.println("Chose parent: "+graph.getCurrentNode().getURL());
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
            //SEARCH_PAGE_RELEVANCY = 6;//MAX_DISTANCE_FROM_HEAD_NODE
            Date now = new Date();
            long diff = now.getTime() - startTime.getTime();//as given
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            text.setText("[v0.2.8] by Simon Kenny and Brendan Flynn - "
                    +"Search page links("+SEARCH_PAGE_RELEVANCY+"), "
                    +"Max distance("+MAX_DISTANCE_FROM_HEAD_NODE+") - "
                    +(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(now))
                    +" [dur mins: "+minutes+"]"
            );
            // find root node
            Node parent = graph.getParent();
            while( parent.getParent() != null ) {
                parent = parent.getParent();
            }
            Utils.saveScreenshot(screenShotSavePath, parent);
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
            graph.setCurrentNode(null);
            webpageNodeHead.getChildren().clear();
            allNodes.clear();
            allNodes.add(webpageNodeHead);
        }
        keywordMatching.resetMatchCounts();
        domainData.reset();
        graph.setCurrentNode(webpageNodeHead);
    }
    
    private void loadNextPage() {
        //purgeIrrelevantNodes();
        lastSearchCount--;
        if( lastSearchCount <= 0 ) {
            System.out.println("------ WANDERED FAR ENOUGH, GOING BACK TO SEARCH");
            graph.setCurrentNode(webpageNodeHead);
        }
        if( graph.getCurrentNode() == webpageNodeHead ) {
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
                        graph.setCurrentNode(unvisitedNode);
                        loadNextPage();
                        return;
                    }
                    // else, no unvisited nodes
                }
                System.out.println("no unvisited nodes, graph complete");
                purgeAllBadNodes();
                if( allNodes.size() >= MIN_NODES_FOR_GOOD_GRAPH ) {
                    System.out.println("num nodes ("+allNodes.size()+"), graph is good");
                    takeScreenShot();
                } else {
                    System.out.println("num nodes ("+allNodes.size()+"), graph is bad");
                }
                System.out.println("reset graph");
                resetNodes();
            }
            startTime = new Date();
            graph.setCurrentNode(createNode(searchTermEngine.generateRandomSearchURL(), webpageNodeHead));
            lastSearchCount = MAX_WANDER_UNTIL_NEXT_SEARCH;
            webpageNodeHead.addChild(graph.getCurrentNode());
            graph.getCurrentNode().setMetrics(SEARCH_PAGE_RELEVANCY, 0); //instead of loadBoilerPipe
            graph.getCurrentNode().setUnmoveable(true);
            return;
        } else {
            // purge irrelevant every mod
            if( lastSearchCount % PURGE_IRRELEVANT_MOD == 0 ) {
                System.out.println("------ PURGED ALL IRRELEVANT NODES");
                purgeIrrelevantNodes();
            }
        }
        // else, not search page
        ArrayList<WebpageNode> children = graph.getCurrentNode().getChildren();
        for( WebpageNode node : children ) {
            if( !node.isVisited() ) {
                graph.setCurrentNode(node);
                System.out.println("selected new node: "+graph.getCurrentNode().getURL());
                loadBoilerpipe();
                return;
            }
        }
        // else no match
        System.out.println("No links, trying to go to parent of "+graph.getCurrentNode().getURL());
        ArrayList<WebpageNode> parents = graph.getCurrentNode().getParents();
        if( parents.isEmpty() ) {
            // must be head node, reset
            System.out.println("No parent, must be head node [THIS SHOULDN'T HAPPEN]");
            // just make sure
            graph.setCurrentNode(webpageNodeHead);
            loadNextPage();
            return;
        }
        if( parents.size() == 1 ) {
            graph.setCurrentNode(parents.get(0));
        } else {
            graph.setCurrentNode(parents.get((int)(Math.random()*(double)parents.size())));
        }
        System.out.println("Chose parent: "+graph.getCurrentNode().getURL());
        // recurse with parent
        loadNextPage();
    }
    
    
    private void loadBoilerpipe() {
        System.out.println("loadBoilerpipe: "+graph.getCurrentNode().getURL());
        (new Thread() {
            @Override
            public void run() {
                int emotionalScore = 0;
                int relevancyScore = 0;
                try {
                    // boilerpipe
                    String text = ArticleExtractor.INSTANCE.getText(new URL(graph.getCurrentNode().getURL()));
                    String lettersOnlyText = text.toLowerCase().replaceAll("[^A-Za-z\\s]+", "");
                    //emotionalScore = nlp.scoreText(lettersOnlyText);
                    System.out.println("Emotional score: "+emotionalScore);
                    String relevancyText = text.toLowerCase().replaceAll("[^A-Za-z0-9\\s-':()]+", "");
                    relevancyScore = keywordMatching.checkNumberOfWordsUsed(relevancyText);
                    System.out.println("Relevancy score: "+relevancyScore);
                    
                    
                } catch (BoilerpipeProcessingException | MalformedURLException ex) {
                    Logger.getLogger(WebpageManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                graph.getCurrentNode().setMetrics(relevancyScore, emotionalScore);
                bestURLLearner.learnFromURL(graph.getCurrentNode().getURL(), relevancyScore);
                System.out.println("Set metrics for "+graph.getCurrentNode().getHashcode());
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
            doc = Jsoup.connect(graph.getCurrentNode().getURL()).userAgent("Chrome").get();
            Platform.runLater(() -> {
                text.setText(trimString(doc.title(),STRING_TRIM_LEN)
                        +"   ["+trimString(graph.getCurrentNode().getURL(),STRING_TRIM_LEN)+"]");
            });
            String title = trimString(doc.title().split("[-]")[0],150);
            graph.getCurrentNode().setPageTitle(title);
            graph.setCurrentPageTitle(title);
            graph.setCurrentPageDomain(
                        Domain.createCleanName(
                                Utils.getDomainFromURL(
                                    graph.getCurrentNode().getURL()
                            )
                    )
            );
            if( graph.getCurrentNode().getLevel() == 1 ) {
                graph.setTitle(title);
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
