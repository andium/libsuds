package com.andium.unix.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;

/**
 * Base class for running Unix Domain Sockets in Java. This class detects the platform / arch it is running on and
 * attempts to load the correct library from the /resources folder. In the event that the library is unable to be
 * loaded, an exception is thrown.
 *
 * This class can represent either kind of socket - Datagram or Stream. Datagram sockets are unidirectional, whereas
 * Stream sockets are bidirectional. Attempting to get an OutputStream on a Datagram SimpleUnixDomainSocketServer, or
 * an InputStream on a Datagram SimpleUnixDomainSocketClient will result in an exception.
 *
 * This socket implements Closable so you can use SimpleUnixDomainSocket within a try-with-resource - this is particularly
 * ideal for one-shot type of messages where we want to quickly connect to a socket, send our message and then disconnect.
 * We don't have to worry about any lingering connections / streams.
 */
public class SimpleUnixDomainSocket implements Closeable{
    // Load the native library depending on platform

    /**
     * The constant SOCK_DGRAM - this represents a Datagram socket (Unidirectional).
     */
    public static final int SOCK_DGRAM = 0;
    /**
     * The constant SOCK_STREAM - this represents a Stream socket (Bidirectional).
     */
    public static final int SOCK_STREAM = 1;

    private static final String LIBNAME = "libsuds";
    private static final Logger staticLogger = LoggerFactory.getLogger(SimpleUnixDomainSocket.class);

    /**
     * The Jar url.
     */
    static URL jarURL;

    /**
     * Attempts to load the native library with the following format:
     * libsuds-(platform)-(arch).(ext)
     * Will throw an exception if it doesn't load successfully.
     */
    static {
        String target = getTarget();
        staticLogger.info("Attempting to load " + target);

        try {
            loadLib(target);
        } catch (IOException e){
            staticLogger.error("Unable to load target " + target, e);
        }
    }

    /**
     * Will make a local copy of the native library to be loaded. It will always overwrite the file if it
     * already exists.
     *
     * TODO - Check to see if the file already exists and skip making a copy
     * TODO - Alternative options to load in the native library (via system properties, etc)
     *
     * @param target libsuds-(platform)-(arch).(ext)
     * @throws IOException
     */
    private static void loadLib(String target) throws IOException{
        try (InputStream in = SimpleUnixDomainSocket.class.getClassLoader().getResourceAsStream(target)) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(target))) {
                byte[] buffer = new byte[2048];
                for (; ; ) {
                    int n = in.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    out.write(buffer, 0, n);
                }
            }
        }

        File libFile = new File(target);

        if (libFile.exists()){
            staticLogger.debug("Lib exists loading");
            System.load(libFile.getCanonicalPath());
            staticLogger.debug("Lib loaded");
        } else {
            throw new UnsatisfiedLinkError("Unable to load library " + target);
        }
    }

    private static String getTarget(){
        String platform = getPlatform();
        // If we're running on Mac, return appropriate ext - otherwise assume Linux
        String ext = platform.equals("darwin") ? "dylib" : "so";
        String arch = getArch();

        return String.format("%s-%s-%s.%s", LIBNAME, platform, arch, ext);
    }

    private static String getArch(){
        String arch = System.getProperty("os.arch").toLowerCase();

        if (arch.equals("amd64") || arch.equals("x86_64")){
            return "x86_64";
        }

        return arch;
    }

    private static String getPlatform(){
        String platform = System.getProperty("os.name").toLowerCase();
        if (platform.equals("mac os x")){
            return "darwin";
        }

        return platform;
    }

    // Native methods implemented in the Unix domain socket C library

    /**
     * Call to the native Socket create. This will also call bind(), listen() and accept()
     * on the created socket object.
     *
     * @param socketFile the socket file
     * @param socketType the socket type
     * @return the int
     */
    protected native static int nativeCreate(String socketFile, int socketType);

    /**
     * Call to the native socket listen. This creates a new socket, but it also calls
     * bind() and listen()
     *
     * @param socketFile the socket file
     * @param socketType the socket type
     * @param backlog    the backlog
     * @return the int
     */
    protected native static int nativeListen(String socketFile, int socketType, int backlog);

    /**
     * Call to the native socket accept. This takes an existing socket handle and calls
     * accept() on the socket.
     *
     * @param nativeSocketFileHandle the native socket file handle
     * @param socketType             the socket type
     * @return the int
     */
    protected native static int nativeAccept(int nativeSocketFileHandle, int socketType);

    /**
     * Call to the native socket open(). This will create a new socket object and then
     * call connect() on the socket.
     *
     * @param socketFile the socket file
     * @param socketType the socket type
     * @return the int
     */
    protected native static int nativeOpen(String socketFile, int socketType);

    /**
     * Call to the native socket read(). It will read len bytes from the
     * socket into the buffer and return the number of bytes read.
     *
     * @param nativeSocketFileHandle the native socket file handle
     * @param b                      the b
     * @param off                    the off
     * @param len                    the len
     * @return the int
     */
    protected native static int nativeRead(int nativeSocketFileHandle,
                                           byte[] b, int off, int len);

    /**
     * Call to the native socket write. It will try and write len bytes
     * from the buffer to the socket and return the number of bytes written.
     *
     * @param nativeSocketFileHandle the native socket file handle
     * @param b                      the b
     * @param off                    the off
     * @param len                    the len
     * @return the int
     */
    protected native static int nativeWrite(int nativeSocketFileHandle,
                                            byte[] b, int off, int len);

    /**
     * Call to the native socket setsockopt(). This will set the time out of the provided
     * socket handle to that of the milis param.
     *
     * @param nativeSocketFileHandle the native socket file handle
     * @param milis                  the milis
     * @return the int
     */
    protected native static int nativeTimeout(int nativeSocketFileHandle, int milis);

    /**
     * Call to the native socket close() and shutdown(SHUT_RDWR);
     *
     * @param nativeSocketFileHandle the native socket file handle
     * @return the int
     */
    protected native static int nativeClose(int nativeSocketFileHandle);

    /**
     * Call to the native socket shutdown(SHUT_RD);
     *
     * @param nativeSocketFileHandle the native socket file handle
     * @return the int
     */
    protected native static int nativeCloseInput(int nativeSocketFileHandle);

    /**
     * Call to the native socket shutdown(SHUT_WR);
     *
     * @param nativeSocketFileHandle the native socket file handle
     * @return the int
     */
    protected native static int nativeCloseOutput(int nativeSocketFileHandle);

    /**
     * Call to the native socket unlink().
     *
     * @param socketFile the socket file
     * @return the int
     */
    protected native static int nativeUnlink(String socketFile);

    // end native methods


    /**
     * The Native socket file handle.
     */
    protected int nativeSocketFileHandle;

    /**
     * The Socket file.
     */
    protected String socketFile;
    /**
     * The Socket type (either SOCK_DGRAM or SOCK_STREAM).
     */
    protected int socketType;

    /**
     * The Input stream for the socket.
     */
    protected SimpleUnixDomainSocketInputStream inputStream;
    /**
     * The Output stream for the socket.
     */
    protected SimpleUnixDomainSocketOutputStream outputStream;

    /**
     * Instantiates a new Simple unix domain socket. It is preferred not to use this constructor but rather
     * SimpleUnixDomainSocket(int,int);
     */
    protected SimpleUnixDomainSocket()
    {
        // default constructor
    }

    /**
     * Instantiates a new Simple unix domain socket with the provided file handle and socket type.
     *
     * @param pSocketFileHandle the p socket file handle
     * @param pSocketType       the p socket type
     */
    protected SimpleUnixDomainSocket(int pSocketFileHandle, int pSocketType){
        this.nativeSocketFileHandle = pSocketFileHandle;
        this.socketType = pSocketType;
        socketFile = null;

        inputStream = new SimpleUnixDomainSocketInputStream();
        if (socketType == SOCK_STREAM){
            outputStream = new SimpleUnixDomainSocketOutputStream();
        }
    }

    /**
     * Gets input stream.
     *
     * @return the input stream
     */
    public SimpleUnixDomainSocketInputStream getInputStream() {
        return inputStream;
    }

    /**
     * Gets output stream.
     *
     * @return the output stream
     */
    public SimpleUnixDomainSocketOutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Sets the socket timeout.
     *
     * @param timeout the timeout
     * @throws IOException the io exception
     */
    public void setSoTimeout(int timeout) throws IOException {
        if (nativeTimeout(nativeSocketFileHandle, timeout) == -1){
            throw new IOException("Unable to configure socket timeout");
        }
    }

    /**
     * Closes the socket, it also checks to see whether the associated streams are closed too.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        nativeClose(nativeSocketFileHandle);
    }

    /**
     * Unlinks the socket file. It is important to do this before your application ends as you may leave a lingering
     * link on the socket file.
     */
    public void unlink(){
        if (socketFile != null){
            nativeUnlink(socketFile);
        }
    }

    /**
     * The type Simple unix domain socket input stream. It behaves as one would expect an inputstream would, except it pipes
     * calls through nativeRead(). It ensures that all requested bytes to be read have been read via nativeRead() - otherwise
     * it throws an IOException.
     */
    protected class SimpleUnixDomainSocketInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            byte[] b = new byte[1];
            int count = nativeRead(nativeSocketFileHandle, b, 0, 1);
            if (count == -1)
                throw new IOException();
            return count > 0 ? (int) b[0] & 0xff : -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            int count = nativeRead(nativeSocketFileHandle, b, off, len);
            if (count == -1)
                throw new IOException();
            return count > 0 ? count : -1;
        }

        // Closes the socket input stream
        public void close() throws IOException {
            nativeCloseInput(nativeSocketFileHandle);
        }
    }

    /**
     * The type Simple unix domain socket output stream. It behaves as one would expect an outputstream would - except
     * it writes via nativeWrite(). It checks that the call to nativeWrite has written all the bytes, otherwise it
     * throws an IOException.
     */
    protected class SimpleUnixDomainSocketOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            byte[] data = new byte[1];
            data[0] = (byte) b;
            if (nativeWrite(nativeSocketFileHandle, data, 0, 1) != 1)
                throw new IOException("Unable to write to Unix domain socket");
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if ((off < 0) || (off > b.length) || (len < 0)
                    || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
            if (nativeWrite(nativeSocketFileHandle, b, off, len) != len)
                throw new IOException("Unable to write to Unix domain socket");
        }

        // Closes the socket output stream
        public void close() throws IOException {
            nativeCloseOutput(nativeSocketFileHandle);
        }
    }
}
