/*
 *  Simple HTTP get webclient test
 */

#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>

const char* ssid     = "Stanford Residences";
const char* password = "yourpassword";

void setup() {
  Serial.begin(115200);
  delay(100);

  // We start by connecting to a WiFi network

  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  
  WiFi.begin(ssid);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  
  Firebase.begin("washiato.firebaseIO.com", "N1tSzx0jsBjsBrbpnmHL8Gn3INnlwyrkbyMq110v");
}

int value = 0;

void loop() {
  static int n = 0;
  
  // set value
  Firebase.set("number", 42.0);
  // handle error
  if (Firebase.failed()) {
      Serial.print("setting /number failed:");
      Serial.println(Firebase.error());  
      return;
  }
  delay(1000);
  
  // update value
  Firebase.set("number", 43.0);
  delay(1000);

  // get value 
  Serial.print("number: ");
  Serial.println((float)Firebase.get("number"));
  delay(1000);

  // remove value
  Firebase.remove("number");
  delay(1000);

  // set string value
  Firebase.set("message", "hello world");
  delay(1000);
  // set bool value
  Firebase.set("truth", false);
  delay(1000);

  // append a new value to /logs
  String name = Firebase.push("logs", n++);
  Serial.print("pushed: /logs/");
  Serial.println(name);
  delay(1000);
}
