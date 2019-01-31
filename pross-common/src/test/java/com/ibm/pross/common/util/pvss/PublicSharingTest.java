package com.ibm.pross.common.util.pvss;

import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.pross.common.util.crypto.paillier.PaillierKeyGenerator;
import com.ibm.pross.common.util.crypto.paillier.PaillierKeyPair;
import com.ibm.pross.common.util.crypto.paillier.PaillierPrivateKey;
import com.ibm.pross.common.util.crypto.paillier.PaillierPublicKey;
import com.ibm.pross.common.util.shamir.Polynomials;
import com.ibm.pross.common.util.shamir.ShamirShare;

public class PublicSharingTest {

	// TODO: Also implement negative test cases, invalid commitments, invalid encryptions
	
	@Test
	public void testVerifyAllShares() {
		fail("Not yet implemented");
	}

	@Test
	public void testVerifyShare() {
		fail("Not yet implemented");
	}

	@Test
	public void testAccessShare() {
		fail("Not yet implemented");
	}

	@Test
	public void testAll() {

		final int keyLength = 1024;
		final int n = 5;
		final int t = 3;

		// Generate shareholder public keys
		System.out.print("Generating keys...");
		final PaillierKeyPair[] keyPairs = new PaillierKeyPair[n];
		final PaillierPublicKey[] publicKeys = new PaillierPublicKey[n];
		final PaillierPrivateKey[] privateKeys = new PaillierPrivateKey[n];
		final PaillierKeyGenerator generator = new PaillierKeyGenerator(keyLength);
		for (int i = 0; i < n; i++) {
			keyPairs[i] = generator.generate();
			publicKeys[i] = keyPairs[i].getPublicKey();
			privateKeys[i] = keyPairs[i].getPrivateKey();
		}
		System.out.println(" Done.");
		System.out.println();

		// Warm up
		final PublicSharingGenerator pvssDealer = new PublicSharingGenerator(n, t);
		for (int i = 0; i < 1; i++) {
			pvssDealer.shareSecret(BigInteger.valueOf(1), publicKeys);
		}

		System.out.print("Generating public sharing...");
		final BigInteger specifiedSecret = BigInteger.valueOf(844);
		long start1 = System.nanoTime();
		final PublicSharing sharing = pvssDealer.shareSecret(specifiedSecret, publicKeys);
		long end1 = System.nanoTime();
		System.out.println(" Done.");
		System.out.println("Operation took: " + ((long) (end1 - start1) / 1_000_000.0) + " ms");
		System.out.println();

		System.out.print("Checking public sharing...");
		long start2 = System.nanoTime();
		boolean allGood = sharing.verifyAllShares(publicKeys);
		long end2 = System.nanoTime();
		System.out.println(" Done.");
		System.out.println("Operation took: " + ((long) (end2 - start2) / 1_000_000.0) + " ms");
		System.out.println("Public sharing length: " + sharing.getSize() + " bytes");
		System.out.println();

		System.out.println("All shares are good: " + allGood);
		Assert.assertTrue(allGood);

		System.out.print("Decrypting shares...");
		final ShamirShare[] shares = new ShamirShare[n];
		for (int i = 0; i < n; i++) {
			shares[i] = sharing.accessShare1(i, privateKeys[i]);
		}
		System.out.println(" Done.");
		System.out.println();

		System.out.print("Recovering secret...");
		final BigInteger secret = Polynomials.interpolateComplete(Arrays.asList(shares), t, 0);
		System.out.println(" Done.");
		System.out.println();

		System.out.println("Recovered secret: " + secret);
		Assert.assertEquals(specifiedSecret, secret);

		System.out.println("Public sharing: " + sharing);
	}
}
