#import "BiometricData.h"
#import <LocalAuthentication/LocalAuthentication.h>
@implementation BiometricData
RCT_EXPORT_MODULE()

- (BOOL)isSupportFinger:(LAContext *)context{
    NSError *error;
    BOOL isSupportFinger = [context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error];
    return isSupportFinger;
}
/**
 * 判断生物识别类别
 */
RCT_REMAP_METHOD(checkSupportBiometric,checkBiometricTypeWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    LAContext *context = [[LAContext alloc] init];
    if(![self isSupportFinger:context]){
        reject(@"-1",@"no support",nil);
        return;
    }
    if(context.biometryType == LABiometryTypeTouchID){
        resolve(@"fingerprint");
    }
    else if(context.biometryType == LABiometryTypeFaceID){
        resolve(@"face");
    }
}
RCT_REMAP_METHOD(unlockApp,unlockWithTitle:(NSString *)title andSubTitle:(NSString *)subTitle andNegativeButtonText:(NSString *)negativeButtonText andResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    LAContext *context = [[LAContext alloc] init];
    if(![self isSupportFinger:context]){
        reject(@"-1",@"no support",nil);
        return;
    }
    context.localizedCancelTitle = negativeButtonText;
    [context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics localizedReason:title reply:^(BOOL success, NSError * _Nullable error) {
        if(success){
            resolve(@"success");
        }
        else{
            reject(@"-1",@"failed",nil);
        }
    }];
    
}
RCT_REMAP_METHOD(encryptData,encryptDataWithTitle:(NSString *)title andKeyName:(NSString *)keyName andNegativeButtonText:(NSString *)negativeButtonText andData:(NSString *)data andResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    LAContext *context = [[LAContext alloc] init];
    if(![self isSupportFinger:context]){
        reject(@"-1",@"no support",nil);
        return;
    }
    context.localizedCancelTitle = negativeButtonText;
    CFErrorRef error = NULL;
    SecAccessControlRef accessControl = SecAccessControlCreateWithFlags(kCFAllocatorDefault,kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,kSecAccessControlUserPresence,&error);
    if(error != NULL){
        reject(@"-1",@"failed",nil);
        return;
    }
    NSDictionary *queryRead = @{
            (__bridge id)kSecClass : (__bridge id)kSecClassInternetPassword,
            (__bridge id)kSecAttrAccount : keyName,
            (__bridge id)kSecAttrServer : @"hashnut.io",
            (__bridge id)kSecAttrAccessControl : (__bridge id)accessControl,
            (__bridge id)kSecUseAuthenticationContext : context,
            (__bridge id)kSecReturnData : @YES,
            (__bridge id)kSecMatchLimit : (__bridge id)kSecMatchLimitOne,
    };
    
    OSStatus checkStatus = SecItemCopyMatching((__bridge CFDictionaryRef)queryRead, nil);
    if(checkStatus == errSecSuccess){
        // 已经存在，更新
        NSDictionary *query = @{
                (__bridge id)kSecClass : (__bridge id)kSecClassInternetPassword,
                (__bridge id)kSecAttrAccessControl : (__bridge id)accessControl,
                (__bridge id)kSecUseAuthenticationContext : context,
                (__bridge id)kSecAttrAccount : keyName,
                (__bridge id)kSecAttrServer : @"hashnut.io",
        };
        NSDictionary *update = @{
            (__bridge id)kSecValueData : [data dataUsingEncoding:NSUTF8StringEncoding],
        };
        OSStatus status = SecItemUpdate((__bridge CFDictionaryRef)query, (__bridge CFDictionaryRef)update);
        if(status != errSecSuccess){
            reject(@"-1",@"failed",nil);
            return;
        }
        resolve(keyName);
    }
    else{
        // 没有存在，写入
        NSDictionary *query = @{
                (__bridge id)kSecClass : (__bridge id)kSecClassInternetPassword,
                (__bridge id)kSecAttrAccessControl : (__bridge id)accessControl,
                (__bridge id)kSecUseAuthenticationContext : context,
                (__bridge id)kSecAttrAccount : keyName,
                (__bridge id)kSecAttrServer : @"hashnut.io",
                (__bridge id)kSecValueData : [data dataUsingEncoding:NSUTF8StringEncoding],
        };
        OSStatus status = SecItemAdd((__bridge CFDictionaryRef)query, nil);
        if(status != errSecSuccess){
            reject(@"-1",@"failed",nil);
            return;
        }
        resolve(keyName);
    }
}
RCT_REMAP_METHOD(decryptData,decryptDataWithTitle:(NSString *)title andKeyName:(NSString *)keyName andNegativeButtonText:(NSString *)negativeButtonText andResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    LAContext *context = [[LAContext alloc] init];
    if(![self isSupportFinger:context]){
        reject(@"-1",@"no support",nil);
        return;
    }
    context.localizedCancelTitle = negativeButtonText;
    CFErrorRef error = NULL;
    SecAccessControlRef accessControl = SecAccessControlCreateWithFlags(kCFAllocatorDefault,kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,kSecAccessControlUserPresence,&error);
    if(error != NULL){
        reject(@"-1",@"failed",nil);
        return;
    }
    NSDictionary *query = @{
            (__bridge id)kSecClass : (__bridge id)kSecClassInternetPassword,
            (__bridge id)kSecAttrAccount : keyName,
            (__bridge id)kSecAttrServer : @"hashnut.io",
            (__bridge id)kSecAttrAccessControl : (__bridge id)accessControl,
            (__bridge id)kSecUseAuthenticationContext : context,
            (__bridge id)kSecReturnData : @YES,
            (__bridge id)kSecMatchLimit : (__bridge id)kSecMatchLimitOne,
    };
    CFTypeRef dataTypeRef = NULL;
    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, &dataTypeRef);
    if(status != errSecSuccess){
        reject(@"-1",@"failed",nil);
        return;
    }
    NSString *pwd = [[NSString alloc] initWithData:(__bridge NSData * _Nonnull)(dataTypeRef) encoding:NSUTF8StringEncoding];
    resolve(pwd);
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeBiometricDataSpecJSI>(params);
}
#endif

@end
