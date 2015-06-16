package com.phonegap.plugins;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;

import android.content.Context;

import com.customlbs.library.Indoors;
import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.IndoorsLocationListener;
import com.customlbs.library.LocalizationParameters;
import com.customlbs.library.model.Building;
import com.customlbs.library.model.Floor;
import com.customlbs.library.model.Zone;
import com.customlbs.library.callbacks.IndoorsServiceCallback;
import com.customlbs.library.callbacks.LoadingBuildingStatus;
import com.customlbs.shared.Coordinate;

public class indoors extends CordovaPlugin implements IndoorsLocationListener {  

	private CallbackContext callbackContext;
	private Indoors indoors;
	private IndoorsServiceCallback serviceCallback;
	private Building building;
	
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

    	if(action.equals("init")) {
    		this.callbackContext = callbackContext;
    		final indoors self = this;
        	final String apiKey = args.getString(0);
        	final long buildingId = args.getLong(1);

            serviceCallback = new IndoorsServiceCallback() {
            	@Override
            	public void onError(IndoorsException e) {
            		sendResult("error", e.getMessage(), "error", PluginResult.Status.ERROR);
            	}
            	
            	@Override
            	public void connected() {
            	    indoors = IndoorsFactory.getInstance();
            	    LocalizationParameters params = new LocalizationParameters();
//            	    params.setBatchPushInterval(60000);				// how often data is sent to the server. "data" is mostly the position, used for analytics on the server
//            	    params.setTrackingInterval(10000);				// how often data is queued up to be sent to the server later (see BatchPushInterval)
//            	    params.setPositionCalculationInterval(1000);	// how often to calculate a position internally
//            	    params.setPositionUpdateInterval(0);			// how often a calculated position (see PositionCalculationInterval) is sent to the UI
               	    indoors.setLocatedCloudBuilding(buildingId, params, true);
            	    indoors.registerLocationListener(self);
            	    
            		sendResult("connected", "success", "success", PluginResult.Status.OK);
            	}
            };
       		IndoorsFactory.createInstance(self.getApplicationContext(), apiKey, serviceCallback, true);
        	
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }
    	else if(action.equals("setEvaluationMode")) {
    		if(indoors instanceof Indoors) { 
    			if(args.getBoolean(0)) {
    				indoors.enableEvaluationMode();
    			}
    			else {
    				indoors.disableEvaluationMode();
    			}
	    		
        		sendResult("setEvaluationMode", "success", "success", PluginResult.Status.OK);
	            return true;
    		}
    		else {
        		sendResult("setEvaluationModeError", "indoo.rs not initialized", "error", PluginResult.Status.ERROR);
	            return false;
    		}
    	}
    	else if(action.equals("destruct")) {
    		if(indoors instanceof Indoors) { 
	        	indoors.removeLocationListener(this);
	        	IndoorsFactory.releaseInstance(serviceCallback);
	        	
	            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
	            pluginResult.setKeepCallback(false);
	            callbackContext.sendPluginResult(pluginResult);
	            return true;
    		}
    		else {
        		sendResult("destructError", "indoo.rs not initialized", "error", PluginResult.Status.ERROR);
	            return false;
    		}
    	}
    	
        return false;
    }

	@Override
	public void onError(IndoorsException indoorsException) {
		try {
			sendResult("error", indoorsException.getMessage(), "error", PluginResult.Status.ERROR);
		}
		catch(Exception e) {}
	}

	@Override
	public void changedFloor(int floorLevel, String name) {
		try {
			if(floorLevel != Integer.MAX_VALUE) {	// nur wenn auch definitiv sicher
				if(name.equals("floor")) {		// Name Hack (indoo.rs SDK <= 1.9.0 floor name is always "floor")
					Floor floor = building.getFloorByLevel(floorLevel);
					name = floor.getName();
				}
				
				sendResult("changedFloor", floorLevel + "|" + name, "message", PluginResult.Status.OK);
			}
		}
		catch(Exception e) {}
	}

	@Override
	public void enteredZones(List<Zone> zones) {
		try {
			String zoneNames = "";
	    	for (int i = 0; i < zones.size(); i++) {
	    		if(zoneNames.length() > 0) zoneNames += "|";
	    		zoneNames += zones.get(i).getName();
	    	}
	    	
	        sendResult("enteredZone", zoneNames, "message", PluginResult.Status.OK);
		}
		catch(Exception e) {}
	}

	@Override
	public void buildingLoaded(Building building) {
		this.building = building;
		sendResult("buildingLoaded", "success", "success", PluginResult.Status.OK);
	}

	@Override
	public void leftBuilding(Building building) {
		try {
			sendResult("leftBuilding", building.getName(), "message", PluginResult.Status.OK);
		}
		catch(Exception e) {}
	}

//	@Override
//	public void loadingBuilding(int progress) {
//		sendResult("loadingBuilding", String.valueOf(progress), "success", PluginResult.Status.OK);
//	}

	@Override
	public void orientationUpdated(float orientation) {
		//sendResult("orientationUpdated", String.valueOf(orientation), "message", PluginResult.Status.OK);
	}

	@Override
	public void positionUpdated(Coordinate userPosition, int accuracy) {
	    try {
			JSONObject re = new JSONObject();
			re.put("x", userPosition.x);
			re.put("y", userPosition.y);
			re.put("z", userPosition.z);
			re.put("accuracy", accuracy);
			
			sendResult("positionUpdated", re.toString(), "message", PluginResult.Status.OK);
	    }
	    catch (JSONException exception) {
	    	exception.printStackTrace();
	    }
	}
    
	private void sendResult(String event, String message, String type, Status status) {
	    JSONObject e = createEvent(event, message, type);
	    PluginResult pluginResult = new PluginResult(status, e);
	    pluginResult.setKeepCallback(true);
	    this.callbackContext.sendPluginResult(pluginResult);
	}

	private JSONObject createEvent(String event, String data, String type) {
	    try {
		  JSONObject e = new JSONObject();
	      e.put("type", type);
	      e.put("indoorsEvent", event);
	      e.put("indoorsData", data);
	      return e;
	    }
	    catch (JSONException exception) {
	    	exception.printStackTrace();
	    }

	    return null;
	}

	/**
	 * Gets the application context from cordova's main activity.
	 * @return the application context
	 */
	private Context getApplicationContext() {
		return this.cordova.getActivity().getApplicationContext();
	}

    @Override
    public void loadingBuilding(LoadingBuildingStatus lbs) {
        sendResult("loadingBuilding", String.valueOf(lbs.getProgress()), "success", PluginResult.Status.OK);
    }
    
}