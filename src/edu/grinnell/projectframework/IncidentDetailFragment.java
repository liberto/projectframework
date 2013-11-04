package edu.grinnell.projectframework;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class IncidentDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    Incident mItem;

    public IncidentDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = Incident.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_incident_detail, container, false);
        String displayInfo = "Incident Title: " + mItem.getIncidentTitle() +
        		"\nIncident Description: " + mItem.getIncidentDescription() +
        		"\nDate of Incident Description: " + mItem.getIncidentDate() +
        		"\nIncident Location: " + mItem.getLocationName() +
        		"\n\nThis incident is ";
        if(mItem.getIncidentActive()==0) displayInfo += "APPROVED.";
        else displayInfo += "PENDING.";
        		
        		
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.infoText)).setText(displayInfo);
        }
        return rootView;
    }
}
