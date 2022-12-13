import { NativeModules, Platform } from 'react-native';
const LINKING_ERROR =
  `The package 'react-native-biometric-data' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';
const BiometricData = NativeModules.BiometricData ? NativeModules.BiometricData : new Proxy({}, { get() { throw new Error(LINKING_ERROR); }, });
export type BiometricType = 'face' | 'fingerprint' | 'unknown' | 'none';
export interface BiometricConfig {
  title: string;
  subTitle: string;
  negativeButtonText: string;
}
export function checkSupportBiometric(): Promise<BiometricType> {
  return BiometricData.checkSupportBiometric();
}
export function unlockApp(biometricConfig: BiometricConfig): Promise<boolean> {
  const { title, subTitle, negativeButtonText } = biometricConfig;
  return BiometricData.unlockApp(title, subTitle, negativeButtonText);
}
export function encryptData(biometricConfig: BiometricConfig, data: string): Promise<string> {
  const { title, subTitle, negativeButtonText } = biometricConfig;
  return BiometricData.encryptData(title, subTitle, negativeButtonText, data);
}
export function decryptData(biometricConfig: BiometricConfig, data: string): Promise<string> {
  const { title, subTitle, negativeButtonText } = biometricConfig;
  return BiometricData.decryptData(title, subTitle, negativeButtonText, data);
}
