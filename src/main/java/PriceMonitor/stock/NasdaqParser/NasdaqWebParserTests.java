package PriceMonitor.stock.NasdaqParser;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Optional;

/**
 * Created by zhuoli on 7/29/16.
 */
public class NasdaqWebParserTests {

    @Test
    public void QuoteSymbolePriceTest() {
        NasdaqWebParser parser = new NasdaqWebParser();
        double price = parser.QuoteSymbolePrice("jd");
        Assert.assertTrue(price > 0);
    }

    @Test
    public void GetElementTextTest() {
        NasdaqWebParser parser = new NasdaqWebParser();
        Optional<LocalDate> jddate = parser.QupteEarningReportDate("jd");
        Optional<LocalDate> yrddate = parser.QupteEarningReportDate("yrd");
        Optional<LocalDate> teslaDate = parser.QupteEarningReportDate("tsla");
        System.out.println(jddate.get());
        System.out.println(yrddate.get());
        Assert.assertTrue(jddate.isPresent());
        Assert.assertTrue(yrddate.isPresent());
        Assert.assertTrue(!teslaDate.isPresent());
    }

    @Test
    public void DateFormatParse() {
        String text = "Aug 01, 2016";
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("MMM dd, yyyy").toFormatter();
        TemporalAccessor ta = formatter.parse(text);
        Instant instant = LocalDate.from(ta).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Date d = Date.from(instant);
        System.out.println(d);
    }
}
