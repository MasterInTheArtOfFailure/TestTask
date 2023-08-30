package org.example;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TicketsJson ticketsJson = TicketsFunctions.readFromJsonFile("tickets.json");
        List<TicketsJson.Ticket> tickets = ticketsJson.getTickets();

        List<TicketsJson.Ticket> filteredTickets = TicketsFunctions.filterByOriginAndDestination(tickets, "VVO", "TLV");

        double averagePrice = TicketsFunctions.getAveragePrice(filteredTickets);
        double medianPrice = TicketsFunctions.getMedianPrice(filteredTickets);
        double diff = Math.abs(averagePrice - medianPrice);

        HashMap<String, Duration> minFlightTimes = TicketsFunctions.findMinTimeBetweenCitiesForEachCarrier(filteredTickets, false,"Asia/Vladivostok","Asia/Jerusalem");

        System.out.println("1 task: ");
        minFlightTimes.forEach((k, v) -> System.out.printf("%s: %s \n", k, TicketsFunctions.beautifyTimeOutput(v)));

        System.out.println("2 task");
        System.out.println("Median price: " + medianPrice);
        System.out.println("Average price: " + averagePrice);
        System.out.println("Difference: " + diff);
    }
}
