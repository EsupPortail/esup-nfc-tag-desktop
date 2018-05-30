package org.esupportail.esupnfctagdesktop;

import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.esupportail.esupnfctagdesktop.service.EncodingException;
import org.esupportail.esupnfctagdesktop.service.EncodingService;
import org.esupportail.esupnfctagdesktop.service.pcsc.PcscException;
import org.esupportail.esupnfctagdesktop.ui.EsupNcfClientStackPane;
import org.esupportail.esupnfctagdesktop.ui.Toast;
import org.esupportail.esupnfctagdesktop.utils.Utils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


@SuppressWarnings("restriction")
public class EsupNfcTagDesktopApplication extends Application {
	
	private final static Logger log = Logger.getLogger(EsupNfcTagDesktopApplication.class);
	
	private static String esupNfcTagServerUrl;
	
	private static EncodingService encodingService;
	
	private static EsupNcfClientStackPane esupNfcClientStackPane ;

	public static void main(String... args) {
		
		Properties prop = new Properties();
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("esupnfctag.properties");
		try {
			prop.load(in);
		} catch (IOException e1) {
			log.error("sgcUrl not found");
		} 
		
		esupNfcTagServerUrl = prop.getProperty("esupNfcTagServerUrl");
		encodingService = new EncodingService(esupNfcTagServerUrl);

		log.info("Startup OK");
		log.info("esupNfcTagServerUrl : " + esupNfcTagServerUrl);

		launch();
	}
	
	@Override
	public void start(final Stage primaryStage) throws Exception {
		
		VBox root = new VBox();
		
		esupNfcClientStackPane = new EsupNcfClientStackPane(esupNfcTagServerUrl, getMacAddress());

		root.getChildren().add(esupNfcClientStackPane);

		final Scene scene = new Scene(root, 500, 800);
		
		scene.widthProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
				esupNfcClientStackPane.webView.setPrefWidth(scene.getWidth());
			}
		});
		
		scene.heightProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
				esupNfcClientStackPane.webView.setPrefHeight(scene.getHeight());
			}
		});

		
		primaryStage.setTitle("Esup-NFC-Tag-Desktop");
		primaryStage.setScene(scene);
		primaryStage.setMinWidth(500);
		primaryStage.setMinHeight(500);
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    public void handle(WindowEvent event) {
		    	exit();
		    }
		});

		primaryStage.show();
	    
		launchEncodingLoop();
		
	}
	
	public static void launchEncodingLoop() {
		
		Task<Void> task = new Task<Void>() {
		    @Override 
		    public Void call() {

				while (true) {
					while (true) {
						updateParams();
						try{
							if (encodingService.isCardPresent() && esupNfcClientStackPane.getReadyToScan().equals("ok")) {
								showWaitingToast();
								break;
							}
						}catch (Exception e){
							String message = "pcsc error : " + e.getMessage();
							log.error(message);
							launchToast(message);
						}
						Utils.sleep(1000);
					}
		
					try {
						encodingService.pcscConnection();
						String csn = encodingService.readCsn();
						String encodingResult;
						if (encodingService.authType.equals("CSN")) {
							encodingResult = encodingService.csnNfcComm(csn);
						} else {
							encodingResult = encodingService.desfireNfcComm(csn);
						}
						esupNfcClientStackPane.getReadyToScan();
						if ("END".equals(encodingResult)) {
							playSound("success.wav");
							log.info("Encoding :  OK");
						} else {
							playSound("fail.wav");
							log.warn("Nothing to do - message from server : " + encodingResult);
						}
						hideWaitingToast();
						while (!encodingService.pcscCardOnTerminal());
						encodingService.pcscDisconnect();
					} catch (PcscException e) {
						hideWaitingToast();
						launchToast("pcsc error");
						log.error("pcsc error", e);
					} catch (EncodingException e) {
						hideWaitingToast();
						launchToast("Invalid tag");
						playSound("fail.wav");
						log.error("encoding error" , e);
						while (!encodingService.pcscCardOnTerminal());
					}
					Utils.sleep(1000);
				}
		    }
		};
		
		new Thread(task).start();
		
	}
	
	private static void launchToast(final String text){
		Platform.runLater(new Runnable() {
			public void run() {
					Toast.makeText(esupNfcClientStackPane, text, 2000);
			}
		});
	}
	
	private static void showWaitingToast(){
		Platform.runLater(new Runnable() {
			public void run() {
				Toast.showProgressIndicator(esupNfcClientStackPane);
			}
		});
	}

	private static void hideWaitingToast(){
		Platform.runLater(new Runnable() {
			public void run() {
				Toast.hideProgressIndicator(esupNfcClientStackPane);
			}
		});
	}
	
	private static void playSound(final String soundFile) {
		try {
			Media media = new Media(EsupNfcTagDesktopApplication.class.getResource("/sound/"+soundFile).toExternalForm());
			MediaPlayer player = new MediaPlayer(media);
			player.play();
		} catch (Exception e) {
			log.error("error play sound" , e);
		}

	}
	
	private static void updateParams() {
		while (true) {
			esupNfcClientStackPane.readLocalStorage();
			encodingService.numeroId = esupNfcClientStackPane.getNumeroId();
			encodingService.eppnInit = esupNfcClientStackPane.getEppnInit();
			encodingService.authType = esupNfcClientStackPane.getAuthType();

			if (encodingService.numeroId == null || encodingService.numeroId.equals("undefined")) {
				Utils.sleep(1000);
				continue;
			} else {
				break;
			}
		}
		log.trace("numeroId = " + encodingService.numeroId);
		log.trace("eppnInit = " + encodingService.eppnInit);
		log.trace("authType = " + encodingService.authType);
		log.trace("ready = " + esupNfcClientStackPane.getReadyToScan());
	}
	
	private static String getMacAddress(){
    	Enumeration<NetworkInterface> netInts = null;
    	try {
			netInts = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			log.error("error get network int list");
		}
    	final StringBuilder sb = new StringBuilder();
		while(true) {
			byte[] mac = null;
			try {
				NetworkInterface netInf = netInts.nextElement();
				mac = netInf.getHardwareAddress();
				if(mac != null) {
					if(mac.length>0) {
				    	for (int i = 0; i < mac.length; i++) {
				    	        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
				    	}	
			    		break;
					}
		    	}
			} catch (Exception e) {
				log.error("mac address read error");
			}

		}
		return sb.toString();
	}

	private static void exit() {
		System.gc();
		System.exit(0);
	}
	
}
