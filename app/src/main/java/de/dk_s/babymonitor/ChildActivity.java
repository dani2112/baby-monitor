package de.dk_s.babymonitor;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ChildActivity extends AppCompatActivity implements SoundAnimationFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
