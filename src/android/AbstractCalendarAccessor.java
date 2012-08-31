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

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.cordova.api.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractCalendarAccessor {

  public static final String LOG_TAG = "Calendar";

  protected static class Event {
    String id;
    String description;
    String location;
    String summary;
    String start;
    String end;
     //attribute DOMString status;
    // attribute DOMString transparency;
    // attribute CalendarRepeatRule recurrence;
    // attribute DOMString reminder;

    String eventId;
    boolean recurring = false;
    boolean allDay;
    ArrayList<Attendee> attendees;

    public JSONObject toJSONObject() {
      JSONObject obj = new JSONObject();
      try {
        obj.put("id", this.id);
        obj.putOpt("description", this.description);
        obj.putOpt("location", this.location);
        obj.putOpt("summary", this.summary);
        // Format to GMT string.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (this.start != null) {
          obj.put("start", sdf.format(new Date(Long.parseLong(this.start))));
        }
        if (this.end != null) {
          obj.put("end", sdf.format(new Date(Long.parseLong(this.end))));
        }
        obj.put("Xallday", this.allDay);
        if (this.attendees != null) {
          JSONArray arr = new JSONArray();
          for (Attendee attendee : this.attendees) {
            arr.put(attendee.toJSONObject());
          }
          obj.put("Xattendees", arr);
        }
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return obj;
    }
  }

  protected static class Attendee {
    String id;
    String name;
    String email;
    String status;

    public JSONObject toJSONObject() {
      JSONObject obj = new JSONObject();
      try {
        obj.put("id", this.id);
        obj.putOpt("name", this.name);
        obj.putOpt("email", this.email);
        obj.putOpt("status", this.status);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return obj;
    }
  }

  protected CordovaInterface cordova;

  private EnumMap<KeyIndex, String> calendarKeys;

  public AbstractCalendarAccessor(CordovaInterface cordova) {
    this.cordova = cordova;
    this.calendarKeys = initContentProviderKeys();
  }

  protected enum KeyIndex {
    CALENDARS_ID,
    CALENDARS_VISIBLE,
    EVENTS_ID,
    EVENTS_CALENDAR_ID,
    EVENTS_DESCRIPTION,
    EVENTS_LOCATION,
    EVENTS_SUMMARY,
    EVENTS_START,
    EVENTS_END,
    EVENTS_RRULE,
    EVENTS_ALL_DAY,
    INSTANCES_ID,
    INSTANCES_EVENT_ID,
    INSTANCES_BEGIN,
    INSTANCES_END,
    ATTENDEES_ID,
    ATTENDEES_EVENT_ID,
    ATTENDEES_NAME,
    ATTENDEES_EMAIL,
    ATTENDEES_STATUS
  }

  protected abstract EnumMap<KeyIndex, String> initContentProviderKeys();

  protected String getKey(KeyIndex index) {
    return this.calendarKeys.get(index);
  }

  protected abstract Cursor queryAttendees(String[] projection,
      String selection, String[] selectionArgs, String sortOrder);
  protected abstract Cursor queryCalendars(String[] projection,
      String selection, String[] selectionArgs, String sortOrder);
  protected abstract Cursor queryEvents(String[] projection,
      String selection, String[] selectionArgs, String sortOrder);
  protected abstract Cursor queryEventInstances(long startFrom, long startTo,
      String[] projection, String selection, String[] selectionArgs,
      String sortOrder);

  private Event[] fetchEventInstances(long startFrom, long startTo) {
    String[] projection = {
      this.getKey(KeyIndex.INSTANCES_ID),
      this.getKey(KeyIndex.INSTANCES_EVENT_ID),
      this.getKey(KeyIndex.INSTANCES_BEGIN),
      this.getKey(KeyIndex.INSTANCES_END)
    };
    String sortOrder = this.getKey(KeyIndex.INSTANCES_BEGIN) + " ASC, " +
      this.getKey(KeyIndex.INSTANCES_END) + " ASC";
    // Fetch events from instances table in ascending order by time.
    Cursor cursor = queryEventInstances(startFrom, startTo, projection, null,
        null, sortOrder);
    Event[] instances = null;
    if (cursor.moveToFirst()) {
      int idCol = cursor.getColumnIndex(this.getKey(KeyIndex.INSTANCES_ID));
      int eventIdCol = cursor.getColumnIndex(this.getKey(
            KeyIndex.INSTANCES_EVENT_ID));
      int beginCol = cursor.getColumnIndex(this.getKey(
            KeyIndex.INSTANCES_BEGIN));
      int endCol = cursor.getColumnIndex(this.getKey(KeyIndex.INSTANCES_END));
      int count = cursor.getCount();
      int i = 0;
      instances = new Event[count];
      do {
        // Use the start/end time from the instances table. For recurring
        // events the events table contain the start/end time for the
        // origin event (as you would expect).
        instances[i] = new Event();
        instances[i].id = cursor.getString(idCol);
        instances[i].eventId = cursor.getString(eventIdCol);
        instances[i].start = cursor.getString(beginCol);
        instances[i].end = cursor.getString(endCol);
        i += 1;
      } while (cursor.moveToNext());
    }
    return instances;
  }

  private String[] getActiveCalendarIds() {
    // Get only active calendars.
    Cursor cursor = queryCalendars(new String[]{this.getKey(
          KeyIndex.CALENDARS_ID)},
        this.getKey(KeyIndex.CALENDARS_VISIBLE) + "=1", null, null);
    String[] calendarIds = null;
    if (cursor.moveToFirst()) {
      calendarIds = new String[cursor.getCount()];
      int i = 0;
      do {
        int col = cursor.getColumnIndex(this.getKey(KeyIndex.CALENDARS_ID));
        calendarIds[i] = cursor.getString(col);
        i += 1;
      } while (cursor.moveToNext());
    }
    return calendarIds;
  }

  private Map<String, Event> fetchEventsAsMap(Event[] instances) {
    if (instances == null) {
      return null;
    }
    // Only selecting from active calendars, no active calendars = no events.
    String[] activeCalendarIds = getActiveCalendarIds();
    if (activeCalendarIds.length == 0) {
      return null;
    }
    String[] projection = new String[]{
      this.getKey(KeyIndex.EVENTS_ID),
      this.getKey(KeyIndex.EVENTS_DESCRIPTION),
      this.getKey(KeyIndex.EVENTS_LOCATION),
      this.getKey(KeyIndex.EVENTS_SUMMARY),
      this.getKey(KeyIndex.EVENTS_START),
      this.getKey(KeyIndex.EVENTS_END),
      this.getKey(KeyIndex.EVENTS_RRULE),
      this.getKey(KeyIndex.EVENTS_ALL_DAY)
    };
    // Get all the ids at once from active calendars.
    StringBuffer select = new StringBuffer();
    select.append(this.getKey(KeyIndex.EVENTS_ID) + " IN (");
    select.append(instances[0].eventId);
    for (int i = 1; i < instances.length; i++) {
      select.append(",");
      select.append(instances[i].eventId);
    }
    select.append(") AND " + this.getKey(KeyIndex.EVENTS_CALENDAR_ID) +
        " IN (");
    select.append(activeCalendarIds[0]);
    for (int i = 1; i < activeCalendarIds.length; i++) {
      select.append(",");
      select.append(activeCalendarIds[i]);
    }
    select.append(")");
    Cursor cursor = queryEvents(projection, select.toString(), null, null);
    Map<String, Event> eventsMap = new HashMap<String, Event>();
    if (cursor.moveToFirst()) {
      int[] cols = new int[projection.length];
      for (int i = 0; i < cols.length; i++) {
        cols[i] = cursor.getColumnIndex(projection[i]);
      }
      do {
        Event event = new Event();
        event.id = cursor.getString(cols[0]);
        event.description = cursor.getString(cols[1]);
        event.location = cursor.getString(cols[2]);
        event.summary = cursor.getString(cols[3]);
        event.start = cursor.getString(cols[4]);
        event.end = cursor.getString(cols[5]);
        event.recurring = !TextUtils.isEmpty(cursor.getString(cols[6]));
        event.allDay = cursor.getInt(cols[7]) != 0;
        eventsMap.put(event.id, event);
      } while (cursor.moveToNext());
    }
    return eventsMap;
  }

  private Map<String, ArrayList<Attendee>> fetchAttendeesForEventsAsMap(
      String[] eventIds) {
    // At least one id.
    if (eventIds.length == 0) {
      return null;
    }
    String[] projection = new String[]{
      this.getKey(KeyIndex.ATTENDEES_EVENT_ID),
      this.getKey(KeyIndex.ATTENDEES_ID),
      this.getKey(KeyIndex.ATTENDEES_NAME),
      this.getKey(KeyIndex.ATTENDEES_EMAIL),
      this.getKey(KeyIndex.ATTENDEES_STATUS)
    };
    StringBuffer select = new StringBuffer();
    select.append(this.getKey(KeyIndex.ATTENDEES_EVENT_ID) + " IN (");
    select.append(eventIds[0]);
    for (int i = 1; i < eventIds.length; i++) {
      select.append(",");
      select.append(eventIds[i]);
    }
    select.append(")");
    // Group the events together for easy iteration.
    Cursor cursor = queryAttendees(projection, select.toString(), null,
        this.getKey(KeyIndex.ATTENDEES_EVENT_ID) + " ASC");
    Map<String, ArrayList<Attendee>> attendeeMap =
        new HashMap<String, ArrayList<Attendee>>();
    if (cursor.moveToFirst()) {
      int[] cols = new int[projection.length];
      for (int i = 0; i < cols.length; i++) {
        cols[i] = cursor.getColumnIndex(projection[i]);
      }
      ArrayList<Attendee> array = null;
      String currentEventId = null;
      do {
        String eventId = cursor.getString(cols[0]);
        if (currentEventId == null || !currentEventId.equals(eventId)) {
          currentEventId = eventId;
          array = new ArrayList<Attendee>();
          attendeeMap.put(currentEventId, array);
        }
        Attendee attendee = new Attendee();
        attendee.id = cursor.getString(cols[1]);
        attendee.name = cursor.getString(cols[2]);
        attendee.email = cursor.getString(cols[3]);
        attendee.status = cursor.getString(cols[4]);
        array.add(attendee);
      } while (cursor.moveToNext());
    }
    return attendeeMap;
  }

  public JSONArray findEvents(long startFrom, long startTo) {
    // Fetch events from the instance table.
    Event[] instances = fetchEventInstances(startFrom, startTo);
    // Fetch events from the events table for more event info.
    Map<String, Event> eventMap = fetchEventsAsMap(instances);
    // Fetch event attendees
    Map<String, ArrayList<Attendee>> attendeeMap =
        fetchAttendeesForEventsAsMap(eventMap.keySet().toArray(new String[0]));
    // Merge the event info with the instances and turn it into a JSONArray.
    JSONArray result = new JSONArray();
    for (Event instance : instances) {
      Event event = eventMap.get(instance.eventId);
      if (event != null) {
        instance.description = event.description;
        instance.location = event.location;
        instance.summary = event.summary;
        if (!event.recurring) {
          instance.start = event.start;
          instance.end = event.end;
        }
        instance.allDay = event.allDay;
        instance.attendees = attendeeMap.get(instance.eventId);
        result.put(instance.toJSONObject());
      }
    }
    return result;
  }
}
