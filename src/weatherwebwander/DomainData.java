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

/**
 *
 * @author simonkenny
 */
public class DomainData {
    
    private static final DomainData INSTANCE = new DomainData();
    
    protected DomainData() {}
    
    public static DomainData getInstance() {
        return INSTANCE;
    }
    
    private final List<String> domains = new ArrayList<>();
    private final Map<String,Image> favicons = new HashMap<>();
    
    public List<String> getDomains() {
        return domains;
    }
    
    public Map<String,Image> getFavicons() {
        return favicons;
    }
    
    public void updateFavicons() {
        System.out.println("Updating favicons");
        for( String domainStr : domains ) {
            if( favicons.get(domainStr) == null ) {
                // add
                Image image = null;
                try {
                    image = Utils.getFavIcon(domainStr);
                } catch (IOException ex) {
                    Logger.getLogger(DomainList.class.getName()).log(Level.SEVERE, null, ex);
                }
                if( image != null ) {
                    favicons.put(domainStr, image);
                }
            }
        }
    }
}
