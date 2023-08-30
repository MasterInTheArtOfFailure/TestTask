package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;


public class TicketsFunctions {
    /**
     *
     * @param originRegion - region that corresponds to city
     * @param destinationRegion - region that corresponds to city
     * @return difference between timezones, while taking into account daylight saving time (1 or 0 hours added in milliseconds)
     * (if time is universal, then this function will be skipped, if flagUTC is set to true)
     */
    public static Duration getTimeZoneDifference(String originRegion, String destinationRegion) {
        TimeZone originTZ = TimeZone.getTimeZone(originRegion);
        TimeZone destinationTZ = TimeZone.getTimeZone(destinationRegion);
        return Duration.ofMillis(originTZ.getRawOffset() - destinationTZ.getRawOffset() + originTZ.getDSTSavings() - destinationTZ.getDSTSavings());
    }

    public static LocalDateTime LdtFormatter(String date, String time) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");//use H from SimpleDateFormatter
        return LocalDateTime.parse(date + " " + time, format);
    }

    public static TicketsJson readFromJsonFile(String path) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(path);
        try {
            return objectMapper.readValue(file, TicketsJson.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static HashMap<String, Duration> findMinTimeBetweenCitiesForEachCarrier(List<TicketsJson.Ticket> tickets, boolean flagUTC, String originRegion, String destinationRegion) {
        HashMap<String, Duration> minFlightTimes = new HashMap<>();

        tickets.forEach(ticket -> {
            String carrierJson = ticket.getCarrier();
            LocalDateTime departureLdt = LdtFormatter(ticket.getDeparture_date(), ticket.getDeparture_time());
            LocalDateTime arrivalLdt = LdtFormatter(ticket.getArrival_date(), ticket.getArrival_time());
            Duration flightTime = Duration.between(departureLdt, arrivalLdt);
            if (!minFlightTimes.containsKey(carrierJson) || flightTime.compareTo(minFlightTimes.get(carrierJson)) < 0) {
                minFlightTimes.put(carrierJson, flightTime);
            }
        });
        if (!flagUTC) {
            minFlightTimes.entrySet().forEach(entry -> entry.setValue(entry.getValue().plus(getTimeZoneDifference(originRegion, destinationRegion))));
        }
        return minFlightTimes;
    }

    public static List<TicketsJson.Ticket> filterByOriginAndDestination(List<TicketsJson.Ticket> tickets, String origin, String destination) {
        return tickets.stream()
                .filter(ticket -> ticket.getOrigin().equals(origin) && ticket.getDestination().equals(destination))
                .collect(Collectors.toList());
    }
    public static double getAveragePrice(List<TicketsJson.Ticket> ticketList) {
        return ticketList.stream().collect(Collectors.averagingDouble(TicketsJson.Ticket::getPrice));
    }

    public static double getMedianPrice(List<TicketsJson.Ticket> ticketList) {
        DoubleStream ds = ticketList.stream().mapToDouble(TicketsJson.Ticket::getPrice).sorted();
        return ticketList.size() % 2 == 0 ?
                ds.skip(ticketList.size() / 2 - 1).limit(2).average().orElseThrow(IllegalStateException::new) ://throws exception if OptionalDouble returns null
                ds.skip(ticketList.size() / 2 - 1).findFirst().orElseThrow(IllegalStateException::new);
    }

    public static String beautifyTimeOutput(Duration dt) {
        return String.format("%s:%s", dt.toHours(), beautifyMinutes(dt));
    }

    public static String beautifyMinutes(Duration dt) {
        return dt.toMinutesPart() < 10 ?
                ("0" + dt.toMinutesPart()) :
                String.valueOf(dt.toMinutesPart());
    }
}
