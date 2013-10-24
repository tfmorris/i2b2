/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 * 
 *  rk903
 *     
 */
package edu.harvard.i2b2.frclient.security;

import java.util.Hashtable;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.frclient.security.HighEncryption;

/**
 * This is convenient  class to do encryption and decryption of 
 *  empi, encounter and notes fields.
 *  
 *  It uses same key for empi and encounter field and for notes it uses 
 *  seperate key.
 *  
 * 
 * @author rk903
 */
public class CryptUtil {

	
	private HighEncryption empiHighEnc = null;
	/**
	 * Default note key.
	 */
	private String notesKey = " ";
	
	
	/**
	 * Default constructor
	 * Uses default keys for empi, encounter and notes.
	 */
	public CryptUtil()  {
		initHighEncrypt();
	}
	
	/**
	 * Constructor to accept only notes key.
	 * Default key is used for empi and encounter encryption. 
	 * @param notesKey
	 */
	public CryptUtil(String notesKey)  {
		this.notesKey = notesKey;
		initHighEncrypt();
	}
	
	
	/**
	 * Initialize HighEncryption variable
	 * for empi and notes.
	 */
	private void initHighEncrypt() { 
		try {
			//init high encryption with empikey
			//Hashtable<String, String> hashEmpiTemp = new Hashtable<String, String>();
			//hashEmpiTemp.put("A:\\I401.txt", empiKey);
			//empiHighEnc = new HighEncryption("A:\\I401.txt", hashEmpiTemp);

			//init high encryption with notes key
			Hashtable<String, String> hashNotestemp = new Hashtable<String, String>();
			hashNotestemp.put("A:\\I401.txt", notesKey);
			empiHighEnc = new HighEncryption("A:\\I401.txt", hashNotestemp);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Return encrypted encounter ide
	 * @param encounterIde
	 * @return
	 */
	public String encryptEncounterIde(String encounterIde) { 
		try {
			return empiHighEnc.generic_encnum_encrypt(encounterIde);
		} catch (I2B2Exception e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	
	/**
	 * Return encrypted patient ide.
	 * @param patientIde
	 * @return
	 */
	public String encryptPatientIde(String patientIde) { 
		 String encryptPatientIde = empiHighEnc.mrn_encrypt(patientIde, true, "EMPI");
		 if (encryptPatientIde!=null && encryptPatientIde.trim().length()>0) { 
			 encryptPatientIde = '(' + encryptPatientIde; 
		 }
		 return  encryptPatientIde;
	}
	
	/**
	 * Decrypt encounter ide.
	 * @param encryptEncounterIde
	 * @return
	 */
	public String decryptEncounterIde(String encryptEncounterIde) {
		return null;//empiHighEnc.generic_decrypt(encryptEncounterIde);
	}
	
	/**
	 * Decrypt patient ide.
	 * @param encryptPatientIde
	 * @return
	 */
	public String decryptPatientIde(String encryptPatientIde) {
		return empiHighEnc.mrn_decrypt(encryptPatientIde,true);
	}
	
	/**
	 * Encrypt notes using notes key
	 * @param notes
	 * @return
	 */
	public String encryptNotes(String notes) { 
try {
		return empiHighEnc.generic_encrypt(notes);	
} catch (I2B2Exception e) {
	// TODO Auto-generated catch block
	return null;
}
}
	
	/**
	 * Decrypt notes with notes key.
	 * @param encrypted notes
	 * @return
	 */
	public String decryptNotes(String encryptedNotes) {
		try {
		return empiHighEnc.generic_decrypt(encryptedNotes);
		} catch (I2B2Exception e) {
			// TODO Auto-generated catch block
			return null;
		}
		}
	
	
	
}
