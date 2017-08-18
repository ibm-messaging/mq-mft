/**
 * 
 */
package mft.secure;
/**
 * ==================================================================================
 * Copyright (c) IBM Corporation 2017
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *  Contributors:
 *    Shashikanth Rao T - Initial Contribution
 *    
 * ==================================================================================
 * 
 * This class implements encryption/decryption of files using BouncyCastle PGP Implementation
 * and is based on sample https://github.com/damico/OpenPgp-BounceCastle-Example/blob/master/src/org/jdamico/bc/openpgp/utils/PgpHelper.java
 * 
 * ==================================================================================
 * 
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;

import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;

/**
 * CryptDecryptUtil - Implements Encryption/Decryption
 *
 */
public class CryptDecryptUtil {
	// Identifier for Key Flags sub packet that defines usage type
    private static final int   KEY_FLAGS = 27;
    // Certification type
    private static final int[] MASTER_KEY_CERTIFICATION_TYPES = new int[]{
    	PGPSignature.POSITIVE_CERTIFICATION,
    	PGPSignature.CASUAL_CERTIFICATION,
    	PGPSignature.NO_CERTIFICATION,
    	PGPSignature.DEFAULT_CERTIFICATION
    };

    private String passPhrase = null;
    private KeyFingerPrintCalculator fingerPrintCalculator = null;
    private String keyRing = null;
    
    private boolean initialized = false;
    private PGPPublicKey publicKey = null;    
    private boolean enableDebugLog = false;
    
    /**
     * Constructor
     * @param keyRingFileName
     * @param debug
     * @throws Exception
     */
    public CryptDecryptUtil(final String keyRingFileName, final String passPhrase, final boolean debug) throws Exception {
    	this.fingerPrintCalculator = new BcKeyFingerprintCalculator();
    	this.keyRing = keyRingFileName;
    	this.enableDebugLog = debug;
    	this.passPhrase = passPhrase;
    }
    
    /**
     * Initialise
     * 
     * @param readPrivateKey
     * @throws FileNotFoundException
     * @throws IOException
     * @throws PGPException
     * @throws CryptDecryptException 
     */
    private void initialise(final boolean readPrivateKey) throws FileNotFoundException, IOException, PGPException, CryptDecryptException {
    	if(enableDebugLog)Trace.logInfo("CryptDecryptUtil.initialise", "Entry");
    	
    	Security.addProvider(new BouncyCastleProvider());

    	// Read and cache public key
    	if(!readPrivateKey) {
    		if(enableDebugLog)Trace.logInfo("CryptDecryptUtil.initialise", "Reading PublicKey");
    		publicKey = readPublicKey();
    	} else {
    		
    	}
    	
    	initialized = true;
    	if(enableDebugLog)Trace.logInfo("CryptDecryptUtil.initialise", "Exit");
    }
    

    /**
     * Set passphrase for private key store
     * @param passphrase
     */
	public void setPassphrase(String passphrase) {
		this.passPhrase = passphrase;
	}

    /**
     * Reads the public key from the specified key file.
     * 
     * @param in - Stream pointing to key store.
     * @param fingerPrintCalculator - An alogrithm to be used for fingerprint calculation. 
     *                                This sample uses BcKeyFingerprintCalculator algorithm.
     * @return Returns a public key read from the specified key file. 
     * @throws IOException
     * @throws PGPException
     * @throws CryptDecryptException 
     */
    private PGPPublicKey readPublicKey() throws IOException, PGPException, CryptDecryptException {
    	if(enableDebugLog)Trace.logInfo("CryptDecryptUtil.readPublicKey", "Entry");
        PGPPublicKey publicKey = null;

		InputStream publicKeyStream = new FileInputStream(keyRing);

    	// Read the key file using BouncyCastle utility class to build a collection.
        PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKeyStream), fingerPrintCalculator);

        // Iterate through the key rings to find a key that can be used for encryption
        Iterator<PGPPublicKeyRing> rIt = keyRingCollection.getKeyRings();
        while (publicKey == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = rIt.next();
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
            while (publicKey == null && kIt.hasNext()) {
                PGPPublicKey key = kIt.next();
                if (key.isEncryptionKey()) {
                    publicKey = key;
                }
            }
        }

        // The key store does not contain any key.
        if (publicKey == null) {
            throw new CryptDecryptException("Can't find public key in the key ring.");
        }
        
        // Check if the key can be used for encryption. If not throw an exception
        if (!isForEncryption(publicKey)) {
            throw new CryptDecryptException("KeyID " + publicKey.getKeyID() + " not flagged for encryption.");
        }

        if(enableDebugLog)Trace.logInfo("CryptDecryptUtil.readPublicKey", "Exit");
        return publicKey;
    }

    /**
     * Load a secret key ring collection from keyIn and find the private key corresponding to
     * keyID if it exists.
     *
     * @param keyIn input stream representing a key ring collection.
     * @param keyID keyID we want.
     * @param pass passphrase to decrypt secret key with.
     * @return
     * @throws IOException
     * @throws PGPException
     * @throws NoSuchProviderException
     */
    private PGPPrivateKey findPrivateKey(InputStream keyIn, long keyID, char[] pass,  KeyFingerPrintCalculator fingerPrintCalculator)
    	throws IOException, PGPException, NoSuchProviderException {
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn), fingerPrintCalculator);
        return findPrivateKey(pgpSec.getSecretKey(keyID), pass);

    }

    /**
     * Get private key using the given passphrase
     * @param pgpSecKey The secret key
     * @param pass passphrase to decrypt secret key with
     * @return Private key
     * @throws PGPException
     */
    private PGPPrivateKey findPrivateKey(PGPSecretKey pgpSecKey, char[] pass)
    	throws PGPException {
    	if (pgpSecKey == null) return null;

        PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(pass);
        return pgpSecKey.extractPrivateKey(decryptor);
    }

    /**
     * Decrypt the given encrypted file.
     * 
     * @param in - Stream pointing to encrypted file
     * @param out - Stream pointing to decrypt file
     * @param keyIn - Private key
     * @param passwd - Password for the private key
     * @param fingerPrintCalculator - Algorithm to be used for calculating fingerprint.
     * @throws Exception
     */
	public void decryptFile(final String encryptedFile, final String decryptedFile) throws Exception {		
		if(enableDebugLog)Trace.logInfo("CryptDecryptUtil.decryptFile", "Entry");
    	
		InputStream encryptedStream = null;
		OutputStream decryptedStream = null;
		InputStream privateKeyStream = null;
		
		try {
			// Do initialization if not done already
			privateKeyStream = new FileInputStream(keyRing);
			
			// Get a stream for writing decrypted file
	        decryptedStream = new FileOutputStream(decryptedFile);
	        // Get the stream for encrypted file
	        encryptedStream = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(new FileInputStream(encryptedFile));
	        // Initialize factory to read PGP objects such as keys, key rings and key ring collections, or PGP encrypted data.
	        PGPObjectFactory pgpFactory = new PGPObjectFactory(encryptedStream, fingerPrintCalculator);

	        PGPEncryptedDataList encryptedObjectList;
	        Object o = pgpFactory.nextObject();
	        // the first object might be a PGP marker packet.
	        if (o instanceof  PGPEncryptedDataList) {
	        	encryptedObjectList = (PGPEncryptedDataList) o;
	        } else {
	        	encryptedObjectList = (PGPEncryptedDataList) pgpFactory.nextObject();
	        }

	        // Find the private key with the given list of encrypted objects
	        PGPPublicKeyEncryptedData pbe = null;
	        PGPPrivateKey secretKey = null;

	        @SuppressWarnings("unchecked")
			Iterator<PGPPublicKeyEncryptedData> it = encryptedObjectList.getEncryptedDataObjects();
	        
	        while (secretKey == null && it.hasNext()) {
	            pbe = it.next();
	            secretKey = findPrivateKey(privateKeyStream, pbe.getKeyID(), passPhrase.toCharArray(), fingerPrintCalculator);
	        }

	        if (secretKey == null) {
	            throw new IllegalArgumentException("Secret key for message not found.");
	        } else {
	        	if(enableDebugLog)Trace.logInfo("CryptDecryptUtil.decryptFile", "Key found");	        	
	        }
	        
	        InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(secretKey));
	        PGPObjectFactory plainFact = new PGPObjectFactory(clear, fingerPrintCalculator);
	        Object message = plainFact.nextObject();

	        if (message instanceof  PGPCompressedData) {
	            PGPCompressedData cData = (PGPCompressedData) message;
	            PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream(), fingerPrintCalculator);
	            message = pgpFact.nextObject();
	        }

	        if (message instanceof  PGPLiteralData) {
	            PGPLiteralData ld = (PGPLiteralData) message;
	            InputStream unc = ld.getInputStream();
	            int ch;

	            while ((ch = unc.read()) >= 0) {
	            	decryptedStream.write(ch);
	            }
	        } else if (message instanceof  PGPOnePassSignatureList) {
	            throw new PGPException("Encrypted message contains a signed message - not literal data.");
	        } else {
	            throw new PGPException("Message is not a simple encrypted file - type unknown.");
	        }

	        if (pbe.isIntegrityProtected()) {
	            if (!pbe.verify()) {
	            	throw new PGPException("Message failed integrity check");
	            }
	        }
		}catch (Exception ex) {
			Trace.logException("CryptDecryptUtil.decryptFile", ex);
		}finally {
			if(decryptedStream != null)
				decryptedStream.close();
			if(encryptedStream != null)
				encryptedStream.close();
			if(privateKeyStream != null)
				privateKeyStream.close();
			if(enableDebugLog)Trace.logInfo("CryptDecryptUtil.decryptFile", "Exit");
		}
    }

	/**
	 * Encrypt the given file 
	 * @param unencryptedFileName - Name of the unecrypted file
	 * @param encryptedFileName - Name of the encrypted file, will have .enc as extension
	 * @throws IOException 
	 * @throws NoSuchProviderException
	 * @throws PGPException
	 * @throws CryptDecryptException 
	 */
    public void encryptFile(final String unencryptedFileName, final String encryptedFileName)
        throws IOException, NoSuchProviderException, PGPException, CryptDecryptException {
    	if(enableDebugLog)Trace.logInfo("CryptDecryptUtil.encryptFile", "Entry");

    	// Initialise PGP provider and read public key
    	if(!initialized) initialise(false);
		
		FileOutputStream encrytedFile = new FileOutputStream(encryptedFileName);
		        
		// Compress the input plain text file in ZIP format.
    	ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
        PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY, new File(unencryptedFileName) );
        comData.close();

        // Encrypt the file using Triple-DES algorithm
        BcPGPDataEncryptorBuilder dataEncryptor = new BcPGPDataEncryptorBuilder(PGPEncryptedData.TRIPLE_DES);
        dataEncryptor.setWithIntegrityPacket(false);
        dataEncryptor.setSecureRandom(new SecureRandom());
        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(dataEncryptor);
        encryptedDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(publicKey));
        byte[] bytes = bOut.toByteArray();
        OutputStream cOut = encryptedDataGenerator.open(encrytedFile, bytes.length);
        cOut.write(bytes);
        cOut.close();
        encrytedFile.close();
        
        if(enableDebugLog)Trace.logInfo("CryptDecryptUtil.encryptFile", "Exit");
    }


    /**
     * Can public key be used for encryption
     * @param key - Public key
     * @return true if public key has required type of subkeys for encryption else false
     */
    private boolean isForEncryption(PGPPublicKey key){
        if (key.getAlgorithm() == PublicKeyAlgorithmTags.RSA_SIGN
            || key.getAlgorithm() == PublicKeyAlgorithmTags.DSA
            || key.getAlgorithm() == PublicKeyAlgorithmTags.ECDSA){
            return false;
        }

        return hasKeyFlags(key, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
    }

    /**
     * Check if the public key has the required key types that can be used for encryption
     * @param encKey Public key used of encryption
     * @param keyUsage - Usage type - Encrypt Communication or Storage
     * @return true if Key has sub keys matching encryption requirement
     */
    @SuppressWarnings("unchecked")
	private boolean hasKeyFlags(PGPPublicKey encKey, int keyUsage) {
        if (encKey.isMasterKey()) {
            for (int i = 0; i != CryptDecryptUtil.MASTER_KEY_CERTIFICATION_TYPES.length; i++) {
                for (Iterator<PGPSignature> eIt = encKey.getSignaturesOfType(CryptDecryptUtil.MASTER_KEY_CERTIFICATION_TYPES[i]); eIt.hasNext();) {
                    PGPSignature sig = eIt.next();
                    if (!isMatchingUsage(sig, keyUsage)) {
                        return false;
                    }
                }
            }
        }
        else {
            for (Iterator<PGPSignature> eIt = encKey.getSignaturesOfType(PGPSignature.SUBKEY_BINDING); eIt.hasNext();) {
                PGPSignature sig = eIt.next();
                if (!isMatchingUsage(sig, keyUsage)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if the PGPSignature contains sub keys that suit our usage. We need 
     * sub keys that are suitable for encrypting storage. 
     * @param sig - PGPSignature
     * @param keyUsage - Usage type - Encrypt storage or communication
     * @return true if a sub key for encrypting storage is found else false 
     */
    private boolean isMatchingUsage(PGPSignature sig, int keyUsage) {
        if (sig.hasSubpackets()) {
            PGPSignatureSubpacketVector sv = sig.getHashedSubPackets();
            if (sv.hasSubpacket(CryptDecryptUtil.KEY_FLAGS)) {
            	if ((sv.getKeyFlags() == 0 && keyUsage == 0)) {
            		return false;
            	}
            }
        }
        return true;
    }
}
