var audioContext;
window.addEventListener('load', init, false);
function init() {
  try {
    window.AudioContext = window.AudioContext||window.webkitAudioContext;
    audioContext = new AudioContext();
  }
  catch(ex) {
    console.log('Audio playback not supported');
  }
}

function SoundReceiver() {
    this.bufferedChunkCount = 3;
    this.chunkQueue = [];

    this.channelCount = 1;
    this.sampleRate = 41000;
    this.startTime = 0;
}

SoundReceiver.prototype.channelCount = 1;
SoundReceiver.sampleRate = 41000;

SoundReceiver.prototype.startSoundReceiving = function(url) {
    var websocket = new WebSocket('ws://192.168.178.31:8082/audio-stream');
    websocket.binaryType = 'arraybuffer';
    websocket.onopen = function (event) {
      console.log('Websocket connection opened');
    };
    websocket.onclose = function(){
       console.log('Websocket connection closed');
    }
    var self = this;
    websocket.onmessage = function (event) {
        var data = event.data;
        var dataLength = data.byteLength;
        var dataView = new DataView(data);
        var processedAudio = new Float32Array(dataLength);
        for(var i = 0; i < dataLength; i++) {
            var sample = dataView.getInt8(i);
            processedAudio[i] = sample < 0 ? sample / 0x80 : sample / 0x7F;
        }
        var audioBuffer = audioContext.createBuffer(self.channelCount, dataLength, self.sampleRate);
        audioBuffer.getChannelData(0).set(processedAudio);
        var source = audioContext.createBufferSource();
        source.buffer = audioBuffer;
        source.start(self.startTime);
        source.connect(audioContext.destination);
        self.startTime += audioBuffer.duration;
        console.log('Play');
    };
}

var receiver = new SoundReceiver();
receiver.startSoundReceiving();