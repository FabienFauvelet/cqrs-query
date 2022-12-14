package org.acme.in.dto.event;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class UpdateEventCommand extends CreateEventCommand {
    private UUID id;
}
