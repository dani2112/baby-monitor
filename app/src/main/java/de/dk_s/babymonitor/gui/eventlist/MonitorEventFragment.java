package de.dk_s.babymonitor.gui.eventlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.dk_s.babymonitor.R;
import de.dk_s.babymonitor.gui.eventlist.content.BabymonitorEventContent;
import de.dk_s.babymonitor.monitoring.AlarmController;
import de.dk_s.babymonitor.monitoring.db.DatabaseEventLogger;
import de.dk_s.babymonitor.monitoring.db.DatabaseEventLoggerContract;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MonitorEventFragment extends Fragment {

    private static final String TAG = "MonitorEventFragment";

    private static final String COLUMN_COUNT = "column-count";

    private int columnCount = 1;
    private OnListFragmentInteractionListener onListFragmentInteractionListener;

    private MyMonitorEventRecyclerViewAdapter myMonitorEventRecyclerViewAdapter = null;

    private RecyclerView recyclerView = null;

    private BroadcastReceiver broadcastReceiver = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MonitorEventFragment() {
    }

    @SuppressWarnings("unused")
    public static MonitorEventFragment newInstance(int columnCount) {
        MonitorEventFragment fragment = new MonitorEventFragment();
        Bundle args = new Bundle();
        args.putInt(COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            columnCount = getArguments().getInt(COLUMN_COUNT);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<BabymonitorEventContent.BabymonitorEvent> eventList = new ArrayList<>();
                Cursor cursor = new DatabaseEventLogger(getActivity()).getAllEntries();
                if (cursor.moveToFirst()) {

                    while (cursor.isAfterLast() == false) {
                        int eventType = cursor.getInt(cursor
                                .getColumnIndex(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_EVENT_TYPE));
                        long timestamp = cursor.getLong(cursor
                                .getColumnIndex(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_TIMESTAMP));
                        eventList.add(new BabymonitorEventContent.BabymonitorEvent(eventType, timestamp));
                        cursor.moveToNext();
                    }
                }
                if(myMonitorEventRecyclerViewAdapter != null) {
                    BabymonitorEventContent.BabymonitorEvent[] babymonitorEvents = new BabymonitorEventContent.BabymonitorEvent[eventList.size()];
                    myMonitorEventRecyclerViewAdapter.replaceContent(eventList.toArray(babymonitorEvents));
                    recyclerView.smoothScrollToPosition(babymonitorEvents.length);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitorevent_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (columnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, columnCount));
            }
            myMonitorEventRecyclerViewAdapter = new MyMonitorEventRecyclerViewAdapter(onListFragmentInteractionListener);
            recyclerView.setAdapter(myMonitorEventRecyclerViewAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            onListFragmentInteractionListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onListFragmentInteractionListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(AlarmController.EVENT_DB_UPDATED));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(broadcastReceiver);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(BabymonitorEventContent.BabymonitorEvent item);
    }
}
