package edu.grinnell.projectframework;

/* Ushahidi Web API commands were found at:
 * https://wiki.ushahidi.com/display/WIKI/Ushahidi+Public+API
 * https://wiki.ushahidi.com/display/WIKI/Ushahidi+Admin+API*/

import java.io.*;

import javax.xml.bind.DatatypeConverter;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Scanner;

import java.net.URL;
import java.net.URLEncoder;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UshahidiAPI {
        private ArrayList<Incident> incidentsList = new ArrayList<Incident>();
        private ArrayList<Incident> pendingIncidents = new ArrayList<Incident>();
        private int index;
        private int pendingIndex;
        private String server;
        private int maxId = 0; 
        private String username;
        private String password;
        
        /**
         * 
         * @param server
         *         a URL string that can be used to go to a web page containing JSON text
         * @throws Exception
         *         When the given server URL is empty.
         * @pre 
         *         The constructor must be given a valid http URL. It will run update() so that it can collect the incidents from the 
         *         given URL and add all of them to the ArrayList incidentsList.
         * @post
         *         The result of the constructor will be the initialization of the fields index and server and the ArrayList 
         *         incidentsList containing all of the Incidents from the URL.          
         */
        public UshahidiAPI(String server, String username, String password) 
                        throws Exception {
                if(server == null)
                        throw new Exception("The sever URL must contain characters.");
                this.server = server;
                this.index = 0;
                this.pendingIndex = 0;
                this.username = username;
                this.password = password;
                update();
        } // Ushahidi(String)
        
        /**
         * 
         * @throws Exception
         *         In the case that the server is not given a valid URL the method will throw an exception.
         * @pre 
         *  update() can be called if the field server has be initialized with a valid URL that contains JSON text. 
         *  This JSON text must be obtained by using the Ushahidi web API. In addition to that, this method expects
         *  that all of the Incidents are complete, that all of their fields have valid values.
         * @post
         *         This arrayList called incidentsList will be filled with incident objects. incidentsList will be filled 
         *  with a number of the incidents the given source has. This number is not limited to the 20 incidents the
         *  the web API is able to show. Each incident object declared will match the incident that is corresponds
         *  to.
         */
        public void update() 
                        throws Exception {

                JSONObject JSONText;
                JSONObject JSONErrorCodes; 
                String errorNumber;

                do {                        
                        URL serverURL;
                        
                        // Test to see if maxId is 0, which would mean this is the first time this method has been ran.
                        if (this.maxId == 0) 
                                serverURL = new URL(this.server + "/api?task=incidents&by=all&limit=5000");
                        // If maxId is not 0, then this must have been called before.
                        else        
                                serverURL = new URL(this.server + "/api?task=incidents&by=sinceid&id=" + this.maxId);
                        HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
                        connection.connect();
                        
                        BufferedReader incidentParser = 
                                        new BufferedReader(
                                                        new InputStreamReader(
                                                                        connection.getInputStream()));
                
                        String inputLine;
                        String data = "";
                        
                        while ((inputLine  = incidentParser.readLine()) != null) {
                                data += inputLine;
                        } // while (inptuLine != null)

                        JSONText = new JSONObject(data.substring(data.indexOf("{")));                        
                        JSONErrorCodes = new JSONObject(data.substring(data.indexOf("{")));

                        errorNumber = (String) JSONErrorCodes.getJSONObject("error").get("code");
                        if (!errorNumber.contentEquals("007")) {
                                JSONText = (JSONObject) JSONText.get("payload");
                                JSONArray array = JSONText.getJSONArray("incidents"); 
                                for (int i = 0; i< array.length(); i++) {
                                        Incident newIncident = new Incident((JSONObject) array.get(i));
                                        this.incidentsList.add(newIncident);
                                        if(newIncident.getIncidentId() > this.maxId)
                                                this.maxId = newIncident.getIncidentId();
                                } // for (int i = 0; i< array.length(); i++)
                        } // if        (!errorNumber.contentEquals("007"))
                }
                while(!errorNumber.contentEquals("007"));        
        } // update()

        /**
         * 
         * @return
         *         The next Incident object in incidentsList
         * @throws Exception
         *         In the case of there being no other Incidents, in the array list, at or after the current index.
         * @pre
         *         nextIncident() will update the list of Incidents and check that there are still Incidents after the 
         *         current Incident in the array list and, if there are, it will return the next incident in the 
         *         array list. In addition to that, it will also increment the index. 
         * @post
         *         The result of this method will only ever be an Incident object at the current index of 
         *         the array list. The only other outcome of nextIncident() is a thrown Exception. 
         */
        public Incident nextIncident() 
                        throws Exception {
                update();
                if (this.incidentsList.size() > index) {
                        return this.incidentsList.get(index++);
                } // if (this.incidentsList.size() > index)
                else {
                        this.index = 0;
                        throw new Exception("No new Incidents");
                } // else
        } // nextIncident()

        /**
         * 
         * @return
         *         The previous Incident object in incidentsList
         * @throws Exception
         *         In the case of there being no other Incidents, in the array list, at or before the current index.
         * @pre
         *         prevIncident() will update the list of Incidents check that there are still Incidents 
         *         before the current Incident in the array list and, if there are, it will return the 
         *         preceding incident in the array list.
         * @post
         *         The result of this method will only ever be an Incident object at the current index of the array
         *         list. The only other outcome of prevIncident() is a thrown Exception.
         */
        public Incident prevIncident() 
                        throws Exception {
                update();
                if (index > 0 && this.incidentsList.size() > 0) {
                        return this.incidentsList.get(--index);
                } // if (index >= 0)
                else {
                        this.index = 0;
                        throw new Exception("No previous Incidents");
                } // else
        } // prevIncident()        
        
        /*delete() cannot throw an exception. A POST method, requesting to delete an incident, which is sent to the web API
         * will always return true for success. This means that there is no way to confirm a successful incident deletion. We
         * could always just check the web API and see if the incident we deleted is still there, but this is inefficient. I
         * decided to always return the data returned by the web API because the deleteIncident() methods will throw exceptions
         * if you attempt an invalid incident. */
        /**
         * 
         * @param incidentId
         *         a non negative int 
         * @return
         *         a String that contains the Http response header
         * @throws IOException 
         *         In the case that the server is not given a valid URL the method will throw an exception.
         * @pre
         *         delete() will connect to the server with queried authentication, and delete an incident with the given
         *  incidentId.
         * @post
         *         After delete() is called there will be one less incident in the array list and in the Ushahidi server,
         *         assuming that incidentId matches an Incident's Id number. 
         */
        public String delete(int incidentId) 
                        throws IOException         {
                // Connect to URL with the delete GET request 
                
                Authenticator.setDefault (new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication (username, password.toCharArray());
                    }
                });                        
                
                String encodedData = "task=" + URLEncoder.encode("reports", "UTF-8")
                                + "&action=" + URLEncoder.encode("delete", "UTF-8")
                                + "&incident_id=" + URLEncoder.encode(Integer.toString(incidentId), "UTF-8");
                
                String type = "application/x-www-form-urlencoded";
                
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
        
                BufferedReader incidentParser = 
                                new BufferedReader(
                                                new InputStreamReader(
                                                                connection.getInputStream()));
                                                                
                String inputLine;
                String data = "";
                
                while ((inputLine  = incidentParser.readLine()) != null) {
                        data += inputLine;
                } // while (inptuLine != null)
                return data;
        } // delete
        
        /**
         * 
         * @return
         *         the deleted Incident 
         * @throws Exception
         *         When there are no Incidents left in the ArrayList.
         * @pre
         *         deleteIncident() will remove an Incident, at the current index, from the ArrayIncident incidentsList. 
         *         After it removed the Incident it will return the removed Incident. 
         * @post
         *         The result of deleteIncident() will be an arrayList with one less incident. In the case that there
         *         no Incidents in the ArrayList deleteIncident() will throw an Exception. 
         */
        public Incident deleteIncident() 
                        throws Exception {
                update();
                if (index >= 0 && incidentsList.size() > index){
                        Incident temp = this.incidentsList.get(index);                        
                                        
                        this.incidentsList.remove(index);
                        delete(temp.getIncidentId());
                        return temp;
                } // if
                else{
                        this.index = 0;
                        throw new Exception("No Incidents left in ArrayList");
                } // else
        } //deleteIncident()
        
        /**
         * 
         * @param id
         *         an int representing the Incident id
         * @return
         *         the deleted Incident
         * @throws Exception
         *         When there are no Incidents left in the ArrayList or none have an ID that matches the given one
         * @pre
         *         deleteIncident() will remove the first Incident, within the ArrayIncident incidentsList, that has an 
         *         incidentId that matches the one given. After it removed the Incident it will return the removed Incident. 
         * @post
         *         The result of deleteIncident() will be an arrayList with one less incident. 
         */
        public Incident deleteIncident(int id) 
                        throws Exception {
                update();
                if (index >= 0 && this.incidentsList.size() > index){ 
                        Incident temp;
                        for(int i = 0; i < this.incidentsList.size(); i++) {
                                temp = this.incidentsList.get(i);
                                if (id == temp.getIncidentId()) {
                                        this.incidentsList.remove(i);
                                        delete(temp.getIncidentId());
                                        return temp;
                                } // if (id == temp.getIncidentId())
                        } //for
                } // if
                else{
                        this.index = 0;
                        throw new Exception("No Incidents left in ArrayList");
                } // else
                throw new Exception("No Incidents with an ID of " + id);
        } // deleteIncident(int)
        
        /**
         * 
         * @param index
         *         an integer that marks which Incident the client will consider for approval.
         * @return
         * 
         * @pre
         *         approve() will take an integer and will get the incident from the array list that has that integer
         *         as its index. After obtaining the correct incident approve() will then ask the client if they want
         *         to approve the current incident. If the client says yes the Incident will be approved, if the user
         *         says no the Incident will not be approved. 
         * @post
         */
        public String approve(int incidentId) 
                throws IOException         {
                        // Connect to URL with the delete GET request 
                        
                        Authenticator.setDefault (new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication (username, password.toCharArray());
                            }
                        });                        
                        
                        String encodedData = "task=" + URLEncoder.encode("reports", "UTF-8")
                                        + "&action=" + URLEncoder.encode("approve", "UTF-8")
                                        + "&incident_id=" + URLEncoder.encode(Integer.toString(incidentId), "UTF-8");
                        
                        String type = "application/x-www-form-urlencoded";
                        
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
                        
                        BufferedReader incidentParser = 
                                        new BufferedReader(
                                                        new InputStreamReader(
                                                                        connection.getInputStream()));
                                                                        
                        String inputLine;
                        String data = "";
                        
                        while ((inputLine  = incidentParser.readLine()) != null) {
                                data += inputLine;
                        } // while (inptuLine != null)
                        return data;
        } // approve(int)
        
        /**
         * 
         * @throws Exception
         *         An exception will be thrown if there are no pending Incidents.
         * @pre 
         *         approve() will call the approve(int) method with its parameter being the current pending Incident's id. 
         * @post
         *         The result of approve() will be the current pending Incident being removed from the list, of pending Incidents, 
         *         and then approving it. The only other outcome will be a thrown exception.
         */
        public void approve() 
        throws Exception {
                if(this.pendingIndex < 0 && this.pendingIndex <= this.pendingIncidents.size() && this.pendingIncidents.size() < 0)
                        throw new Exception("No pending Incidents available");
                Incident temp = this.pendingIncidents.get(this.pendingIndex);
                this.pendingIncidents.remove(this.pendingIndex);
                approve(temp.getIncidentId());
        } // approve ()

        /**
         * 
         * @return
         *         An arrayList of pending Incidents objects
         * @throws IOException 
         * @throws ParseException 
         * @throws JSONException 
         * @pre
         *         getPending will only get the pending incidents from the URL given to the constructor.
         * @post
         *         The result of getPending() will be an arrayList of all pending incidents from the URL given
         *         to the constructor.
         */
        public ArrayList<Incident> getPending() 
                        throws IOException, JSONException, ParseException {
                JSONObject JSONText;
                
                Authenticator.setDefault (new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication (username, password.toCharArray());
                    }
                });                        
                
                String encodedData = "task=" + URLEncoder.encode("reports", "UTF-8")
                                + "&by=" + URLEncoder.encode("all", "UTF-8")
                                + "&limit=" + URLEncoder.encode(Integer.toString(5000), "UTF-8");
                
                String type = "application/x-www-form-urlencoded";
                
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
                
                BufferedReader incidentParser = 
                                new BufferedReader(
                                                new InputStreamReader(
                                                                connection.getInputStream()));
        
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
                        if(newIncident.getIncidentActive() == 0) {
                                this.pendingIncidents.add(newIncident);
                        } // if(newIncident.getIncidentActive().contentEquals("1"))
                } // for (int i = 0; i< array.length(); i++)

                return pendingIncidents;
        } // getPending()

        /**
         * 
         * @return
         *         an Incident object
         * @throws Exception
         *         In the case of there being no other pending Incidents, in the array list, at or after the current index.
         * @pre
         *         nextPending() will update the list of pending Incidents and check that there are still Incidents after the 
         *         current Incident in the array list and, if there are, it will return the next pending incident in the 
         *         array list. In addition to that it will also increment pendingIndex.
         * @post
         *         The result of this method will only ever be an Incident object at the current pendingIndex of 
         *         the array list. The only other outcome of nextPending() will be an thrown Exception.        
         */
        public Incident nextPending() 
                        throws Exception {
                getPending();
                if (this.pendingIncidents.size() > pendingIndex)
                        return this.pendingIncidents.get(this.pendingIndex++);
                this.pendingIndex = 0;
                throw new Exception("No pending Incidents available");
        } // nextPending()
        
        /**
         * 
         * @return
         *         an Incident object
         * @throws Exception
         *         In the case of there being no other pending Incidents, in the array list, at or after the current index.
         * @pre
         *         prevPending() will update the list of pending Incidents and check that there are still Incidents before the 
         *         current Incident in the array list and, if there are, it will return the next pending incident in the 
         *         array list. In addition to that it will also increment pendingIndex.
         * @post
         *         The result of this method will only ever be an Incident object at the current pendingIndex of the array
         *         list. The only other outcome of prevIncident() will be a thrown Exception.
         */
        public Incident prevPending() 
                        throws Exception {
                getPending();
                if (pendingIndex > 0 && this.pendingIncidents.size() > 0) 
                        return this.pendingIncidents.get(--pendingIndex);
                
                this.pendingIndex = 0;
                throw new Exception("No pending Incidents available");
        } // prevIncident()
        
        public ArrayList<Incident> getPendingIncidents() {
                return this.pendingIncidents;
        } // getPendingIncidents()
        
        public ArrayList<Incident> getIncidentsList() {
                return this.incidentsList;
        }// getIncidentsList()
} // API
