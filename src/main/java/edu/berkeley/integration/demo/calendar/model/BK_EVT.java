package edu.berkeley.integration.demo.calendar.model;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class BK_EVT {

	private String sectionId;
	private String building;
	private String endTime;
	private String startTime;
	private String instructorNames;
	private String meetingDay;
	private String room;

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getInstructorNames() {
		return instructorNames;
	}

	public void setInstructorNames(String instructorNames) {
		this.instructorNames = instructorNames;
	}

	public String getMeetingDay() {
		return meetingDay;
	}

	public void setMeetingDay(String meetingDay) {
		this.meetingDay = meetingDay;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public Event toEvent() {
		Event evt = new Event();

		/**
		 * Calendar
		 */
		Calendar c = Calendar.getInstance();
		c.set(Calendar.AM_PM, Calendar.AM);

		evt.setSummary(this.sectionId);
		evt.setLocation(String.format("%s %s", this.building, this.room));
		evt.setDescription(this.sectionId);

		if (this.endTime.length() >= 7 && this.endTime.substring(5, 7).equals("PM")) {
			c.set(Calendar.AM_PM, Calendar.PM);
		}

		String BYDAY = new String();
		for (int i = 0; i < this.meetingDay.length(); i++) {
			char mt = this.meetingDay.charAt(i);
			if (mt != ' ') {
				int day = (i+1);
				if (BYDAY.length() == 0) {
					c.set(Calendar.DAY_OF_WEEK, day);
				} else {
					BYDAY += ",";
				}
				switch (day) {
					case Calendar.SUNDAY:
						BYDAY += "SU";
						break;
					case Calendar.MONDAY:
						BYDAY += "MO";
						break;
					case Calendar.TUESDAY:
						BYDAY += "TU";
						break;
					case Calendar.WEDNESDAY:
						BYDAY += "WE";
						break;
					case Calendar.THURSDAY:
						BYDAY += "TH";
						break;
					case Calendar.FRIDAY:
						BYDAY += "FR";
						break;
					case Calendar.SATURDAY:
						BYDAY += "SA";
						break;
				}
			}
		}

		c.set(Calendar.HOUR, new Integer(this.endTime.substring(0, 2)));
		c.set(Calendar.MINUTE, new Integer(this.endTime.substring(2, 4)));
		c.set(Calendar.SECOND, 0);
		evt.setEnd(new EventDateTime().setDateTime(new DateTime(c.getTime(), TimeZone.getTimeZone("PST"))).setTimeZone("America/Los_Angeles"));

		c.set(Calendar.HOUR, new Integer(this.startTime.substring(0, 2)));
		c.set(Calendar.MINUTE, new Integer(this.startTime.substring(2, 4)));
		c.set(Calendar.SECOND, 0);
		evt.setStart(new EventDateTime().setDateTime(new DateTime(c.getTime(), TimeZone.getTimeZone("PST"))).setTimeZone("America/Los_Angeles"));

		evt.setRecurrence(Arrays.asList(String.format("RRULE:FREQ=WEEKLY;BYDAY=%s;UNTIL=20130511T070000Z", BYDAY)));
		evt.setAnyoneCanAddSelf(true);
		return evt;
	}

	@Override
	public String toString() {
		return String.format("%s | %s %s | %s %s-%s", this.sectionId, this.building, this.room, this.meetingDay, this.startTime, this.endTime);
	}

}