//
//  CDVindoo.rs.h
//  nomos
//
//  Created by Richard Sulzenbacher 
//  Copyright (c) 2014 Nomos System. All rights reserved.
//

#import <Cordova/CDVPlugin.h>
#import <Indoors/Indoors.h>
#import <Indoors/IndoorsDelegate.h>

@interface CDVindoors : CDVPlugin {
}

@property (nonatomic, strong) IndoorsLocationAdapter* indoorsAdapter;
@property (nonatomic, strong) IDSBuilding* building;
@property NSUInteger buildingId;
@property (nonatomic, copy) NSString *callbackId;

-(void)message:(NSString *)event withData:(NSString *)data;
-(void)success:(NSString *)event withData:(NSString *)data;
-(void)fail:(NSString *)event withData:(NSString *)data;

@end
