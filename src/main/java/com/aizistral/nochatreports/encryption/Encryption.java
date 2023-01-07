package com.aizistral.nochatreports.encryption;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base16;

public abstract class Encryption {
	private static final List<Encryption> REGISTERED = new ArrayList<>();
	protected static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
	protected static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
	protected static final Base16 BASE16 = new Base16(false);

	public static final AESCFB8Encryption AES_CFB8_BASE64 = new AESCFB8Encryption("Base64");
	public static final AESCFB8Encryption AES_CFB8_BASE64R = new AESCFB8Encryption("Base64R");
	public static final AESCFB8Encryption AES_CFB8_SUS16 = new AESCFB8Encryption("Sus16");
	public static final AESCFB8Encryption AES_CFB8_MC256 = new AESCFB8Encryption("MC256");
	public static final AESGCMEncryption AES_GCM = new AESGCMEncryption();
	public static final AESECBEncryption AES_ECB = new AESECBEncryption();
	public static final CaesarEncryption CAESAR = new CaesarEncryption();

	private final String id, name;

	protected Encryption(String id, String name) {
		this.id = id;
		this.name = name;

		if (REGISTERED.stream().filter(e -> e.getID().equals(id) || e.getName().equals(name))
				.findAny().isPresent())
			throw new IllegalStateException("Duplicate encryption algorithm registered! ID: " + this.getID() +
					", Name: " + this.getName());

		REGISTERED.add(this);
	}

	public String getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public abstract String getRandomKey();

	public abstract String getDefaultKey();

	public abstract boolean supportsPassphrases();

	public abstract String getPassphraseKey(String passphrase) throws UnsupportedOperationException;

	public abstract boolean validateKey(String key);

	public abstract Encryptor<?> getProcessor(String key) throws InvalidKeyException;

	public abstract Encryptor<?> getRandomProcessor();

	public static List<Encryption> getRegistered() {
		return Collections.unmodifiableList(REGISTERED);
	}

}
