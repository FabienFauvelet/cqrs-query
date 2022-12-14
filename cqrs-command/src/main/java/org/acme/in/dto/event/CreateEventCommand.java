package org.acme.in.dto.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateEventCommand {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private UUID teacherId;
    private ArrayList<UUID> reservedResources;
    private int nbMaxParticipant;
    private String type;
}
