package edu.projectuz.mCal.importers.xml;

import edu.projectuz.mCal.helpers.DateHelper;
import edu.projectuz.mCal.core.models.CalendarEvent;
import edu.projectuz.mCal.importers.base.BaseEventImporter;
import edu.projectuz.mCal.importers.base.ImporterSourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.io.StringReader;


public class XMLImporter extends BaseEventImporter {

    private final Logger logger = LogManager.getLogger(XMLImporter.class);

    public XMLImporter(final String sourcePath,
                       final ImporterSourceType sourceType) {
        super(sourcePath, sourceType);
    }

    public final ArrayList<CalendarEvent> convertToObject() {
        String dateFormat = "yyyy/MM/dd HH:mm";
        ArrayList<CalendarEvent> listOfEvents = new ArrayList<>();

        try {

            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            Document document =
                    documentBuilder.parse(new InputSource(
                            new StringReader(getSourceContent())));
            NodeList nodeList = document.getElementsByTagName("vevent");

            for (int i = 0; i < nodeList.getLength(); i++) {

                helperForParser(nodeList, listOfEvents, dateFormat, i);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
        return listOfEvents;
    }

    private void helperForParser(
            final NodeList nodeList,
            final ArrayList<CalendarEvent> listOfEvents,
            final String dateFormat, final int i) {

        CalendarEvent eventObject = new CalendarEvent();
        Node node = nodeList.item(i);

        if (node.getNodeType() == Node.ELEMENT_NODE) {

            Element element = (Element) node;

            eventObject.setTitle(element.getElementsByTagName(
                    "summary").item(0).getTextContent().trim());
            eventObject.setStartDate(DateHelper.stringToDate(element.
                    getElementsByTagName("dtstart").item(0).getTextContent().
                    trim(),
                    dateFormat, DateHelper.stringToTimeZone(
                            element.getElementsByTagName("tzid").item(0).
                                    getTextContent().trim())));
            eventObject.setEndDate(DateHelper.stringToDate(element.
                    getElementsByTagName("dtend").item(0).getTextContent().
                    trim(),
                    dateFormat, DateHelper.stringToTimeZone(element.
                            getElementsByTagName("tzid").item(0).
                            getTextContent().trim())));
            eventObject.setDescription(element.
                    getElementsByTagName("description").
                    item(0).getTextContent().trim());
            eventObject.setTag(element.getElementsByTagName("tag").
                    item(0).getTextContent().trim());
            eventObject.setTimeZone(DateHelper.stringToTimeZone(element.
                    getElementsByTagName("tzid").item(0).
                    getTextContent().trim()));
            listOfEvents.add(eventObject);
        }
    }

    @Override
    public final String getName() {
        return "XML Importer";
    }

    @Override
    public void importData() {

    }
}

