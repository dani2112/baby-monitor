package de.dk_s.babymonitor.monitoring;


import java.util.Deque;

public interface AudioEventHistoryDataProvider {

    public Deque<BabyVoiceMonitor.AudioEvent> getRecentAudioEvents();

}
