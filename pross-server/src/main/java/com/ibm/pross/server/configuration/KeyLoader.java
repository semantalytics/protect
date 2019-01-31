package com.ibm.pross.server.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import com.ibm.pross.server.app.KeyGeneratorCli;

public class KeyLoader {

	private final List<PublicKey> verificationKeys;
	private final List<PublicKey> encryptionKeys;

	private final PrivateKey signingKey;
	private final PrivateKey decryptionKey;

	public KeyLoader(final File keyPath, final int numServers, final int myIndex)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		this.verificationKeys = new ArrayList<>(numServers);
		this.encryptionKeys = new ArrayList<>(numServers);

		// Load all public keys
		for (int keyIndex = 1; keyIndex <= numServers; keyIndex++) {
			final File publicKeyFile = new File(keyPath, "public-" + keyIndex);

			try (PemReader reader = new PemReader(new FileReader(publicKeyFile.getAbsolutePath()))) {
				this.verificationKeys.add((PublicKey) deserializeKey(reader.readPemObject()));
				this.encryptionKeys.add((PublicKey) deserializeKey(reader.readPemObject()));
			}
		}

		// Load private key for our index
		final File publicKeyFile = new File(keyPath, "private-" + myIndex);
		try (PemReader reader = new PemReader(new FileReader(publicKeyFile.getAbsolutePath()))) {
			this.signingKey = (PrivateKey) deserializeKey(reader.readPemObject());
			this.decryptionKey = (PrivateKey) deserializeKey(reader.readPemObject());
		}
	}

	private static Key deserializeKey(final PemObject pemObject)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		final KeyFactory ecKeyFactory = KeyFactory.getInstance("ECDSA");
		final KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");

		switch (pemObject.getType()) {
		case "PAILLIER PRIVATE KEY":
			final RSAPrivateKey privateKey = (RSAPrivateKey) rsaKeyFactory
					.generatePrivate(new PKCS8EncodedKeySpec(pemObject.getContent()));
			return KeyGeneratorCli.convertToPaillierPrivateKey(privateKey);
		case "PAILLIER PUBLIC KEY":
			final RSAPublicKey publicKey = (RSAPublicKey) rsaKeyFactory
					.generatePublic(new X509EncodedKeySpec(pemObject.getContent()));
			return KeyGeneratorCli.convertToPaillierPublicKey(publicKey);
		case "EC PRIVATE KEY":
			return ecKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(pemObject.getContent()));
		case "EC PUBLIC KEY":
			return ecKeyFactory.generatePublic(new X509EncodedKeySpec(pemObject.getContent()));
		default:
			throw new IllegalArgumentException("Unrecognized type");
		}
	}

	public PublicKey getEncryptionKey(int serverIndex) {
		return this.encryptionKeys.get(serverIndex - 1);
	}

	public PublicKey getVerificationKey(int serverIndex) {
		return this.verificationKeys.get(serverIndex - 1);
	}

	public PrivateKey getSigningKey() {
		return this.signingKey;
	}

	public PrivateKey getDecryptionKey() {
		return this.decryptionKey;
	}

	@Override
	public String toString() {
		return "KeyLoader [verificationKeys=" + verificationKeys + ", encryptionKeys=" + encryptionKeys
				+ ", signingKey=" + signingKey + ", decryptionKey=" + decryptionKey + "]";
	}

}
