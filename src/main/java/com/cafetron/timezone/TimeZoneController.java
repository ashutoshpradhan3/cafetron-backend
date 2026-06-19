package com.cafetron.timezone;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping({"/timezones", "/api/timezones"})
public class TimeZoneController {

    private static final DateTimeFormatter OFFSET_FORMATTER = DateTimeFormatter.ofPattern("OOOO");
    private static final List<String> SUPPORTED_ZONES = List.of(
            "Asia/Kolkata",
            "UTC",
            "Asia/Dubai",
            "Asia/Singapore",
            "Europe/London",
            "Europe/Berlin",
            "America/New_York",
            "America/Chicago",
            "America/Denver",
            "America/Los_Angeles",
            "Australia/Sydney"
    );

    @GetMapping
    public List<TimeZoneOption> getTimeZones() {
        return SUPPORTED_ZONES.stream()
                .map(this::toOption)
                .toList();
    }

    private TimeZoneOption toOption(String zoneId) {
        ZoneId zone = ZoneId.of(zoneId);
        String offset = ZonedDateTime.now(zone).format(OFFSET_FORMATTER);
        String label = zoneId.replace('_', ' ');
        return new TimeZoneOption(zoneId, label, offset);
    }
}
