package net.securustech.ews.util.date;

import lombok.Getter;
import lombok.Setter;

import java.util.TimeZone;

@Getter
@Setter
public class EWSDateFormat {

    private String dateFormat;
    private TimeZone timeZone;

    public EWSDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public EWSDateFormat(String dateFormat, TimeZone timeZone) {
        this.dateFormat = dateFormat;
        this.timeZone = timeZone;
    }

    public static EWSDateFormat withoutTimeZone(String dateFormat) {

        return new EWSDateFormat(dateFormat);
    }

    public static EWSDateFormat withTimeZone(String dateFormat, TimeZone timeZone) {

        return new EWSDateFormat(dateFormat, timeZone);
    }
}
