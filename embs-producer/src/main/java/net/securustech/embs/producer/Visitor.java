package net.securustech.embs.producer;

import javax.validation.constraints.NotNull;

/**
 * Created by Himanshu on 2/23/17.
 */
public class Visitor {

    private static final long serialVersionUID = 273190272199676491L;

    @NotNull
    private String visitorId;

    @NotNull
    private String crmId;

    @NotNull
    private String eventType;

    public String getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    public String getCrmId() {
        return crmId;
    }

    public void setCrmId(String crmId) {
        this.crmId = crmId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Visitor{");
        sb.append("visitorId='").append(visitorId).append('\'');
        sb.append(", crmId='").append(crmId).append('\'');
        sb.append(", eventType='").append(eventType).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Visitor)) return false;

        Visitor visitor = (Visitor) o;

        if (visitorId != null ? !visitorId.equals(visitor.visitorId) : visitor.visitorId != null) return false;
        if (crmId != null ? !crmId.equals(visitor.crmId) : visitor.crmId != null) return false;
        return !(eventType != null ? !eventType.equals(visitor.eventType) : visitor.eventType != null);

    }

    @Override
    public int hashCode() {
        int result = visitorId != null ? visitorId.hashCode() : 0;
        result = 31 * result + (crmId != null ? crmId.hashCode() : 0);
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        return result;
    }
}
