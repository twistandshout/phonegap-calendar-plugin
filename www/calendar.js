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

cordova.define('cordova/plugin/calendar', function(require, exports, module) {
  var exec = require('cordova/exec');


  /**
   * @constructor
   */
  var Calendar = function() {
  };


  /**
   * Find calendar event items in the calendar based on a CalendarEventFilter
   * object.
   * @param {Function} success Success callback.
   * @param {Function=} opt_error Error callback.
   * @param {CalendarFindOptions=} opt_options Options to apply to the output
   *     of this method.
   */
  Calendar.prototype.findEvents = function(success, opt_error, opt_options) {

    // If cordova is not ready, wait for it.
    if (!cordova.exec) {
      ws.log('cordova is not yet ready in Calendar findEvents');
      document.addEventListener('deviceready', _.bind(function() {
        this.findEvents(success, opt_error, opt_options);
      }, this), false);
      return;
    }

    var filter = opt_options && opt_options.filter || {};
    // Default limit to 3 days from now.
    var now = new Date();
    var later = new Date();
    later.setDate(now.getDate() + 3);
    filter.startAfter || (filter.startAfter = now.getTime());
    filter.startBefore || (filter.startBefore = later.getTime());
    cordova.exec(success, opt_error, 'Calendar', null, [filter]);
  };

  var calendar = new Calendar();
  module.exports = calendar;
});
