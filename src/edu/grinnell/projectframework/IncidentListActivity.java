package edu.grinnell.projectframework;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class IncidentListActivity extends FragmentActivity
        implements IncidentListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_list);

        if (findViewById(R.id.incident_detail_container) != null) {
            mTwoPane = true;
            ((IncidentListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.incident_list))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(IncidentDetailFragment.ARG_ITEM_ID, id);
            IncidentDetailFragment fragment = new IncidentDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.incident_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, IncidentDetailActivity.class);
            detailIntent.putExtra(IncidentDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
