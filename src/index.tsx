import { NativeModules, Platform } from 'react-native';
const LINKING_ERROR =
  `The package 'react-native-biometric-data' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';
const BiometricData = NativeModules.BiometricData ? NativeModules.BiometricData : new Proxy({}, { get() { throw new Error(LINKING_ERROR); }, });
export type BiometricType = 'face' | 'fingerprint' | 'unknown' | 'none';
export interface BiometricConfigIOS {
  title: string;
  keyName?: string,
  negativeButtonText: string;
}
export interface BiometricConfig extends BiometricConfigIOS {
  subTitle: string; // only android
}
export function checkSupportBiometric(): Promise<BiometricType> {
  return BiometricData.checkSupportBiometric();
}
export function unlockApp(biometricConfig: BiometricConfig): Promise<boolean> {
  const { title, subTitle, negativeButtonText } = biometricConfig;
  return BiometricData.unlockApp(title, subTitle, negativeButtonText);
}
export function encryptData(biometricConfig: BiometricConfig, data: string): Promise<string> {
  if (Platform.OS === 'ios') throw new Error('encryptData is not supported on ios');
  const { title, subTitle, negativeButtonText } = biometricConfig;
  return BiometricData.encryptData(title, subTitle, negativeButtonText, data);
}
export function decryptData(biometricConfig: BiometricConfig, data: string): Promise<string> {
  if (Platform.OS === 'ios') throw new Error('encryptData is not supported on ios');
  const { title, subTitle, negativeButtonText } = biometricConfig;
  return BiometricData.decryptData(title, subTitle, negativeButtonText, data);
}

export function saveKeyChainData(biometricConfig: BiometricConfigIOS, data: string): Promise<string> {
  if (Platform.OS === 'android') throw new Error('saveData is not supported on android');
  const { title, negativeButtonText, keyName } = biometricConfig;
  return BiometricData.encryptData(title, keyName, negativeButtonText, data);
}
export function getKeyChainData(biometricConfig: BiometricConfigIOS): Promise<string> {
  if (Platform.OS === 'android') throw new Error('getData is not supported on android');
  const { title, negativeButtonText, keyName } = biometricConfig;
  return BiometricData.decryptData(title, keyName, negativeButtonText);
}