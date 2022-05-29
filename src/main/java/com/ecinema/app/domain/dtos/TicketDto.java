package com.ecinema.app.domain.dtos;

import com.ecinema.app.domain.contracts.AbstractDto;
import com.ecinema.app.domain.enums.Letter;
import com.ecinema.app.domain.enums.TicketStatus;
import com.ecinema.app.domain.enums.TicketType;
import com.ecinema.app.domain.objects.SeatDesignation;
import com.ecinema.app.util.UtilMethods;
import lombok.*;

import java.time.LocalDateTime;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TicketDto extends AbstractDto {

    private Long userId;
    private String email;
    private String username;
    private String movieTitle;
    private Long paymentCardId;
    private Boolean isRefundable;
    private TicketType ticketType;
    private Letter showroomLetter;
    private TicketStatus ticketStatus;
    private LocalDateTime endDateTime;
    private LocalDateTime showDateTime;
    private LocalDateTime creationDateTime;
    private SeatDesignation seatDesignation;

    public String showDateTimeFormatted() {
        return showDateTime != null ? UtilMethods.localDateTimeFormatted(showDateTime) : null;
    }

    public String endDateTimeFormatted() {
        return endDateTime != null ? UtilMethods.localDateTimeFormatted(endDateTime) : null;
    }

    public String creationDateTimeFormatted() {
        return creationDateTime != null ? UtilMethods.localDateTimeFormatted(creationDateTime) : null;
    }

    public String ticketTypeFormatted() {
        return ticketType != null ? ticketType + " $" + ticketType.getPrice() : null;
    }

}
