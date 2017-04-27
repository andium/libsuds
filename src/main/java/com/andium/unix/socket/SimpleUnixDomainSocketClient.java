package com.andium.unix.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Creates a Unix domain socket and connects it to the server specified by the socket file. This class
 * shouldn't only be used to connect to a socket, being served by an existing server.
 */
public class SimpleUnixDomainSocketClient extends SimpleUnixDomainSocket {

    private final Logger logger = LoggerFactory.getLogger(SimpleUnixDomainSocketClient.class);

    /**
     * Instantiates a new Simple unix domain socket client.
     *
     * @param socketFile  the name of the socket file
     * @param pSocketType the socket type (use SOCK_DGRAM or SOCK_STREAM)
     * @throws IOException the io exception if it is unable to construct the socket
     */
    public SimpleUnixDomainSocketClient(String socketFile, int pSocketType) throws IOException {
        super.socketFile = socketFile;
        super.socketType = pSocketType;

        logger.debug("Calling nativeOpen()");
        if ((nativeSocketFileHandle = nativeOpen(socketFile, socketType)) == -1){
            throw new IOException("Unable to open the socket");
        }
        logger.debug("Socket opened");

        if (socketType == SOCK_STREAM){
            inputStream = new SimpleUnixDomainSocketInputStream();
        }
        outputStream = new SimpleUnixDomainSocketOutputStream();
    }

    /**
     * Override of the base class, to ensure that the caller doesn't try and perform bidirectional data passes on a
     * unidirectional socket.
     *
     * @return SimpleUnixDomainSocketInputStream
     * @throws UnsupportedOperationException if we try to get the input stream on a unidirectional socket, this unchecked
     * exception is thrown
     */
    @Override
    public SimpleUnixDomainSocketInputStream getInputStream() {
        if (socketType == SOCK_STREAM){
            return inputStream;
        } else {
            throw new UnsupportedOperationException("Datagram sockets are unidirectional. This socket type does not support InputStreams");
        }
    }
}
