#import "BiometricData.h"
#import <LocalAuthentication/LocalAuthentication.h>

@implementation BiometricData
RCT_EXPORT_MODULE()

/**
 * 判断生物识别类别
 */
RCT_REMAP_METHOD(checkSupportBiometric,checkBiometricTypeWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    LAContext *context = [[LAContext alloc] init];
    // 判断是否可用
    NSError *error;
    BOOL isSupportFinger = [context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error];
    if(error){
        reject([NSString stringWithFormat:@"%ld",(long)error.code],error.description,error);
    }
    else{
        int type = 0;
        if(context.biometryType == LABiometryTypeTouchID){
            if(isSupportFinger){
                resolve(@"fingerprint");
                type = 2;
            }
        }
        else if(context.biometryType == LABiometryTypeFaceID){
            if(isSupportFinger){
                resolve(@"face");
                type = 1;
            }
        }
        if(type == 0){
            reject(@"-1",@"no support",nil);
        }
    }
}
RCT_REMAP_METHOD(unlockApp,unlockWithTitle:(NSString *)title andResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    LAContext *context = [[LAContext alloc] init];
    NSError *error;
    BOOL isSupportFinger = [context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error];
    if(!error && isSupportFinger){
        [context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics localizedReason:title reply:^(BOOL success, NSError * _Nullable error) {
            if(success){
                resolve([NSNumber numberWithBool:success]);
            }
            else{
                // 主动取消
//                if(error.code == LAErrorUserFallback){
//
//                }
                reject(@"-1",@"failed",nil);
            }
        }];
    }
}
RCT_REMAP_METHOD(encryptData,encryptDataWithTitle:(NSString *)title andData:(NSString *)data andResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    LAContext *context = [[LAContext alloc] init];
    NSError *error;
    BOOL isSupportFinger = [context canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error];
    if(!error && isSupportFinger){
        [context evaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics localizedReason:title reply:^(BOOL success, NSError * _Nullable error) {
            if(success){
            }
            else{
            }
        }];
    }
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
