/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import weatherwebwander.gradedstring.GradedString;
import weatherwebwander.gradedstring.GradedStringReverseOrderComparitor;

/**
 *
 * @author simonkenny
 */
public class URLWordHistogramChart {
    
    private BestURLLearner bestURLLearner;
    
    BarChart<Number,String> barChart;
    XYChart.Series dataSeries;
    
    
    public URLWordHistogramChart(Pane pane, BestURLLearner bestURLLearner) {
        this.bestURLLearner = bestURLLearner;
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
    
    private final int TOP_DISPLAY_TERMS_AMOUNT = 5;
    private void createDataset() {
        // --- sort
        // create combined list
        List<GradedString> terms = new ArrayList<>();
        terms.addAll(bestURLLearner.getWords());
        // sort list, keep TOP_DISPLAY_TERMS_AMOUNT
        Collections.sort(terms, new GradedStringReverseOrderComparitor());
        terms = terms.subList(0, terms.size() < TOP_DISPLAY_TERMS_AMOUNT ? terms.size() : TOP_DISPLAY_TERMS_AMOUNT);
        // --- manage dataset
        // remove data not on new term list
        List<Object> toRemove = new ArrayList<>();
        for( int i = 0 ; i < dataSeries.getData().size() ; i++ ) {
            XYChart.Data xyData = (XYChart.Data)dataSeries.getData().get(i);
            boolean match = false;
            for( int j = 0 ; j < terms.size() ; j++ ) {
                GradedString term = terms.get(j);
                if( ((String)xyData.getYValue()).equals(term.getString()) ) {
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
            GradedString term = terms.get(i);
            boolean match = false;
            for( Object obj : dataSeries.getData() ) {
                XYChart.Data xyData = (XYChart.Data)obj;
                if( ((String)xyData.getYValue()).equals(term.getString()) ) {
                    xyData.setXValue(term.getGrade());
                    match = true;
                }
            }
            if( !match ) { // then add
                dataSeries.getData().add(new XYChart.Data(term.getGrade(), term.getString()));
            }
        }
    }
    
    private void createChart() {
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        barChart = new BarChart<Number,String>(xAxis,yAxis);
        barChart.setPrefHeight(300);
        barChart.setTitle("Top "+TOP_DISPLAY_TERMS_AMOUNT+" predictive URL words");
        xAxis.setLabel("Score");  
        xAxis.setTickLabelRotation(90);
        yAxis.setLabel("Word");        
        barChart.getData().addAll(dataSeries);
    }
}
