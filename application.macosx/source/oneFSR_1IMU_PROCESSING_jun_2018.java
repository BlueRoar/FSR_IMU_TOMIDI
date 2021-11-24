import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 
import controlP5.*; 
import themidibus.*; 
import g4p_controls.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class oneFSR_1IMU_PROCESSING_jun_2018 extends PApplet {


 // gui control library
 //MIDI IO Library

int fsr1 = 0;
int fsr2 = 0;
int FSR1MIN = 100;
int FSR2MIN = 100;
boolean IMUon = false;
int ROLLMIN = 0;
int ROLLMAX = 180;
int PITCHMIN;
int PITCHMAX;
int YAWMIN;
int YAWMAX;
MidiBus myBus;
ControlP5 cp5;

//reset timer 
int savedTime;
int totalTime = 4000;

GCheckbox printSerialCheckbox;

Serial myPort;  // Create object from Serial class

final String serialPort = "/dev/cu.usbserial-DN0314LQ"; // replace this with your serial port. On windows you will need something like "COM1".

float [] q = new float [4];
float [] hq = null;
float [] Euler = new float [3]; // psi, theta, phi
boolean      printSerial = true;
int lf = 10; // 10 is '\n' in ASCII
byte[] inBuffer = new byte[22]; // this is the number of chars on each line from the Arduino (including /r/n)

PFont font;
final int VIEW_SIZE_X = 800, VIEW_SIZE_Y = 600;


public void setup() 
{
  
  myPort = new Serial(this, serialPort, 9600);  

  font = createFont("Courier", 32); 

  // List all available Midi devices on STDOUT. This will show each device's index and name.
  MidiBus.list(); 

  // declare your IAC in / out channels
  //                 parent In Out
  //                   |    |  |
  myBus = new MidiBus(this, 1, 1);

  //declare your gui control
  cp5 = new ControlP5(this);

  // printSerialCheckbox = new GCheckbox(this, 5, 50, 200, 20, "Print serial data");

  // create sliders
  cp5.addSlider("FSR1")
    .setPosition(10, 50)
      .setSize(80, 200)
        .setRange(0, 700)
          .setValue(128)
            ;
  // create sliders
  cp5.addSlider("FSR1MIN")
    .setPosition(110, 50)
      .setSize(30, 200)
        .setRange(0, 1010)
          .setValue(217)
            ;
  cp5.addSlider("FSR2")
    .setPosition(700, 50)
      .setSize(80, 200)
        .setRange(0, 933)
          .setValue(128)
            ;


  cp5.addSlider("FSR2MIN")
    .setPosition(650, 50)
      .setSize(30, 200)
        .setRange(0, 933)
          .setValue(180)
            ;
  // 9DOF values

  cp5.addSlider("ROLLMIN")
    .setPosition(200, 50)
      .setSize(20, 80)
        .setRange(-180, 180)
          .setValue(0)
            ;
  cp5.addSlider("ROLL")
    .setPosition(250, 50)
      .setSize(20, 80)
        .setRange(-180, 180)
          .setValue(128)
            ;
  cp5.addSlider("ROLLMAX")
    .setPosition(300, 50)
      .setSize(20, 80)
        .setRange(-180, 180)
          .setValue(180)
            ;

  cp5.addSlider("PITCHMIN")
    .setPosition(350, 50)
      .setSize(20, 80)
        .setRange(-90, 90)
          .setValue(0)
            ;

  cp5.addSlider("PITCH")
    .setPosition(400, 50)
      .setSize(20, 80)
        .setRange(-90, 90)
          .setValue(128)
            ;

  cp5.addSlider("PITCHMAX")
    .setPosition(450, 50)
      .setSize(20, 80)
        .setRange(-90, 90)
          .setValue(90)
            ;

  cp5.addSlider("YAWMIN")
    .setPosition(500, 50)
      .setSize(20, 80)
        .setRange(-180, 180)
          .setValue(0)
            ;

  cp5.addSlider("YAW")
    .setPosition(550, 50)
      .setSize(20, 80)
        .setRange(-180, 180)
          .setValue(128)
            ;
  cp5.addSlider("YAWMAX")
    .setPosition(600, 50)
      .setSize(20, 80)
        .setRange(-180, 180)
          .setValue(180)
            ;

  delay(100);
  myPort.clear();
  myPort.write("1");
}


public float decodeFloat(String inString) {
  byte [] inData = new byte[4];

  if (inString.length() == 8) {
    inData[0] = (byte) unhex(inString.substring(0, 2));
    inData[1] = (byte) unhex(inString.substring(2, 4));
    inData[2] = (byte) unhex(inString.substring(4, 6));
    inData[3] = (byte) unhex(inString.substring(6, 8));
  }

  int intbits = (inData[3] << 24) | ((inData[2] & 0xff) << 16) | ((inData[1] & 0xff) << 8) | (inData[0] & 0xff);
  return Float.intBitsToFloat(intbits);
}




// READ SERIAL //


public void readQ() {
  if (myPort.available() >= 18) {
    String inputString = myPort.readStringUntil('\n');
    //print(inputString);
    if (inputString != null && inputString.length() > 0) {
      String [] inputStringArr = split(inputString, ",");
      if (inputStringArr.length >= 5) { // q1,q2,q3,q4,\r\n so we have 5 elements
        // q0 through q2 will be your roll pitch yaw eulers.. q3 is extra
        // the below code reads stores the serial data into the array
        q[0] = PApplet.parseFloat(inputStringArr[0]);
        q[1] = PApplet.parseFloat(inputStringArr[1]);
        q[2] = PApplet.parseFloat(inputStringArr[2]);
        q[3] = PApplet.parseFloat(inputStringArr[3]);
        fsr1 = PApplet.parseInt(inputStringArr[4]);
        fsr2 = PApplet.parseInt(inputStringArr[5]);
        
        // the below code sets the controller GUI which also sends the midi via the corresponding function
        cp5.getController("ROLL").setValue(degrees(Euler[2])); 
        cp5.getController("PITCH").setValue(degrees(Euler[1])); 
        cp5.getController("YAW").setValue(degrees(Euler[0])); 
        cp5.getController("FSR1").setValue(fsr1); 
        cp5.getController("FSR2").setValue(fsr2); 
 
      }
    }
  }
}


public void buildBoxShape() {
  //box(60, 10, 40);
  noStroke();
  beginShape(QUADS);

  //Z+ (to the drawing area)
  fill(0xff00ff00);
  vertex(-30, -5, 20);
  vertex(30, -5, 20);
  vertex(30, 5, 20);
  vertex(-30, 5, 20);

  //Z-
  fill(0xff0000ff);
  vertex(-30, -5, -20);
  vertex(30, -5, -20);
  vertex(30, 5, -20);
  vertex(-30, 5, -20);

  //X-
  fill(0xffff0000);
  vertex(-30, -5, -20);
  vertex(-30, -5, 20);
  vertex(-30, 5, 20);
  vertex(-30, 5, -20);

  //X+
  fill(0xffffff00);
  vertex(30, -5, -20);
  vertex(30, -5, 20);
  vertex(30, 5, 20);
  vertex(30, 5, -20);

  //Y-
  fill(0xffff00ff);
  vertex(-30, -5, -20);
  vertex(30, -5, -20);
  vertex(30, -5, 20);
  vertex(-30, -5, 20);

  //Y+
  fill(0xff00ffff);
  vertex(-30, 5, -20);
  vertex(30, 5, -20);
  vertex(30, 5, 20);
  vertex(-30, 5, 20);

  endShape();
}


public void drawCube() {  
  pushMatrix();
  translate(VIEW_SIZE_X/2, VIEW_SIZE_Y/2 + 50, 0);
  scale(5, 5, 5);

  // a demonstration of the following is at 
  // http://www.varesano.net/blog/fabio/ahrs-sensor-fusion-orientation-filter-3d-graphical-rotating-cube
  rotateZ(-Euler[2]);
  rotateX(-Euler[1]);
  rotateY(-Euler[0]);

  buildBoxShape();

  popMatrix();
}


public void draw() {
  background(0xff000000);
  fill(0xffffffff);

  readQ();

  if (hq != null) { // use home quaternion
    quaternionToEuler(quatProd(hq, q), Euler);
    text("Disable home position by pressing \"n\"", 20, VIEW_SIZE_Y - 30);
  } else {
    quaternionToEuler(q, Euler);
    text("Point interface at monitor then press \"c\"", 20, VIEW_SIZE_Y - 30);
  }

  // euler angels text
  /* textFont(font, 20);
   textAlign(LEFT, TOP);
   text("Euler Angles:\nYaw (psi)  : " + degrees(Euler[0]) + "\nPitch (theta): " + degrees(Euler[1]) + "\nRoll (phi)  : " + degrees(Euler[2]), 200, 20);
   */
  drawCube();
}


public void keyPressed() {
  if (key == 'c') {
    println("pressed c");

    // set hq the home quaternion as the quatnion conjugate coming from the sensor fusion
    hq = quatConjugate(q);
  } else if (key == 'n') {
    println("pressed n");
    hq = null;
  }
}

// See Sebastian O.H. Madwick report 
// "An efficient orientation filter for inertial and intertial/magnetic sensor arrays" Chapter 2 Quaternion representation

public void quaternionToEuler(float [] q, float [] euler) {
  euler[0] = atan2(2 * q[1] * q[2] - 2 * q[0] * q[3], 2 * q[0]*q[0] + 2 * q[1] * q[1] - 1); // psi
  euler[1] = -asin(2 * q[1] * q[3] + 2 * q[0] * q[2]); // theta
  euler[2] = atan2(2 * q[2] * q[3] - 2 * q[0] * q[1], 2 * q[0] * q[0] + 2 * q[3] * q[3] - 1); // phi
}

public float [] quatProd(float [] a, float [] b) {
  float [] q = new float[4];

  q[0] = a[0] * b[0] - a[1] * b[1] - a[2] * b[2] - a[3] * b[3];
  q[1] = a[0] * b[1] + a[1] * b[0] + a[2] * b[3] - a[3] * b[2];
  q[2] = a[0] * b[2] - a[1] * b[3] + a[2] * b[0] + a[3] * b[1];
  q[3] = a[0] * b[3] + a[1] * b[2] - a[2] * b[1] + a[3] * b[0];

  return q;
}

// returns a quaternion from an axis angle representation
public float [] quatAxisAngle(float [] axis, float angle) {
  float [] q = new float[4];

  float halfAngle = angle / 2.0f;
  float sinHalfAngle = sin(halfAngle);
  q[0] = cos(halfAngle);
  q[1] = -axis[0] * sinHalfAngle;
  q[2] = -axis[1] * sinHalfAngle;
  q[3] = -axis[2] * sinHalfAngle;

  return q;
}

// return the quaternion conjugate of quat
public float [] quatConjugate(float [] quat) {
  float [] conj = new float[4];

  conj[0] = quat[0];
  conj[1] = -quat[1];
  conj[2] = -quat[2];
  conj[3] = -quat[3];

  return conj;
}

public void handleToggleControlEvents(GToggleControl checkbox, GEvent event) { 
  // Checkbox toggle events, check if print events is toggled.
  if (checkbox == printSerialCheckbox) {
    printSerial = printSerialCheckbox.isSelected();
  }
}


// what to do when the faders move

public void ROLL(int theValue) {

  // myBus.sendControllerChange(channel, number, value); //SYNTAX
  int midivalue = PApplet.parseInt(map(theValue, ROLLMIN, ROLLMAX, 0, 127));
  myBus.sendControllerChange(0, 1, midivalue); // Send a controllerChange
}


public void PITCH(int theValue) {

  // myBus.sendControllerChange(channel, number, value); //SYNTAX
  int midivalue = PApplet.parseInt(map(theValue, PITCHMIN,PITCHMAX, 0, 127));
  myBus.sendControllerChange(0, 2, midivalue); // Send a controllerChange
}



public void YAW(int theValue) {

  // myBus.sendControllerChange(channel, number, value); //SYNTAX
  int midivalue = PApplet.parseInt(map(theValue, YAWMIN, YAWMAX, 0, 127));
  myBus.sendControllerChange(0, 3, midivalue); // Send a controllerChange
}



public void FSR1(int theValue) {

  // myBus.sendControllerChange(channel, number, value); //SYNTAX
  int midivalue = PApplet.parseInt(map(theValue, FSR1MIN, 700, 0, 127));
  myBus.sendControllerChange(0, 4, midivalue); // Send a controllerChange
     
}
public void FSR1MIN(int theValue) {
FSR1MIN = theValue;
}
public void FSR2(int theValue) {

  // myBus.sendControllerChange(channel, number, value); //SYNTAX
  int midivalue = PApplet.parseInt(map(theValue, FSR2MIN, 933, 0, 127));
  myBus.sendControllerChange(0, 5, midivalue); // Send a controllerChange

/// reset stuff kinda //
     if (theValue > 800)
    { 
      int passedTime = millis() - savedTime;
      // has 5 seconds passed?
     if (passedTime > totalTime)
    {
  println("FSR - pressed c");
    
        // set hq the home quaternion as the quatnion conjugate coming from the sensor fusion
        hq = quatConjugate(q);
     savedTime = millis();
    }
  
    }
    // end reset kinda works


}
public void FSR2MIN(int theValue) {

  // myBus.sendControllerChange(channel, number, value); //SYNTAX
FSR2MIN = theValue;
}


public void ROLLMIN(int theValue) {

  // myBus.sendControllerChange(channel, number, value); //SYNTAX
ROLLMIN = theValue;
}
public void ROLLMAX(int theValue) {

  // myBus.sendControllerChange(channel, number, value); //SYNTAX
ROLLMAX = theValue;
}

public void YAWMIN(int theValue) {

  // myBus.sendControllerChange(channel, number, value); //SYNTAX
YAWMIN = theValue;
}

public void YAWMAX(int theValue) {

  // myBus.sendControllerChange(channel, number, value); //SYNTAX
YAWMAX = theValue;
}
public void mouseClicked() {
  //println(mouseX, " + ", mouseY);
 // println(YAWMAX);
}
  public void settings() {  size(800, 600, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "oneFSR_1IMU_PROCESSING_jun_2018" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
