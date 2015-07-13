/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author simonkenny
 */
public class Domain {
    List<String> urls = new ArrayList<>();
    String cleanName;
    int count;
    int relevancy;

    public Domain(String url) {
        urls.add(url);
        cleanName = createCleanName(url);
        count = 1;
        relevancy = 1;
    }

    public Domain(String url, int count, int relevancy) {
        urls.add(url);
        cleanName = createCleanName(url);
        this.count = count;
        this.relevancy = relevancy;
    }

    public void addToCount() {
        count++;
    }
    
    public void addToCount(int relevancy) {
        count++;
        this.relevancy += relevancy;
    }
    
    // clean name helpers
    
    private String createCleanName(String URL) {
        String domainName;
        String []parts = URL.split("[.]");
        if( parts.length >= 3 ) {
            domainName = parts[1];
        } else if( parts.length >= 1 ) {
            //domainName = parts[0].split("[//]")[1];
            String []domainNameSub = parts[0].split("[//]");
            if( domainNameSub.length >= 2 ) {
                domainName = domainNameSub[2];
            } else {
                domainName = URL;
            }
        } else { // fail safe, shouldn't happen
            domainName = URL;
        }
        return domainName;
    }
    
    
    public boolean addUrl(String url) {
        if( urlHasCleanName(url) ) {
            boolean match = false;
            for( String str : urls ) {
                if( str.equals(url) ) {
                    match = true;
                    break;
                }
            }
            if( !match ) {
                urls.add(url);
            }
            return true;
        }
        return false;
    }
    
    public boolean urlHasCleanName(String url) {
        String urlCleanName = createCleanName(url);
        return cleanName.equals(urlCleanName);
    }
    
    // accessors
    
    public String getCleanName() {
        return cleanName;
    }
    
    public String getURL() {
        return urls.get(0);
    }

    public String getURL(int idx) {
        if( idx >= 0 && idx < urls.size() ) {
            return urls.get(idx);
        }
        return null;
    }

    public int getCount() {
        return count;
    }
    
    public int getRelevancy() {
        return relevancy;
    }
}