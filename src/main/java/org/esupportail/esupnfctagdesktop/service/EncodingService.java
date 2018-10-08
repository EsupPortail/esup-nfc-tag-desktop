package org.esupportail.esupnfctagdesktop.service;

import javax.smartcardio.CardException;

import org.apache.log4j.Logger;
import org.esupportail.esupnfctagdesktop.domain.CsnMessageBean;
import org.esupportail.esupnfctagdesktop.domain.NfcResultBean;
import org.esupportail.esupnfctagdesktop.service.pcsc.PcscException;
import org.esupportail.esupnfctagdesktop.service.pcsc.PcscUsbService;
import org.esupportail.esupnfctagdesktop.utils.Utils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	public String desfireNfcComm(String cardId) throws EncodingException, PcscException {
		NfcResultBean nfcResultBean = null;
		log.info("Encoding : Start");
		String result = "";
		while(true){
			String url = esupNfcTagServerUrl + "/desfire-ws/?result="+ result +"&numeroId="+numeroId+"&cardId="+cardId;
			Response response = null;
			if(result != "") {
				String msg = result.substring(result.length() - 2);
				response = Response.getResponse(Integer.parseInt(msg, 16));
				log.info("RAPDU : " + result + ", with status : " + response);
			}
			if(response == null || response.equals(Response.OPERATION_OK) || response.equals(Response.ADDITIONAL_FRAME)) {
				try{
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
				}catch (Exception e){
					throw new EncodingException("Unknow exception on desfire comm", e);
				}
			} else {
				throw new EncodingException("desfire status error : " + response);
			}
		}
	}
	
	public String csnNfcComm(String cardId) throws EncodingException, PcscException {
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
		    throw new EncodingException("rest call error for : " + url, e);
		}
		return nfcComm;
	}
	

	public boolean pcscCardOnTerminal() {
		try {
			return pcscUsbService.isCardOnTerminal();
		} catch (CardException e) {
			return false;
		}
	}
	
	public boolean isCardPresent() throws CardException{
		return pcscUsbService.isCardPresent();
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
	
	public enum Response {
		OPERATION_OK				(0x00),
		NO_CHANGES					(0x0C),
		OUT_OF_EEPROM_ERROR			(0x0E),
		ILLEGAL_COMMAND_CODE		(0x1C),
		INTEGRITY_ERROR				(0x1E),
		NO_SUCH_KEY					(0x40),
		LENGTH_ERROR				(0x7E),
		PERMISSION_DENIED			(0x9D),

		/** A parameter has an invalid value. */
		PARAMETER_ERROR				(0x9E),

		APPLICATION_NOT_FOUND		(0xA0),
		APPLICATION_INTEGRITY_ERROR	(0xA1),

		/** Current authentication status does not allow the requested command. */
		AUTHENTICATION_ERROR		(0xAE),

		ADDITIONAL_FRAME			(0xAF),
		BOUNDARY_ERROR				(0xBE),
		PICC_INTEGRITY_ERROR		(0xC1),

		/** Previous command was incomplete. Not all frames were read. */
		COMMAND_ABORTED				(0xCA),

		PICC_DISABLED_ERROR			(0xCD),

		/** Maximum number of applications reached. */
		COUNT_ERROR					(0xCE),

		DUPLICATE_ERROR				(0xDE),
		EEPROM_ERROR				(0xEE),
		FILE_NOT_FOUND				(0xF0),
		FILE_INTEGRITY_ERROR		(0xF1),

		/** Card sent back the wrong nonce. */
		COMPROMISED_PCD				(1002),

		// nfcjlib custom codes
		WRONG_ARGUMENT				(1001),
		UNKNOWN_CODE				(2013);

		private final int code;

		private Response(int code) {
			this.code = code;
		}

		private int getCode() {
			return this.code;
		}

		public static Response getResponse(int code) {
			for (Response s : Response.values())
				if (code == s.getCode())
					return s;
			return UNKNOWN_CODE;
		}

	}

}
