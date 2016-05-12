/*
 *  Washiato first pass
 */

#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include "ESP8266_MMA8452Q.h"
#include <Arduino.h>

#define DOOR_PIN 16

#define MACHINE_UNOCCUPIED 0
#define MACHINE_WASHING 1
#define MACHINE_FINISHED 2

#define Window_Samples 1000
#define UPPER_ACCEL_THRESHOLD 1200
#define LOWER_ACCEL_THRESHOLD 800

const char* ssid = "Stanford Residences";

//const char* ssid = "AdriAndroid";

static String MAC_address = "";
static int MachineState = MACHINE_UNOCCUPIED;
MMA8452Q accel;

void setup() {
  Serial.begin(115200);
  delay(100);
  Serial.println("");
  Serial.print("Attempting to connect to ");
  Serial.println(ssid);

  //Set up GPIO
  pinMode(DOOR_PIN, INPUT);       // Set door pin as input
  digitalWrite(DOOR_PIN, HIGH);   // Activate internal pullups (apparently not working)
  
  //Connect to wifi
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
  accel.init(SCALE_2G, ODR_800);
  Serial.println("Accelerometer Successfully Initialized");

  setFBStatus(MACHINE_UNOCCUPIED);
}

void loop() {
 int NextMachineState = MachineState;
 bool isMoving = checkMovement();
 bool doorStatus = checkDoor();
 
 switch(MachineState) {
  case MACHINE_UNOCCUPIED:
    if (isMoving) {
      Serial.println("Now Washing");
      NextMachineState = MACHINE_WASHING;
      setFBStatus(MACHINE_WASHING);
    }
    break;
  case MACHINE_WASHING:
    if (!isMoving) {
      Serial.println("Now Finished");
      NextMachineState = MACHINE_FINISHED;
      setFBStatus(MACHINE_FINISHED);
    }
    break;
  case MACHINE_FINISHED:
    if (doorStatus) {
      Serial.println("Now Unoccupied");
      NextMachineState = MACHINE_UNOCCUPIED;
      setFBStatus(MACHINE_UNOCCUPIED);
    }
    break;
 }
 MachineState = NextMachineState;
}

// Function to calculate moving average/variance and return bool to indicate whether washer is on
// Using algorithms from here: http://jonisalonen.com/2014/efficient-and-accurate-rolling-standard-deviation/
bool checkMovement(void) {
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
    x_s = x_s - (x_s/(Window_Samples)) + (sq(new_x-x_mu)/(Window_Samples));
    // Calculate y stuff
    y_mu = y_mu - (y_mu/Window_Samples) + (new_y/Window_Samples);
    y_s = y_s - (y_s/(Window_Samples)) + (sq(new_y-y_mu)/(Window_Samples));
    // Calculate z stuff
    z_mu = z_mu - (z_mu/Window_Samples) + (new_z/Window_Samples);
    z_s = z_s - (z_s/(Window_Samples)) + (sq(new_z-z_mu)/(Window_Samples));
    //Serial.println(x_s);

    Serial.print((String)sq(new_x-x_mu) + "\t");
    Serial.print((String)sq(new_y-y_mu) + "\t");
    Serial.print((String)sq(new_z-z_mu) + "\t");

    Serial.println(sqrt(sq(x_s)+sq(y_s)+sq(z_s)));
    
  }

  // decide whether we are moving
  bool isMoving = ((x_s>Accel_Var_Threshold) || (y_s>Accel_Var_Threshold) || (z_s>Accel_Var_Threshold));

  // Introduce software hysteresis so we don't flip back and forth
  if (isMoving) {
    Accel_Var_Threshold = LOWER_ACCEL_THRESHOLD;
  } else {
    Accel_Var_Threshold = UPPER_ACCEL_THRESHOLD;
  }
  
  return isMoving;
}

// Returns true if door switch is open and false if it is closed
bool checkDoor() {
  return digitalRead(DOOR_PIN);
}

// Function to set the status of the washing machine in FB
void setFBStatus(int newStatus) {
  Firebase.set("Machines/" + MAC_address + "/Status", newStatus);
}

