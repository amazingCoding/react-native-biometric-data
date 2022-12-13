package com.biometricdata;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class CryptographyManager {
  private int KEY_SIZE = 256;
  private String ANDROID_KEYSTORE = "AndroidKeyStore";
  private String ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM;
  private String ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE;
  private String ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;

  public Cipher getInitializedCipherForEncryption(String keyName) throws NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException {
    Cipher cipher = getCipher();
    SecretKey secretKey = getOrCreateSecretKey(keyName);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    return cipher;
  }
  public Cipher getInitializedCipherForDecryption(String keyName, byte[] initializationVector) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException {
    Cipher cipher = getCipher();
    SecretKey secretKey = getOrCreateSecretKey(keyName);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, initializationVector));
    return cipher;
  }
  public Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
    return Cipher.getInstance(ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_BLOCK_MODE + "/"  + ENCRYPTION_PADDING);
  }
  public byte[] encryptData(String plaintext, Cipher cipher) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    return cipherText;
  }
  public String decryptData(byte[] ciphertext, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
    byte[] res = cipher.doFinal(ciphertext);
    return new String(res, StandardCharsets.UTF_8);
  }

  public SecretKey getOrCreateSecretKey(String keyName) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
    KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
    keyStore.load(null);
    Key key = keyStore.getKey(keyName, null);
    if (key != null) {
      Log.d("CryptographyManager", "Secret key is already in the keystore");
      return (SecretKey)key;
    }
    KeyGenParameterSpec.Builder paramsBuilder = new KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
      .setBlockModes(ENCRYPTION_BLOCK_MODE)
      .setEncryptionPaddings(ENCRYPTION_PADDING)
      .setKeySize(KEY_SIZE)
      .setUserAuthenticationRequired(true);
    KeyGenParameterSpec keyGenParams = paramsBuilder.build();
    KeyGenerator keyGenerator =  KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
    keyGenerator.init(keyGenParams);
    return keyGenerator.generateKey();
  }

}
