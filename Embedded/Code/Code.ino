/*
 *  Washiato first pass
 */

#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include "ESP8266_MMA8452Q.h"
#include <Arduino.h>
#include <LiquidCrystal.h>

#define BACKLIGHT_PIN 15

// Machine States
#define MACHINE_UNOCCUPIED 0
#define MACHINE_WASHING 2
#define MACHINE_FINISHED 1

#define OMW_TIMER 0

// Some Constants
#define Window_Samples 5000
#define UPPER_ACCEL_THRESHOLD 200
#define LOWER_ACCEL_THRESHOLD 800
#define DOOR_THRESHOLD 500000
#define RMS_CAP 10000
#define ONE_MIN 60000


// NEED TO BE CHANGED FOR INDIVIDUAL DEVICES!!!
String NFC_ID = "04457C8A6F4080";
const char* ssid = "Stanford Residences";

//const char* ssid = "washiato";
//const char* pwd = "washiato";

// Module level variables
static String MAC_address = "";
static int MachineState = MACHINE_UNOCCUPIED;
static unsigned long lastEvent;
static unsigned long lastOMWTime;
static unsigned long lastCompleteTime;
static bool isMoving;
static bool door_close;
static bool lastOMWStatus;
MMA8452Q accel;
LiquidCrystal lcd(0, 2, 12, 13, 14, 16);

// Initialization function
void setup() {
  Serial.begin(115200);
  delay(100);
  Serial.println("");
  Serial.print("Attempting to connect to ");
  Serial.println(ssid);

  //Set up GPIO
  pinMode(BACKLIGHT_PIN, OUTPUT);

  // set up the LCD's number of columns and rows:
  lcd.begin(16, 2);
  // Print a message to the LCD.
  lcd.print("Connecting to: ");
  lcd.setCursor(0,1);
  lcd.print(ssid);
  // Turn on backlight
  digitalWrite(BACKLIGHT_PIN, HIGH);
  
  //Connect to wifi
  //WiFi.begin(ssid, pwd);
  WiFi.begin(ssid);

  // Get MAC address for use as a unique ID
  byte MAC_array[6];
  WiFi.macAddress(MAC_array);
  for (int i=0; i < sizeof(MAC_array); ++i) {
    MAC_address = MAC_address + String(MAC_array[i], HEX);
  }

  Serial.print("Device MAC: ");
  Serial.println(MAC_address);

  // Wait until you are actually connected
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  // Print WiFi data for debugging
  Serial.println("");
  Serial.println("WiFi connected");  
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  //Set up firebase object
  Firebase.begin("washiato.firebaseIO.com", "N1tSzx0jsBjsBrbpnmHL8Gn3INnlwyrkbyMq110v");
  Serial.println("Firebase Successfully Initialized");

  // Set up accelerometer object
  lcd.clear();
  lcd.print("Init Accel");
  accel.init(SCALE_2G, ODR_800);
  Serial.println("Accelerometer Successfully Initialized");

  // Set FB and LCD statuses
  setFBStatus(MACHINE_UNOCCUPIED);
  setLCDStatus(MACHINE_UNOCCUPIED);
  digitalWrite(BACKLIGHT_PIN, LOW);  
}

// Main loop containing SM
void loop() {
  
 int NextMachineState = MachineState;
 // Update Acceleration values first
 checkMovement();

 // Machine State machine
 switch(MachineState) {
  
  case MACHINE_UNOCCUPIED:
    // If we have detected that it is moving, go to washing state
    if (isMoving) {
      Serial.println("Now Washing");
      NextMachineState = MACHINE_WASHING;
      setFBStatus(MACHINE_WASHING);
      setLCDStatus(MACHINE_WASHING);
      lastEvent = millis();
    }
    break;
  
  case MACHINE_WASHING:
    // If the machine is no longer moving, go to finished state
    if (!isMoving) {
      Serial.println("Now Finished");
      NextMachineState = MACHINE_FINISHED;
      setFBStatus(MACHINE_FINISHED);
      setLCDStatus(MACHINE_FINISHED);
      digitalWrite(BACKLIGHT_PIN, HIGH);
      lastEvent = millis();
      lastCompleteTime = millis();
      setFBTime(0);
    }
    break;
    
  case MACHINE_FINISHED:
      // If a door close has been detected, go to unoccupied state
      if (door_close) {
        Serial.println("Now Unoccupied");
        NextMachineState = MACHINE_UNOCCUPIED;
        setFBStatus(MACHINE_UNOCCUPIED);
        setLCDStatus(MACHINE_UNOCCUPIED);
        resetOMWStatus();
        digitalWrite(BACKLIGHT_PIN, LOW);
        lastEvent = millis();
        lastOMWStatus = false;
        setFBTime(0);
      }

     // If more than a second has passed since we checked if someone is on the way
     if (millis() - lastOMWTime > 1000) {
      // Check if someone is on the way
      if (checkOMWStatus() && lastOMWStatus == false) {
        lcd.clear();
        lcd.print("I'm on my way!");
        lastEvent = millis();
        lastOMWStatus = true;
      }
      lastOMWTime = millis();
     }

     // If we have recently rolled over a minute, update firebase
     if (((millis()-lastCompleteTime)%60000) < 20) {
      setFBTime((uint16_t)((millis()-lastCompleteTime)/60000));
      Serial.println("Setting FB Time");
     }
    break;
 }
 
 // Set the time line on the LCD
 setLCDTime();
 MachineState = NextMachineState;
}

// Function to calculate moving average/variance and return bool to indicate whether washer is on
// Using algorithms from here: http://jonisalonen.com/2014/efficient-and-accurate-rolling-standard-deviation/
void checkMovement(void) {
  // Initialize averages to current value so that we come out of reset with zero variance
  accel.read();
  static float x_mu = (float)accel.x;   // moving average of x acceleration
  static float x_s;                     // moving variance of x acceleration
  static float y_mu = (float)accel.y;   // moving average of y acceleration
  static float y_s;                     // moving variance of y acceleration
  static float z_mu = (float)accel.z;   // moving average of z acceleration
  static float z_s;                     // moving variance of z acceleration

  static float Accel_Var_Threshold = UPPER_ACCEL_THRESHOLD;

  // Update variances if new accelerometer values are available
  if (accel.available()) {
    accel.read();
    float new_x = (float)accel.x;
    float new_y = (float)accel.y;
    float new_z = (float)accel.z;
    // Calculate x stuff
    x_mu = x_mu - (x_mu/Window_Samples) + (new_x/Window_Samples);
    float x_diff = sq(new_x-x_mu);
    
    // Calculate y stuff
    y_mu = y_mu - (y_mu/Window_Samples) + (new_y/Window_Samples);
    float y_diff = sq(new_y-y_mu);
    
    // Calculate z stuff
    z_mu = z_mu - (z_mu/Window_Samples) + (new_z/Window_Samples);
    float z_diff = sq(new_z-z_mu);

    // Check door slam
    door_close = (sqrt(sq(x_diff)+sq(y_diff)+sq(z_diff)) > DOOR_THRESHOLD); 

    // Implement cap to lessen impact of short impacts
    if (x_diff > RMS_CAP) {
      x_diff = RMS_CAP;
    }

    if (y_diff > RMS_CAP) {
      y_diff = RMS_CAP;
    }

    if (z_diff > RMS_CAP) {
      z_diff = RMS_CAP;
    }
    
    // if value wasn't from door, add it to new magnitude
    if (!door_close) {
      x_s = x_s - (x_s/(Window_Samples)) + (x_diff/(Window_Samples));
      y_s = y_s - (y_s/(Window_Samples)) + (y_diff/(Window_Samples));
      z_s = z_s - (z_s/(Window_Samples)) + (z_diff/(Window_Samples));
    }

    //Serial.println(sqrt(sq(x_s)+sq(y_s)+sq(z_s)));
    
  }

  // decide whether we are moving
  isMoving = (sqrt(sq(x_s)+sq(y_s)+sq(z_s)) > Accel_Var_Threshold);

  // Introduce software hysteresis so we don't flip back and forth
//  if (isMoving) {
//    Accel_Var_Threshold = LOWER_ACCEL_THRESHOLD;
//  } else {
//    Accel_Var_Threshold = UPPER_ACCEL_THRESHOLD;
//  }
  
  return;
}

// Function to set the status of the washing machine in FB
void setFBStatus(int newStatus) {
  Firebase.set("Machines/" + NFC_ID + "/status", newStatus);
}

// Function to set the time on the lcd panel since the "last event" variable has been updated
void setLCDTime(void) {
  lcd.setCursor(0,1);
  unsigned long timeDiff = millis()-lastEvent;
  if (timeDiff<ONE_MIN) {
    lcd.print((String)(timeDiff/1000) + " sec ago");
  } else {
    lcd.print((String)(timeDiff/60000) + " m " + (String)((timeDiff%60000)/1000) + " s ago ");
  }
}

// Function to clear the LCD screen and set the status line of the LCD display
void setLCDStatus(int newStatus) {
  switch (newStatus) {
    case MACHINE_UNOCCUPIED:
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("Machine unloaded");
      break;
    case MACHINE_WASHING:
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("Machine started");
      break;
    case MACHINE_FINISHED:
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("Machine finished");
    break;
  }
}

// Function to reset the on my way status for the machine in Firebase
void resetOMWStatus(void) {
  Firebase.set("Machines/" + NFC_ID + "/omw", false);
}

// Returns true if the clothing owner has responded in the app
bool checkOMWStatus(void) {
  return (bool)Firebase.get(("Machines/" + NFC_ID + "/omw").c_str());
}

// Takes a time in minutes since the last cycle completed and posts it to firebase
void setFBTime(uint16_t newMins) {
  Firebase.set("Machines/" + NFC_ID + "/time", newMins);
}

