package com.aizistral.nochatreports.core;

import java.util.Optional;

import com.aizistral.nochatreports.NoChatReports;
import com.aizistral.nochatreports.config.NCRConfig;
import com.aizistral.nochatreports.encryption.AESEncryption;
import com.aizistral.nochatreports.encryption.AESEncryptor;
import com.aizistral.nochatreports.encryption.Encryptor;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;

public class EncryptionUtil {

	public record DetailedDecryptionInfo(Component decrypted, int keyIndex, @Nullable String encapsulation) {}

	public static Optional<Component> tryDecrypt(Component component) {
		// Try out all encryptors
		int index = 0;
		for(Encryptor<?> encryption : NCRConfig.getEncryption().getAllEncryptors()) {
			Component copy = recreate(component);

			if(tryDecrypt(copy, encryption)) {
				return Optional.of(copy);
			}
			index++;
		}
		return Optional.empty();
	}

	public static Optional<DetailedDecryptionInfo> tryDecryptDetailed(Component component) {
		// Try out all encryptors
		int index = 0;
		for(Encryptor<?> encryption : NCRConfig.getEncryption().getAllEncryptors()) {
			Component copy = recreate(component);

			if(tryDecrypt(copy, encryption)) {
				String encapsulation = null;
				if(encryption instanceof AESEncryptor<?> aesEncryption)
					encapsulation = aesEncryption.getDecryptLastUsedEncapsulation();
				return Optional.of(new DetailedDecryptionInfo(copy, index, encapsulation));
			}
			index++;
		}
		return Optional.empty();
	}

	public static boolean tryDecrypt(Component component, Encryptor<?> encryptor) {
		boolean decryptedSiblings = false;
		for (Component sibling : component.getSiblings()) {
			if (tryDecrypt(sibling, encryptor)) {
				decryptedSiblings = true;
			}
		}

		if (component.getContents() instanceof LiteralContents literal) {
			var decrypted = tryDecrypt(literal.text(), encryptor);

			if (decrypted.isPresent()) {
				((MutableComponent)component).contents = new LiteralContents(decrypted.get());
				return true;
			}
		} else if (component.getContents() instanceof TranslatableContents translatable) {
			for (Object arg : translatable.args) {
				if (arg instanceof MutableComponent mutable) {
					if (tryDecrypt(mutable, encryptor)) {
						decryptedSiblings = true;
					}
				}
			}
		}

		return decryptedSiblings;
	}

	public static Optional<String> tryDecrypt(String message, Encryptor<?> encryptor) {
		try {
			String[] splat = message.contains(" ") ? message.split(" ") : new String[] { message };
			String decryptable = splat[splat.length-1];

			String decrypted = encryptor.decrypt(decryptable);

			if (decrypted.startsWith("#%"))
				return Optional.of(message.substring(0, message.length() - decryptable.length()) + decrypted.substring(2, decrypted.length()));
			else
				return Optional.empty();
		} catch (Exception ex) {
			return Optional.empty();
		}
	}

	public static Component recreate(Component component) {
		return Component.Serializer.fromJson(Component.Serializer.toStableJson(component));
	}

}
