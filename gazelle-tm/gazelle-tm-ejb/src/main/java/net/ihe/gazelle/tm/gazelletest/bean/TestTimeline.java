package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceEvent;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestTimeline {
    private static final Logger LOG = LoggerFactory.getLogger(TestTimeline.class);
    private static final String LINE_FEED = "\r\n";
    private static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final SimpleDateFormat JAVAFORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss zz yyyy", Locale.ENGLISH);

    private List<TimelineBand> bands;

    public TestTimeline() {
        super();
        this.bands = new ArrayList<TestTimeline.TimelineBand>();
    }

    private static String getDateAsISO8601String(Date date) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getDateAsISO8601String");
        }
        String result = ISO8601FORMAT.format(date);
        // convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
        // - note the added colon for the Timezone
        result = result.substring(0, result.length() - 2) + ":" + result.substring(result.length() - 2);
        return result;
    }

    private static StringBuilder appendAttribute(StringBuilder sb, String key, String value) {
        return sb.append(key).append("=\"").append(value).append("\" ");
    }

    public static void main(String[] args) {
        try {
            Date d = new Date();
            System.out.println(d.toString());
            System.out.println(JAVAFORMAT.format(d));
            Date d2 = JAVAFORMAT.parse(d.toString());
            System.out.println(d2);
        } catch (ParseException e) {
            LOG.error("" + e.getMessage());
        }
    }

    public void addTestInstance(TestInstance ti, List<Status> statuses) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestInstance");
        }
        TimelineBand band = new TimelineBand(ti);

        List<Event> events;
        if (ti.isOldComments()) {
            events = parseEvents(ti.getComments(), statuses);
        } else {
            events = parseEventsUsing(ti, statuses);
        }
        for (Event testInstanceEvent : events) {
            band.addEvent(testInstanceEvent);
        }
        if (band.computeInterval()) {
            bands.add(band);
        }
    }

    private List<Event> parseEventsUsing(TestInstance ti, List<Status> statuses) {
        List<TestInstanceEvent> testInstanceEvents = ti.getListTestInstanceEvent();
        List<Event> result = new ArrayList<TestTimeline.Event>();
        for (TestInstanceEvent testInstanceEvent : testInstanceEvents) {
            if (testInstanceEvent.getType() == TestInstanceEvent.STATUS_CHANGED) {
                Event event = new Event();
                event.date = testInstanceEvent.getDateOfEvent();
                event.monitor = testInstanceEvent.getUsername();
                event.status = testInstanceEvent.getStatus();
                result.add(event);
            }
        }
        return result;
    }

    private List<Event> parseEvents(String comments, List<Status> statuses) {
        List<Event> events = new ArrayList<Event>();

        int index = comments.indexOf("---Status changed to---");
        while (index != -1) {
            String infos = comments.substring(0, index);
            String statusStr = comments.substring(index);

            infos = infos.substring(infos.lastIndexOf("<b>") + 3);
            infos = infos.substring(0, infos.lastIndexOf("</b>"));

            String[] split = infos.split(" : ");
            String dateStr = split[0];
            String monitor = split[1];

            statusStr = statusStr.substring(statusStr.indexOf(" : ") + 3);
            statusStr = statusStr.substring(0, statusStr.indexOf("</") - 1);

            Date date;
            try {
                date = JAVAFORMAT.parse(dateStr);
            } catch (ParseException e) {
                date = null;
                // FIXME
            }

            if (date != null) {
                Status status = getStatus(statuses, statusStr);
                if ((statusStr != null) && (status != null)) {
                    Event event = new Event();
                    event.date = date;
                    event.monitor = monitor;
                    event.status = status;
                    events.add(event);
                } else {
                    // FIXME
                }
            }

            comments = comments.substring(index + 5);
            index = comments.indexOf("---Status changed to---");
        }

        return events;
    }

    private Status getStatus(List<Status> statuses, String statusStr) {
        for (Status status : statuses) {
            if (status.getKeyword().equals(statusStr)) {
                return status;
            }
        }
        return null;
    }

    public String getTimeLineXML() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTimeLineXML");
        }
        for (TimelineBand band : bands) {
            // For label
            Calendar pessimistEnd = Calendar.getInstance();
            pessimistEnd.setTime(band.startDate);
            pessimistEnd.add(Calendar.MINUTE, 60);

            Calendar bestEnd = Calendar.getInstance();
            bestEnd.setTime(band.endDate);
            bestEnd.add(Calendar.MINUTE, 10);

            band.uiEnd = new Date(Math.max(bestEnd.getTimeInMillis(), pessimistEnd.getTimeInMillis()));
        }

        Collections.sort(bands, new Comparator<TimelineBand>() {
            @Override
            public int compare(TimelineBand paramT1, TimelineBand paramT2) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("compare");
                }
                return paramT1.uiEnd.compareTo(paramT2.uiEnd);
            }
        });

        List<List<TimelineBand>> orderedBands = new ArrayList<List<TimelineBand>>();
        for (TimelineBand band : bands) {
            int trackNum = -1;
            for (int i = 0; (i < orderedBands.size()) && (trackNum == -1); i++) {
                List<TimelineBand> trackBands = orderedBands.get(i);
                TimelineBand lastBand = trackBands.get(trackBands.size() - 1);
                if (lastBand.uiEnd.compareTo(band.startDate) < 0) {
                    trackNum = i;
                }

            }
            if (trackNum == -1) {
                trackNum = orderedBands.size();
                orderedBands.add(new ArrayList<TestTimeline.TimelineBand>());
            }
            orderedBands.get(trackNum).add(band);
            band.trackNum = trackNum;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<data ");
        appendAttribute(sb, "date-time-format", "iso8601");
        sb.append(">").append(LINE_FEED);
        for (TimelineBand band : bands) {
            band.getTimelineXML(sb);
        }
        sb.append("</data>").append(LINE_FEED);
        return sb.toString();
    }

    public String getStart() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStart");
        }
        Collections.sort(bands, new Comparator<TimelineBand>() {
            @Override
            public int compare(TimelineBand paramT1, TimelineBand paramT2) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("compare");
                }
                return paramT1.startDate.compareTo(paramT2.startDate);
            }
        });
        Date date;
        if (bands.size() > 0) {
            date = bands.get(0).startDate;
        } else {
            date = new Date();
        }
        return getDateAsISO8601String(date);
    }

    public String getEnd() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEnd");
        }
        Collections.sort(bands, new Comparator<TimelineBand>() {
            @Override
            public int compare(TimelineBand paramT1, TimelineBand paramT2) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("compare");
                }
                return paramT2.endDate.compareTo(paramT1.endDate);
            }
        });
        Date date;
        if (bands.size() > 0) {
            date = bands.get(0).endDate;
        } else {
            date = new Date();
        }
        return getDateAsISO8601String(date);
    }

    private class TimelineEvent {
        Date date;
        Status status;
        String label;
    }

    private class Event {
        Date date;
        String monitor;
        Status status;
    }

    private class TimelineBand {
        public Date uiEnd;
        int trackNum = 0;
        List<TimelineEvent> events = new ArrayList<TimelineEvent>();
        Date startDate;
        Date endDate;
        TestInstance testInstance;

        TimelineBand(TestInstance testInstance) {
            super();
            this.testInstance = testInstance;
        }

        void addEvent(Event testInstanceEvent) {
            TimelineEvent event = new TimelineEvent();
            event.date = testInstanceEvent.date;
            event.status = testInstanceEvent.status;
            event.label = testInstanceEvent.status.getKeyword() + " - " + testInstanceEvent.monitor;
            events.add(event);
        }

        boolean computeInterval() {
            long startTime = Long.MAX_VALUE;
            long endTime = Long.MIN_VALUE;
            for (TimelineEvent event : events) {
                long eventTime = event.date.getTime();
                startTime = Math.min(startTime, eventTime);
                endTime = Math.max(endTime, eventTime);
            }
            if ((startTime != Long.MAX_VALUE) && (endTime != Long.MIN_VALUE)) {
                startDate = new Date(startTime);
                endDate = new Date(endTime);
                return true;
            } else {
                return false;
            }
        }

        StringBuilder getTimelineXML(StringBuilder sb) {
            sb.append("<event durationEvent=\"true\" ");
            appendAttribute(sb, "start", getDateAsISO8601String(startDate));
            appendAttribute(sb, "end", getDateAsISO8601String(endDate));
            String title = StringEscapeUtils.escapeXml(Integer.toString(testInstance.getId())) + " - "
                    + testInstance.getLastStatus().getKeyword();
            appendAttribute(sb, "title", title);
            appendAttribute(sb, "link", "http://gazelle.ihe.net/EU-CAT/testing/test/test/TestInstance.seam?id="
                    + Integer.toString(testInstance.getId()));
            appendAttribute(sb, "textColor", "#000000");
            appendAttribute(sb, "color", Status.getGraphColor(testInstance.getLastStatus()));
            appendAttribute(sb, "trackNum", Integer.toString(trackNum));
            sb.append(" />").append(LINE_FEED);
            for (TimelineEvent event : events) {
                sb.append("<event durationEvent=\"false\" ");
                appendAttribute(sb, "start", getDateAsISO8601String(event.date));
                String icon = getIconUrl(event.status);
                if (icon != null) {
                    appendAttribute(sb, "icon", icon);
                }
                appendAttribute(sb, "trackNum", Integer.toString(trackNum));
                appendAttribute(sb, "caption", StringEscapeUtils.escapeXml(event.label));
                appendAttribute(sb, "description", StringEscapeUtils.escapeXml(event.label));
                sb.append(">").append(StringEscapeUtils.escapeXml(event.label)).append("</event>").append(LINE_FEED);
            }
            return sb;
        }

        private String getIconUrl(Status status) {

            if (status.equals(Status.STARTED)) {
                return "started.png";
            }
            if (status.equals(Status.COMPLETED)) {
                return "completed.png";
            }
            if (status.equals(Status.PAUSED)) {
                return "paused.png";
            }
            if (status.equals(Status.VERIFIED)) {
                return "verified.png";
            }
            if (status.equals(Status.ABORTED)) {
                return "aborted.png";
            }
            if (status.equals(Status.PARTIALLY_VERIFIED)) {
                return "partial.png";
            }
            if (status.equals(Status.FAILED)) {
                return "failed.png";
            }
            if (status.equals(Status.CRITICAL)) {
                return "critical.png";
            }
            return null;
        }
    }

}
