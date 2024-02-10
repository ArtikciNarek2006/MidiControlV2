package com.midi_control.util;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

public class MidiUtil {
    public static boolean midiSupported(@NonNull Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI);
    }
}
