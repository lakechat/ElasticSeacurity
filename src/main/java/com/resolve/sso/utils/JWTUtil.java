package com.resolve.sso.utils;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

public class JWTUtil {
	private static final Logger logger = LogManager.getLogger(JWTUtil.class);
	private static final PrivateKey priKey;
	private static final RSAPublicKey pubKey;
	private static final String pubKeyString;
	
	static {
		KeyPair keyPair = getkeyPair();
		if(keyPair != null) {
			priKey = keyPair.getPrivate();
			pubKey = (RSAPublicKey)keyPair.getPublic();
		}else {
			priKey = null;
			pubKey = null;
		}
		pubKeyString = getPubKeyString();
	}
	
	public static String getPubKey() {
		return pubKeyString;
	}
	
	
	private static String getPubKeyString() {
		String result = null;
		try {
			result = pubKey.getModulus().toString() + "|" +
					pubKey.getPublicExponent().toString();
		}catch(Exception e) {
			logger.error("[getPubkeyString] exception: " + e.getMessage());
		}
		return result;
	}

	private static KeyPair getkeyPair() {
		try {
			// FileInputStream is = new FileInputStream("jwt.jks");
			InputStream is = ClassLoader.getSystemResourceAsStream("jwt.jks");

			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, "password".toCharArray());

			String alias = "selfsigned";

			Key key = keystore.getKey(alias, "password".toCharArray());
			if (key instanceof PrivateKey) {
				// Get certificate of public key
				Certificate cert = keystore.getCertificate(alias);

				// Get public key
				PublicKey publicKey = cert.getPublicKey();

				return new KeyPair(publicKey, (PrivateKey) key);
			}
		} catch (Exception e) {
			logger.error("[getkeyPair] exception: " + e.getMessage());
		}
		return null;

	}
	
	public static String buildJWS(Header header, Claims claim, PrivateKey priKey) {
		if(header == null) {
			header.setType("jwt");
		}
		String jws = Jwts.builder().setHeaderParams(header).setClaims(claim).signWith(priKey).compact();
		
		return jws;
	}
	
	public static String buildJWS(Header header, Claims claim) {
		if(header == null) {
			header = Jwts.header();
			header.setType("jwt");
		}
		JwtBuilder builder = Jwts.builder().setHeaderParams(header);
		builder = builder.setClaims(claim);
		builder = builder.signWith(priKey);
		String jws = builder.compact();
		
		return jws;
	}
	
	public static String generateJWT(Header header, Claims claim) {

		if (priKey != null) {
			String jws = buildJWS(header, claim);
			if (logger.isDebugEnabled()) {
				logger.debug("[generateJWT] received header: " + JsonUtil.getJson(header));
				logger.debug("[generateJWT] received claim: " + JsonUtil.getJson(claim));
				logger.debug("[generateJWT] get JWS string: " + jws);
			}
			return jws;
		} else {
			return null;
		}
	}
	
	private static PublicKey getPubKeyFromString(String keyString) throws Exception{
		String []Parts = keyString.split("\\|");
		String internalkeyString = getPubKeyString();
		int n = keyString.compareTo(internalkeyString);
		logger.debug("keu compare: "+n);
		RSAPublicKeySpec Spec = new RSAPublicKeySpec(
		        new BigInteger(Parts[0]),
		        new BigInteger(Parts[1]));
		RSAPublicKey pubKey = (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(Spec);
		return pubKey;
	}
	
	public static String parseJWS(String jws, String keyString) throws Exception {
		//JwsHeader header = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jws).getHeader();
		
		Jwt jwt =  Jwts.parserBuilder().setSigningKey(getPubKeyFromString(keyString)).build().parse(jws);
		JwsHeader header = (JwsHeader)jwt.getHeader();
		Claims claims = (Claims)jwt.getBody();
		if (logger.isDebugEnabled()) {
			logger.debug(header.toString());
			logger.debug(claims.toString());
		}
		
		return null;
	}

}
