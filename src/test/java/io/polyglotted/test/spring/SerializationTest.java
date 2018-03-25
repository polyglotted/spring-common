package io.polyglotted.test.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.polyglotted.common.model.GeoPoint;
import io.polyglotted.common.model.GeoShape;
import io.polyglotted.common.model.GeoType;
import io.polyglotted.common.model.MapResult;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.polyglotted.common.model.AuthToken.tokenBuilder;
import static io.polyglotted.common.model.Subject.subjectBuilder;
import static io.polyglotted.common.util.BaseSerializer.deserialize;
import static io.polyglotted.common.util.BaseSerializer.serialize;
import static io.polyglotted.common.util.BaseSerializer.serializeBytes;
import static io.polyglotted.common.util.ListBuilder.immutableList;
import static io.polyglotted.common.util.ListBuilder.immutableSet;
import static io.polyglotted.common.util.MapBuilder.immutableMap;
import static io.polyglotted.common.util.UuidUtil.uuidFrom;
import static io.polyglotted.test.spring.SerializationTest.MyConst.BAZ;
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
                new SimpleClass().aString("foo").anIp(InetAddress.getByName("172.0.0.1")).aUrl(new URL("http://www.google.com"))
                    .aUri(new URI("s3://foo.zing.com")).aUuid(uuidFrom("f57f7027-33c9-5173-a00f-8ae3cdd93ff4")).aBoolean(true)
                    .aGeoPoint(new GeoPoint(-90, 90)).aGeoShape(new GeoShape(GeoType.point, "-1.1", null))
                    .aBinary("foo".getBytes()).aBuffer(ByteBuffer.wrap("bar".getBytes())).aDateTime(ZonedDateTime.of(2016, 2, 15, 4, 30, 0, 0, UTC))
                    .bDateTime(OffsetDateTime.of(2016, 2, 15, 4, 30, 0, 0, UTC)).bTime(LocalTime.of(4, 30).atOffset(UTC)).aDate(LocalDate.of(2016, 2, 15))
                    .aTime(LocalTime.of(4, 30)).cDateTime(LocalDateTime.of(2016, 2, 15, 4, 30, 0, 0)).dDateTime(new Date(1455510600000L))
            },
            {
                new Simplified().fullStr("foo").email("foo@bar.co").bigInt(BigInteger.TEN).date(1455510600000L).prim("tux")},
            {
                new CollClass().booleanList(immutableList(true, false, true)).doubleList(immutableList(10.0, (double) 20, 30.0))
                    .longSet(immutableSet(5L, 2L)).dateSet(immutableSet(new Date(1455510600000L), new Date(1455510700000L)))
                    .localDates(immutableList(LocalDate.of(2016, 2, 15))).objectSet(immutableSet("foo", true, 2))
                    .stringIntegerMap(immutableMap("a", 1, "b", 2)).primMap(immutableMap("a", "foo", "b", 2, "c", false))
            },
            {
                new RefClass().simplified(new Simplified().email("b@c.io")).simples(immutableList(new SimpleClass().aString("oui"),
                    new SimpleClass().aDate(LocalDate.of(2017, 1, 25)))).schemeMap(immutableMap(BAZ, new SimpleClass().anInt(25)))
            },
            {
                subjectBuilder().usernameMd5("foo.bar.baz", "mister@misty.co").role("my_role1").metadata("mfaEnabled", false).build()
            },
            {
                tokenBuilder().accessToken("y7nvAiCrpP8HRJkxgdb3s3T4").expiresIn(1200).tokenType("Bearer").refreshToken("fooBarBaz").build()
            },
        };
    }

    @Test @Parameters(method = "objInputs")
    public void serialiseNative(Object expected) throws Exception {
        String json = serialize(objectMapper, expected);
        assertThat("\n" + json + "\n" + serialize(expected), json, is(serialize(expected)));
        Object actual = deserialize(objectMapper, json, expected.getClass());
        assertThat(json, actual, is(expected));
    }

    @Test @Parameters(method = "objInputs")
    public void serializeAndConstruct(Object expected) throws Exception {
        byte[] bytes = serializeBytes(objectMapper, expected);
        MapResult mapResult = deserialize(objectMapper, bytes);
        assertThat(serialize(objectMapper, mapResult), bytes, is(serializeBytes(objectMapper, mapResult)));
    }

    public static Object[][] jsonInputs() throws Exception {
        return new Object[][]{
            {"{\"fullStr\":null,\"date\":\"2016-02-15T04:30Z\"}", new Simplified().date(1455510600000L)},
            {"{\"dateLongs\":[\"2016-02-15T04:30Z\"]}", new CollClass().dateLongs(immutableList(1455510600000L))},
            {"{\"primMap\":{\"qux\":\"2016-02-15T04:30Z\"}}", new CollClass().primMap(immutableMap("qux", "2016-02-15T04:30Z"))},
        };
    }

    @Test @Parameters(method = "jsonInputs")
    public void serializeStrAsDateLong(String json, Object expected) throws Exception {
        Object actual = deserialize(objectMapper, json, expected.getClass());
        assertThat(json, actual, is(expected));
    }

    @Test
    public void serializeEmptyStringToNull() throws Exception {
        Simplified expected = new Simplified().fullStr("");
        String json = serialize(objectMapper, expected);
        assertThat(json, deserialize(objectMapper, json, Simplified.class), is(new Simplified()));
    }

    enum MyConst {BAZ}

    @Accessors(fluent = true, chain = true)
    @Setter @EqualsAndHashCode
    static class SimpleClass {
        private String aString;
        private InetAddress anIp;
        private URL aUrl;
        private URI aUri;
        private java.util.UUID aUuid;
        private boolean aBoolean;
        private GeoPoint aGeoPoint;
        private GeoShape aGeoShape;
        private byte[] aBinary;
        private ByteBuffer aBuffer;
        private LocalDate aDate;
        private LocalTime aTime;
        private OffsetTime bTime;
        private ZonedDateTime aDateTime;
        private OffsetDateTime bDateTime;
        private LocalDateTime cDateTime;
        private Date dDateTime;
        private byte aByte;
        private short aShort;
        private int anInt;
        private long aLong;
        private float aFloat;
        private double aDouble;
    }

    @Accessors(fluent = true, chain = true)
    @Setter @EqualsAndHashCode
    static class Simplified {
        private String fullStr;
        private String email;
        private BigInteger bigInt;
        private long date;
        private Object prim;
        private byte[] content;
    }

    @Accessors(fluent = true, chain = true)
    @Setter @EqualsAndHashCode
    static class CollClass {
        private List<Boolean> booleanList;
        private List<Double> doubleList;
        private Set<Long> longSet;
        private Set<Date> dateSet;
        private List<LocalDate> localDates;
        private List<Long> dateLongs;
        private Set<Object> objectSet;
        private Map<String, Integer> stringIntegerMap;
        private Map<String, Object> primMap;
    }

    @Accessors(fluent = true, chain = true)
    @Setter @EqualsAndHashCode
    static class RefClass {
        private Simplified simplified;
        private List<SimpleClass> simples;
        Set<Object> generics;
        private Map<MyConst, SimpleClass> schemeMap;
    }
}