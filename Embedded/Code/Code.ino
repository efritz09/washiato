/*
 *  Washiato first pass
 */

#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include "ESP8266_MMA8452Q.h"

#define MACHINE_UNOCCUPIED 0
#define MACHINE_WASHING 1
#define MACHINE_FINISHED 2

#define Window_Samples 50

const char* ssid = "Stanford Residences";
static String MAC_address = "";
static int MachineState = MACHINE_UNOCCUPIED;
MMA8452Q accel;

void setup() {
  Serial.begin(115200);
  Serial.println("");
  Serial.print("Connecting to ");
  Serial.println(ssid);

  //Connect to wifi
  WiFi.begin(ssid);

  // Wait until you are actually connected
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  // Get MAC address for use as a unique ID
  byte MAC_array[6];
  WiFi.macAddress(MAC_array);
  for (int i=0; i < sizeof(MAC_array); ++i) {
    MAC_address = MAC_address + String(MAC_array[i], HEX);
  }

  // Print WiFi data for debugging
  Serial.println("");
  Serial.println("WiFi connected");  
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.print("Device MAC: ");
  Serial.println(MAC_address);

  //Set up firebase object
  Firebase.begin("washiato.firebaseIO.com", "N1tSzx0jsBjsBrbpnmHL8Gn3INnlwyrkbyMq110v");

  // Set up accelerometer object
  accel.init(SCALE_2G, ODR_200);
}

void loop() {
 bool isMoving = checkMovement();
}

// Function to calculate moving average/variance and return bool to indicate whether washer is on
bool checkMovement(void) {
  static float z_u;
  static float z_s;
  if (accel.available()) {
    accel.read();
    float new_z = (float)accel.z;
    z_u = z_u + (new_z-z_u)/Window_Samples;
    Serial.println(z_u);
  }
}

