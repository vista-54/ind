//
//  CDVindoo.rs.m
//  nomos
//
//  Created by Richard Sulzenbacher 
//  Copyright (c) 2014 Nomos System. All rights reserved.
//

#import "CDVindoo.rs.h"
#import <Cordova/CDVPluginResult.h>


@interface IDLocationAdapter : IndoorsLocationAdapter
    @property (assign) NSString* lastZones;
    @property (nonatomic,strong) CDVindoors* indoorsInstance;
@end

@implementation IDLocationAdapter

- (id) initWithReference: (CDVindoors*) indoorsInstance {
    self.indoorsInstance = indoorsInstance;
    return self;
}

- (NSArray *)zonesForLocation:(IDSCoordinate *)location {
    NSMutableArray *zones = [NSMutableArray array];
   
    if (self.indoorsInstance.building) {
        for (IDSFloor *floor in [self.indoorsInstance.building.floors allValues]) {
            if (floor.level == location.z) {
                for (IDSZone* zone in floor.zones) {
                    UIBezierPath* polygonPath = [zone poloygonWithXScale:1 yScale:1];
                    CGPoint userPoint = CGPointMake(location.x, location.y);
                    if([polygonPath containsPoint:userPoint]) {
                        [zones addObject:zone];
                    }
                }
            }
        }
    }
    
    return zones;
}

- (void)positionUpdated:(IDSCoordinate*)userPosition {
    NSMutableDictionary* resultDict = [[NSMutableDictionary alloc] init];
    [resultDict setObject:[NSString stringWithFormat:@"%d", userPosition.x] forKey:@"x"];
    [resultDict setObject:[NSString stringWithFormat:@"%d", userPosition.y] forKey:@"y"];
    [resultDict setObject:[NSString stringWithFormat:@"%d", userPosition.z] forKey:@"z"];
    [resultDict setObject:[NSString stringWithFormat:@"%d", userPosition.accuracy] forKey:@"accuracy"];
    
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:resultDict options:kNilOptions error:nil];
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    [self.indoorsInstance message:@"positionUpdated" withData:[NSString stringWithFormat:@"%@", jsonString]];
    
    // betretene Zonen ermitteln und enteredZone Event feuern...
    NSArray *zones = [self zonesForLocation:userPosition];
    NSString *data = @"";
    for (IDSZone* zone in zones) {
        data = [[data stringByAppendingString:@"|"] stringByAppendingString:zone.name];
    }
    if([data length] > 0 || self.lastZones != data) {
        [self.indoorsInstance message:@"enteredZone" withData:data];
        self.lastZones = data;
    }
}

- (void)changedFloor:(int)floorLevel withName:(NSString*)name {
    if(floorLevel != INT32_MAX) {
        NSString *data = [[[NSString stringWithFormat:@"%d", floorLevel] stringByAppendingString:@"|"] stringByAppendingString:name];
        [self.indoorsInstance message:@"changedFloor" withData:data];
    }
}

- (void)leftBuilding:(IDSBuilding*) building {
    NSString *bid = [NSString stringWithFormat:@"%d", building.buildingID];
    [self.indoorsInstance message:@"leftBuilding" withData:bid];
}

@end




@interface CDVindoors () <IndoorsServiceDelegate, LoadingBuildingDelegate>
@end


@implementation CDVindoors

- (void)onReset {
    NSLog(@"onReset");
    [self destruct:NULL];
    [super onReset];
}

- (void)init:(CDVInvokedUrlCommand *)command {
	self.callbackId = command.callbackId;
    NSLog(@"init");
    
    NSString* apiKey = [command.arguments objectAtIndex:0];
    self.buildingId = [[command.arguments objectAtIndex:1] integerValue];
    
    [[Indoors alloc] initWithLicenseKey:apiKey andServiceDelegate:self];
    
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [commandResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:commandResult callbackId:self.callbackId];
}

- (void)destruct:(CDVInvokedUrlCommand *)command {
    NSLog(@"destruct");
    [[Indoors instance] stopLocalization];
    [[Indoors instance] removeLocationListener: self.indoorsAdapter];
}

- (void)setEvaluationMode:(CDVInvokedUrlCommand *)command {
    BOOL flag = [[command.arguments objectAtIndex:0] boolValue];
    [[Indoors instance] enableEvaluationMode:flag];
    [self success:@"setEvaluationMode" withData:@"success"];
}




-(void)message:(NSString *)event withData:(NSString *)data {
    [self _callback:event withData:data withType:@"message" withStatusCode:CDVCommandStatus_OK];
}

-(void)success:(NSString *)event withData:(NSString *)data {
    [self _callback:event withData:data withType:@"success" withStatusCode:CDVCommandStatus_OK];
}

-(void)fail:(NSString *)event withData:(NSString *)data {
    [self _callback:event withData:data withType:@"error" withStatusCode:CDVCommandStatus_ERROR];
}

-(void)_callback:(NSString *)event withData:(NSString *)data withType:(NSString *)type withStatusCode:(int)statusCode {
    NSMutableDictionary* resultDict = [[NSMutableDictionary alloc] init];
    [resultDict setObject:type forKey:@"type"];
    [resultDict setObject:event forKey:@"indoorsEvent"];
    [resultDict setObject:data forKey:@"indoorsData"];
    
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:statusCode messageAsDictionary:resultDict];
    [commandResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:commandResult callbackId:self.callbackId];
}



#pragma mark - IndoorsServiceDelegate
- (void)connected {
    [self success:@"connected" withData:@"success"];
    
    self.indoorsAdapter = [[IDLocationAdapter alloc] initWithReference:self];
    [[Indoors instance] registerLocationListener: self.indoorsAdapter];
    
    self.building = [[IDSBuilding alloc] init];
    [self.building setBuildingID: self.buildingId];
    [[Indoors instance] getBuilding:self.building forRequestDelegate:self];
}

- (void) onError:(IndoorsError*) indoorsError {
    [self fail:@"error" withData:[indoorsError debugDescription]];
}



#pragma mark - LoadingBuildingDelegate
- (void)loadingBuilding:(NSNumber*)progress {
    [self success:@"loadingBuilding" withData:[progress stringValue]];
}

- (void)buildingLoaded:(IDSBuilding*)building {
    self.building = building;
    [[Indoors instance] startLocalization];
    
    [self success:@"buildingLoaded" withData:@"success"];
}

- (void)loadingBuildingFailed {
    [self fail:@"error" withData:@"Failed to load building"];
}

@end
