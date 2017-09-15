package org.esupportail.esupnfcclient;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.esupportail.esupnfcclient.service.EncodingException;
import org.esupportail.esupnfcclient.service.EncodingService;
import org.esupportail.esupnfcclient.service.pcsc.PcscException;
import org.esupportail.esupnfcclient.ui.EsupNcfClientJFrame;
import org.esupportail.esupnfcclient.utils.Utils;

public class EsupNfcClientApplication {

	private static String esupNfcTagServerUrl = "https://esup-nfc-tag.univ-ville.fr";
	
	private final static Logger log = Logger.getLogger(EsupNfcClientApplication.class);

	private static EncodingService encodingService = new EncodingService(esupNfcTagServerUrl);
	private static EsupNcfClientJFrame esupNfcClientJFrame ;

	public static void main(String... args) {

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
				if (encodingService.isCardPresent() && esupNfcClientJFrame.getReadyToScan().equals("ok"))
					break;
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
	
}
