cordova-indoors
===============

Cordova/Phonegap Plugin for <a href="http://indoo.rs">indoo.rs</a>

## Requirements

see http://indoo.rs/instructions/ how to add indoo.rs SDK to your application.


## Manual Plugin Android Installation

1) [Create a basic Cordova Android application](http://docs.phonegap.com/en/3.0.0/guide_platforms_android_index.md.html#Android%20Platform%20Guide)
 
2) In the Cordova Android application you will need to put the following in your `res/xml/config.xml` file as a child to the **widget** tag:
```xml
<feature name="indoors">
    <param name="android-package" value="com.phonegap.plugins.indoors" />
</feature>
```
3) copy `indoors.java` from `Android/src/com/phonegap/plugins` folder to your Cordova Android application into `src/com/phonegap/plugins`. You may have to create that directory path in your project. 

4) copy the `www/indoors.js` file into your application's `assets/www` folder.

5) include `indoors.js` into your **index.html**
```html
<script type="text/javascript" charset="utf-8" src="indoors.js"></script>
```

6) see Example Usage how to use Plugin


## Manual Plugin iOS Installation (Mac OS X)

1) [Create a basic Cordova iOS application](http://docs.phonegap.com/en/3.0.0/guide_platforms_ios_index.md.html#iOS%20Platform%20Guide)

2) Locate the **plugins** section of your Project Navigator and create a group "indoors". Make sure it is added as a "group" (yellow folder)

3) copy `CDVindoo.rs.h` and `CDVindoo.rs.m` from the **iOS** folder into the new group "indoors".

4) Find the `config.xml` file in the project navigator and add a new entry as a child to the `widget` tag:
```xml
<feature name="indoors">
    <param name="ios-package" value="CDVindoors" />
</feature>
```
5) copy the `www/indoors.js` file into the **www** directory in Xcode.

6) include `indoors.js` into your **index.html**
```html
<script type="text/javascript" charset="utf-8" src="indoors.js"></script>
```

7) see Example Usage how to use Plugin



## EXAMPLE USAGE: 

```javascript
IndoorNav.init('APIKEY', 'BUILDINGID');

$(window).unload(function() {
  IndoorNav.destruct();
});


IndoorNav = {
		
	init: function(apikey, building) {
		IndoorNav.indoors = new indoors(apikey, building);
		IndoorNav.indoors.onmessage = function(e) {
   			console.log('MESSAGE: ' + e.data.indoorsEvent + ' | DATA: ' + e.data.indoorsData) ; //TODO
    	};
    	IndoorNav.indoors.onsuccess = function(e) {
   			console.log('SUCCESS: ' + e.data.indoorsEvent + ' | DATA: ' + e.data.indoorsData); //TODO
    	};
    	IndoorNav.indoors.onerror = function(e) {
   			console.log('ERROR: ' + e.data.indoorsEvent + ' | DATA: ' + e.data.indoorsData) ; //TODO
    	};
	},
	
	destruct: function() {
		if(typeof IndoorNav.indoors != 'undefined') {
			IndoorNav.indoors.destruct();
		}
	}
};
```
