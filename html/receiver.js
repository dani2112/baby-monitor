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
    this.chunkQueue = [];
    this.chunkLength = 0.25;
    this.lastTime = 0;
    this.isStarted = false;

    this.channelCount = 1;
    this.sampleRate = 44100;
}

SoundReceiver.prototype.startSoundReceiving = function(url) {
    /* Keep this reference in nested functions */
    var self = this;
    /* Start sound scheduler */
    setTimeout(function schedule() {
        if(self.chunkQueue.length > 3 && isStarted == false) {
            isStarted = true;
            var currentTime = audioContext.currentTime;
            self.lastTime = currentTime;
            self.playSound(self.chunkQueue.shift().data, self.lastTime);
        } else if (self.chunkQueue.length > 0 && isStarted == true){
            var currentTime = audioContext.currentTime;
            console.log(currentTime);
            if(currentTime - self.lastTime > self.chunkLength - 0.1) {
                self.lastTime = self.lastTime + self.chunkLength;
                self.playSound(self.chunkQueue.shift().data, self.lastTime);
            }
        } else {
            isStarted = false;
        }
        setTimeout(schedule, 100);
    }, 0);

    /* Open websocket connection */
    var websocket = new WebSocket('ws://192.168.178.31:8082/audio-stream');
    websocket.binaryType = 'arraybuffer';
    websocket.onopen = function (event) {
      console.log('Websocket connection opened');
    };
    websocket.onclose = function(){
       console.log('Websocket connection closed');
    }
    websocket.onmessage = function (event) {
        var data = event.data;
        var dataLength = data.byteLength;
        var processedAudio = self.convertData(data);
        var currentTimestamp = audioContext.currentTime;
        self.chunkQueue.push({data: processedAudio, timestamp: currentTimestamp});
    };
}

SoundReceiver.prototype.convertData = function(data) {
        var dataLength = data.byteLength;
        var dataView = new DataView(data);
        var processedAudio = new Float32Array(dataLength);
        for(var i = 0; i < dataLength; i++) {
            var sample = dataView.getInt8(i);
            processedAudio[i] = sample < 0 ? sample / 0x80 : sample / 0x7F;
        }
        return processedAudio;
}

SoundReceiver.prototype.playSound = function(data, time) {
        var self = this;
        var dataLength = data.byteLength;
        var audioBuffer = audioContext.createBuffer(self.channelCount, dataLength, self.sampleRate);
        audioBuffer.getChannelData(0).set(data);
        var source = audioContext.createBufferSource();
        source.buffer = audioBuffer;
        source.start(time);
        source.connect(audioContext.destination);
}

var receiver = new SoundReceiver();
receiver.startSoundReceiving();