package io.polyglotted.test.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.InetAddresses;
import io.polyglotted.common.model.GeoPoint;
import io.polyglotted.common.model.GeoShape;
import io.polyglotted.common.model.GeoType;
import io.polyglotted.test.spring.ObjectInputs.CollClass;
import io.polyglotted.test.spring.ObjectInputs.RefClass;
import io.polyglotted.test.spring.ObjectInputs.SimpleClass;
import io.polyglotted.test.spring.ObjectInputs.Simplified;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import static io.polyglotted.common.util.UuidUtil.uuidFrom;
import static io.polyglotted.test.spring.ObjectInputs.MyConst.BAZ;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@JsonTest @RunWith(JUnitParamsRunner.class)
public class SerializationTest {

    @ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired private ObjectMapper objectMapper = null;

    public static Object[][] objInputs() throws Exception {
        return new Object[][]{
            {
                new SimpleClass().aString("foo").anIp(InetAddresses.forString("172.0.0.1")).aUrl(new URL("http://www.google.com"))
                    .aUri(new URI("s3://foo.zing.com")).aUuid(uuidFrom("f57f7027-33c9-5173-a00f-8ae3cdd93ff4")).aBoolean(true)
                    .aGeoPoint(new GeoPoint(-90, 90)).aGeoShape(new GeoShape(GeoType.point, "-1.1", null))
                    .aBinary("foo".getBytes()).aBuffer(ByteBuffer.wrap("bar".getBytes())).aDateTime(ZonedDateTime.of(2016, 2, 15, 4, 30, 0, 0, UTC))
                    .bDateTime(OffsetDateTime.of(2016, 2, 15, 4, 30, 0, 0, UTC)).bTime(LocalTime.of(4, 30).atOffset(UTC)).aDate(LocalDate.of(2016, 2, 15))
                    .aTime(LocalTime.of(4, 30)).cDateTime(LocalDateTime.of(2016, 2, 15, 4, 30, 0, 0)).dDateTime(new Date(1455510600000L))
            },
            {
                new Simplified().fullStr("foo").email("foo@bar.co").bigInt(BigInteger.TEN).date(1455510600000L).prim("tux")},
            {
                new CollClass().booleanList(ImmutableList.of(true, false, true)).doubleList(ImmutableList.of(10.0, (double) 20, 30.0))
                    .longSet(ImmutableSet.of(5L, 2L)).dateSet(ImmutableSet.of(new Date(1455510600000L), new Date(1455510700000L)))
                    .localDates(ImmutableList.of(LocalDate.of(2016, 2, 15))).objectSet(ImmutableSet.of("foo", true, 2))
                    .stringIntegerMap(ImmutableMap.of("a", 1, "b", 2)).primMap(ImmutableMap.of("a", "foo", "b", 2, "c", false))
            },
            {
                new RefClass().simplified(new Simplified().email("b@c.io")).simples(ImmutableList.of(new SimpleClass().aString("oui"),
                    new SimpleClass().aDate(LocalDate.of(2017, 1, 25)))).schemeMap(ImmutableMap.of(BAZ, new SimpleClass().anInt(25)))
            }
        };
    }

    @Test @Parameters(method = "objInputs")
    public void loginFailure(Object expected) throws Exception {
        String json = serialize(expected);
        Object actual = deserialize(json, expected.getClass());
        assertThat("Failed comparison \nexpected: " + json + "\nactual: " + serialize(actual), actual, is(expected));
    }

    private String serialize(Object value) throws IOException { return objectMapper.writeValueAsString(value); }

    private <T> T deserialize(String json, Class<T> clazz) throws IOException { return objectMapper.readValue(json, clazz); }
}