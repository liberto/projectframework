/*Required fields found at: https://wiki.ushahidi.com/display/WIKI/Ushahidi+Public+API*/

package ushahidi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Incident {
	//Required fields.
	private int incidentId = -1; //The identification number corresponding to the incident.
	private String incidentTitle = ""; //Title of the incident.
	private String incidentDescription = ""; //Description of the report.
	private Date incidentDate; // The date format is mm/dd/yy.
	private int incidentMode; //The mode the incident is in.
	private int incidentActive = 0; //Is the incident active (0 = not active => pending and 1 = active => approved)
	private int incidentVerified = 0; //Is the incident verified.
	private int locationId; //The identification number corresponding to the location of the incident.
	private String locationName = ""; //The name of the location.
	private double locationLatitude; //Latitude of the report's location.
	private double locationLongitude; //Longitude of the report's location.
	private JSONArray categories = null;//The category id (or list of category i.ds) under which the report should be filed.
	private JSONArray media = null; //A collection of media.
	private JSONArray comments = null; //Additional comments on the incident.
	private JSONArray error = null; //Is there an error involving this incident.
	private JSONArray customFields = null; //Any additional fields the client wishes to add.
	
	//These are default values given to fields when no value is given by the JSON Object. 
	private int invalidLocationId = -1;
	private double invalidLatitude = 191919;
	private double invalidLongitude = 191919;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy hh:mm:ss");
	
	
	public Incident() {
		
	} // Incident()
	
	public Incident(int id, String title) {
		this.incidentId = id;
		this.incidentTitle = title;
	} // Incident(int, String)
	
	public Incident(JSONObject input) 
			throws JSONException, ParseException {
		incidentId = Integer.parseInt(input.getJSONObject("incident").get("incidentid").toString());
		incidentTitle = input.getJSONObject("incident").get("incidenttitle").toString();
		incidentDescription = input.getJSONObject("incident").get("incidentdescription").toString();
		incidentDate = dateFormat.parse(input.getJSONObject("incident").get("incidentdate").toString());
		incidentMode = Integer.parseInt(input.getJSONObject("incident").get("incidentmode").toString());
		incidentActive = Integer.parseInt(input.getJSONObject("incident").get("incidentactive").toString());
		incidentVerified = Integer.parseInt(input.getJSONObject("incident").get("incidentverified").toString());
		
		try{
			if(1 == input.getJSONObject("incident").get("locationid").toString().compareTo("0"))
				locationId = Integer.parseInt(input.getJSONObject("incident").get("locationid").toString());
			else
				locationId = invalidLocationId;
		} // try{locationId = Integer.parseInt(input.getJSONObject("incident").get("locationid").toString())}
		catch(java.lang.NumberFormatException e) {
			locationId = invalidLocationId;
		} // catch(java.lang.NumberFormatException e)
		
		locationName = input.getJSONObject("incident").get("locationname").toString();
		
		try{locationLatitude = Double.parseDouble(input.getJSONObject("incident").get("locationlatitude").toString());
		} // try{locationLatitude = Double.parseDouble(input.getJSONObject("incident").get("locationlatitude").toString())}
		catch(java.lang.NumberFormatException e) {
			locationLatitude = invalidLatitude;
		} // catch(java.lang.NumberFormatException e)
		
		try{locationLongitude = Double.parseDouble(input.getJSONObject("incident").get("locationlongitude").toString());
		} // try{locationLongitude = Double.parseDouble(input.getJSONObject("incident").get("locationlongitude").toString())}
		catch(java.lang.NumberFormatException e) {
			locationLongitude = invalidLongitude;
		} // catch(java.lang.NumberFormatException e)
			
		categories = input.getJSONArray("categories");
		media = input.getJSONArray("media");
		comments = input.getJSONArray("comments");
		
		try {error = input.getJSONArray("error");
		} // try{error = input.getJSONArray("error")}
		catch(JSONException e) {
			error = null;
		} // catch(JSONException e)
		
		try {customFields = input.getJSONArray("customfields");
		} // try{customFields = input.getJSONArray("customfields")}
		catch(JSONException e){
			customFields = null;
		} // catch(JSONException e)
	} // Incident(JSONObject)

	public String toString() {
		return "INCIDENT ["+ "Title: " + this.incidentTitle + "\nID: " + this.incidentId +"\nDescription: " + this.incidentDescription + "\nDate: " + this.incidentDate 
				+ "\nLocation ID: " + this.locationId + "\nLocation Name: " + this.locationName + "\nLocation Latitude: " + this.locationLatitude 
				+ "\nLocation Longitude: " + this.locationLongitude + "]\n";
	} // toString()

	//Get methods for the fields within the Incident class
	public int getIncidentId() {
		return incidentId;
	} // getIncidentId()

	public String getIncidentTitle() {
		return incidentTitle;
	} // getIncidentTitle()

	public String getIncidentDescription() {
		return incidentDescription;
	} // getIncidentDescription()

	public Date getIncidentDate() {
		return incidentDate;
	} // getIncidentDate()

	public int getIncidentMode() {
		return incidentMode;
	} // getIncidentMode()
	
	public int getIncidentActive() {
		return incidentActive;
	} // getIncidentActive()

	public int getIncidentVerified() {
		return incidentVerified;
	} // getIncidentVerified()

	public int getLocationId() {
		return locationId;
	} // getLocationId()

	public String getLocationName() {
		return locationName;
	} // getLocationName()

	public double getLocationLatitude() {
		return locationLatitude;
	} // getLocationLatitude()

	public double getLocationLongitude() {
		return locationLongitude;
	} // getLocationLongitude()

	public JSONArray getCategories() {
		return categories;
	} // getCategories()

	public JSONArray getError() {
		return error;
	} // getError()
	
	public JSONArray getComments() {
		return comments;
	} // getComments()
	
	public JSONArray getMedia() {
		return media;
	} // getMedia()

	public JSONArray getCustomFields() {
		return customFields;
	} // getCustomFields()
} // Incident
