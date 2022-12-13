import * as React from 'react';

import { StyleSheet, View, Text, TouchableOpacity } from 'react-native';
import { checkSupportBiometric, encryptData, unlockApp, decryptData } from 'react-native-biometric-data';

export default function App() {
  const [result, setResult] = React.useState('');
  const [data, setData] = React.useState('');
  const [dData, setDecryptData] = React.useState('');
  const awaitEncryptionString = 'Hello world';
  React.useEffect(() => {
    getData()
  }, []);
  const getData = async () => {
    try {
      const res = await checkSupportBiometric()
      console.log(res);

      setResult(res)
    } catch (error) {
      console.log(error);
    }
  }
  const handleUnlock = async () => {
    try {
      const res = await unlockApp({
        title: 'Unlock123',
        subTitle: 'Unlock',
        negativeButtonText: 'Cancel'
      })
      console.log(res);
    } catch (error) {
      console.log(error);

    }


  }
  const handleEncrypt = async () => {
    try {
      const res = await encryptData({
        title: 'encryptData',
        subTitle: 'encrypt your data by biometric',
        negativeButtonText: 'Cancel'
      }, awaitEncryptionString)
      setData(res)
    } catch (error) {
      console.log(error);

    }
  }
  const handleDecrypt = async () => {
    try {
      const res = await decryptData({
        title: 'decryptData',
        subTitle: 'decrypt your data by biometric',
        negativeButtonText: 'Cancel'
      }, data)
      setDecryptData(res)
    } catch (error) {
      console.log(error);
    }
  }

  return (
    <View style={styles.container}>
      <Text style={{ color: '#fff' }}>Type: {result}</Text>
      <TouchableOpacity onPress={handleUnlock} style={{ height: 50, backgroundColor: "#ff00ff", width: '80%', alignItems: 'center', justifyContent: 'center', marginTop: 20, borderRadius: 10 }}>
        <Text style={{ color: '#fff' }}>Unlock</Text>
      </TouchableOpacity>

      <Text style={{ color: '#fff', width: '80%', marginTop: 40 }}>encryptData</Text>
      <Text style={{ borderColor: "#d9d9d9", borderWidth: 1, marginTop: 10, padding: 10, width: '80%', borderRadius: 10 }}>{awaitEncryptionString}</Text>
      <TouchableOpacity onPress={handleEncrypt} style={{ height: 50, backgroundColor: "#ff00ff", width: '80%', alignItems: 'center', justifyContent: 'center', marginTop: 20, borderRadius: 10 }}>
        <Text style={{ color: '#fff' }}>encryptData</Text>
      </TouchableOpacity>
      <Text style={{ color: '#fff', width: '80%', marginTop: 20 }}>encrypt result</Text>
      <Text style={{ borderColor: "#d9d9d9", borderWidth: 1, marginTop: 10, padding: 10, width: '80%', borderRadius: 10 }}>{data}</Text>
      <TouchableOpacity onPress={handleDecrypt} style={{ height: 50, backgroundColor: "#ff00ff", width: '80%', alignItems: 'center', justifyContent: 'center', marginTop: 20, borderRadius: 10 }}>
        <Text style={{ color: '#fff' }}>decryptData</Text>
      </TouchableOpacity>
      <Text style={{ color: '#fff', width: '80%', marginTop: 20 }}>decrypt result</Text>
      <Text style={{ borderColor: "#d9d9d9", borderWidth: 1, marginTop: 10, padding: 10, width: '80%', borderRadius: 10 }}>{dData}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: "#000",
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
