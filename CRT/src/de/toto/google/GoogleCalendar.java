package de.toto.google;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.util.List;

public class GoogleCalendar {
   
    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar
        getCalendarService() throws IOException {
        Credential credential = GoogleCredentials.authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
        		GoogleCredentials.HTTP_TRANSPORT, GoogleCredentials.JSON_FACTORY, credential)
                .setApplicationName("CRT")
                .build();
    }

    public static void main(String[] args) throws IOException {
    	readCalendar();
    }
    
    public static void readCalendar() throws IOException {
    	// Build a new authorized API client service.
        // Note: Do not confuse this class with the
        //   com.google.api.services.calendar.model.Calendar class.
        com.google.api.services.calendar.Calendar service =
            getCalendarService();

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("h665u166dnu0o8clood9i0oik0@group.calendar.google.com")
            .setMaxResults(10)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();
        List<Event> items = events.getItems();
        if (items.size() == 0) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }
    }

}