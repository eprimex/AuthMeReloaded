package fr.xephi.authme.security.crypts;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 implements EncryptionMethod {

	@Override
	public String getHash(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(password.getBytes());
        byte[] digest = md5.digest();
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,digest));
	}

	@Override
	public boolean comparePassword(String hash, String password, String playerName) throws NoSuchAlgorithmException {
		return hash.equals(getHash(password, ""));
	}

}
