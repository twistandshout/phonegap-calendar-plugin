/**
 * Copyright (c) 2012, Twist and Shout, Inc. http://www.twist.com/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


/**
 * @author yvonne@twist.com (Yvonne Yip)
 */

package com.twist.android.plugins.calendar;

import android.os.Build;
import android.util.Log;

import com.twist.android.plugins.calendar.AbstractCalendarAccessor;
import com.twist.android.plugins.calendar.CalendarProviderAccessor;
import com.twist.android.plugins.calendar.LegacyCalendarAccessor;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.String.format;

public class Calendar extends Plugin {

  private static final String LOG_TAG = AbstractCalendarAccessor.LOG_TAG;

  private AbstractCalendarAccessor calendarAccessor;

  private AbstractCalendarAccessor getCalendarAccessor() {
    if (this.calendarAccessor == null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        Log.d(LOG_TAG, "Initializing calendar plugin");
        this.calendarAccessor = new CalendarProviderAccessor(this.cordova);
      } else {
        Log.d(LOG_TAG, "Initializing legacy calendar plugin");
        this.calendarAccessor = new LegacyCalendarAccessor(this.cordova);
      }
    }
    return this.calendarAccessor;
  }

  /**
   * @param action The action to execute.
   * @param args JSONArray of arguments for the plugin.
   * @param callbackId The callback id used when calling back into JavaScript.
   * @return A PluginResult object with a status and message.
   */
  @Override
  public PluginResult execute(String action, JSONArray args,
      String callbackId) {
    long startMs = System.currentTimeMillis();
    PluginResult result;
    if (args.length() > 0) {
      long startFrom = 0;
      long startTo = 0;
      try {
        JSONObject jsonFilter = args.getJSONObject(0);
        startFrom = jsonFilter.optLong("startAfter");
        startTo = jsonFilter.optLong("startBefore");
      } catch (JSONException e) {
        return new PluginResult(PluginResult.Status.ERROR);
      }
      JSONArray jsonEvents = getCalendarAccessor().findEvents(
          startFrom, startTo);
      result = new PluginResult(PluginResult.Status.OK, jsonEvents);
    } else {
      result = new PluginResult(PluginResult.Status.ERROR);
    }
    Log.d(LOG_TAG, format("query took %d ms", System.currentTimeMillis() -
        startMs));
    return result;
  }
}
