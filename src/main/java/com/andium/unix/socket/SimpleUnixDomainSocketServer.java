package com.andium.unix.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Creates a Unix domain socket and connects it to the server specified by the socket file. Based on the constructor,
 * it can accept either 1 or n simultaneous connections. Due to the blocking nature of the socket server, it is suggested
 * that you use separate threads to call accept() and then handle any connections deriving from accept();
 *
 * e.g.
 *
 * int conn = 3;
 * boolean running = true;
 * SimpleUnixDomainSocketServer server;
 * ExecutorService mainService = Executors.newSingleThreadExecutor();
 * ExecutorService connService = Executors.newFixedThreadPool(conn);
 *
 * mainService.submit(()->{
 *          server = new SimpleUnixDomainSocketServer("socket.sock",
 *                                                     SimpleUnixDomainSocket.SOCK_STREAM,
 *                                                     conn);
 *          while (running){
 *              try {
 *                  SimpleUnixDomainSocket socket = server.accept(); //blocking
 *                  connService.submit(()->{
 *                     // handle socket operations
 *                  });
 *              } catch (IOException e){
 *                  // handle exception
 *              }
 *          }
 * });
 *
 *
 * When you wish to shut down the socket, you can forcibly stop the ExecutorService (using ExecutorService#shutdownNow)
 * and perform an socket clean up on the server object. This may not be the best approach to take, but it will be enough
 * to get you started.
 *
 */
public class SimpleUnixDomainSocketServer extends SimpleUnixDomainSocket{

    private final Logger logger = LoggerFactory.getLogger(SimpleUnixDomainSocketServer.class);

    /**
     * Instantiates a new Simple unix domain socket server. This constructor will create a socket
     * which only accepts a single connection. If you wish to accept more than connection, then use the
     * SimpleUnixDomainSocketServer(String,int,int) constructor.
     *
     * @param socketFile the name of the socket file
     * @param socketType the socket type (use SOCK_DGRAM or SOCK_STREAM)
     * @throws IOException the io exception
     */
    public SimpleUnixDomainSocketServer(String socketFile, int socketType) throws IOException{
        super.socketFile = socketFile;
        super.socketType = socketType;
        logger.debug("Attempting to create socket");

        if ((nativeSocketFileHandle = nativeCreate(socketFile, socketType)) == -1){
            throw new IOException("Unable to open domain socket");
        }
        logger.debug("Socket created with handle " + nativeSocketFileHandle);

        inputStream = new SimpleUnixDomainSocketInputStream();
        if (socketType == SOCK_STREAM){
            outputStream = new SimpleUnixDomainSocketOutputStream();
        }
    }

    /**
     * Instantiates a new Simple unix domain socket server. It will allow, at most, the number of connections
     * specified by the connections param.
     *
     * @param socketfile  the name of the socket file
     * @param socketType  the socket type (use SOCK_DGRAM or SOCK_STREAM)
     * @param connections the number of simultaneous connections pending to be accepted
     * @throws IOException the io exception
     */
    public SimpleUnixDomainSocketServer(String socketfile, int socketType, int connections) throws IOException{
        super.socketFile = socketfile;
        super.socketType = socketType;

        if ((nativeSocketFileHandle = nativeListen(socketFile, socketType, connections)) == -1){
            throw new IOException("Unable to open and listen on Unix domain socket");
        }
        logger.debug("listening to socket at " + socketfile);
    }

    /**
     * Blocking call to accept() on the socket. This will return a new SimpleUnixDomainSocket object for each
     * connection that the server accepts. Calls to accept() WILL block until the server either faults or accepts
     * a new connection. It is suggested that you use a separate thread to call accept.
     *
     * @return the simple unix domain socket
     * @throws IOException the io exception
     */
    public SimpleUnixDomainSocket accept() throws IOException{
        logger.debug("Calling accept()");
        int newSocketFileHandle = -1;
        if ((newSocketFileHandle = nativeAccept(nativeSocketFileHandle, socketType))==-1){
            throw new IOException("Unable to accept on Unix domain socket");
        }
        logger.debug("Accept completed with code " + newSocketFileHandle);
        return new SimpleUnixDomainSocket(newSocketFileHandle, socketType);
    }

    @Override
    public SimpleUnixDomainSocketOutputStream getOutputStream() {
        if (socketType == SOCK_DGRAM){
            throw new UnsupportedOperationException("Datagram sockets are unidirectional. Use a SOCK_STREAM type and/or use accept() to get a new socket for each connection.");
        } else {
            return outputStream;
        }
    }
}
