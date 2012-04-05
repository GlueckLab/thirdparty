/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.client.engine.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.restlet.client.Context;
import org.restlet.client.data.CharacterSet;
import org.restlet.client.data.Range;
import org.restlet.client.engine.Edition;
import org.restlet.client.representation.Representation;

/**
 * Basic IO manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public final class BioUtils {









    /**
     * Returns the size effectively available. This returns the same value as
     * {@link Representation#getSize()} if no range is defined, otherwise it
     * returns the size of the range using {@link Range#getSize()}.
     * 
     * @param representation
     *            The representation to evaluate.
     * @return The available size.
     */
    public static long getAvailableSize(Representation representation) {
         return representation.getSize();
    }

    /**
     * Returns a reader from an input stream and a character set.
     * 
     * @param stream
     *            The input stream.
     * @param characterSet
     *            The character set. May be null.
     * @return The equivalent reader.
     * @throws UnsupportedEncodingException
     *             if a character set is given, but not supported
     */
    public static Reader getReader(InputStream stream, CharacterSet characterSet)
            throws UnsupportedEncodingException {
        if (characterSet != null) {
            return new InputStreamReader(stream, characterSet.getName());
        }

        return new InputStreamReader(stream);
    }











    /**
     * Converts an input stream to a string.<br>
     * As this method uses the InputstreamReader class, the default character
     * set is used for decoding the input stream.
     * 
     * @see <a href=
     *      "http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html"
     *      >InputStreamReader class</a>
     * @see #toString(InputStream, CharacterSet)
     * @param inputStream
     *            The input stream.
     * @return The converted string.
     */
    public static String toString(InputStream inputStream) {
        return toString(inputStream, null);
    }

    /**
     * Converts an input stream to a string using the specified character set
     * for decoding the input stream. Once read, the input stream is closed.
     * 
     * @see <a href=
     *      "http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html"
     *      >InputStreamReader class</a>
     * @param inputStream
     *            The input stream.
     * @param characterSet
     *            The character set
     * @return The converted string.
     */
    public static String toString(InputStream inputStream,
            CharacterSet characterSet) {
        String result = null;

        if (inputStream != null) {
             if (inputStream instanceof StringInputStream) {
             return ((StringInputStream) inputStream).getText();
             } else {
             try {
             if (characterSet != null) {
             result = toString(new InputStreamReader(inputStream,
             characterSet.getName()));
             } else {
             result = toString(new InputStreamReader(inputStream));
             }
             } catch (Exception e) {
             // Returns an empty string
             }
             }
        }

        return result;
    }

    /**
     * Converts a reader to a string.
     * 
     * @see <a
     *      href="http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html">InputStreamReader
     *      class</a>
     * @param reader
     *            The characters reader.
     * @return The converted string.
     */
    public static String toString(Reader reader) {
        String result = null;

        if (reader != null) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader
                        : new BufferedReader(reader, IoUtils.getBufferSize());
                char[] buffer = new char[2048];
                int charsRead = br.read(buffer);

                while (charsRead != -1) {
                    sb.append(buffer, 0, charsRead);
                    charsRead = br.read(buffer);
                }

                br.close();
                result = sb.toString();
            } catch (Exception e) {
                // Returns an empty string
            }
        }

        return result;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private BioUtils() {
    }

}
