package io.polyglotted.test.spring;

import com.amazonaws.services.s3.model.ObjectMetadata;
import io.polyglotted.common.model.MapResult;

import javax.activation.MimetypesFileTypeMap;

import static io.polyglotted.common.util.MapRetriever.deepRetrieve;
import static io.polyglotted.common.util.NullUtil.nonNull;

//TODO MOVE TO AWS-COMMON
abstract class AwsContentUtil {
    private static final String OCTET_STREAM = "application/octet-stream";
    private static final String BINARY_STREAM = "binary/octet-stream";
    private static final MimetypesFileTypeMap MIME_MAP = new MimetypesFileTypeMap();

    static ObjectMetadata contentTypeMetaData(String fileName, Long contentLength) {
        ObjectMetadata omd = new ObjectMetadata();
        omd.setContentType(MIME_MAP.getContentType(fileName));
        if (contentLength != null) { omd.setContentLength(contentLength); }
        return omd;
    }

    static String fetchContentType(ObjectMetadata metadata, MapResult attachment, String file) {
        return nonNull(nullIf(metadata.getContentType()), nullIf(deepRetrieve(attachment, "indexed.content_type")), MIME_MAP.getContentType(file));
    }

    private static String nullIf(String value) {
        return OCTET_STREAM.equals(nonNull(value, OCTET_STREAM)) ? null : BINARY_STREAM.equals(nonNull(value, BINARY_STREAM)) ? null : value;
    }
}