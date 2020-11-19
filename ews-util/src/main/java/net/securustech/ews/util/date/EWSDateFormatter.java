package net.securustech.ews.util.date;

import net.securustech.ews.exception.entities.EWSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.TimeZone;

import static net.securustech.ews.exception.entities.EWSErrorCodeDefinitions.EWS_DATE_FORMAT_ERROR;
import static net.securustech.ews.exception.entities.EWSErrorCodeDefinitions.EWS_DATE_PARSE_ERROR;
import static org.springframework.util.StringUtils.isEmpty;

@Component
public class EWSDateFormatter {

    private final Logger LOGGER = LoggerFactory.getLogger(EWSDateFormatter.class);

    public Date formatDate(Date date, EWSDateFormat targetEWSDateFormat) throws EWSException {

        if (!isEmpty(date)) {

            SimpleDateFormat targetDateFormat = applyEWSDateFormat(targetEWSDateFormat);

            try {
                return targetDateFormat.parse(targetDateFormat.format(date));
            } catch (ParseException e) {
                LOGGER.error("EWSDateFormatter@ToEWSDate@DateParseError :::", e);
                throw new EWSException(e, EWS_DATE_PARSE_ERROR.getEwsErrorCode(), EWS_DATE_PARSE_ERROR.getEwsErrorMessage() +
                        " ::: date :-> " + date + " required dateFormat :-> " + targetDateFormat);
            }
        }
        return null;
    }

    public String formatDateAsString(Date date, EWSDateFormat targetEWSDateFormat) throws EWSException {

        if (!isEmpty(date)) {

            SimpleDateFormat targetDateFormat = applyEWSDateFormat(targetEWSDateFormat);

            return targetDateFormat.format(formatDate(date, targetEWSDateFormat));
        }
        return null;
    }

    public Date formatDate(String date, EWSDateFormat sourceEWSDateFormat, EWSDateFormat targetEWSDateFormat) throws EWSException {

        if (!isEmpty(date)) {

            SimpleDateFormat targetDateFormat = applyEWSDateFormat(targetEWSDateFormat);

            try {
                if (sourceEWSDateFormat != null) {

                    SimpleDateFormat sourceDateFormat = applyEWSDateFormat(sourceEWSDateFormat);
                    return sourceDateFormat.parse(date);

                } else {

                    return targetDateFormat.parse(date);
                }
            } catch (ParseException e) {
                LOGGER.error("EWSDateFormatter@ToEWSDate@DateParseError :::", e);
                throw new EWSException(e, EWS_DATE_PARSE_ERROR.getEwsErrorCode(), EWS_DATE_PARSE_ERROR.getEwsErrorMessage() +
                        " ::: date :-> " + date + " source dateFormat :-> " + sourceEWSDateFormat.getDateFormat() + " target dateFormat :-> " + targetEWSDateFormat.getDateFormat());
            }
        }
        return null;
    }

    public String formatDateAsString(String date, EWSDateFormat sourceEWSDateFormat, EWSDateFormat targetEWSDateFormat) throws EWSException {

        if (!isEmpty(date)) {

            SimpleDateFormat targetDateFormat = applyEWSDateFormat(targetEWSDateFormat);

            return targetDateFormat.format(formatDate(date, sourceEWSDateFormat, targetEWSDateFormat));
        }
        return null;
    }

    public Date toDate(String dateString) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        return simpleDateFormat.parse(dateString);
    }

    private void applyTimeZone(EWSDateFormat ewsDateFormat, SimpleDateFormat simpleDateFormat) {

        if (ewsDateFormat != null && ewsDateFormat.getTimeZone() != null) {

            simpleDateFormat.setTimeZone(ewsDateFormat.getTimeZone());
        }
    }

    private SimpleDateFormat applyEWSDateFormat(EWSDateFormat ewsDateFormat) {

        SimpleDateFormat targetDateFormat = new SimpleDateFormat();
        if (ewsDateFormat != null && ewsDateFormat.getDateFormat() != null) {

            targetDateFormat = new SimpleDateFormat(ewsDateFormat.getDateFormat());
            applyTimeZone(ewsDateFormat, targetDateFormat);
        }
        return targetDateFormat;
    }

    public TimeZone toTimeZone(String timezone) {

        return TimeZone.getTimeZone(timezone.split(" ")[0]);
    }

    public Date toDate(net.securustech.ews.util.dto.Date normalizedDate, String time) throws EWSException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        String dateData = normalizedDate.getYear() + "" + normalizedDate.getMonth() + "" + normalizedDate.getDate() + " " + time;
        try {
            return simpleDateFormat.parse(dateData);
        } catch (ParseException e) {
            LOGGER.error("EWSDateFormatter@ToDate@DateParseError :::", e);
            throw new EWSException(e, EWS_DATE_PARSE_ERROR.getEwsErrorCode(), EWS_DATE_PARSE_ERROR.getEwsErrorMessage() +
                    " ::: date :-> " + dateData);
        }
    }

    public Date formatDateWithEndOfDay(Date date) {

        return Date.from(
                LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
                        .with(LocalTime.MAX)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
    }

    public String formatDateWithEndOfDayAsString(Date date) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

        return simpleDateFormat.format(formatDateWithEndOfDay(date));
    }

    public String formatDateAsString(String sourceDate, String sourceDatePattern, String targetDatePattern)
            throws EWSException {
        String toDateString = null;
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(sourceDatePattern).toFormatter();
            LocalDateTime ldt = LocalDateTime.parse(sourceDate, formatter);
            toDateString = ldt.format(DateTimeFormatter.ofPattern(targetDatePattern));
        } catch (Exception e) {
            LOGGER.error("Error while formating sourceDate>>> Source Date:" + sourceDate +
                    ">>> SourceDatePattern:" + sourceDatePattern + ">>>Error message:" + e.getMessage(), e);
            throw new EWSException(e, EWS_DATE_FORMAT_ERROR.getEwsErrorCode(), EWS_DATE_FORMAT_ERROR.getEwsErrorMessage() + " ::: Error while formating sourceDate>>> Source Date:" + sourceDate +
                    ">>> SourceDatePattern:" + sourceDatePattern + ">>>Error message:" + e.getMessage());

        }
        return toDateString;
    }


    public String formatDateWithTzAsString(String sourceDate, String sourceDatePattern,
                                           String sourceTimezone, String targetDatePattern, String targetTimezone)
            throws EWSException {
        String targetDateString = null;

        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(sourceDatePattern).toFormatter();
            ZonedDateTime zdSourceDate = ZonedDateTime.parse(sourceDate + " " + sourceTimezone, formatter);
            targetDateString = zdSourceDate.format(DateTimeFormatter.ofPattern(targetDatePattern).withZone(ZoneId.of(targetTimezone)));
        } catch (Exception e) {
            LOGGER.error("Error while formating date>>> Source Date:" + sourceDate +
                    ">>> SourceDatePattern:" + sourceDatePattern + ">>> sourceTimezone:" + sourceTimezone
                    + ">>>Error message:" + e.getMessage(), e);
            throw new EWSException(e, EWS_DATE_FORMAT_ERROR.getEwsErrorCode(), EWS_DATE_FORMAT_ERROR.getEwsErrorMessage() + " ::: Error while formating date>>> Source Date:" + sourceDate +
                    ">>> SourceDatePattern:" + sourceDatePattern + ">>> sourceTimezone:" + sourceTimezone
                    + ">>>Error message:" + e.getMessage());
        }
        return targetDateString;
    }

    public Date formatDateWithTzAsDate(String sourceDate, String sourceDatePattern, String sourceTimezone,
                                       String targetDatePattern, String targetTimezone)
            throws EWSException {
        Date targetDate = null;
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(sourceDatePattern).toFormatter();
            ZonedDateTime zdSourceDate = ZonedDateTime.parse(sourceDate + " " + sourceTimezone, formatter);
            String targetDateString = zdSourceDate.format(DateTimeFormatter.ofPattern(targetDatePattern).withZone(ZoneId.of(targetTimezone)));
            ZonedDateTime zdTargetDate = ZonedDateTime.parse(targetDateString, DateTimeFormatter.ofPattern(targetDatePattern).withZone(ZoneId.of(targetTimezone)));
            targetDate = Date.from(zdTargetDate.toInstant());
        } catch (Exception e) {
            LOGGER.error("Error while formating date>>> Source Date:" + sourceDate +
                    ">>> SourceDatePattern:" + sourceDatePattern + ">>> sourceTimezone:" + sourceTimezone
                    + ">>>Error message:" + e.getMessage(), e);
            throw new EWSException(e, EWS_DATE_FORMAT_ERROR.getEwsErrorCode(), EWS_DATE_FORMAT_ERROR.getEwsErrorMessage() + " ::: Error while formating date>>> Source Date:" + sourceDate +
                    ">>> SourceDatePattern:" + sourceDatePattern + ">>> sourceTimezone:" + sourceTimezone
                    + ">>>Error message:" + e.getMessage());
        }

        return targetDate;
    }
}
