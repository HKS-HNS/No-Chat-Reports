package com.aizistral.nochatreports.encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Optional;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import net.minecraft.util.StringUtil;
import net.minecraft.util.Tuple;

public abstract class AESEncryption extends Encryption {
	private final String mode, padding;
	private final boolean requiresIV;
	private final String encapsulation;

	protected AESEncryption(String mode, String padding, boolean requiresIV, String encapsulation) {
		super("aes_" + mode.toLowerCase() + "_" + encapsulation.toLowerCase(), "AES/" + mode + "+" + encapsulation);
		this.mode = mode;
		this.padding = padding;
		this.requiresIV = requiresIV;
		this.encapsulation = encapsulation;
	}

	public String getEncapsulation() {
		return encapsulation;
	}

	@Override
	public String getRandomKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);
			SecretKey key = keyGenerator.generateKey();

			return BASE64_ENCODER.encodeToString(key.getEncoded());
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getDefaultKey() {
		return "blfrngArk3chG6wzncOZ5A==";
	}

	@Override
	public boolean supportsPassphrases() {
		return true;
	}

	@Override
	public String getPassphraseKey(String passphrase) {
		try {
			byte[] salt = new byte[16];
			new Random(1738389128127L).nextBytes(salt);

			KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 128); // AES-128
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] key = factory.generateSecret(spec).getEncoded();

			return BASE64_ENCODER.encodeToString(new SecretKeySpec(key, "AES").getEncoded());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean validateKey(String key) {
		if (StringUtil.isNullOrEmpty(key))
			return false;

		try {
			this.getProcessor(key);
		} catch (InvalidKeyException ex) {
			return false;
		}

		return true;
	}

	public String getMode() {
		return this.mode;
	}

	public String getPadding() {
		return this.padding;
	}

	public boolean requiresIV() {
		return this.requiresIV;
	}

}
