package org.esupportail.esupnfcclient.service;

import javax.smartcardio.CardException;

import org.apache.log4j.Logger;
import org.esupportail.esupnfcclient.domain.CsnMessageBean;
import org.esupportail.esupnfcclient.domain.NfcResultBean;
import org.esupportail.esupnfcclient.service.pcsc.PcscException;
import org.esupportail.esupnfcclient.service.pcsc.PcscUsbService;
import org.esupportail.esupnfcclient.ui.EsupNcfClientJFrame;
import org.esupportail.esupnfcclient.utils.Utils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("restriction")
public class EncodingService {

	private final static Logger log = Logger.getLogger(EncodingService.class);
	
	private String esupNfcTagServerUrl;
	public String numeroId;
	public String eppnInit;
	public String authType;
	
	private RestTemplate restTemplate =  new RestTemplate(Utils.clientHttpRequestFactory());
	
	private PcscUsbService pcscUsbService = new PcscUsbService();
	
	public EncodingService(String esupNfcTagServerUrl){
		this.esupNfcTagServerUrl = esupNfcTagServerUrl;
	}
	
	public void pcscConnection() throws PcscException{
		try {
			String cardTerminalName = pcscUsbService.connection();
			log.debug("cardTerminal : " + cardTerminalName);
		} catch (CardException e) {
			throw new PcscException("pcsc connection error", e);
		}
	}
	
	public String readCsn() throws PcscException{
		try {
			String csn = pcscUsbService.byteArrayToHexString(pcscUsbService.hexStringToByteArray(pcscUsbService.getCardId()));
			log.info("csn : "+csn);
			return csn;
		} catch (CardException e) {
			log.error("csn read error" + e);
			throw new PcscException("csn read error", e);
		}
	}
	
	public String desfireNfcComm(String cardId, EsupNcfClientJFrame esupSGCJFrame) throws EncodingException, PcscException {
		String urlTest = esupNfcTagServerUrl + "/desfire-ws/?result=&numeroId="+numeroId+"&cardId="+cardId;
		NfcResultBean nfcResultBean;
		try{
			String test = restTemplate.getForObject(urlTest, String.class);
			System.err.println(test);
			nfcResultBean = restTemplate.getForObject(urlTest, NfcResultBean.class);
		}catch (Exception e) {
			throw new EncodingException("rest call error for : " + urlTest + " - " + e);
		}
		log.info("Rest call : " + urlTest);
		log.info("Result of rest call :" + nfcResultBean);
		if(nfcResultBean.getFullApdu()!=null) {
			log.info("Encoding : Start");
			String result = "";
			while(true){
				log.info("RAPDU : "+ result);
				String url = esupNfcTagServerUrl + "/desfire-ws/?result="+ result +"&numeroId="+numeroId+"&cardId="+cardId;
				nfcResultBean = restTemplate.getForObject(url, NfcResultBean.class);
				log.info("SAPDU : "+ nfcResultBean.getFullApdu());
				if(nfcResultBean.getFullApdu()!=null){
				if(!"END".equals(nfcResultBean.getFullApdu()) ) {
					try {
						result = pcscUsbService.sendAPDU(nfcResultBean.getFullApdu());
					} catch (CardException e) {
						throw new PcscException("pcsc send apdu error", e);
					}
				} else {
					log.info("Encoding  : OK");
					return nfcResultBean.getFullApdu();
				}
				}else{
					throw new EncodingException("return is null");
				}
			}
		} else {
			return nfcResultBean.getFullApdu();
		}
	}
	
	public String csnNfcComm(String cardId, EsupNcfClientJFrame esupSGCJFrame) throws EncodingException, PcscException {
		//cardId = swapPairs(hexStringToByteArray(cardId));
		CsnMessageBean nfcMsg = new CsnMessageBean();
	    nfcMsg.setNumeroId(numeroId);
	    nfcMsg.setCsn(cardId);
	    ObjectMapper mapper = new ObjectMapper();
	    String jsonInString = null;
		String url = esupNfcTagServerUrl + "/csn-ws";
		String nfcComm;
		try{
			jsonInString = mapper.writeValueAsString(nfcMsg);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> entity = new HttpEntity<String>(jsonInString, headers);
			
			
			nfcComm = restTemplate.postForObject(url, entity, String.class);
		}catch (Exception e) {
			throw new EncodingException("rest call error for : " + url + " - " + e);
		}
		return nfcComm;
	}
	

	public boolean pcscCardOnTerminal(){
		try {
			return pcscUsbService.isCardOnTerminal();
		} catch (CardException e) {
			return false;
		}
	}
	
	public boolean isCardPresent(){
		try {
			return pcscUsbService.isCardPresent();
		} catch (CardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void pcscDisconnect() throws PcscException{
		try {
			pcscUsbService.disconnect();
		} catch (PcscException e) {
			throw new PcscException(e.getMessage(), e);
		}
		Utils.sleep(1000);
	}
	
	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	public static String swapPairs(byte[] tagId) {
		String s = new StringBuilder(byteArrayToHexString(tagId)).reverse().toString();
		String even = "";
		String odd = "";
		int length = s.length();

		for (int i = 0; i <= length-2; i+=2) {
			even += s.charAt(i+1) + "" + s.charAt(i);
		}

		if (length % 2 != 0) {
			odd = even + s.charAt(length-1);
			return odd;
		} else {
			return even;
		}
	}
	
	public static String byteArrayToHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length*2];
		int v;

		for(int j=0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j*2] = hexArray[v>>>4];
			hexChars[j*2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}
	
	public static byte[] hexStringToByteArray(String s) {
		s = s.replace(" ", "");
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
}
