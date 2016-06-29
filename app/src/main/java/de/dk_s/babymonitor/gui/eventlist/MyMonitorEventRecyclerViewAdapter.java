package de.dk_s.babymonitor.gui.eventlist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.dk_s.babymonitor.R;
import de.dk_s.babymonitor.gui.eventlist.MonitorEventFragment.OnListFragmentInteractionListener;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;

public class MyMonitorEventRecyclerViewAdapter extends RecyclerView.Adapter<MyMonitorEventRecyclerViewAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener onListFragmentInteractionListener;

    private List<BabyVoiceMonitor.AudioEvent> eventList = new LinkedList<>();

    public MyMonitorEventRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        onListFragmentInteractionListener = listener;
    }

    public void setContent(BabyVoiceMonitor.AudioEvent[] eventArray) {
        eventList.clear();
        for (BabyVoiceMonitor.AudioEvent event : eventArray) {
            eventList.add(event);
        }
        notifyDataSetChanged();
    }

    public void addEventTop(BabyVoiceMonitor.AudioEvent audioEvent) {
        eventList.add(0, audioEvent);
        notifyItemInserted(0);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_monitorevent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.audioEvent = eventList.get(position);

        long eventTimestamp = holder.audioEvent.getTimeStamp();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(eventTimestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = (Date) calendar.getTime();
        String dateString = simpleDateFormat.format(date);
        holder.timeTextView.setText(dateString);

        String eventDescriptionText = "";
        if(holder.audioEvent.getEventType() == 1) {
            eventDescriptionText = "Alarm aktiviert";
        } else if (holder.audioEvent.getEventType() == 3) {
            eventDescriptionText = "Alarm deaktiviert";
        }
        holder.descriptionTextView.setText(eventDescriptionText);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onListFragmentInteractionListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    onListFragmentInteractionListener.onListFragmentInteraction(holder.audioEvent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView timeTextView;
        public final TextView descriptionTextView;
        public BabyVoiceMonitor.AudioEvent audioEvent;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            timeTextView = (TextView) view.findViewById(R.id.timeTextView);
            descriptionTextView = (TextView) view.findViewById(R.id.descriptionTextView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + descriptionTextView.getText() + "'";
        }
    }
}
