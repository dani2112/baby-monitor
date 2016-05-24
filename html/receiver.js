function SoundReceiver() {


}

SoundReceiver.prototype.startSoundReceiving = function(url) {
    var websocket = new WebSocket('ws://192.168.178.31:8082/audio-stream', 'babymonitor');
    websocket.onopen = function (event) {
      console.log('abcd');
    };
    websocket.onclose = function(){
       console.log('Connection closed');
    }
}

var receiver = new SoundReceiver();
receiver.startSoundReceiving();