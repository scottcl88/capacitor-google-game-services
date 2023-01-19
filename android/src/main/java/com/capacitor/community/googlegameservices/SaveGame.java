/* Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Retrieved from https://github.com/playgameservices/android-basic-samples
 */
package com.capacitor.community.googlegameservices;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SaveGame {

    private static final String TAG = "GoogleGameServices";

    // serialization format version
    private static final String SERIAL_VERSION = "1.1";

    Map<String, String> mGameObjects = new HashMap<String, String>();

    public SaveGame() {
    }

    /**
     * Constructs a SaveGame object from serialized data.
     */
    public SaveGame(byte[] data) {
        if (data == null) return; // default progress
        loadFromJson(new String(data));
    }

    /**
     * Constructs a SaveGame object from a JSON string.
     */
    public SaveGame(String json) {
        if (json == null) return; // default progress
        loadFromJson(json);
    }

    /**
     * Replaces this SaveGame's content with the content loaded from the given JSON string.
     */
    public void loadFromJson(String json) {
        zero();
        if (json == null || json.trim().equals("")) return;

        try {
            JSONObject obj = new JSONObject(json);
            String format = obj.getString("version");
            if (!format.equals(SERIAL_VERSION)) {
                throw new RuntimeException("Unexpected version format " + format);
            }
            JSONObject data = obj.getJSONObject("data");
            Iterator<?> iter = data.keys();

            while (iter.hasNext()) {
                String title = (String) iter.next();
                mGameObjects.put(title, data.getString(title));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Save data has a syntax error: " + json, ex);
            mGameObjects.clear();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Save data has an invalid number in it: " + json, ex);
        }
    }

    /**
     * Serializes this SaveGame to an array of bytes.
     */
    public byte[] toBytes() {
        return toString().getBytes();
    }

    /**
     * Serializes this SaveGame to a JSON string.
     */
    @NonNull
    @Override
    public String toString() {
        try {
            JSONObject data = new JSONObject();
            for (String key : mGameObjects.keySet()) {
                data.put(key, mGameObjects.get(key));
            }
            JSONObject obj = new JSONObject();
            obj.put("version", SERIAL_VERSION);
            obj.put("data", data);
            return obj.toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error converting save data to JSON.", ex);
        }
    }

    /**
     * Resets this SaveGame object to be empty.
     */
    public void zero() {
        mGameObjects.clear();
    }

    /**
     * Returns whether or not this SaveGame is empty.
     */
    public boolean isZero() {
        return mGameObjects.keySet().size() == 0;
    }

    /**
     * Adds the key/value pair to the list. Removes from list first if exists.
     *
     * @param key   key for the object
     * @param value the value (recommended as a JSON string object)
     */
    public void setGameObject(String key, String value) {
        mGameObjects.remove(key);
        mGameObjects.put(key, value);
    }
}
