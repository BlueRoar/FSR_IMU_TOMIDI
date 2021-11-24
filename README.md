# FSR_IMU_TOMIDI
Processing code to interface with an Arduino + Xbee and convert FSR and IMU events to MIDI values.

INSTRUCTIONS

Install the FTDIUSBSerial.pkg or download the appropriate FTDI driver so that you can use the Xbee to USB adapter with your computer, you might already have a version installed, so…

Plug the XBee to USB adapter into your Mac. 

Run FSR_IMU_PROCESSING_jun_2018 either from the processing .pde or the application.macosx folder. 

To map the midi values, turn the interface off, but leave the XBee to USB adapter plugged into your computer. Put the software you want to control in midi map mode and then manually move the Roll, Pitch, Yaw, and FSR1 sliders to map them.

Plug a micro usb into the Arduino to power the whole interface from a standard 5V battery bank.

Remember to hold the interface still for a few seconds after turning it on so that it calibrates. 

Now when you move the interface or press the FSR you will get midi control of your software!

$$Profit$$

FTDIUSBSerial download links:
https://www.parallax.com/downloads/parallax-ftdi-usb-drivers-windows
https://www.parallax.com/downloads/mac-ftdi-usb-driver

To upload code to the Arduino Uno clone (Metro Mini) you will need to install the SiLabsUSBDriverDisk for Mac, or download the windows one yourself lol. 
https://learn.adafruit.com/adafruit-metro-mini/pinouts


I already set the Xbee channel to 6969 so it won’t be interfered with by other Xbees with default settings.

To modify the configuration of the Xbee radios… https://www.digi.com/products/xbee-rf-solutions/xctu-software/xctu

