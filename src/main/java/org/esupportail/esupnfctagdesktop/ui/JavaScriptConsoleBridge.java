package org.esupportail.esupnfctagdesktop.ui;

import org.apache.log4j.Logger;

public class JavaScriptConsoleBridge {
	
	private final static Logger log = Logger.getLogger(JavaScriptConsoleBridge.class);

	public void disconnect() {
		System.exit(0);
    }
	
    public void info(String text) {
        log.info("Console Javascript : " + text);
    }

    public void error(String text) {
        log.error("Console Javascript : " + text);
    }
    
    public void warn(String text) {
        log.warn("Console Javascript : " + text);
    }
}
