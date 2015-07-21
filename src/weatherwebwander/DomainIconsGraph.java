/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import weatherwebwander.genalgo.GenAlgoRunner;

/**
 *
 * @author simonkenny
 */
public class DomainIconsGraph extends GraphCanvas {
    
    private final int CONTAINER_SIZE = 180;
    
    private final DomainData domainData;
    private final SquareLayoutParams params;
    
    
    public DomainIconsGraph(DomainData domainData) {
        super();
        this.domainData = domainData;
        params = new SquareLayoutParams();
        params.containerSize = CONTAINER_SIZE;
        System.out.println("DomainIconsGraph: constructed");
        startMainThread();
    }
    
    public void prepareForExit() {
        runMainThread = false;
    }
    
    private boolean runMainThread = true;
    private void startMainThread() {
        System.out.println("DomainIconsGraph: creating main thread");
        (new Thread() {
            public void run() {
                System.out.println("DomainIconsGraph: started main thread");
                while(runMainThread) {
                    try {
                        Thread.sleep(11000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(KeywordHistogramChart.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("<<<<<******====------Updating DomainIconsGraph");
                    update();
                    System.out.println("<<<<<******====------Finished updating DomainIconsGraph");
                }
            }
        }).start();
    }
    
    private void update() {
        System.out.println("DomainIconsGraph: update()");
        // update parameters
        final List<Domain> domains = domainData.getDomains();
        if( domains.isEmpty() ) {
            System.out.println("DomainIconsGraph: no domains, doing nothing");
            return;
        }
        params.numSquares = domains.size();
        System.out.println("DomainIconsGraph: num domains: "+params.numSquares);
        params.squareSizes = new int[params.numSquares];
        for( int i = 0 ; i < params.numSquares ; i++ ) {
            //params.squareSizes[i] = domains.get(i).getCount();
            // TODO : confirm relevancy is a good metric for graph sizes
            params.squareSizes[i] = domains.get(i).getRelevancy();
            System.out.println("DomainIconsGraph: ["+i+"] "+domains.get(i).getURL()
                    +"("+domains.get(i).getCleanName()+"): size "+params.squareSizes[i]);
        }
        // calc
        params.squareSizesRoot = new int[params.numSquares];
	params.squareSizesRootHalf = new float[params.numSquares];
	params.squareSizesRootNorm = new float[params.numSquares];
	float largestRoot = 0.f;
	for( int i = 0 ; i < params.numSquares ; i++ ) {
            params.squareSizesRoot[i] = (int)Math.sqrt(params.squareSizes[i]);
            params.squareSizesRootHalf[i] = params.squareSizesRoot[i] / 2.f;
            if( params.squareSizesRoot[i] > largestRoot ) {
                    largestRoot = params.squareSizesRoot[i];
            }
	}
	for( int i = 0 ; i < params.numSquares ; i++ ) {
            params.squareSizesRootNorm[i] = params.squareSizesRoot[i] / largestRoot;
	}
        System.out.println("DomainIconsGraph: recalculated params");
        // set params in factory
        SquareLayoutEntityFactory factory = new SquareLayoutEntityFactory(params);
        // create genetic algorithm runner, passing factory class reference
        System.out.println("DomainIconsGraph: creating ga runner");
        GenAlgoRunner gaRunner = new GenAlgoRunner<>(factory);
        // run GA
        System.out.println("DomainIconsGraph: running ga");
        while( !gaRunner.isFinished() ) {
            gaRunner.processFrame();
        }
        System.out.println("DomainIconsGraph: finished running ga");
        final SquareLayoutEntity squareLayout = (SquareLayoutEntity)gaRunner.getOverallBestMostFitEntity();
        if( squareLayout != null ) {
            System.out.println("DomainIconsGraph: drawing squares layout");
            // draw based on layout
            final double canvasWidth = getWidth();
            final double canvasHeight = getHeight();
            Platform.runLater(() -> {
                PVector center = new PVector((float)canvasWidth/2,(float)canvasHeight/2);
                GraphicsContext context = getGraphicsContext2D();
                context.clearRect(0, 0, canvasWidth, canvasHeight);
                context.setStroke(Color.GREY);
                /*
                context.strokeRect(center.x - (params.containerSize/2), 
			center.y - (params.containerSize/2),
			params.containerSize, params.containerSize );
                */
		for( int i = 0 ; i < params.numSquares ; i++ ) {
                    // TODO : should the favicon even be there?
                    /*
                    Image favicon = domainData.getFavicon(i);
                    float faviconSize = (float)params.squareSizesRoot[i]
                            *squareLayout.getScaleFactor();
                    if( faviconSize > 32 ) {
                        faviconSize = 32;
                    } else if( faviconSize < 8 ) {
                        faviconSize = 8;
                    }
                            */
                    context.setFill(domainData.getColorForIdx(i));
                    context.fillRect(
                                center.x - (params.containerSize/2)
			 		+ squareLayout.getSquareCenterPos(i).x 
					- ((float)params.squareSizesRootHalf[i]
                                                *squareLayout.getScaleFactor()),
				center.y - (params.containerSize/2) 
					+ squareLayout.getSquareCenterPos(i).y
					- ((float)params.squareSizesRootHalf[i]
                                                *squareLayout.getScaleFactor()),
				(float)params.squareSizesRoot[i]
                                        *squareLayout.getScaleFactor(),
				(float)params.squareSizesRoot[i]
                                        *squareLayout.getScaleFactor()
                    );
                    /*
                    if( favicon != null ) {
                        context.drawImage(favicon,
                                center.x - (params.containerSize/2)
			 		+ squareLayout.getSquareCenterPos(i).x 
					- 16,
				center.y - (params.containerSize/2) 
					+ squareLayout.getSquareCenterPos(i).y
					- 16,
				faviconSize, faviconSize
                        );
                    }
                            */
                }
                // draw names last, make sure always on top
                for( int i = 0 ; i < params.numSquares ; i++ ) {
                    String domainName = domains.get(i).getCleanName();
                    context.setStroke(Color.color(0.2,0.2,0.2));
                    context.strokeText(domainName, 
                            center.x - (params.containerSize/2)
                                    + squareLayout.getSquareCenterPos(i).x 
                                    + ((float)params.squareSizesRootHalf[i]
                                            *squareLayout.getScaleFactor()), 
                            center.y - (params.containerSize/2) 
                                    + squareLayout.getSquareCenterPos(i).y
                                    + ((float)params.squareSizesRootHalf[i]
                                            *squareLayout.getScaleFactor())
                                    );
                            //((float)params.squareSizesRoot[i]
                            //                *squareLayout.getScaleFactor()));
		}
            });
        } else {
            System.out.println("DomainIconsGraph: no best squares layout, ga failed");
        }
    }
    
    
    @Override
    public void draw() {
        // nothing
    }
}
