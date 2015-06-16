/**
 	indoo.rs Plugin for cordova.
 */

var indoors = function(apiKey, buildingId) {
	this.cordovaRef = window.PhoneGap || window.Cordova || window.cordova; 
	this.events = [];
	this.apiKey = apiKey;
	this.buildingId = buildingId;
	  
	this.cordovaExec('init', [apiKey, buildingId]);
};

indoors.prototype = {
	setEvaluationMode: function(flag) {
		this.cordovaExec('setEvaluationMode', [flag]);
	},
	
	destruct: function() {
		this.cordovaExec('destruct', []);
	},
	
	cordovaExec: function(functionName, params) {
		var self = this;
		this.cordovaRef.exec(
			function(e) {
				self._handleEvent(e);
			},
			function(e) {
				self._handleEvent(e);
			}, 'indoors', functionName, params);
	},
		  
	addEventListener: function(type, listener, useCapture) {
		this.events[type] || (this.events[type] = []);
		this.events[type].push(listener);
	},
	
	removeEventListener: function(type, listener, useCapture) {
		var events;
		if(!this.events[type]) return;
		events = this.events[type];
		for (var i = events.length - 1; i >= 0; --i) {
			if(events[i] === listener) {
				events.splice(i, 1);
				return;
			}
		}
	},
	
	dispatchEvent: function(event) {
		var handler;
		var events = this.events[event.type] || [];
		
		for (var i = 0, l = events.length; i < l; i++) {
			events[i](event);
		}
		
		handler = this['on' + event.type];
		if(handler) handler(event);
	},
	
	_handleEvent: function(e) {
		var event = document.createEvent('MessageEvent');
		event.initMessageEvent(e.type, false, false, e, null, null, window, null);
		this.dispatchEvent(event);
	}
};
