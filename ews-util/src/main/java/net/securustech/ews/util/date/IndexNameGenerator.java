package net.securustech.ews.util.date;

import net.securustech.ews.exception.entities.EWSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class IndexNameGenerator {
    private static final Logger log = LoggerFactory.getLogger(IndexNameGenerator.class);

    @Autowired
    private EWSDateFormatter ewsDateFormatter;

    public String getIndexName(String date, String dateFormat, String indexNamePrefix, String indexDateFormat) {
        Assert.notNull(indexDateFormat, "Index date format is null");
        Assert.isTrue(!indexDateFormat.isEmpty(), "Index date format is empty");

        EWSDateFormat sourceFormat = EWSDateFormat.withoutTimeZone(dateFormat);
        EWSDateFormat targetFormat = EWSDateFormat.withoutTimeZone(indexDateFormat);
        String indexDate = null;
        try {
            indexDate = ewsDateFormatter.formatDateAsString(date, sourceFormat, targetFormat);
        } catch (EWSException e) {
            log.error("IndexNameGenerator@GetIndexName@DateParseError :::", e);
        }
        return indexNamePrefix + "-" + indexDate;
    }
}
