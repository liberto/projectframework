package edu.grinnell.projectframework;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Incident {
	
	public static List<Incident> ITEMS = new ArrayList<Incident>();
    public static Map<String, Incident> ITEM_MAP = new HashMap<String, Incident>();

	private int incidentId = -1; //The identification number corresponding to the incident.
    private String incidentTitle = ""; //Title of the incident.
    private String incidentDescription = ""; //Description of the report.
    private Date incidentDate; // The date format is mm/dd/yy.
    private int incidentActive = 0; //Is the incident verified.
    private String locationName = ""; //The name of the location.
    private JSONArray media = null; //A collection of media.
    
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy hh:mm:ss", Locale.US);
    
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
            incidentDate = (Date) dateFormat.parse(input.getJSONObject("incident").get("incidentdate").toString());
            incidentActive = Integer.parseInt(input.getJSONObject("incident").get("incidentactive").toString());
            locationName = input.getJSONObject("incident").get("locationname").toString();
            media = input.getJSONArray("media");
    } // Incident(JSONObject)

    public String toString() {
            return "INCIDENT ["+ "Title: " + this.incidentTitle + "\nID: " + this.incidentId +"\nDescription: " + this.incidentDescription + "\nDate: " + this.incidentDate + "\nLocation Name: " + this.locationName + "]\n";
    } // toString()

    private static void addItem(Incident item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.incidentTitle, item);
    }
    
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
	
	public int getIncidentActive() {
	    return incidentActive;
	} // getIncidentVerified()
	    
	public JSONArray getMedia() {
	    return media;
	} // getMedia()

    public String getLocationName() {
            return locationName;
    } // getLocationName()
}
