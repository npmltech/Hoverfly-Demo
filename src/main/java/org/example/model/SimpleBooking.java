package org.example.intercept;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDate;

public final class SimpleBooking {
    private final int id;
    private final String origin;
    private final String destination;
    private final LocalDate date;

    @JsonCreator
    public SimpleBooking(
        @JsonProperty("id") int id,
        @JsonProperty("origin") String origin,
        @JsonProperty("destination") String destination,
        @JsonProperty("date") LocalDate date
    ) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.date = date;
    }

    public int getId() {
        return this.id;
    }

    public String getOrigin() {
        return this.origin;
    }

    public String getDestination() {
        return this.destination;
    }

    public LocalDate getDate() {
        return this.date;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
