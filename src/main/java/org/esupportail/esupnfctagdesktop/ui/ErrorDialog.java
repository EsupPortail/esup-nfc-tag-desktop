package org.esupportail.esupnfctagdesktop.ui;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ErrorDialog extends JApplet {

	private static final long serialVersionUID = 4376458833316306355L;
	
	private static String message;
	
	public ErrorDialog(String message) {
		this.message = message;
	}
	
	public static void main() {
	    JOptionPane.showMessageDialog(new JFrame(), message, "Dialog",
	        JOptionPane.ERROR_MESSAGE);
	}
}