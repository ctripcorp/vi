package com.ctrip.framework.cs.util;

/**
 * Created by jiang.j on 2016/4/8.
 */


import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

public class IOUtils {
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;
    private static final int BUFFER_SIZE = 8192;
    // readLines
    //-----------------------------------------------------------------------

    /**
     * Gets the contents of an <code>InputStream</code> as a list of Strings,
     * one entry per line, using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input the <code>InputStream</code> to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     * @deprecated 2.5 use {@link #readLines(InputStream, Charset)} instead
     */
    @Deprecated
    public static List<String> readLines(final InputStream input) throws IOException {
        return readLines(input, Charset.defaultCharset());
    }

    /**
     * Gets the contents of an <code>InputStream</code> as a list of Strings,
     * one entry per line, using the specified character encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input the <code>InputStream</code> to read from, not null
     * @param encoding the encoding to use, null means platform default
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 2.3
     */
    public static List<String> readLines(final InputStream input, final Charset encoding) throws IOException {
        try(final InputStreamReader reader = new InputStreamReader(input,encoding)) {
            return readLines(reader);
        }
    }

    /**
     * Gets the contents of an <code>InputStream</code> as a list of Strings,
     * one entry per line, using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input the <code>InputStream</code> to read from, not null
     * @param encoding the encoding to use, null means platform default
     * @return the list of Strings, never null
     * @throws NullPointerException                         if the input is null
     * @throws IOException                                  if an I/O error occurs
     * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
     *                                                      .UnsupportedEncodingException} in version 2.2 if the
     *                                                      encoding is not supported.
     * @since 1.1
     */
    public static List<String> readLines(final InputStream input, final String encoding) throws IOException {
        return readLines(input, encoding);
    }

    public static BufferedReader toBufferedReader(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }
    /**
     * Gets the contents of a <code>Reader</code> as a list of Strings,
     * one entry per line.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedReader</code>.
     *
     * @param input the <code>Reader</code> to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     * @since 1.1
     */
    public static List<String> readLines(final Reader input) throws IOException {
        try(final BufferedReader reader = toBufferedReader(input)) {
            final List<String> list = new ArrayList<>();
            String line = reader.readLine();
            while (line != null) {
                list.add(line);
                line = reader.readLine();
            }
            return list;
        }
    }

    public static String readAll(final InputStream input) throws IOException{
        String defaultCharset = System.getProperty("sun.jnu.encoding");
        Charset fileCharset  = defaultCharset!=null?Charset.forName(defaultCharset):Charset.defaultCharset();
        Scanner s = new Scanner(input,fileCharset.name()).useDelimiter("\\A");
        return s.hasNext()?s.next():"";
    }

    public static byte[] partitionRead(final Path path,int partionSize,int partitionIndex) throws IOException {

        try (SeekableByteChannel sbc = Files.newByteChannel(path);
             InputStream in = Channels.newInputStream(sbc)) {
            long startIndex = (partitionIndex-1)*partionSize;
            if (startIndex < 0) {
                startIndex = 0;
            }
            in.skip(startIndex);
            if (partionSize >  MAX_BUFFER_SIZE)
                throw new OutOfMemoryError("Required array size too large");

            return read(in,  partionSize);

        }
    }
    public static byte[] reverseRead(final Path path,Charset charset,long size) throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(path);
             InputStream in = Channels.newInputStream(sbc)) {
            long startIndex = sbc.size() - size;
            if (startIndex < 0) {
                startIndex = 0;
            }
            in.skip(startIndex);
            if (size > (long) MAX_BUFFER_SIZE)
                throw new OutOfMemoryError("Required array size too large");

            return read(in, (int) size);

        }
    }
    private static byte[] read(InputStream source, int initialSize) throws IOException {
        int capacity = initialSize;
        byte[] buf = new byte[capacity];
        int nread = 0;
        nread = source.read(buf, nread, capacity - nread) ;
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }


    public static InputStream decompressStream(InputStream input) throws IOException {
        PushbackInputStream pb = new PushbackInputStream( input, 2 ); //we need a pushbackstream to look ahead
        byte [] signature = new byte[2];
        int len = pb.read( signature ); //read the signature
        pb.unread( signature, 0, len ); //push back the signature to the stream
        if( signature[ 0 ] == (byte) 0x1f && signature[ 1 ] == (byte) 0x8b ) { //check if matches standard gzip magic number
            return new GZIPInputStream(pb);
        }
        else {
            return pb;
        }
    }

}
