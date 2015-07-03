/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;

/**
 *
 * @author simonkenny
 */
public class KeywordHistogramChart {
    
    private final KeywordMatching keywordMatching;
    private final Pane pane;
    
    BarChart<Number,String> barChart;
    XYChart.Series acrSeries;
    XYChart.Series keySeries;
    
    public KeywordHistogramChart(Pane pane, KeywordMatching keywordMatching) {
        this.pane = pane;
        this.keywordMatching = keywordMatching;
        // setup
        acrSeries = new XYChart.Series();
        acrSeries.setName("Acronyms");
        keySeries = new XYChart.Series();
        keySeries.setName("Keywords");
        createChart();
        pane.getChildren().add(barChart);
        // start update thread
        startMainThread();
    }
    
    private boolean runMainThread = true;
    private void startMainThread() {
        (new Thread() {
            public void run() {
                while(runMainThread) {
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(KeywordHistogramChart.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("<<<<<******====------Updating chart");
                    Platform.runLater(() -> {
                        createDataset();
                        System.out.println("<<<<<******====------Finished updating chart");
                    });
                }
            }
        }).start();
    }

    public void prepareForExit() {
        runMainThread = false;
    }
    
    private void createDataset() {
        String []keywords = keywordMatching.getKeywords();
        String []acronyms = keywordMatching.getAcronyms();
        int []keywordMatchCount = keywordMatching.getKeywordMatchCount();
        int []acronymMatchCount = keywordMatching.getAcronymMatchCount();
        for( int i = 0 ; i < acronymMatchCount.length ; i++ ) {
            if( acronymMatchCount[i] > 1 ) {
                System.out.println("Ac: "+acronyms[i]+" : "+acronymMatchCount[i]);
                boolean match = false;
                for( int j = 0 ; j < acrSeries.getData().size() ; j++ ) {
                    XYChart.Data data = (XYChart.Data)acrSeries.getData().get(j);
                    String str = (String)data.getYValue();
                    if( str.equals(acronyms[i]) ) {
                        data.setXValue(acronymMatchCount[i]);
                        match = true;
                        break;
                    }
                }
                if( !match ) {
                    acrSeries.getData().add(new XYChart.Data(acronymMatchCount[i], acronyms[i]));
                }
            }
        }
        for( int i = 0 ; i < keywordMatchCount.length ; i++ ) {
            if( keywordMatchCount[i] > 1 ) {
                System.out.println("Keyw: "+keywords[i]+" : "+keywordMatchCount[i]);
                boolean match = false;
                for( int j = 0 ; j < keySeries.getData().size() ; j++ ) {
                    XYChart.Data data = (XYChart.Data)keySeries.getData().get(j);
                    String str = (String)data.getYValue();
                    if( str.equals(keywords[i]) ) {
                        data.setXValue(keywordMatchCount[i]);
                        match = true;
                        break;
                    }
                }
                if( !match ) {
                    keySeries.getData().add(new XYChart.Data(keywordMatchCount[i], keywords[i]));
                }
            }
        }
        /*
        if( count == 0 ) {
            System.out.println("No keyword matches");
            keySeries.getData().add(new XYChart.Data(0, "No data"));
        }
                */
    }
    
    private void createChart() {
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        barChart = new BarChart<Number,String>(xAxis,yAxis);
        barChart.setPrefHeight(700);
        barChart.setTitle("Keyword and Acronym matches - all");
        xAxis.setLabel("Count");  
        xAxis.setTickLabelRotation(90);
        yAxis.setLabel("Term");        
        barChart.getData().addAll(acrSeries,keySeries);
    }
    
}
