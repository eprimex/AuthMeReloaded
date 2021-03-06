package fr.xephi.authme.security.crypts;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import fr.xephi.authme.AuthMe;


public class SALTED2MD5 implements EncryptionMethod {

	@Override
	public String getHash(String password, String salt)
			throws NoSuchAlgorithmException {
		return getMD5(getMD5(password) + salt);
	}

	@Override
	public boolean comparePassword(String hash, String password,
			String playerName) throws NoSuchAlgorithmException {
    	String salt = AuthMe.getInstance().database.getAuth(playerName).getSalt();
    	return hash.equals(getMD5(getMD5(password) + salt));
	}

    private String getMD5(String message) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(message.getBytes());
        byte[] digest = md5.digest();
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1,digest));
    }
}
