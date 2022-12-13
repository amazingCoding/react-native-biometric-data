
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNBiometricDataSpec.h"

@interface BiometricData : NSObject <NativeBiometricDataSpec>
#else
#import <React/RCTBridgeModule.h>

@interface BiometricData : NSObject <RCTBridgeModule>
#endif

@end
