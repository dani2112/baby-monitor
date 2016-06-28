package de.dk_s.babymonitor.gui.eventlist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.dk_s.babymonitor.R;
import de.dk_s.babymonitor.gui.eventlist.MonitorEventFragment.OnListFragmentInteractionListener;
import de.dk_s.babymonitor.gui.eventlist.content.BabymonitorEventContent;

public class MyMonitorEventRecyclerViewAdapter extends RecyclerView.Adapter<MyMonitorEventRecyclerViewAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener onListFragmentInteractionListener;

    private List<BabymonitorEventContent.BabymonitorEvent> eventList = new ArrayList<>();

    public MyMonitorEventRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        onListFragmentInteractionListener = listener;
    }

    public void replaceContent(BabymonitorEventContent.BabymonitorEvent[] eventArray) {
        eventList.clear();
        for (BabymonitorEventContent.BabymonitorEvent event : eventArray) {
            eventList.add(event);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_monitorevent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.babymonitorEvent = eventList.get(position);
        holder.timeTextView.setText(String.valueOf(holder.babymonitorEvent.getTimestamp()));
        holder.descriptionTextView.setText(String.valueOf(holder.babymonitorEvent.getEventType()));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onListFragmentInteractionListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    onListFragmentInteractionListener.onListFragmentInteraction(holder.babymonitorEvent);
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
        public BabymonitorEventContent.BabymonitorEvent babymonitorEvent;

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
