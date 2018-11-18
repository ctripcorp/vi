package com.ctrip.framework.cs.util;

import java.io.IOException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Created by jiang.j on 2016/5/3.
 */
public class DesUtil {


  private final static String DES = "DES";

    public static String encrypt(String data, String key) throws Exception {
        byte[] bt = encrypt(data.getBytes(), key.getBytes());
        String strs = DatatypeConverter.printBase64Binary(bt);
        return strs;
    }

    public static String decrypt(String data, String key) throws IOException,
            Exception {
        if (data == null)
            return null;

        byte[] buf = DatatypeConverter.parseBase64Binary(data);
        byte[] bt = decrypt(buf,key.getBytes());
        return new String(bt);
    }

    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();

        DESKeySpec dks = new DESKeySpec(key);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        Cipher cipher = Cipher.getInstance(DES);

        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }


    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();

        DESKeySpec dks = new DESKeySpec(key);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        Cipher cipher = Cipher.getInstance(DES);

        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }}
