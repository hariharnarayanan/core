package net.securustech.embs.producer;

import net.securustech.embs.model.EmailRequest;

public class KafkaEvent {

    private Long id;
    private String event;
    private EmailRequest emailRequest;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public EmailRequest getEmailRequest() {
        return emailRequest;
    }

    public void setEmailRequest(EmailRequest emailRequest) {
        this.emailRequest = emailRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KafkaEvent)) return false;

        KafkaEvent that = (KafkaEvent) o;

        if (!getId().equals(that.getId())) return false;
        if (!getEvent().equals(that.getEvent())) return false;
        return getEmailRequest().equals(that.getEmailRequest());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getEvent().hashCode();
        result = 31 * result + getEmailRequest().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "KafkaEvent{" +
                "id=" + id +
                ", event='" + event + '\'' +
                ", emailRequest=" + emailRequest +
                '}';
    }
}
