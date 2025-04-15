package com.tennisfolio.Tennisfolio.api.base;

import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@Component
public class DecompressorUtil {

    public static String decompressGzip(byte[] compressedData) throws IOException {
        try(GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressedData))){
            return new String(gis.readAllBytes(), "UTF-8");
        }
    }

    public static String decompressDeflate(byte[] compressedData) throws Exception{
        try(InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(compressedData), new Inflater(true))){
            return new String(iis.readAllBytes(), "UTF-8");
        }
    }
}
