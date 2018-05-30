package org.esupportail.esupnfctagdesktop.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.control.ProgressIndicator;

@SuppressWarnings("restriction")
public final class Toast
{

	static StackPane waitToast = new StackPane(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
	
	public static void makeText(final EsupNcfClientStackPane ownerStackPane, String toastMsg, final int toastDelay) {
		
        Text text = new Text(toastMsg);
        text.setFont(Font.font("Verdana", 40));
        text.setFill(Color.BLACK);

        final StackPane toast = new StackPane(text);
        toast.setStyle("-fx-background-radius: 20; -fx-background-color: rgba(0, 0, 0, 0.3); -fx-padding: 50px;");
        toast.setMaxWidth(toastMsg.length() * 10);
        toast.setMaxHeight(100);
        toast.setOpacity(0);

        ownerStackPane.getChildren().add(toast);
        
        Timeline fadeInTimeline = new Timeline();
        KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(500), new KeyValue (toast.opacityProperty(), 1)); 
        fadeInTimeline.getKeyFrames().add(fadeInKey1);   
        fadeInTimeline.setOnFinished(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent ae) {
			    new Thread(new Runnable() {
					public void run() {
					    try
					    {
					        Thread.sleep(toastDelay);
					    }
					    catch (InterruptedException e)
					    {
					        e.printStackTrace();
					    }
					       Timeline fadeOutTimeline = new Timeline();
					        KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(500), new KeyValue (toast.opacityProperty(), 0)); 
					        fadeOutTimeline.getKeyFrames().add(fadeOutKey1);   
					        fadeOutTimeline.setOnFinished(new EventHandler<ActionEvent>() {
								public void handle(ActionEvent aeb) {
									ownerStackPane.getChildren().remove(toast);
								}
							}); 
					        fadeOutTimeline.play();
					}
				}).start();
			}
		}); 
        fadeInTimeline.play();
    }
	
	public static void showProgressIndicator(final EsupNcfClientStackPane ownerStackPane) {
		
		waitToast.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");
        ownerStackPane.getChildren().add(waitToast);
    }
	
	public static void hideProgressIndicator(final EsupNcfClientStackPane ownerStackPane) {
		ownerStackPane.getChildren().remove(waitToast);
    }
	
}