/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import net.sf.image4j.codec.ico.ICODecoder;

/**
 *
 * @author simonkenny
 */
public class Utils {
    public static float sq(float val) {
        return val * val;
    }
    
    public static String[] loadStringToArrayFromCSV(String filename, String []array, Class<?> class_) {
        try {
            CSVFileReader reader;
            InputStream is = class_.getResourceAsStream(filename);
            reader = new CSVFileReader(new BufferedReader(new InputStreamReader(
                    is
            )));
            ArrayList<String> arrayList = reader.getAllTokens(true);
            is.close();
            array = new String[arrayList.size()];
            array = arrayList.toArray(array);
        } catch (IOException ex) {
            Logger.getLogger(SearchTerm.class.getName()).log(Level.SEVERE, null, ex);
            array = null;
        }
        return array;
    }
    
    private static int SAVED_FILE_COUNT = 0;
    
    public static void saveScreenshot(String filePath, Node node){
        
        if( filePath.charAt(filePath.length()-1) != '/' ) {
            filePath = filePath.concat("/");
        }
        filePath = filePath.concat(String.format("SCR_SHOT_%03d.png", SAVED_FILE_COUNT++));
        System.out.println("SCREENSHOT: trying to save scrshot: "+filePath);
        File file = new File(filePath);
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.color(0.9, 0.9, 0.9));
        WritableImage image = node.snapshot(sp, null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image,null),"png",file);
            System.out.println("SCREENSHOT: file save success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public static Image getFavIcon(String domainURL) throws IOException {
        // get fav icon
        String absFavIconURL = domainURL + "/favicon.ico";
        System.out.println("DomainList: Loading favicon: "+absFavIconURL);
        InputStream is;
        try {
            is = new URL(absFavIconURL).openStream ();
            List<BufferedImage> images = ICODecoder.read(is);
            if( images.size() > 0 ) {
                for( BufferedImage image : images ) {
                    if( image != null ) {
                        Image fxImage = SwingFXUtils.toFXImage(image, null);
                        System.out.println("DomainList: Adding image to current node");
                        return fxImage;
                    }
                }
            } else {
                System.out.println("DomainList: Couldn't get ICO file");
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static String getDomainFromURL(String URL) throws MalformedURLException {
        // cut "http:" or "https:" from rest
        System.out.println("getDomainFromURL: url = "+URL);
        String []parts = URL.split("[//]");
        for( int i = 0 ; i < parts.length ; i++ ) {
            System.out.println("getDomainFromURL: parts["+i+"] = "+parts[i]);
        }
        // remove domain front and replace with WWW
        if( parts.length >= 2 ) {
            String domainURL = parts[2].split("[/]")[0];
            System.out.println("getDomainFromURL: parts[2].split(\"[/]\")[0] = "+parts[1].split("[/]")[0]);
            System.out.println("getDomainFromURL: return: "+"http://"+domainURL);
            return "http://"+domainURL;
        }
        // else malformed
        throw new MalformedURLException();
    }
}

