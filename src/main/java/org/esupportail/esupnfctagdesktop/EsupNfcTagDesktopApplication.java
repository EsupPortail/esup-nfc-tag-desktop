package org.esupportail.esupnfctagdesktop;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.esupportail.esupnfctagdesktop.service.EncodingException;
import org.esupportail.esupnfctagdesktop.service.EncodingService;
import org.esupportail.esupnfctagdesktop.service.pcsc.PcscException;
import org.esupportail.esupnfctagdesktop.ui.EsupNcfClientJFrame;
import org.esupportail.esupnfctagdesktop.utils.Utils;

public class EsupNfcTagDesktopApplication {

	private static String esupNfcTagServerUrl;
	
	private final static Logger log = Logger.getLogger(EsupNfcTagDesktopApplication.class);

	private static EsupNcfClientJFrame esupNfcClientJFrame ;

	public static void main(String... args) {
		
		Properties prop = new Properties();
		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("esupnfctag.properties");
		try {
			prop.load(in);
		} catch (IOException e1) {
			log.error("sgcUrl not found");
		} 
		
		esupNfcTagServerUrl = prop.getProperty("esupNfcTagServerUrl");
		EncodingService encodingService = new EncodingService(esupNfcTagServerUrl);		

		log.info("Startup OK");
		log.info("esupNfcTagServerUrl : " + esupNfcTagServerUrl);

		esupNfcClientJFrame = new EsupNcfClientJFrame(esupNfcTagServerUrl, getMacAddress());
		
		addJframeListerners();

		while (true) {
			encodingService.numeroId = esupNfcClientJFrame.getNumeroId();
			encodingService.eppnInit = esupNfcClientJFrame.getEppnInit();
			encodingService.authType = esupNfcClientJFrame.getAuthType();

			if (encodingService.numeroId == null || encodingService.numeroId.equals("undefined")) {
				Utils.sleep(1000);
				continue;
			} else {
				break;
			}
		}
		log.info("numeroId = " + encodingService.numeroId);
		log.info("eppnInit = " + encodingService.eppnInit);
		log.info("authType = " + encodingService.authType);
		log.info("ready = " + esupNfcClientJFrame.getReadyToScan());

		while (true) {
			while (true) {
				try{
				if (encodingService.isCardPresent() && esupNfcClientJFrame.getReadyToScan().equals("ok"))
					break;
				}catch (Exception e){
					String message = "pcsc error : " + e.getMessage();
					log.error(message);
					errorDialog(message, "ERROR");
				}
				Utils.sleep(1000);
			}

			try {
				encodingService.pcscConnection();
				String csn = encodingService.readCsn();
				String encodingResult;
				if (encodingService.authType.equals("CSN")) {
					encodingResult = encodingService.csnNfcComm(csn, esupNfcClientJFrame);
				} else {
					encodingResult = encodingService.desfireNfcComm(csn, esupNfcClientJFrame);
				}
				esupNfcClientJFrame.getReadyToScan();
				if ("END".equals(encodingResult)) {
					log.info("Encoding :  OK");
				} else {
					log.warn("Nothing to do - message from server : " + encodingResult);
				}
				while (!encodingService.pcscCardOnTerminal())
					;
				encodingService.pcscDisconnect();
			} catch (PcscException e) {
				log.error("pcsc error : " + e.getMessage(), e);
			} catch (EncodingException e) {
				log.error("encoding error : " + e.getMessage(), e);
				while (!encodingService.pcscCardOnTerminal());
			}
		}
	}

	private static void addJframeListerners() {
		esupNfcClientJFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
	}

	private static void exit() {
		esupNfcClientJFrame.exit();
		System.gc();
		System.exit(0);
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
	
	public static void errorDialog(String message, String subject){
		JFrame frame = new JFrame();
		JOptionPane.showMessageDialog(frame, message, subject, JOptionPane.ERROR_MESSAGE);
		exit();
	}
	  
	
}
