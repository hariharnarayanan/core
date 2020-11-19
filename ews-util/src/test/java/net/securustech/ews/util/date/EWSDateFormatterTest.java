package net.securustech.ews.util.date;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.securustech.ews.exception.entities.EWSException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {EWSDateFormatter.class, ObjectMapper.class})
public class EWSDateFormatterTest {

    public static final String DD_MMM_YYYY_HH_MM_SS = "dd-MMM-yyyy HH:mm:ss";
    public static final String DD_MMM_YYYY_HH_MM = "dd-MMM-yyyy HH:mm";
    public static final String DD_MMM_YYYY_HH = "dd-MMM-yyyy HH";
    public static final String DD_MMM_YYYY = "dd-MMM-yyyy";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_TIME_WITH_TZ = "d-MMM-yyyy HH:mm:ss zzz";
    public static final String YYYY_MM_DD_T_HH_MM_SS_Z = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String EEE_MMM_DD_HH_MM_SS_YYYY = "EEE MMM dd HH:mm:ss yyyy";

    @Autowired
    private EWSDateFormatter ewsDateFormatter;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void toEWSDateWithTargetFormatOnly() throws Exception {
        String dateInString = "2018-02-15 10:20:21";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = formatter.parse(dateInString);
        EWSDateFormat targetDateFormat = new EWSDateFormat("yyyy-MM-dd HH:mm", null);
        assertEquals("2018-02-15 10:20", ewsDateFormatter.formatDateAsString(date, targetDateFormat));

    }

    @Test
    public void toEWSDateWithBothSourceAndTargetFormats() throws Exception {
        TimeZone timeZone = ewsDateFormatter.toTimeZone("UTC");
        EWSDateFormat sourceDateFormat = new EWSDateFormat("yyyy-MM-dd", timeZone);
        EWSDateFormat targetDateFormat = new EWSDateFormat("yyyy-MM-dd HH:mm:ss", timeZone);
        assertEquals("2019-02-15 00:00:00", ewsDateFormatter.formatDateAsString("2019-02-15", sourceDateFormat, targetDateFormat));
    }

    @Test
    public void toTimeZone() throws Exception {
        TimeZone timeZone = ewsDateFormatter.toTimeZone("UTC");
        assertEquals(TimeZone.getTimeZone("UTC"), timeZone);

    }

    @Test
    public void toEWSDateWithEndOfDay() throws Exception {
        String dateInString = "10-Jan-2016";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        Date date = formatter.parse(dateInString);

        assertEquals("1/10/16 11:59 PM", ewsDateFormatter.formatDateWithEndOfDayAsString(date));
    }

    @Test
    public void whenSourceIsValidReturnSuccess() throws EWSException {
        String targetDate1 = ewsDateFormatter.formatDateAsString("26-FEB-2019 03:05:00", DD_MMM_YYYY_HH_MM_SS, YYYY_MM_DD_HH_MM_SS);
        assertEquals("Incorrect Date format>>" + targetDate1, "2019-02-26 03:05:00", targetDate1);
        String targetDate2 = ewsDateFormatter.formatDateAsString("26-FEB-2019 03:05", DD_MMM_YYYY_HH_MM, YYYY_MM_DD_HH_MM_SS);
        assertEquals("Incorrect Date format>>" + targetDate2, "2019-02-26 03:05:00", targetDate2);
        String targetDate3 = ewsDateFormatter.formatDateAsString("26-FEB-2019 03", DD_MMM_YYYY_HH, YYYY_MM_DD_HH_MM_SS);
        assertEquals("Incorrect Date format>>" + targetDate3, "2019-02-26 03:00:00", targetDate3);
        String targetDate4 = ewsDateFormatter.formatDateAsString("Mon Feb 25 21:05:00 2019", EEE_MMM_DD_HH_MM_SS_YYYY, YYYY_MM_DD_HH_MM_SS);
        assertEquals("Incorrect Date format>>" + targetDate4, "2019-02-25 21:05:00", targetDate4);
    }

    @Test(expected = EWSException.class)
    public void whenSourceIsInvalidReturnException() throws EWSException {
        String targetDate4 = ewsDateFormatter.formatDateAsString("26-FEB-2019", DD_MMM_YYYY, YYYY_MM_DD_HH_MM_SS);

    }

    @Test
    public void whenSourceWithTZIsValidReturnSuccess() throws EWSException {
        String targetDate1 = ewsDateFormatter.formatDateWithTzAsString(
                "26-FEB-2019 03:05:00", DATE_TIME_WITH_TZ, "UTC", YYYY_MM_DD_HH_MM_SS, "US/Eastern");
        assertEquals("Incorrect Date conversion " + targetDate1, "2019-02-25 22:05:00", targetDate1);
        String targetDate2 = ewsDateFormatter.formatDateWithTzAsString(
                "26-FEB-2019 03:05:00", DATE_TIME_WITH_TZ, "UTC", YYYY_MM_DD_T_HH_MM_SS_Z, "US/Eastern");
        assertEquals("Incorrect Date conversion " + targetDate2, "2019-02-25T22:05:00-0500", targetDate2);
    }

    @Test(expected = EWSException.class)
    public void whenSourceWithTZIsInvalidReturnException() throws EWSException {
        String targetDate1 = ewsDateFormatter.formatDateWithTzAsString(
                "26-FEB-2019", DATE_TIME_WITH_TZ, "UTC", YYYY_MM_DD_HH_MM_SS, "US/Eastern");
    }

    @Test
    public void whenSourceWithTZAsDateIsValidReturnSuccess() throws EWSException {
        String exDateStr1 = "Mon Feb 25 21:05:00 CST 2019";
        ZonedDateTime zd1 = ZonedDateTime.parse(exDateStr1, DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy"));
        Date expectedDate1 = Date.from(zd1.toInstant());
        String exDateStr2 = "2019-02-25T22:05:00-0500";
        ZonedDateTime zd2 = ZonedDateTime.parse(exDateStr2, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
        Date expectedDate2 = Date.from(zd2.toInstant());
        Date targetDate1 = ewsDateFormatter.formatDateWithTzAsDate(
                "26-FEB-2019 03:05:00", DATE_TIME_WITH_TZ, "UTC", YYYY_MM_DD_HH_MM_SS, "US/Eastern");
        assertEquals("Incorrect Date conversion " + targetDate1, expectedDate1, targetDate1);
        Date targetDate2 = ewsDateFormatter.formatDateWithTzAsDate(
                "26-FEB-2019 03:05:00", DATE_TIME_WITH_TZ, "UTC", YYYY_MM_DD_T_HH_MM_SS_Z, "US/Eastern");
        assertEquals("Incorrect Date conversion " + targetDate2, expectedDate2, targetDate2);
    }

    @Test(expected = EWSException.class)
    public void whenSourceWithTZAsDateIsInvalidReturnException() throws EWSException {
        Date targetDate1 = ewsDateFormatter.formatDateWithTzAsDate(
                "26-FEB-2019", DATE_TIME_WITH_TZ, "UTC", YYYY_MM_DD_HH_MM_SS, "US/Eastern");
    }

}