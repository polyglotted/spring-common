package io.polyglotted.test.spring;

import io.polyglotted.common.model.GeoPoint;
import io.polyglotted.common.model.GeoShape;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

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

@SuppressWarnings("unused")
class ObjectInputs {
    enum MyConst {
        FOO, BAR, BAZ
    }

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