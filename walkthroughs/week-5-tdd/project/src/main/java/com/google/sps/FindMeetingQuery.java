// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> reqAttendees = request.getAttendees();
    Collection<String> allAttendees = new HashSet<String>(){{
        addAll(request.getAttendees());
        addAll(request.getOptionalAttendees());
    }};
    ArrayList<TimeRange> finalTimes = determineFreeTimes(allAttendees, events, request);
    if (finalTimes.isEmpty()){
        finalTimes = determineFreeTimes(reqAttendees, events, request);
    }
    return finalTimes;
  }

  private ArrayList<TimeRange> determineFreeTimes(Collection<String> attendees, Collection<Event> events, MeetingRequest request){
    ArrayList<TimeRange> timesToWorkAround = new ArrayList<TimeRange>();
    // Only need to work around preexisting events that must be attended by at least one other attendee.
    for (Event event : events){
        if (isRequiredMeeting(event, attendees)){
            timesToWorkAround.add(event.getWhen());
        }
    }
    Collections.sort(timesToWorkAround, TimeRange.ORDER_BY_START);
    // Remove all times that overlap one another, and merge those times into larger, unifying TimeRanges.
    ArrayList<TimeRange> mergedTimes = mergeTimes(timesToWorkAround);
    ArrayList<TimeRange> finalTimes = getFreeTimes(mergedTimes, request.getDuration());
    return finalTimes;
  }

  private boolean isRequiredMeeting(Event event, Collection<String> reqAttendees){
      Set<String> attendees = event.getAttendees();
      Set<String> reqAsSet = new HashSet<String>(reqAttendees);
      // If the intersection of the set of mandatory attendees and the event's attendees is empty, then
      // the meeting is not required. 
      reqAsSet.retainAll(attendees);
      return !reqAsSet.isEmpty();
  }

  private ArrayList<TimeRange> mergeTimes(ArrayList<TimeRange> times){
      ArrayList<TimeRange> mergedTimes = new ArrayList<TimeRange>();
      if (times.isEmpty()){
          return mergedTimes;
      }
      mergedTimes.add(times.get(0));
      // For each time, we can compare it to the time before it due to the ordering. Then, we can 
      // merge the two times into one TimeRange based on the later end time. 
      for (int i = 1; i < times.size(); i++){
          TimeRange currTime = times.get(i);
          int lastIndex = mergedTimes.size() - 1;
          TimeRange lastTime = mergedTimes.get(lastIndex);
          if (lastTime.overlaps(currTime) || lastTime.end() == currTime.start()){
              int laterEnd = Math.max(currTime.end(), lastTime.end());
              TimeRange newRange = TimeRange.fromStartEnd(lastTime.start(), laterEnd, false);
              mergedTimes.set(lastIndex, newRange);
          } else {
              mergedTimes.add(currTime);
          }
      }
      return mergedTimes;
  }

  private ArrayList<TimeRange> getFreeTimes(ArrayList<TimeRange> mergedTimes, long duration){
      ArrayList<TimeRange> finalTimes = new ArrayList<TimeRange>();
      if (mergedTimes.isEmpty() && duration <= TimeRange.WHOLE_DAY.duration()){
          finalTimes.add(TimeRange.WHOLE_DAY);
          return finalTimes;
      } else {
          // Only consider the ranges that are NOT occupied by a TimeRange. If a given range is longer than
          // the duration, add it to our list of valid times. 
          int start = TimeRange.START_OF_DAY;
          for (TimeRange time: mergedTimes){
              int end = time.start();
              TimeRange newRange = TimeRange.fromStartEnd(start, end, false);
              if (duration <= newRange.duration()){
                  finalTimes.add(newRange);
              }
              start = time.end();
          }
          int end = TimeRange.END_OF_DAY;
          TimeRange newRange = TimeRange.fromStartEnd(start, end, true);
          if (duration <= newRange.duration()){
              finalTimes.add(newRange);
          }
      }
      return finalTimes;
  }
}
