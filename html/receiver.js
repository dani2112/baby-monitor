function SoundReceiver() {


}

SoundReceiver.prototype.startSoundReceiving = function(url) {
    var websocket = new WebSocket('ws://192.168.178.31:8082/audio-stream');
    websocket.onopen = function (event) {
      console.log('abcd');
      websocket.send("Here's some text that the server is urgently awaiting!");
    };
    websocket.onclose = function(){
       console.log('Connection closed');
    }
}

var receiver = new SoundReceiver();
receiver.startSoundReceiving();