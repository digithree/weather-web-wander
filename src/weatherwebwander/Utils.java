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
}
