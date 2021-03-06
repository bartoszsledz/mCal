package edu.projectuz.mCal.importers.planuz;

import edu.projectuz.mCal.core.models.CalendarEvent;
import edu.projectuz.mCal.helpers.DateHelper;
import edu.projectuz.mCal.importers.planuz.model.calendars.Calendar;
import edu.projectuz.mCal.importers.planuz.model.calendars.DaysList;
import edu.projectuz.mCal.importers.planuz.model.timetables.GroupTimetable;
import edu.projectuz.mCal.importers.planuz.model.timetables.TimetableDay;
import edu.projectuz.mCal.importers.planuz.model.timetables.TimetableEvent;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * This class can be used for converting planUz group
 * timetable object ({@link GroupTimetable}) into
 * list of {@link CalendarEvent} objects.
 */
public class PlanUzConverter {

    private List<Calendar> calendarsList;

    /**
     * Class constructor that imports calendars which
     * will be used in converting process.
     */
    public PlanUzConverter(List<Calendar> calendarsList) {
        this.calendarsList = calendarsList;
    }

    /**
     * Main function of this class. Allows you to convert
     * a single planUz timetable into list of events.
     *
     * @param timetable - planUz timetable
     * @return List of events.
     */
    public final ArrayList<CalendarEvent> convertTimetable(
            final GroupTimetable timetable) {
        ArrayList<CalendarEvent> calendarEvents = new ArrayList<>();
        addEventsToList(timetable, calendarEvents);
        return calendarEvents;
    }

    /**
     * This function will be changed to get calendars from
     * database instead of importer itself.
     */

    private void addEventsToList(
            final GroupTimetable timetable,
            final ArrayList<CalendarEvent> calendarEvents) {
        for (TimetableDay timetableDay : timetable.getDaysList()) {
            for (TimetableEvent timetableEvent : timetableDay.getEventsList()) {
                calendarEvents.addAll(
                        convertToCalendarEventsList(timetableEvent));
            }
        }
    }

    private ArrayList<CalendarEvent> convertToCalendarEventsList(
            final TimetableEvent timetableEvent) {
        ArrayList<CalendarEvent> calendarEvents = new ArrayList<>();
        DaysType daysType = getTypeOfDays(timetableEvent.getDays());

        switch (daysType) {
            case DATES:
                addEventsWithDates(timetableEvent, calendarEvents);
                break;
            case CALENDAR:
                addEventsWithCalendar(timetableEvent, calendarEvents);
                break;
            default:
                break;
        }
        return calendarEvents;
    }

    /**
     *
     * Dates are separated with ';' char.
     * When there's a calendar name, not dates,
     * then there's no ';' char in name.
     * @param days days.
     * @return day type.
     */
    private DaysType getTypeOfDays(final String days) {
        final int INDEX_WHEN_CHAR_NOT_FOUND = -1;

        if (days.indexOf(';') == INDEX_WHEN_CHAR_NOT_FOUND) {
            return DaysType.CALENDAR;
        } else {
            return DaysType.DATES;
        }
    }

    private void addEventsWithDates(
            final TimetableEvent timetableEvent,
            final ArrayList<CalendarEvent> calendarEvents) {
        String[] dates = getFormattedDates(timetableEvent);

        for (String date : dates) {
            calendarEvents.add(getCalendarEvent(timetableEvent, date));
        }
    }

    private String[] getFormattedDates(final TimetableEvent timetableEvent) {
        String dates = timetableEvent.getDays().replace(" ", "");
        return dates.split(";");
    }

    private CalendarEvent getCalendarEvent(final TimetableEvent timetableEvent,
                                           final String date) {
        String title = timetableEvent.getEventName();
        DateTime startDate = getDate(date, timetableEvent.getStartTime());
        DateTime endDate = getDate(date, timetableEvent.getEndTime());
        String description = getDescription(timetableEvent);
        return new CalendarEvent(title, startDate, endDate, description,
                "UZ", TimeZone.getTimeZone("Europe/Warsaw"));
    }

    private String getDescription(final TimetableEvent timetableEvent) {
        return String.format(
                "Subgroup: %s, Type: %s, Teacher: %s, Room: %s, TimetableDay: %s",
                timetableEvent.getSubgroup(), timetableEvent.getEventType(),
                timetableEvent.getTeacherName(), timetableEvent.getRoom(),
                timetableEvent.getDayName());
    }

    private DateTime getDate(final String date, final String time) {
        return DateHelper.stringToDate(date + " " + time, "dd-MM-yyyy HH:mm",
                TimeZone.getTimeZone("Europe/Warsaw"));
    }

    private void addEventsWithCalendar(
            final TimetableEvent timetableEvent,
            final ArrayList<CalendarEvent> calendarEvents) {
        try {
            DaysList daysList = getDaysList(timetableEvent.getDays());

            for (edu.projectuz.mCal.importers.planuz.model.calendars.Day
                    day : daysList.getDays()) {
                if (day.getDayAccordingToCalendar().equals(
                        timetableEvent.getDayName())) {
                    calendarEvents.add(getCalendarEvent(
                            timetableEvent, day.getDate()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private DaysList getDaysList(final String daysType) throws Exception {
        for (Calendar calendar : calendarsList) {
            if (calendar.isContainingDayType(daysType)) {
                return calendar.getDaysListByType(daysType);
            }
        }
        throw new IllegalArgumentException(String.format(
                "Days list with type: '%s' not found", daysType));
    }
}
