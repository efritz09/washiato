# washiato
ME 202 project

### Ideas for libraries
Since Adriano says we need to use 2 more libraries, here's a short list of some that I think we could use:

[Leak Canary](https://github.com/square/leakcanary): Detects memory leaks and stops them.

[Calligraphy](https://github.com/chrisjenx/Calligraphy): Custom fonts.

Otherwise, check out [this](https://github.com/codepath/android_guides/wiki/Must-Have-Libraries) for more libraries. There's a huge list that's split up into categories at the bottom.

=================================================================================================

### 05/06/2016- Arya
This is a basic barebones app template copied off of my smart bike code. Develop further! What it does:

-Compiles and runs (LOL)

-Registers a user with Firebase

-Allows you to login via Firebase

-Shows a silly laundry meme when you login. I'll play with it more to make it better, but this is what I currently have.


=================================================================================================

### 05/09/2016- Arya
This one reads NFC tags and displays serial number of each tag in Control Activity; also pushes the serial number to Firebase (another child under user uid). FYI, if you try a different NFC tag after, it updates the serial number in Firebase and on screen.

Files changed: ControlActivity.java, AndroidManifest.xml, content_control.xml

Working on how to destroy the serial number data after a washing cycle is complete (because if another user uses it afterward, the old user might still keep seeing time remaining, etc?).

