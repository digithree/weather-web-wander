/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/**
 *
 * @author simonkenny
 */
public class DomainList {
    
    private final Pane pane;
    
    ListView<String> list = new ListView<String>();
    ObservableList<String> data = FXCollections.observableArrayList();
    
    
    public DomainList(Pane pane) {
        this.pane = pane;
        list.setItems(data);
        list.setCellFactory(new Callback<ListView<String>, 
            ListCell<String>>() {
                @Override 
                public ListCell<String> call(ListView<String> list) {
                    return new DomainCell();
                }
            }
        );
        pane.getChildren().add(list);
        startMainThread();
    }
    
    private boolean runMainThread = true;
    private void startMainThread() {
        (new Thread() {
            public void run() {
                while(runMainThread) {
                    try {
                        Thread.sleep(11000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(KeywordHistogramChart.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //System.out.println("<<<<<******====------Updating chart");
                    updateData();
                }
            }
        }).start();
    }
    
    public void prepareForExit() {
        runMainThread = false;
    }
    
    private void updateData() {
        List<String> domainData = DomainData.getInstance().getDomains();
        DomainData.getInstance().updateFavicons();
        List<String> toRemove = new ArrayList<>();
        for( String listStr : data ) {
            boolean match = false;
            for( String domainStr : domainData ) {
                if( domainStr.equals(listStr) ) {
                    match = true;
                }
            }
            if( !match ) {
                toRemove.add(listStr);
            }
        }
        List<String> toAdd = new ArrayList<>();
        for( String domainStr : domainData ) {
            boolean match = false;
            for( String listStr : data ) {
                if( listStr.equals(domainStr) ) {
                    match = true;
                }
            }
            if( !match ) {
                toAdd.add(domainStr);
            }
        }
        System.out.println("------------------Starting remove / add of list data");
        Platform.runLater(() -> {
            data.removeAll(toRemove);
            data.addAll(toAdd);
            System.out.println("------------------Finished");
        });
    }
    
    static class DomainCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            System.out.println("Making DomainCell: "+item);
            //this.getChildren().removeAll();
            HBox hBox = new HBox();
            hBox.setSpacing(20);
            if( item != null ) {
                Image image = DomainData.getInstance().getFavicons().get(item);
                if( image != null ) {
                    hBox.getChildren().addAll(new ImageView(image), new Label(item));
                } else {
                    hBox.getChildren().add(new Label(item));
                }
            } else {
                //hBox.getChildren().add(new Label("Empty item"));
            }
            setGraphic(hBox);
        }
    }
}
