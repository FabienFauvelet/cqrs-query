package models.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import facade.AgendaResource;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class EventTeacherCreation extends TopicMessage{


    @Override
    public EventType getMessageType() {
        return EventType.TEACHER_CREATION;
    }

    public EventTeacherCreation(EventType type, Object body)
    {
        setMessageType(type);
        setBody(new ObjectMapper().convertValue(body, EventTeacherCreationBody.class));
    }

    @Override
    public void insertObject(AgendaResource resource)
    {
        EventTeacherCreationBody myBody = (EventTeacherCreationBody) getBody();
        resource.createTeacher(myBody.getId(), myBody.getLastName(), myBody.getFirstName());
    }

    @Getter
    @Setter
    @JsonDeserialize
    static class EventTeacherCreationBody
    {
        private UUID id;
        private String firstName;
        private String lastName;
    }
}
