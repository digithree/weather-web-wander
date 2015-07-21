/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    
    /*
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
    */
    
    private static Color colorCov( int r, int g, int b ) {
        return Color.color((float)r/256.f,(float)g/256.f,(float)b/256.f, 0.7);
    }
    
    private static final Color []cols = {
        colorCov( 23,128,109 ),
        colorCov( 205,164,222 ),
        colorCov( 109,174,129 ),
        colorCov( 26,72,118 ),
        colorCov( 120,219,226 ),
        colorCov( 255,207,72 ),
        colorCov( 28,169,201 ),
        colorCov( 234,126,93 ),
        colorCov( 227,37,107 ),
        colorCov( 93,118,203 ),
        colorCov( 252,116,253 ),
        colorCov( 255,127,73 ),
        colorCov( 255,83,73 ),
        colorCov( 204,102,102 ),
        colorCov( 116,66,200 ),
        colorCov( 253,219,109 ),
        colorCov( 165,105,79 ),
        colorCov( 178,236,93 ),
        colorCov( 255,110,74 ),
        colorCov( 143,80,157 ),
        colorCov( 255,163,67 ),
        colorCov( 31,206,203 ),
        colorCov( 205,74,74 ),
        colorCov( 255,29,206 ),
        colorCov( 28,172,120 ),
        colorCov( 195,100,197 ),
        colorCov( 28,211,162 ),
        colorCov( 253,94,83 ),
        colorCov( 31,117,254 ),
        colorCov( 29,249,20 ),
    };
    private final List<Color> shuffledCols;
    
    private final List<Domain> domains;
    private final Map<String,Image> favicons;
    
    
    public DomainData() {
        domains = new ArrayList<>();
        favicons = new HashMap<>();
        shuffledCols = Arrays.asList(cols);
        Collections.shuffle(shuffledCols);
        Collections.shuffle(shuffledCols);
        Collections.shuffle(shuffledCols);
        Collections.shuffle(shuffledCols);
        Collections.shuffle(shuffledCols);
    }
    
    public void reset() {
        domains.clear();
        favicons.clear();
        Collections.shuffle(shuffledCols);
        Collections.shuffle(shuffledCols);
        Collections.shuffle(shuffledCols);
        Collections.shuffle(shuffledCols);
        Collections.shuffle(shuffledCols);
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
        return shuffledCols.get(idx%cols.length);
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
