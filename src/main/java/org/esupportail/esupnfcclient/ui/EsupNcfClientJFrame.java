package org.esupportail.esupnfcclient.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

@SuppressWarnings("restriction")
public class EsupNcfClientJFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(EsupNcfClientJFrame.class);
	
	private String esupNfcTagServerUrl;
	
	private String macAdress;
	
	private String numeroId;
	private String eppnInit;
	private String authType;
	private String readyToScan;

	public static WebEngine webEngine;

	public EsupNcfClientJFrame(String esupNfcTagUrl, String adressMac) throws HeadlessException {
		super();
		this.esupNfcTagServerUrl = esupNfcTagUrl;
		this.macAdress = adressMac;
		setLayout(new BorderLayout());
		setTitle("EsupNfcTag - WebCam & PCSC");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setPreferredSize(new Dimension(500, 850));
		setMinimumSize(new Dimension(500, 850));
		final JPanel mainPanel = new JPanel();
		final JFXPanel fxPanel = new JFXPanel();
		
		mainPanel.add(fxPanel);
		
		Platform.runLater(new Runnable() {
			public void run() {
		        final Group  root  =  new  Group();
		        final Scene  scene  =  new  Scene(root, javafx.scene.paint.Color.BLACK);
		        final WebView webView = new WebView();
		        webEngine = webView.getEngine();
		        webEngine.setJavaScriptEnabled(true);
		        webEngine.load(esupNfcTagServerUrl + "/nfc-index?jarVersion=" + getJarVersion() + "&imei=appliJava&macAddress=" + macAdress);
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
		        root.getChildren().add(webView);
		        fxPanel.setScene(scene);
		        webView.setPrefWidth(500);
		        webView.setPrefHeight(800);
				getContentPane().addComponentListener(new ComponentAdapter() {
					@Override
				    public void componentResized(ComponentEvent e) {
				    	mainPanel.resize(getContentPane().getWidth(), getContentPane().getHeight());
				    	fxPanel.resize(getContentPane().getWidth(), getContentPane().getHeight());
				    	webView.setPrefWidth(getContentPane().getWidth());
				    	webView.setPrefHeight(getContentPane().getHeight() - 50);
				    	
				    }
				});
			}
		});
		getContentPane().setBackground(new Color(153, 178, 178));
		JScrollPane mainScrollPane = new JScrollPane(fxPanel);
		getContentPane().add(mainPanel);
		getContentPane().add(mainScrollPane);
		setLocationRelativeTo(null);
		setVisible(true);
		
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
    	readLocalStorage();
    	return numeroId;
    }

    public String getEppnInit(){
    	readLocalStorage();
    	return eppnInit;
    }

    public String getAuthType(){
    	readLocalStorage();
    	return authType;
    }
    
    public String getReadyToScan(){
    	readLocalStorage();
    	return readyToScan;
    }
        
	public String getEsupNfcTagServerUrl() {
		return esupNfcTagServerUrl;
	}

	public String getMacAdress() {
		return this.macAdress;
	}

	public void setMacAdress(String macAdress) {
		this.macAdress = macAdress;
	}

	public void exit(){
	}

}
