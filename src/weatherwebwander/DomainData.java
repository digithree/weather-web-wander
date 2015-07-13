/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 *
 * @author simonkenny
 */
public class DomainData {
    
    
    private static final Color []cols = {
        Color.AQUAMARINE,
        Color.BURLYWOOD,
        Color.CORNFLOWERBLUE,
        Color.DARKSEAGREEN,
        Color.GOLDENROD,
        Color.GREENYELLOW,
        Color.LIGHTCORAL,
        Color.ORCHID,
        Color.PLUM,
        Color.ROSYBROWN,
        Color.SANDYBROWN
    };
    
    private final List<Domain> domains;
    private final Map<String,Image> favicons;
    
    
    public DomainData() {
        domains = new ArrayList<>();
        favicons = new HashMap<>();
    }
    
    public void reset() {
        domains.clear();
        favicons.clear();
    }
    
    public List<Domain> getDomains() {
        return domains;
    }
    
    public Map<String,Image> getFavicons() {
        return favicons;
    }
    
    public Image getFavicon(String domain) {
        return favicons.get(domain);
    }
    
    public Image getFavicon(int idx) {
        if( idx >= 0 && idx < domains.size() ) {
            return favicons.get(domains.get(idx).getURL());
        }
        return null;
    }
    
    public Color getColorForIdx(int idx) {
        return cols[idx%cols.length];
    }
    
    public int addDomain(String newURL, int relevancy) {
        Domain matchDomain = null;
        int idx = 0;
        for( Domain domain : domains ) {
            if( domain.addUrl(newURL) ) {
                matchDomain = domain;
                break;
            }
            idx++;
        }
        if( matchDomain == null ) {
            domains.add(new Domain(newURL, 1, relevancy));
            downloadFavicon(newURL);
        } else {
            matchDomain.addToCount(relevancy);
        }
        return idx;
    }
    
    
    private void downloadFavicon(String domainName) {
        // add
        Image image = null;
        try {
            image = Utils.getFavIcon(domainName);
        } catch (IOException ex) {
            Logger.getLogger(DomainData.class.getName()).log(Level.SEVERE, null, ex);
        }
        favicons.put(domainName, image);
        if( image != null ) {
            System.out.println("DomainData: downloaded favicon for "+domainName);
        } else {
            System.out.println("DomainData: couldn't download favicon for "+domainName);
        }
    }
}
