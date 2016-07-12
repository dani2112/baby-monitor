package de.dk_s.babymonitor.gui.eventlist;

import java.util.List;

import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;

public interface EventHistoryDataProvider {

    public List<BabyVoiceMonitor.AudioEvent> get24HoursAudioEvents();

    public BabyVoiceMonitor.AudioEvent getLastAudioEvent();


}
