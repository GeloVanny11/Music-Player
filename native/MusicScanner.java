package com.local.musicplayer;

import android.Manifest;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import com.getcapacitor.*;
import com.getcapacitor.annotation.*;

@CapacitorPlugin(name = "MusicScanner", permissions = {
  @Permission(alias = "audio", strings = {"android.permission.READ_MEDIA_AUDIO"}),
  @Permission(alias = "storage", strings = {Manifest.permission.READ_EXTERNAL_STORAGE})
})
public class MusicScanner extends Plugin {
  private String alias() { return Build.VERSION.SDK_INT >= 33 ? "audio" : "storage"; }

  @PluginMethod
  public void scan(PluginCall call) {
    if (getPermissionState(alias()) != PermissionState.GRANTED) {
      requestPermissionForAlias(alias(), call, "onPerm");
      return;
    }
    run(call);
  }

  @PermissionCallback
  private void onPerm(PluginCall call) {
    if (getPermissionState(alias()) == PermissionState.GRANTED) run(call);
    else call.reject("denied");
  }

  private void run(PluginCall call) {
    JSArray arr = new JSArray();
    String[] cols = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
      MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA};
    String sel = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DURATION + " > 10000";
    try (Cursor c = getContext().getContentResolver().query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols, sel, null,
        MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC")) {
      while (c != null && c.moveToNext()) {
        JSObject o = new JSObject();
        String artist = c.getString(1);
        o.put("title", c.getString(0));
        o.put("artist", "<unknown>".equals(artist) ? "" : artist);
        o.put("duration", c.getLong(2));
        o.put("path", c.getString(3));
        arr.put(o);
      }
    } catch (Exception e) { call.reject(String.valueOf(e.getMessage())); return; }
    JSObject r = new JSObject();
    r.put("tracks", arr);
    call.resolve(r);
  }
}
