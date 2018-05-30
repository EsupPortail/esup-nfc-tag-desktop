package org.esupportail.esupnfctagdesktop.ui;

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

@SuppressWarnings("restriction")
public class EsupNcfClientStackPane extends StackPane {

	private final static Logger log = Logger.getLogger(EsupNcfClientStackPane.class);
	
	private String esupNfcTagServerUrl;
	
	private String numeroId;
	private String eppnInit;
	private String authType;
	private String readyToScan;
	
	public WebView webView = new WebView();
	
	private WebEngine webEngine =webView.getEngine();

	public EsupNcfClientStackPane(String esupNfcTagUrl, final String macAdress) throws HeadlessException {
		
		esupNfcTagServerUrl = esupNfcTagUrl;
		
		Platform.runLater(new Runnable() {
			public void run() {
		        webEngine.setJavaScriptEnabled(true);
		        String url = esupNfcTagServerUrl + "/nfc-index?jarVersion=" + getJarVersion() + "&imei=appliJava&macAddress=" + macAdress;
		        log.info("webView load : " + url);
		        webEngine.load(url);
		        
		        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
					public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
						JSObject window = (JSObject) webEngine.executeScript("window");
		                window.setMember("Android", new JavaScriptConsoleBridge());
		                webEngine.executeScript("window.onerror = function myErrorHandler(errorMsg, url, lineNumber) {Android.error(errorMsg)}");
					}
		        });
		        
		        webEngine.locationProperty().addListener(new ChangeListener<String>() {

					public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	                
						if(newValue.length() >= 12){
							if("download-jar".equals(newValue.substring(newValue.length() - 12))){
								try {
									FileUtils.copyURLToFile(new URL(newValue), new File("esupnfctagdesktop.jar"));
									webEngine.loadContent("<html>Téléchargment terminé dans le dossier de lancement du jar</html>", "text/html");
								} catch (IOException e) {
									log.error("jar download error", e);
								}
								
							}
						}
					}

		        });
			}
		});
		StackPane webviewPane = new StackPane(webView);
		getChildren().add(webviewPane);
	}
    
    public void readLocalStorage(){
    	Platform.runLater(new Runnable() {
    	    public void run() {
    	    	JSObject window = (JSObject) webEngine.executeScript("window");
    	    	numeroId = window.getMember("numeroId").toString();
    	    	eppnInit = window.getMember("eppnInit").toString();
    	    	authType = window.getMember("authType").toString();
    	    	readyToScan = window.getMember("readyToScan").toString();
    	    }
    	});
    }
    
    private String getJarVersion() {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(
    		    this.getClass().getResourceAsStream("/versionJar.txt")));
        try {
        	String version = reader.readLine();
        	log.info("jar version is : " + version);
			return version;
		} catch (IOException e) {
			log.error("read version error", e);
		}
        return null;
    }
    
    public String getNumeroId(){
    	return numeroId;
    }

    public String getEppnInit(){
    	return eppnInit;
    }

    public String getAuthType(){
    	return authType;
    }
    
    public String getReadyToScan(){
    	return readyToScan;
    }
        
	public String getEsupNfcTagServerUrl() {
		return esupNfcTagServerUrl;
	}

}
