package applet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class TheApplet extends Applet {


	private final static byte CLA_TEST                    = (byte) 0x90;
	private final static byte INS_GENERATE_RSA_KEY        = (byte) 0xF6;
	private final static byte INS_RSA_ENCRYPT             = (byte) 0xA0;
	private final static byte INS_RSA_DECRYPT             = (byte) 0xA2;
	private final static byte INS_GET_PUBLIC_RSA_KEY      = (byte) 0xFE;
	private final static byte INS_PUT_PUBLIC_RSA_KEY      = (byte) 0xF4;
	private final static byte INS_DES_ENCRYPT             = (byte) 0xB0;
	private final static byte INS_DES_DECRYPT             = (byte) 0xB2;

	private final static short SW_NO_KEYS				  = (byte) 0x6300;

	// cipher instances
	private Cipher cRSA_NO_PAD;
	// key objects
	private KeyPair keyPair;
	private Key publicRSAKey, privateRSAKey;

	// cipher key length
	private short cipherRSAKeyLength;

	private final static byte[] DES_SECRET = new byte[] {
			(byte) 0x13,  (byte) 0x37,
			(byte) 0xBA,  (byte) 0xBE,
			(byte) 0xCA,  (byte) 0xFE,
			(byte) 0xDE,  (byte) 0xAD
	};

	private Cipher desECBNoPadEncrypt, desECBNoPadDecrypt;
	private Key secretDESKey;

	protected TheApplet() {
		publicRSAKey = privateRSAKey = null;
		cRSA_NO_PAD = null;

		cipherRSAKeyLength = KeyBuilder.LENGTH_RSA_1024;
		// build RSA pattern keys
		publicRSAKey = KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, cipherRSAKeyLength, true);
		privateRSAKey = KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, cipherRSAKeyLength, false);
		// get cipher RSA instance
		cRSA_NO_PAD = Cipher.getInstance((byte)0x0C, false);

		try {
			secretDESKey = KeyBuilder.buildKey(KeyBuilder.TYPE_DES, KeyBuilder.LENGTH_DES, false);
			((DESKey) secretDESKey).setKey(DES_SECRET, (short) 0);
		} catch(Exception ignored) {}

		if(secretDESKey != null)
			try {
				desECBNoPadEncrypt = Cipher.getInstance(Cipher.ALG_DES_ECB_NOPAD, false);
				desECBNoPadEncrypt.init(secretDESKey, Cipher.MODE_ENCRYPT);

				desECBNoPadDecrypt = Cipher.getInstance(Cipher.ALG_DES_ECB_NOPAD, false);
				desECBNoPadDecrypt.init(secretDESKey, Cipher.MODE_DECRYPT);
			} catch(Exception ignored) {}

		this.register();
	}


	public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
		new TheApplet();
	}


	public void process(APDU apdu) throws ISOException {
		if (selectingApplet())
			return;

		byte[] buffer = apdu.getBuffer();

		if (buffer[ISO7816.OFFSET_CLA] != CLA_TEST)
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

		switch(buffer[ISO7816.OFFSET_INS]) {
			case INS_GENERATE_RSA_KEY: generateRSAKey(); break;
			case INS_RSA_ENCRYPT: RSAEncrypt(apdu); break;
			case INS_RSA_DECRYPT: RSADecrypt(apdu); break;
			case INS_GET_PUBLIC_RSA_KEY: getPublicRSAKey(apdu); break;
			case INS_PUT_PUBLIC_RSA_KEY: putPublicRSAKey(apdu); break;
			case INS_DES_ENCRYPT: DESEncrypt(apdu); break;
			case INS_DES_DECRYPT: DESDecrypt(apdu); break;
			default: ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
		}
	}


	void generateRSAKey() {
		this.keyPair = new KeyPair(KeyPair.ALG_RSA, (short) publicRSAKey.getSize());
		keyPair.genKeyPair();
		this.publicRSAKey = keyPair.getPublic();
		this.privateRSAKey = keyPair.getPrivate();
	}


	// RSA Encrypt (with public key)
	void RSAEncrypt(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		// initialize the algorithm with default key
		this.cRSA_NO_PAD.init(publicRSAKey, Cipher.MODE_ENCRYPT);
		// compute internel test
		this.cRSA_NO_PAD.doFinal(buffer, (short) 0, (short) (cipherRSAKeyLength / 8), buffer, (short) 1);
		// compare result with the patern
		//buffer[0] = Util.arrayCompare(buffer, (short) 1, cRSAPublicEncResult, (short) 0, (short) (cipherRSAKeyLength / 8));
		// send difference
		apdu.setOutgoingAndSend((short) 0, (short) 1);
	}


	// RSA Decrypt (with private key)
	void RSADecrypt(APDU apdu) {
		apdu.setIncomingAndReceive();

		byte[] buffer = apdu.getBuffer();
		short length = (short) (buffer[4] & 0xFF);

		cRSA_NO_PAD.init(this.privateRSAKey, Cipher.MODE_DECRYPT);
		cRSA_NO_PAD.doFinal(buffer, (short) 5, (short) length, buffer, (short) 0);
		apdu.setOutgoingAndSend((short) 0, length);
	}


	void getPublicRSAKey(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		// get the element type and length
		byte keyElement = (byte) (buffer[ISO7816.OFFSET_P2] & 0xFF);
		// check correct type (modulus or exponent)
		if((keyElement != 0x00) && (keyElement != 0x01))
			ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
		// check elements request
		if(keyElement == 0)
			// retrieve modulus
			buffer[0] = (byte) ((RSAPublicKey) publicRSAKey).getModulus(buffer, (short) 1);
		else
			// retrieve exponent
			buffer[0] = (byte) ((RSAPublicKey) publicRSAKey).getExponent(buffer, (short) 1);
		// send the key element
		apdu.setOutgoingAndSend((short) 0, (short) ((buffer[0] & 0xFF) + 1));
	}


	void putPublicRSAKey(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		// get the element type and length
		byte keyElement = (byte)(buffer[ISO7816.OFFSET_P1] & 0xFF);
		short publicValueLength = (short)(buffer[ISO7816.OFFSET_LC] & 0xFF);
		// check correct type (modulus or exponent)
		if((keyElement != 0x00) && (keyElement != 0x01))
			ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
		// use data in
		apdu.setIncomingAndReceive();
		// initialize RSA public key
		// check elements length for modulus only because exponent is naturaly short
		if(keyElement == 0) {
			// loading modulus
			if(publicValueLength != (short)(cipherRSAKeyLength/8))
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
			// initialize modulus
			((RSAPublicKey)publicRSAKey).setModulus(buffer, (short)ISO7816.OFFSET_CDATA, (short)(buffer[ISO7816.OFFSET_LC] & 0xFF));
		} else
			// initialize exponent
			((RSAPublicKey)publicRSAKey).setExponent(buffer, (short)ISO7816.OFFSET_CDATA, (short)(buffer[ISO7816.OFFSET_LC] & 0xFF));
	}

	void DESDecrypt(APDU apdu) {
		apdu.setIncomingAndReceive();

		byte[] buffer = apdu.getBuffer();
		short length = (short) (buffer[4] & 0xFF);

		desECBNoPadDecrypt.doFinal(buffer, (short) 5, (short) length, buffer, (short) 0);
		apdu.setOutgoingAndSend((short) 0, length);
	}

	void DESEncrypt(APDU apdu) {
		apdu.setIncomingAndReceive();

		byte[] buffer = apdu.getBuffer();
		short length = (short) (buffer[4] & 0xFF);

		desECBNoPadEncrypt.doFinal(buffer, (short) 5, (short) length, buffer, (short) 0);
		apdu.setOutgoingAndSend((short) 0, length);
	}
}
