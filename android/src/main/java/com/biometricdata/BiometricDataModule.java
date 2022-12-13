package com.biometricdata;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

@ReactModule(name = BiometricDataModule.NAME)
public class BiometricDataModule extends ReactContextBaseJavaModule {
  public static final String NAME = "BiometricData";
  public static final String secretKeyName = "hashnut_biometric_wallet_encryption_key";
  ReactApplicationContext reactContext;
  public BiometricDataModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  // checkSupportBiometric
  public boolean isSupportBiometric() {
    BiometricManager biometricManager = BiometricManager.from(reactContext);
    switch (biometricManager.canAuthenticate()) {
      case BiometricManager.BIOMETRIC_SUCCESS:
        return true;
      case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
      case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
      case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
        return false;
    }
    return false;
  }

  @ReactMethod
  public void checkSupportBiometric(Promise promise) {
    if (isSupportBiometric()) {
      promise.resolve("unknown");
    } else {
      promise.resolve("none");
    }
  }
  // unlockApp
  @ReactMethod
  public void unlockApp(String title,String subTitle,String negativeButtonText,Promise promise) {
    if(!isSupportBiometric()){
      promise.reject("-1","not support biometric");
    }
    else {
      Executor executor = ContextCompat.getMainExecutor(reactContext);
      // run in main thread
      Objects.requireNonNull(getCurrentActivity()).runOnUiThread(new Runnable() {
        @Override
        public void run() {
          BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) Objects.requireNonNull(getCurrentActivity()), executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
              super.onAuthenticationError(errorCode, errString);
              promise.reject(String.valueOf(errorCode),errString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
              super.onAuthenticationSucceeded(result);
              promise.resolve("success");
            }

            @Override
            public void onAuthenticationFailed() {
              super.onAuthenticationFailed();
              promise.reject("-1","failed");
            }
          });
          BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subTitle)
            .setNegativeButtonText(negativeButtonText)
            .setConfirmationRequired(false)
            .build();
          biometricPrompt.authenticate(promptInfo);
        }
      });
    }
  }
  public static String hex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte aByte : bytes) {
      result.append(String.format("%02x", aByte));
    }
    return result.toString();
  }
  // hexToByteArray
  public static byte[] hexToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
        + Character.digit(s.charAt(i+1), 16));
    }
    return data;
  }
  // encryptData
  @ReactMethod
  public void encryptData(String title,String subTitle,String negativeButtonText,String data,Promise promise) {
    if(!isSupportBiometric()){
      promise.reject("-1","not support biometric");
    }
    else {
      Executor executor = ContextCompat.getMainExecutor(reactContext);
      CryptographyManager cryptographyManager = new CryptographyManager();
      Cipher cipher;
      try {
        cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName);
        Objects.requireNonNull(getCurrentActivity()).runOnUiThread(new Runnable() {
          @Override
          public void run() {
            BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) Objects.requireNonNull(getCurrentActivity()), executor, new BiometricPrompt.AuthenticationCallback() {
              @Override
              public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                promise.reject(String.valueOf(errorCode),errString.toString());
              }

              @Override
              public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                try {
                  Cipher cipher = Objects.requireNonNull(Objects.requireNonNull(result.getCryptoObject()).getCipher());
                  byte[] ciphertext = cryptographyManager.encryptData(data, cipher);
                  byte[] iv = cipher.getIV();
                  String ciphertextString = BiometricDataModule.hex(ciphertext);
                  String ivString = BiometricDataModule.hex(iv);
                  promise.resolve(ciphertextString + "-" + ivString);
                }
                catch (Exception e) {
                  e.printStackTrace();
                  promise.reject("-1",e.getMessage());
                }
              }

              @Override
              public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                promise.reject("-1","failed");
              }
            });
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
              .setTitle(title)
              .setSubtitle(subTitle)
              .setNegativeButtonText(negativeButtonText)
              .setConfirmationRequired(false)
              .build();
            biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
          }
        });
      } catch (Exception e) {
        e.printStackTrace();
        promise.reject("-1",e.getMessage());
      }

    }
  }
  // decryptData
  @ReactMethod
  public void decryptData(String title,String subTitle,String negativeButtonText,String data,Promise promise) {
    if (!isSupportBiometric()) {
      promise.reject("-1", "not support biometric");
    } else {
      Executor executor = ContextCompat.getMainExecutor(reactContext);
      CryptographyManager cryptographyManager = new CryptographyManager();
      Cipher cipher;
      try {
        cipher = cryptographyManager.getInitializedCipherForDecryption(secretKeyName, hexToByteArray(data.split("-")[1]));
        Objects.requireNonNull(getCurrentActivity()).runOnUiThread(new Runnable() {
          @Override
          public void run() {
            BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) Objects.requireNonNull(getCurrentActivity()), executor, new BiometricPrompt.AuthenticationCallback() {
              @Override
              public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                promise.reject(String.valueOf(errorCode), errString.toString());
              }

              @Override
              public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                try {
                  Cipher cipher = Objects.requireNonNull(Objects.requireNonNull(result.getCryptoObject()).getCipher());
                  byte[] ciphertext = hexToByteArray(data.split("-")[0]);
                  String plaintext = cryptographyManager.decryptData(ciphertext, cipher);
                  promise.resolve(plaintext);
                } catch (Exception e) {
                  e.printStackTrace();
                  promise.reject("-1", e.getMessage());
                }
              }

              @Override
              public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                promise.reject("-1", "failed");
              }
            });
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
              .setTitle(title)
              .setSubtitle(subTitle)
              .setNegativeButtonText(negativeButtonText)
              .setConfirmationRequired(false)
              .build();
            biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
          }
        });
      } catch (Exception e) {
        e.printStackTrace();
        promise.reject("-1", e.getMessage());
      }
    }
  }
}
