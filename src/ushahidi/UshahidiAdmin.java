package ushahidi;

import javax.xml.bind.DatatypeConverter;

import java.text.ParseException;

import java.util.ArrayList;

import java.net.URL;
import java.net.URLEncoder;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class UshahidiAdmin {
	private ArrayList<Incident> pendingIncidents = new ArrayList<Incident>();
	private int index;
	private int pendingIndex;
	private String server; 
	private String username;
	private String password;
	
	/**
	 * 
	 * @param server
	 * 	a URL string that can be used to go to a web page containing JSON text
	 * @throws Exception
	 * 	When the given server URL is empty.
	 * @pre 
	 * 	The constructor must be given a valid http URL. It will run update() so that it can collect the incidents from the 
	 * 	given URL and add all of them to the ArrayList incidentsList.
	 * @post
	 * 	The result of the constructor will be the initialization of the fields index and server and the ArrayList 
	 * 	incidentsList containing all of the Incidents from the URL. 	 
	 */
	public UshahidiAdmin(String server, String username, String password) 
			throws Exception {
		if(server == null)
			throw new Exception("The sever URL must contain characters.");
		this.server = server;
		this.index = 0;
		this.pendingIndex = 0;
		this.username = username;
		this.password = password;
		getPending();
	} // Ushahidi(String)
	
	
	/**
	 * 
	 * @return
	 * 	An arrayList of pending Incidents objects
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws JSONException 
	 * @pre
	 * 	getPending will only get the pending incidents from the URL given to the constructor.
	 * @post
	 * 	The result of getPending() will be an arrayList of all pending incidents from the URL given
	 * 	to the constructor.
	 */
	public ArrayList<Incident> getPending() 
			throws IOException, JSONException, ParseException {
		JSONObject JSONText;
		
		Authenticator.setDefault (new Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication (username, password.toCharArray());
		    }
		});			
		
		// Construct URL
		String encodedData = "task=" + URLEncoder.encode("reports", "UTF-8")
				+ "&by=" + URLEncoder.encode("all", "UTF-8")
				+ "&limit=" + URLEncoder.encode(Integer.toString(5000), "UTF-8");
		
		String type = "application/x-www-form-urlencoded";
		
		// Connect to server
		URL serverURL = new URL(this.server + "/api");
		HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		
		// Authenticate user
		String userpass = username + ":" + password;
		String auth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
		connection.setRequestProperty("Authorization", auth);
		
		connection.setRequestProperty("Content-Type", type);
		connection.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
		OutputStream os = connection.getOutputStream();
		os.write(encodedData.getBytes());
		
		// Read from server
		BufferedReader incidentParser = 
				new BufferedReader(
						new InputStreamReader(
								connection.getInputStream()));
	
		// Use data from server to create JSONObjects
		String inputLine;
		String data = "";
		
		while ((inputLine  = incidentParser.readLine()) != null) {
			data += inputLine;
		} // while (inptuLine != null)
			JSONText = new JSONObject(data.substring(data.indexOf("{")));			
			JSONText = (JSONObject) JSONText.get("payload");
		JSONArray array = JSONText.getJSONArray("incidents"); 
		
		for (int i = 0; i< array.length(); i++) {
			Incident newIncident = new Incident((JSONObject) array.get(i));
			// Check if newIncident is not active
			if(newIncident.getIncidentActive() == 0) {
				this.pendingIncidents.add(newIncident);
			} // if(newIncident.getIncidentActive().contentEquals("1"))
		} // for (int i = 0; i< array.length(); i++)

		return pendingIncidents;
	} // getPending()
	
	/**
	 * 
	 * @return
	 * 	an Incident object
	 * @throws Exception
	 * 	In the case of there being no other pending Incidents, in the array list, at or after the current index.
	 * @pre
	 * 	nextPending() will update the list of pending Incidents and check that there are still Incidents after the 
	 * 	current Incident in the array list and, if there are, it will return the next pending incident in the 
	 * 	array list. In addition to that it will also increment pendingIndex.
	 * @post
	 * 	The result of this method will only ever be an Incident object at the current pendingIndex of 
	 * 	the array list. The only other outcome of nextPending() will be an thrown Exception.	
	 */
	public Incident nextPending() 
			throws Exception {
		// Call getPending() and make sure we have all of the pending incidents
		getPending();
		
		// If the number of pending incidents is greater than the pendingIndex, return the next pending incident.
		if (this.pendingIncidents.size() > pendingIndex)
			return this.pendingIncidents.get(this.pendingIndex++);
		this.pendingIndex = 0;
		throw new Exception("No pending Incidents available");
	} // nextPending()
	
	/**
	 * 
	 * @return
	 * 	an Incident object
	 * @throws Exception
	 * 	In the case of there being no other pending Incidents, in the array list, at or after the current index.
	 * @pre
	 * 	prevPending() will update the list of pending Incidents and check that there are still Incidents before the 
	 * 	current Incident in the array list and, if there are, it will return the next pending incident in the 
	 * 	array list. In addition to that it will also increment pendingIndex.
	 * @post
	 * 	The result of this method will only ever be an Incident object at the current pendingIndex of the array
	 * 	list. The only other outcome of prevIncident() will be a thrown Exception.
	 */
	public Incident prevPending() 
			throws Exception {
		// Call getPending() and make sure we have all of the pending incidents
		getPending();
		// If the pendingIndex is greater than the number of pendingIncidents, return the previous pending incident.
		if (pendingIndex > 0 && this.pendingIncidents.size() > 0) 
			return this.pendingIncidents.get(--pendingIndex);
		
		this.pendingIndex = 0;
		throw new Exception("No pending Incidents available");
	} // prevPending()
	
	
	/*delete() cannot throw an exception. A POST method, requesting to delete an incident, which is sent to the web API
	 * will always return true for success. This means that there is no way to confirm a successful incident deletion. We
	 * could always just check the web API and see if the incident we deleted is still there, but this is inefficient. I
	 * decided to always return the data returned by the web API because the deleteIncident() methods will throw exceptions
	 * if you attempt an invalid incident. */
	/**
	 * 
	 * @param incidentId
	 * 	a non negative int 
	 * @return
	 * 	a String that contains the Http response header
	 * @throws IOException 
	 * 	In the case that the server is not given a valid URL the method will throw an exception.
	 * @pre
	 * 	delete() will connect to the server with queried authentication, and delete an incident with the given
	 *  incidentId.
	 * @post
	 * 	After delete() is called there will be one less incident in the array list and in the Ushahidi server,
	 * 	assuming that incidentId matches an Incident's Id number. 
	 */
	public String delete(int incidentId) 
			throws IOException	 {

		// Authenticate user
		Authenticator.setDefault (new Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication (username, password.toCharArray());
		    }
		});			
		
		// Construct URL			
		String encodedData = "task=" + URLEncoder.encode("reports", "UTF-8")
				+ "&action=" + URLEncoder.encode("delete", "UTF-8")
				+ "&incident_id=" + URLEncoder.encode(Integer.toString(incidentId), "UTF-8");
		
		String type = "application/x-www-form-urlencoded";
		
		// Connect to server with Delete Post request
		URL serverURL = new URL(this.server + "/api");
		HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		
		String userpass = username + ":" + password;
		String auth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
		connection.setRequestProperty("Authorization", auth);
		
		connection.setRequestProperty("Content-Type", type);
		connection.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
		OutputStream os = connection.getOutputStream();
		os.write(encodedData.getBytes());
	
		// Read from server
		BufferedReader incidentParser = 
				new BufferedReader(
						new InputStreamReader(
								connection.getInputStream()));
		//Record data from server								
		String inputLine;
		String data = "";
		
		while ((inputLine  = incidentParser.readLine()) != null) {
			data += inputLine;
		} // while (inptuLine != null)

		// Data returned by web API, will tell us if the deletion was a success or not and any error codes we might have
		return data;
	} // delete (int)
	
	/**
	 * 
	 * @return
	 * 	the deleted Incident 
	 * @throws Exception
	 * 	When there are no Incidents left in the ArrayList.
	 * @pre
	 * 	deleteIncident() will remove an Incident, at the current index, from the ArrayIncident incidentsList. 
	 * 	After it removed the Incident it will return the removed Incident. 
	 * @post
	 * 	The result of deleteIncident() will be an arrayList with one less incident. In the case that there
	 * 	no Incidents in the ArrayList deleteIncident() will throw an Exception. 
	 */
	public Incident deleteIncident() 
			throws Exception {
		// Call getPending() and make sure that we have all of the pending incidents
		getPending();
		
		// Check if there are any pendingIncidents in our array list
		if (index >= 0 && pendingIncidents.size() > index){
			Incident temp = this.pendingIncidents.get(index);			
			
			// Remove from the arraylist 
			this.pendingIncidents.remove(index);
			// Call delete() to remove the incident from the Ushahidi server					
			delete(temp.getIncidentId());
			return temp;
		} // if (index >= 0 && pendingIncidents.size() > index)
		else{
			this.index = 0;
			throw new Exception("No Incidents left in ArrayList");
		} // else
	} //deleteIncident()
	
	/**
	 * 
	 * @param id
	 * 	an int representing the Incident id
	 * @return
	 * 	the deleted Incident
	 * @throws Exception
	 * 	When there are no Incidents left in the ArrayList or none have an ID that matches the given one
	 * @pre
	 * 	deleteIncident() will remove the first Incident, within the ArrayIncident incidentsList, that has an 
	 * 	incidentId that matches the one given. After it removed the Incident it will return the removed Incident. 
	 * @post
	 * 	The result of deleteIncident() will be an arrayList with one less incident. 
	 */
	public Incident deleteIncident(int id) 
			throws Exception {
		// Call getPending() and make sure that we have all of the pending incidents
		getPending();
		// Check if there are any pendingIncidents in our array list
		if (index >= 0 && this.pendingIncidents.size() > index){ 
			Incident temp;
			for(int i = 0; i < this.pendingIncidents.size(); i++) {
				temp = this.pendingIncidents.get(i);
				if (id == temp.getIncidentId()) {

					// Remove from the arraylist 
					this.pendingIncidents.remove(i);
					// Call delete() to remove the incident from the Ushahidi server
					delete(temp.getIncidentId());
					return temp;
				} // if (id == temp.getIncidentId())
			} //for
		} // if (index >= 0 && this.pendingIncidents.size() > index)
		else{
			this.index = 0;
			throw new Exception("No Incidents left in ArrayList");
		} // else
		throw new Exception("No Incidents with an ID of " + id);
	} // deleteIncident(int)
	
	/**
	 * 
	 * @param index
	 * 	an integer that marks which Incident the client will consider for approval.
	 * @return
	 * 
	 * @pre
	 * 	approve() will take an integer and will get the incident from the array list that has that integer
	 * 	as its index. After obtaining the correct incident approve() will then ask the client if they want
	 * 	to approve the current incident. If the client says yes the Incident will be approved, if the user
	 * 	says no the Incident will not be approved. 
	 * @post
	 */
	public String approve(int incidentId) 
		throws IOException	 {

			// Authenticate user
			Authenticator.setDefault (new Authenticator() {
			    protected PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication (username, password.toCharArray());
			    }
			});			
			
			// Construct URL			
			String encodedData = "task=" + URLEncoder.encode("reports", "UTF-8")
					+ "&action=" + URLEncoder.encode("approve", "UTF-8")
					+ "&incident_id=" + URLEncoder.encode(Integer.toString(incidentId), "UTF-8");
			
			String type = "application/x-www-form-urlencoded";
			
			// Connect to server with Approve Post request
			URL serverURL = new URL(this.server + "/api");
			HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			
			String userpass = username + ":" + password;
			String auth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
			connection.setRequestProperty("Authorization", auth);
			
			connection.setRequestProperty("Content-Type", type);
			connection.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
			OutputStream os = connection.getOutputStream();
			os.write(encodedData.getBytes());
			
			// Read from server
			BufferedReader incidentParser = 
					new BufferedReader(
							new InputStreamReader(
									connection.getInputStream()));
			//Record data from server							
			String inputLine;
			String data = "";
			
			while ((inputLine  = incidentParser.readLine()) != null) {
				data += inputLine;
			} // while (inptuLine != null)

			// Data returned by web API, will tell us if the deletion was a success or not and any error codes we might have
			return data;
	} // approve(int)
	
	/**
	 * 
	 * @throws Exception
	 * 	An exception will be thrown if there are no pending Incidents.
	 * @pre 
	 * 	approve() will call the approve(int) method with its parameter being the current pending Incident's id. 
	 * @post
	 * 	The result of approve() will be the current pending Incident being removed from the list, of pending Incidents, 
	 * 	and then approving it. The only other outcome will be a thrown exception.
	 */
	public void approve() 
	throws Exception {
		// Check if there are pending incidents
		if(this.pendingIndex < 0 && this.pendingIndex <= this.pendingIncidents.size() && this.pendingIncidents.size() < 0)
			throw new Exception("No pending Incidents available");

		// Save current pending incident into temperary incident
		Incident temp = this.pendingIncidents.get(this.pendingIndex);

		// Remove current pending incident from arraylist since it will no longer be a pending incident.
		this.pendingIncidents.remove(this.pendingIndex);

		// Approve current pending incident
		approve(temp.getIncidentId());
	} // approve ()
	
	public ArrayList<Incident> getPendingIncidents() {
		return this.pendingIncidents;
	} // getPendingIncidents()
} // UshahidiAdmin
 
