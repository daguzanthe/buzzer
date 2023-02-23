var SerialPort = require('serialport');
var xbee_api = require('xbee-api');
var C = xbee_api.constants;
var mac_buzzer_64 = "0013a20041c34b12"
var mac_buzzer1_16 = "fd40"
var mac_buzzer2_16 = "5ff7"
var buzzer_command_status = 0x04
var broadcast = "FFFFFFFFFFFFFFFF"
var num_player = 0
var clicker_mac = broadcast

var names = ["pedro", "thomas", "damien", "matteo"]



const mqtt = require('mqtt')
const client  = mqtt.connect('mqtt://broker.hivemq.com:1883')



client.on('connect', function () {
  client.subscribe("buzz", function (err) {
    if (!err) {
      client.publish("test", 'Hello MQTT')
    }
  })
})


///////////////////////////////////////////////////////////////
// MQTT messages
///////////////////////////////////////////////////////////////
client.on('message', function (topic, message) {
  console.log(topic, message.toString())

  // ALL OFF
  var frame_obj_blink = {
    type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
    destination64: broadcast,
    command: "D0",
    commandParameter: [0x04],
  };

  // MQTT message is  a MAC adress or true
  if (message.toString() != "false") {
    // MQTT message is  a MAC adress
    if (message.toString() != "true") {
      clicker_mac = message.toString()

      // First Clicker ON
      buzzer_command_status = 0x05
      frame_obj = {
        type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
        destination64: clicker_mac,
        command: "D0",
        commandParameter: [0x05],
      };
      xbeeAPI.builder.write(frame_obj);

      // MQTT message is true
    } else {
      // blink the buzzer 10 times
      for (let index = 0; index < 10; index++) 
      {
        frame_obj_blink = {
          type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
          destination64: clicker_mac, 
          command: "D0",
          commandParameter: [0x05],
        };
        xbeeAPI.builder.write(frame_obj_blink);

        frame_obj_blink = {
          type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
          destination64: clicker_mac, 
          command: "D0",
          commandParameter: [0x04],
        };
        xbeeAPI.builder.write(frame_obj_blink);


      }

      // Switch all off at the end
      frame_obj_blink = {
        type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
        destination64: broadcast,
        command: "D0",
        commandParameter: [0x04],
      };
      xbeeAPI.builder.write(frame_obj_blink);
    }
  
    // MQTT message is false
  } else {
    // Switch all off
    buzzer_command_status = 0x04
    frame_obj = {
      type: C.FRAME_TYPE.REMOTE_AT_COMMAND_REQUEST,
      destination64: broadcast,
      command: "D0",
      commandParameter: [0x04],
    };

    xbeeAPI.builder.write(frame_obj);
  }
  

})


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


xbeeAPI.parser.on("data", function (frame) {
  //on new device is joined, register it

  if (C.FRAME_TYPE.ZIGBEE_RECEIVE_PACKET === frame.type) {
    console.log("C.FRAME_TYPE.ZIGBEE_RECEIVE_PACKET");
    let dataReceived = String.fromCharCode.apply(null, frame.data);
    // console.log(">> ZIGBEE_RECEIVE_PACKET >", dataReceived);

  }

  if (C.FRAME_TYPE.NODE_IDENTIFICATION === frame.type) {
    console.log("NODE_IDENTIFICATION");

    // publish mac of the buzz man on mqtt
    var clicker_mac = frame.remote64 
    client.publish("player/login", clicker_mac + ',' + names[num_player]); 
    num_player = (num_player + 1)%4;

  } else if (C.FRAME_TYPE.ZIGBEE_IO_DATA_SAMPLE_RX === frame.type) {


    console.log("ZIGBEE_IO_DATA_SAMPLE_RX")
    var clicker_mac = frame.remote64;
    console.log(String(clicker_mac) + " cliked");
    console.log(clicker_mac)
    if (frame.digitalSamples.DIO1 == 1) {
      // publish mac of the buzz man on mqtt
      client.publish("player/action", clicker_mac)

    }
    
  } else if (C.FRAME_TYPE.REMOTE_COMMAND_RESPONSE === frame.type) {
    console.log("REMOTE_COMMAND_RESPONSE :")
    console.log(frame)
    
  } else {
    console.debug(frame);
    

    let dataReceived = String.fromCharCode.apply(null, frame.commandData)
    console.log(dataReceived);
  }

});




