package net.securustech.embs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class EmailRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String inmateId;
    private String inmateName;
    private String apptId;
    private String siteName;
    private String contactId;
    private String apptDate;
    private String apptTime;
    private String duration;
    private String status;
    private String visitorName;
    private String visitorEmailId;
    private String visitorType;
    private String visitType;
    private String visitorLocationName;
    private String visitorTerminalName;
    private String apptPin;
    private String cancelReason;
    private String comments;
    private String senderId;
    private boolean refundFlg;

    private String newApptDate;
    private String newApptTime;
}
