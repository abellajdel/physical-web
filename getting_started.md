# Seeing the Physical Web for yourself

In order to get up and running you need two things: 1) A hardware beacon and 2) software on your phone/tablet to see that beacon.

The software is the easiest thing to take care of as there is an android app (and source) here in this repo (We're also working on an iOS port) Here is the link to the [android app](https://github.com/scottjenson/physical-web/tree/master/android/PhysicalWeb/build/apk)

The trickier thing is to get a beacon broadcasting your URL. So many BLE devices make this far too hard. We're working on a much simple maker-friendly device but that's not quite ready yet. The simplest way, at the moment, is to get an [RFDuino](http://www.rfduino.com/) as it is so easy to program (it's not pretty or very small, it's just fairly cheap and easy to program)

This will allow you to change the NAME parameter in the ad packet with:

    void setup() {
      RFduinoBLE.deviceName = "cnn.com";
      RFduinoBLE.begin();
    }

Once this is up and running, the Physical Web android app should see it.

If there are other devices out there that can setup easily, please do a pull require and add it to this list.
