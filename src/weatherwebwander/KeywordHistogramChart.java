/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    XYChart.Series dataSeries;
    
    
    public KeywordHistogramChart(Pane pane, KeywordMatching keywordMatching) {
        this.pane = pane;
        this.keywordMatching = keywordMatching;
        // setup
        dataSeries = new XYChart.Series();
        dataSeries.setName("Terms");
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
                        Thread.sleep(7000);
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
    
    private class CountComparator implements Comparator<TermWithCount> {
        @Override
        public int compare(TermWithCount twc1, TermWithCount twc2) {
            int c1 = twc1.getCount();
            int c2 = twc2.getCount();
            // note: uses reverse ordering
            if (c1 > c2) {
                return -1;
            } else if (c1 < c2){
                return 1;
            } else {
                return 0;
            }
        }
    }
    
    public class TermWithCount  {
        private String term;
        private int count;
        
        public TermWithCount(String term, int count) {
            this.term = term;
            this.count = count;
        }
        
        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
    
    private class FrequencyComparator implements Comparator<XYChart.Data> {
        @Override
        public int compare(XYChart.Data data1, XYChart.Data data2) {
            int c1 = ((Number)data1.getXValue()).intValue();
            int c2 = ((Number)data2.getXValue()).intValue();
            // note: uses reverse ordering
            if (c1 > c2) {
                return -1;
            } else if (c1 < c2){
                return 1;
            } else {
                return 0;
            }
        }
    }
    
    private final int TOP_DISPLAY_TERMS_AMOUNT = 10;
    private void createDataset() {
        String []keywords = keywordMatching.getKeywords();
        String []acronyms = keywordMatching.getAcronyms();
        int []keywordMatchCount = keywordMatching.getKeywordMatchCount();
        int []acronymMatchCount = keywordMatching.getAcronymMatchCount();
        // --- sort
        // create combined list
        List<TermWithCount> terms = new ArrayList<TermWithCount>();
        for( int i = 0 ; i < keywordMatchCount.length ; i++ ) {
            terms.add(new TermWithCount(keywords[i],keywordMatchCount[i]));
        }
        for( int i = 0 ; i < acronymMatchCount.length ; i++ ) {
            terms.add(new TermWithCount(acronyms[i],acronymMatchCount[i]));
        }
        // sort list, keep TOP_DISPLAY_TERMS_AMOUNT
        Collections.sort(terms, new CountComparator());
        terms = terms.subList(0, terms.size() < TOP_DISPLAY_TERMS_AMOUNT ? terms.size() : TOP_DISPLAY_TERMS_AMOUNT);
        // --- manage dataset
        // remove data not on new term list
        List<Object> toRemove = new ArrayList<>();
        for( int i = 0 ; i < dataSeries.getData().size() ; i++ ) {
            XYChart.Data xyData = (XYChart.Data)dataSeries.getData().get(i);
            boolean match = false;
            for( int j = 0 ; j < terms.size() ; j++ ) {
                TermWithCount term = terms.get(j);
                if( ((String)xyData.getYValue()).equals(term.getTerm()) ) {
                    match = true;
                    break;
                }
            }
            if( !match ) {
                toRemove.add(xyData);
            }
        }
        dataSeries.getData().removeAll(toRemove);
        // add data to dataset
        for( int i = 0 ; i < terms.size() ; i++ ) {
            TermWithCount term = terms.get(i);
            boolean match = false;
            for( Object obj : dataSeries.getData() ) {
                XYChart.Data xyData = (XYChart.Data)obj;
                if( ((String)xyData.getYValue()).equals(term.getTerm()) ) {
                    xyData.setXValue(term.getCount());
                    match = true;
                }
            }
            if( !match ) { // then add
                dataSeries.getData().add(new XYChart.Data(term.getCount(), term.getTerm()));
            }
        }
    }
    
    private void createChart() {
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        barChart = new BarChart<Number,String>(xAxis,yAxis);
        barChart.setPrefHeight(450);
        barChart.setTitle("What's most relevant?");
        xAxis.setLabel("Count");  
        xAxis.setTickLabelRotation(90);
        yAxis.setLabel("Term");        
        barChart.getData().addAll(dataSeries);
    }
    
}
