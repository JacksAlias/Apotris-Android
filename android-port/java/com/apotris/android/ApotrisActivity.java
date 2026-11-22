package com.apotris.android;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import org.libsdl.app.SDLActivity;

/**
 * Apotris Android uses a single native library: {@code libmain.so} (see {@code meson.build},
 * {@code shared_library('main', ...)}). SDL2 and the game are linked into that .so.
 */
public class ApotrisActivity extends SDLActivity {
    private static final String INPUT_TAG = "ApotrisInput";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApotrisAssetLoader.syncIfNeeded(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String[] getLibraries() {
        return new String[] { "main" };
    }

    /**
     * Fire TV remotes report their D-pad and media buttons as Android key events,
     * not as game-controller axes/buttons. Translate the sideways-held remote into
     * the keyboard controls understood by Apotris before SDL receives the event.
     *
     * Physical D-pad mapping requested for the sideways remote:
     *   Right -> Up, Down -> Right, Left -> Down, Up -> Left.
     * Media buttons:
     *   Rewind -> Z (rotate anticlockwise), Fast-forward -> X (clockwise),
     *   Play/Pause -> C (hold).
     *
     * Back, Home, Menu, Select, and volume keys are deliberately not intercepted.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int mappedKeyCode;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mappedKeyCode = KeyEvent.KEYCODE_DPAD_UP;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mappedKeyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mappedKeyCode = KeyEvent.KEYCODE_DPAD_DOWN;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                mappedKeyCode = KeyEvent.KEYCODE_DPAD_LEFT;
                break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                mappedKeyCode = KeyEvent.KEYCODE_Z;
                break;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                mappedKeyCode = KeyEvent.KEYCODE_X;
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                mappedKeyCode = KeyEvent.KEYCODE_C;
                break;
            default:
                return super.dispatchKeyEvent(event);
        }

        KeyEvent mappedEvent = new KeyEvent(
                event.getDownTime(),
                event.getEventTime(),
                event.getAction(),
                mappedKeyCode,
                event.getRepeatCount(),
                event.getMetaState(),
                event.getDeviceId(),
                event.getScanCode(),
                event.getFlags(),
                event.getSource());

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT
                || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
                || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            Log.i(INPUT_TAG,
                    "key=" + KeyEvent.keyCodeToString(event.getKeyCode())
                    + " mapped=" + KeyEvent.keyCodeToString(mappedKeyCode)
                    + " action=" + (event.getAction() == KeyEvent.ACTION_DOWN ? "DOWN" : "UP")
                    + " repeat=" + event.getRepeatCount()
                    + " device=" + event.getDeviceId()
                    + " scan=" + event.getScanCode()
                    + " downTime=" + event.getDownTime()
                    + " eventTime=" + event.getEventTime());
        }

        return SDLActivity.handleKeyEvent(null, mappedKeyCode, mappedEvent, null);
    }
}
