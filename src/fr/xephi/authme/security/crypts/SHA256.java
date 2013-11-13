package fr.xephi.authme.security.crypts;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 implements EncryptionMethod {

	@Override
	public String getHash(String password, String salt) throws NoSuchAlgorithmException {
		return "$SHA$" + salt + "$" + getSha256(getSha256(password) + salt);
	}

	@Override
	public boolean comparePassword(String hash, String password, String playerName)
			throws NoSuchAlgorithmException {
		String[] line = hash.split("\\$");
		return hash.equals(getHash(password, line[2]));
	}
	
	private String getSha256(String password) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();
        sha256.update(password.getBytes());
        byte[] digest = sha256.digest();
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,digest));
	}

}
