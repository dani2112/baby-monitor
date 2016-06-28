package de.dk_s.babymonitor.gui.eventlist.content;

public class BabymonitorEventContent {


    public static class BabymonitorEvent {

        private final int eventType;

        private final long timestamp;

        public BabymonitorEvent(int eventType, long timestamp) {
            this.eventType = eventType;
            this.timestamp = timestamp;
        }

        public int getEventType() {
            return eventType;
        }

        public long getTimestamp() {
            return  timestamp;
        }

    }


}
