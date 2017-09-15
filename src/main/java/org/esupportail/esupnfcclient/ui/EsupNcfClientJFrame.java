package org.esupportail.esupnfcclient.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

@SuppressWarnings("restriction")
public class EsupNcfClientJFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private String esupNfcTagServerUrl;
	private String macAdress;
	
	private String numeroId;
	private String eppnInit;
	private String authType;
	private String readyToScan;

	public static WebEngine webEngine;

	public EsupNcfClientJFrame(String esupNfcTagServerUrl, String macAddress) throws HeadlessException {
		super();
		this.esupNfcTagServerUrl = esupNfcTagServerUrl;
		this.macAdress = macAddress;
		setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		setLayout(new BorderLayout());
		setTitle("EsupNfcTag - WebCam & PCSC");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(500, 800);

		JPanel mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(500, 800));

		final JFXPanel fxPanel = new JFXPanel();
		mainPanel.add(fxPanel);
		
		Platform.runLater(new Runnable() {
			public void run() {
				initJavaFX(fxPanel, macAdress);
			}
		});

		getContentPane().setBackground(new Color(153, 178, 178));
		getContentPane().add(mainPanel);
		pack();
		setLocationRelativeTo(null);

		setVisible(true);
	}

    private void initJavaFX(JFXPanel fxPanel, String macAdress) {
        Scene scene = createScene(macAdress);
        fxPanel.setScene(scene);
    }
    
    private Scene createScene(String macAdress) {

    	Group  root  =  new  Group();
        Scene  scene  =  new  Scene(root, javafx.scene.paint.Color.ALICEBLUE);
        WebView webView = new WebView();
        webView.setPrefSize(500, 800);
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.load(this.esupNfcTagServerUrl + "/nfc/locations?imei=appliJava&macAddress=" + this.macAdress);
        root.getChildren().add(webView);
        return (scene);
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
