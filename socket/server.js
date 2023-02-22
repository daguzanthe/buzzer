var SerialPort = require('serialport');
var xbee_api = require('xbee-api');
var C = xbee_api.constants;
var mac_buzzer_64 = "0013a20041c34b12"
var mac_buzzer1_16 = "fd40"
var mac_buzzer2_16 = "5ff7"
var buzzer_command_status = 0x04
var broadcast = "FFFFFFFFFFFFFFFF"
var topic = "buzz"


const mqtt = require('mqtt')
const client  = mqtt.connect('mqtt://broker.hivemq.com:1883')




// me: mom can we have a simple sleep methode
// mom: we already have a simple sleep methode at home 
// sleep methode at home : 
function sleep(milliseconds) {
  var start = new Date().getTime();
  for (var i = 0; i < 1e7; i++) {
    if ((new Date().getTime() - start) > milliseconds){
      break;
    }
  }
}

// function sleep(ms) {
//   return new Promise(resolve => setTimeout(resolve, ms));
// }



client.on('connect', function () {
  client.subscribe(topic, function (err) {
    if (!err) {
      client.publish(topic, 'Hello mqtt cest pedro')
    }
  })
})

client.on('message', function (topic, message) {
  // message is Buffer
  console.log(topic, message.toString())

  if (message.toString() == "false") 
  {
    frame_obj_mess = { // AT Request to be sent
      type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
      destination64: broadcast,
      command: "D0",
      commandParameter: [0x04],
    };
    xbeeAPI.builder.write(frame_obj_mess);
  } else {
    for (let index = 0; index < 10; index++) 
    {
      frame_obj_mess = { // AT Request to be sent
        type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
        destination64: message.toString(), //????????????????????????????????????????????????????????????????????????????????
        command: "D0",
        commandParameter: [0x05],
      };
      xbeeAPI.builder.write(frame_obj_mess);

      sleep(100)

      frame_obj_mess = { // AT Request to be sent
        type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
        destination64: message.toString(), //????????????????????????????????????????????????????????????????????????????????
        command: "D0",
        commandParameter: [0x04],
      };
      xbeeAPI.builder.write(frame_obj_mess);
    }
    
  }
  

  //client.end()
})


// var storage = require("./storage")
require('dotenv').config()


const SERIAL_PORT = process.env.SERIAL_PORT;

var xbeeAPI = new xbee_api.XBeeAPI({
  api_mode: 1
});

let serialport = new SerialPort(SERIAL_PORT, {
  baudRate: parseInt(process.env.SERIAL_BAUDRATE) || 9600,
}, function (err) {
  if (err) {
    return console.log('Error: ', err.message)
  }
});

serialport.pipe(xbeeAPI.parser);
xbeeAPI.builder.pipe(serialport);

serialport.on("open", function () {
  var frame_obj = { // AT Request to be sent
    type: C.FRAME_TYPE.AT_COMMAND,
    command: "NI",
    commandParameter: [],
  };

  xbeeAPI.builder.write(frame_obj);

  frame_obj = { // AT Request to be sent
    type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
    destination64: broadcast,
    command: "NI",
    commandParameter: [],
  };
  xbeeAPI.builder.write(frame_obj);

});

// All frames parsed by the XBee will be emitted here

// storage.listSensors().then((sensors) => sensors.forEach((sensor) => console.log(sensor.data())))

xbeeAPI.parser.on("data", function (frame) {
  //on new device is joined, register it

  if (C.FRAME_TYPE.ZIGBEE_RECEIVE_PACKET === frame.type) {
    console.log("C.FRAME_TYPE.ZIGBEE_RECEIVE_PACKET");
    let dataReceived = String.fromCharCode.apply(null, frame.data);
    console.log(">> ZIGBEE_RECEIVE_PACKET >", dataReceived);

  }

  if (C.FRAME_TYPE.NODE_IDENTIFICATION === frame.type) {
    // let dataReceived = String.fromCharCode.apply(null, frame.nodeIdentifier);
    console.log("NODE_IDENTIFICATION");

    // publish mac of the buzz man on mqtt
    var clicker_mac = frame.remote64 //  ?????????????????????????????????????????????????????????????????????????????????????????,
    client.publish("player/login", clicker_mac)

  } else if (C.FRAME_TYPE.ZIGBEE_IO_DATA_SAMPLE_RX === frame.type) {


    console.log("ZIGBEE_IO_DATA_SAMPLE_RX")
    var clicker_mac = frame.remote64
    console.log("clicker_mac!!!!!!")
    console.log(clicker_mac)
    if (frame.digitalSamples.DIO1 == 1) {
      buzzer_command_status = 0x05
      frame_obj = { // AT Request to be sent
        type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
        destination64: clicker_mac,
        command: "D0",
        commandParameter: [0x05],
      };
      
      // publish mac of the buzz man on mqtt
      client.publish("player/action", clicker_mac)
          
    if (condition) {
      xbeeAPI.builder.write(frame_obj);
    }
    

    }
    
  } else if (C.FRAME_TYPE.REMOTE_COMMAND_RESPONSE === frame.type) {
    console.log("REMOTE_COMMAND_RESPONSE :")
    console.log(frame)
    // let dataReceived = String.fromCharCode.apply(null, frame.commandData);
    // console.log(dataReceived)
  } else {
    console.debug(frame);
    

    let dataReceived = String.fromCharCode.apply(null, frame.commandData)
    console.log(dataReceived);
  }

});




