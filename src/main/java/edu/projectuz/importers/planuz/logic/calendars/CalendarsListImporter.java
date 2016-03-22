package edu.projectuz.importers.planuz.logic.calendars;

import edu.projectuz.importers.planuz.logic.HtmlComponentName;
import edu.projectuz.importers.planuz.model.calendars.Calendar;
import edu.projectuz.importers.planuz.model.calendars.CalendarsList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This is a main class for importing planUz calendars.
 * It imports all information about every planUz calendar.
 * <br>
 * Tip: Starting from this main calendars importer and
 * knowing the structure every single element of calendar
 * can be accessed.
 * <br>
 * For example you can use it to import specific type of
 * days within any calendar using:
 * <br>
 * new CalendarsListImporter().importCalendars().getCalendarByName("D").
 * getDaysListByType("D - Studia stacjonarne");
 * <br>
 * or even get information about one day like this:
 * <br>
 * new CalendarsListImporter().importCalendars().getCalendarByName("D").
 * getDaysListByType("D - Studia stacjonarne").getDayByDate("24-02-2016")
 * <br>
 * At the first level of this hierarchy there's
 * {@link CalendarsList}
 * class which object is returned by {@link #importCalendars()}.
 */
public class CalendarsListImporter {

    private String calendarsUrl = "http://plan.uz.zgora.pl/kalendarze_lista.php";
    private Document htmlContent;

    /**
     * This is a main function of this class.
     * It is used to import every single planUz calendar.
     *
     * @return Returns instance of {@link CalendarsList} class.
     */
    public CalendarsList importCalendars() {
        importHtmlContent();
        String description = getCalendarsDescription();
        CalendarsList calendarsList = new CalendarsList(description);
        Elements calendarsTableRows = getCalendarsTableRows();
        calendarsList.setListOfCalendars(convertRowsIntoCalendarsList(calendarsTableRows));
        return calendarsList;
    }

    private void importHtmlContent() {
        try {
            htmlContent = Jsoup.connect(calendarsUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCalendarsDescription() {
        return htmlContent.select(HtmlComponentName.HEADER).
                get(Index.TITLE_HEADER).text();
    }

    private Elements getCalendarsTableRows() {
        Element table = htmlContent.select(HtmlComponentName.TABLE).
                get(Index.CALENDARS_TABLE);
        return table.select(HtmlComponentName.ROW);
    }

    private ArrayList<Calendar> convertRowsIntoCalendarsList(Elements calendarsTableRows) {
        ArrayList<Calendar> calendarsList = new ArrayList<>();
        for(int rowIndex = Index.FIRST_ROW_WITH_CALENDARS; rowIndex < calendarsTableRows.size(); rowIndex++) {
            Element row = calendarsTableRows.get(rowIndex);
            Calendar calendar = convertRowIntoCalendar(row);
            calendarsList.add(calendar);
        }
        return calendarsList;
    }

    private Calendar convertRowIntoCalendar(Element row) {
        Elements columns = row.select(HtmlComponentName.COLUMN);
        String name = columns.get(Index.NAME_COLUMN).text();
        String description = columns.get(Index.DESCRIPTION_COLUMN).text();
        String calendarUrl = columns.get(Index.NAME_COLUMN).
                select(HtmlComponentName.ADDRESS).attr(HtmlComponentName.URL);
        return new SingleCalendarImporter(name, description, calendarUrl).importCalendar();
    }
}
