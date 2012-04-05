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

package org.restlet.client.engine.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.restlet.client.Context;
import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.data.Method;
import org.restlet.client.data.Parameter;
import org.restlet.client.data.Status;
import org.restlet.client.engine.ConnectorHelper;
import org.restlet.client.engine.http.header.DispositionReader;
import org.restlet.client.engine.http.header.HeaderConstants;
import org.restlet.client.engine.http.header.HeaderUtils;
import org.restlet.client.representation.Representation;
import org.restlet.client.util.Series;

/**
 * Low-level HTTP client call.
 * 
 * @author Jerome Louvel
 */
public abstract class ClientCall extends Call {

    /**
     * Returns the local IP address or 127.0.0.1 if the resolution fails.
     * 
     * @return The local IP address or 127.0.0.1 if the resolution fails.
     */
    public static String getLocalAddress() {
            return "127.0.0.1";
    }

    /**
     * Parse the Content-Disposition header value
     * 
     * @param value
     *            Content-disposition header
     * @return Filename
     * @deprecated Use {@link DispositionReader} instead.
     */
    @Deprecated
    public static String parseContentDisposition(String value) {
        if (value != null) {
            String key = "FILENAME=\"";
            int index = value.toUpperCase().indexOf(key);
            if (index > 0) {
                return value
                        .substring(index + key.length(), value.length() - 1);
            }

            key = "FILENAME=";
            index = value.toUpperCase().indexOf(key);
            if (index > 0) {
                return value.substring(index + key.length(), value.length());
            }
        }

        return null;
    }

    /** The parent HTTP client helper. */
    private volatile HttpClientHelper helper;

    /**
     * Constructor setting the request address to the local host.
     * 
     * @param helper
     *            The parent HTTP client helper.
     * @param method
     *            The method name.
     * @param requestUri
     *            The request URI.
     */
    public ClientCall(HttpClientHelper helper, String method, String requestUri) {
        this.helper = helper;
        setMethod(method);
        setRequestUri(requestUri);
        setClientAddress(getLocalAddress());
    }

    /**
     * Returns the content length of the request entity if know,
     * {@link Representation#UNKNOWN_SIZE} otherwise.
     * 
     * @return The request content length.
     */
    protected long getContentLength() {
        return HeaderUtils.getContentLength(getResponseHeaders());
    }

    /**
     * Returns the HTTP client helper.
     * 
     * @return The HTTP client helper.
     */
    public HttpClientHelper getHelper() {
        return this.helper;
    }



     /**
     * Returns the request entity string if it exists.
     *
     * @return The request entity string if it exists.
     */
     public abstract String getRequestEntityString();


    /**
     * Returns the response entity if available. Note that no metadata is
     * associated by default, you have to manually set them from your headers.
     * 
     * @param response
     *            the Response to get the entity from
     * @return The response entity if available.
     */
    public Representation getResponseEntity(Response response) {
        Representation result = null;
        // boolean available = false;
        long size = Representation.UNKNOWN_SIZE;

        // Compute the content length
        Series<Parameter> responseHeaders = getResponseHeaders();
        String transferEncoding = responseHeaders.getFirstValue(
                HeaderConstants.HEADER_TRANSFER_ENCODING, true);
        if ((transferEncoding != null)
                && !"identity".equalsIgnoreCase(transferEncoding)) {
            size = Representation.UNKNOWN_SIZE;
        } else {
            size = getContentLength();
        }

        if (!getMethod().equals(Method.HEAD.getName())
                && !response.getStatus().isInformational()
                && !response.getStatus()
                        .equals(Status.REDIRECTION_NOT_MODIFIED)
                && !response.getStatus().equals(Status.SUCCESS_NO_CONTENT)
                && !response.getStatus().equals(Status.SUCCESS_RESET_CONTENT)) {
            // Make sure that an InputRepresentation will not be instantiated
            // while the stream is closed.
            InputStream stream = getUnClosedResponseEntityStream(getResponseEntityStream(size));
             InputStream channel = null;

            if (stream != null) {
                result = getRepresentation(stream);
            } else if (channel != null) {
                result = getRepresentation(channel);
                // } else {
                // result = new EmptyRepresentation();
            }
        }

        result = HeaderUtils.extractEntityHeaders(responseHeaders, result);
        if (result != null) {
            result.setSize(size);

            // Informs that the size has not been specified in the header.
            if (size == Representation.UNKNOWN_SIZE) {
                getLogger()
                        .fine("The length of the message body is unknown. The entity must be handled carefully and consumed entirely in order to surely release the connection.");
            }
        }
        // }

        return result;
    }


    /**
     * Returns the response entity stream if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * @return The response entity stream if it exists.
     */
    public abstract InputStream getResponseEntityStream(long size);

    /**
     * Checks if the given input stream really contains bytes to be read. If so,
     * returns the inputStream otherwise returns null.
     * 
     * @param inputStream
     *            the inputStream to check.
     * @return null if the given inputStream does not contain any byte, an
     *         inputStream otherwise.
     */
    private InputStream getUnClosedResponseEntityStream(InputStream inputStream) {
        InputStream result = null;

        if (inputStream != null) {
            try {
                if (inputStream.available() > 0) {
                    result = inputStream;
                }
            } catch (IOException ioe) {
                getLogger().log(Level.FINER, "End of response entity stream.",
                        ioe);
            }

        }

        return result;
    }

    @Override
    protected boolean isClientKeepAlive() {
        return true;
    }

    @Override
    protected boolean isServerKeepAlive() {
        return !HeaderUtils.isConnectionClose(getResponseHeaders());
    }


    /**
     * Sends the request to the client. Commits the request line, headers and
     * optional entity and send them over the network.
     * 
     * @param request
     *            The high-level request.
     * @param response
     *            The high-level response.
     * @param callback
     *            The callback invoked upon request completion.
     */
    public void sendRequest(Request request, Response response,
            org.restlet.client.Uniform callback) throws Exception {
        Context.getCurrentLogger().warning(
                "Currently callbacks are not available for this connector.");
    }

    /**
     * Indicates if the request entity should be chunked.
     * 
     * @return True if the request should be chunked
     */
    protected boolean shouldRequestBeChunked(Request request) {
        return request.isEntityAvailable()
                && (request.getEntity() != null)
                && (request.getEntity().getSize() == Representation.UNKNOWN_SIZE);
    }
}
