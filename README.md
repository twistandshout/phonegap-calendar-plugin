# Android Calendar Plugin for PhoneGap #

This plugin is tested on Gingerbread (Samsung Galaxy S2) and Jellybean. Since there is no official Calendar API before Ice Cream Sandwich, your mileage may vary for older Androids.

## Adding the Plugin to your project ##

Either:

1. Copy `calendar.js` to your project's `www` folder and include a reference to it in your html files.
2. Create `src/com/twist/android/plugins/calendar` in your project and move the java files into it.

Or use pluginstall: https://github.com/alunny/pluginstall

## JavaScript API ##

This plugin attempts to confirm to the W3C Calendar API (http://dev.w3.org/2009/dap/calendar/). Currently only `findEvents` is implemented.

Get the plugin object with `cordova.require('cordova/plugin/calendar')`.

### findEvents ###
`findEvents(successCb, errorCb, filter)`

Find calendar events from active calendars.

```js
var later = new Date();
later.setDate(later.getDate() + 3); // 3 days from now
  
cordova.require('cordova/plugin/calendar').findEvents(function(events) {
  // Do something with returned events array.
}, function() {
  // There's an error for some reason.
}, {
  'startAfter': new Date().getTime(),
  'startBefore': later.getTime()
});
```

## JavaScript Object Format ##

### Calendar Event ###

A calendar event may have the following attributes:

* `id` Unique identifier. Required,
* `description` Event description. Optional.
* `location` Event location. Optional.
* `summary` Event summary. Optional.
* `start` Event start time in a UTC time string (http://www.ietf.org/rfc/rfc3339.txt). Optional.
* `end` Event end time in a UTC time string (http://www.ietf.org/rfc/rfc3339.txt). Optional.
* `Xallday` True for all day events. Required.
* `Xattendees` Array of attendees. Optional.

### Attendee ###

An attendee may have the following attributes:

* `id` Unique identifier for the event. Required.
* `name` Attendee name. Optional.
* `email` Attendee email. Optional.
* `status` Attendee acceptance status. 0 = none, 1 = accepted, 2 = declined, 3 = invited, 4 = tentative.

## Licence ##

```
Copyright (c) 2012, Twist and Shout, Inc. http://www.twist.com/
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met: 

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```