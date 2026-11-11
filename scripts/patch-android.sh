#!/bin/bash
set -e
P=android/app/src/main/java/com/local/musicplayer
cp native/MusicScanner.java $P/
cat > $P/MainActivity.java <<'JAVA'
package com.local.musicplayer;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
public class MainActivity extends BridgeActivity {
  @Override public void onCreate(Bundle b) { registerPlugin(MusicScanner.class); super.onCreate(b); }
}
JAVA
M=android/app/src/main/AndroidManifest.xml
sed -i 's#<application#<uses-permission android:name="android.permission.READ_MEDIA_AUDIO"/>\n    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32"/>\n    <application#' $M
