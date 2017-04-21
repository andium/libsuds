#include "SimpleUnixDomainSocket.h"

#include <jni.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/un.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <errno.h>

// Macro to print out an error
#define ASSERTNOERR(cond, msg, jenv) do { \
    if (cond){ fprintf(stderr, "[%d] ", errno); perror(msg); throwException(jenv, msg, strerror(errno)); return -1; }} while(0)


void throwException(JNIEnv *env, char* msg, char* detail){
    // DOOLEY PLEASE DON'T JUDGE ME
    size_t msg_len = strlen(msg) + strlen(detail) + 1;
    char *message = (char *) malloc(msg_len);
    strncat(message, msg, strlen(msg));
    strncat(message, detail, strlen(detail));

    jclass Exception = (*env)->FindClass(env, "java/lang/Exception");
    (*env)->ThrowNew(env, Exception, message);

    free(message);
}

/*
*  In our Java class, we can specify whether we want a unidirectional (SOCK_DGRAM) or bidirectional (SOCK_STREAM) socket
*  Using the constant values 0 and 1 to represent this; SOCK_TYPE replaces them with the appropriate socket macro
*/
#define SOCK_TYPE(type) ((type)==0? SOCK_DGRAM : SOCK_STREAM)

// Just making sure the SUN_LEN macro has been defined. This just returns the actual length of an initialised sockaddr_un
#ifndef SUN_LEN
#define SUN_LEN(su) \
            (sizeof(*(su)) - sizeof((su)->sun_path) + strlen((su)->sun_path))
#endif

// Initialises and checks the socket address
socklen_t sockaddr_init(const char* socketFile, struct sockaddr_un* sa){
    socklen_t salen;

    bzero(sa, sizeof(struct sockaddr_un));
    sa->sun_family = AF_UNIX;
    strcpy(sa->sun_path, socketFile);

    salen = SUN_LEN(sa);
    return salen;
}


// nativeCreate - only accepts a single connection. Use listen() if you want multiple connections
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeCreate(JNIEnv * jEnv, jclass jClass, jstring jSocketFile, jint jSocketType){
    int s; // socket file handle
    struct sockaddr_un sa;
    const char *socketFile = (*jEnv)->GetStringUTFChars(jEnv, jSocketFile, NULL);

    socklen_t salen = sockaddr_init(socketFile, &sa);

    // create the socket
    s = socket(PF_UNIX, SOCK_TYPE(jSocketType), 0);
    ASSERTNOERR(s == -1, "nativeCreate: socket", jEnv);

    // call to unlink to ensure that nothing else is using the socket
    // TODO - change the java workflow so this isn't necessary. Maybe do the check in Java
    unlink(socketFile);
    // bind to the socket - this is where the actual socket file is created
    ASSERTNOERR(bind(s, (struct sockaddr *)&sa, salen) == -1, "nativeCreate: bind", jEnv);
    // if you're bidirectional, call accept() now
    if (SOCK_TYPE(jSocketType) == SOCK_STREAM){
        ASSERTNOERR(listen(s,0) == -1, "nativeCreate: listen", jEnv);
        s = accept(s, (struct sockaddr*)&sa, &salen);
        ASSERTNOERR(s == -1, "nativeCreate: accept", jEnv);
    }

    (*jEnv)->ReleaseStringUTFChars(jEnv, jSocketFile, socketFile);

    return s;
}

// nativeListen
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeListen(JNIEnv *jEnv, jclass jClass, jstring jSocketFile, jint jSocketType, jint jBacklog){
    int s;
    struct sockaddr_un sa;
    const char *socketFile = (*jEnv)->GetStringUTFChars(jEnv, jSocketFile, NULL);

    socklen_t salen = sockaddr_init(socketFile, &sa);

    // create the socket
    s = socket(PF_UNIX, SOCK_TYPE(jSocketType), 0);
    ASSERTNOERR(s == -1, "nativeListen: socket", jEnv);

    unlink(socketFile);
    // bind to the socket - this is where the actual socket file is created
    ASSERTNOERR(bind(s, (struct sockaddr *)&sa, salen) == -1, "nativeListen: bind", jEnv);
    if (SOCK_TYPE(jSocketType) == SOCK_STREAM) {
        ASSERTNOERR(listen(s, jBacklog) == -1, "nativeListen: listen", jEnv);
    }

    (*jEnv)->ReleaseStringUTFChars(jEnv, jSocketFile, socketFile);

    // return the listening socket file handle
    return s;
}

// nativeAccept
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeAccept(JNIEnv * jEnv, jclass jClass, jint jSocketFileHandle, jint jSocketType){
    int s = -1; // socket file handle

    ASSERTNOERR(jSocketFileHandle == -1, "nativeAccept: socket", jEnv);
    if (SOCK_TYPE(jSocketType) == SOCK_STREAM) {
        s = accept(jSocketFileHandle, NULL, 0);
        ASSERTNOERR(s == -1, "nativeAccept: accept", jEnv);
    }

    // return the socket file handle
    return s;
}

// nativeOpen
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeOpen(JNIEnv * jEnv, jclass jClass, jstring jSocketFile, jint jSocketType){
    int s; // socket file handle
    struct sockaddr_un sa;
    const char *socketFile =
        (*jEnv)->GetStringUTFChars(jEnv, jSocketFile, NULL);
    socklen_t salen = sockaddr_init(socketFile, &sa);

    s = socket(PF_UNIX, SOCK_TYPE(jSocketType), 0);
    ASSERTNOERR(s == -1, "nativeOpen: socket", jEnv);

    if (connect(s, (struct sockaddr *)&sa, salen) == -1) {
	perror("nativeOpen: connect");
	int close_ = close(s);
	ASSERTNOERR(close_ == -1, "nativeOpen: close connect error socket", jEnv);
	return -1;
    }

    (*jEnv)->ReleaseStringUTFChars(jEnv, jSocketFile, socketFile);

    // return the socket file handle
    return s;
}

// nativeRead
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeRead(JNIEnv * jEnv, jclass jClass, jint jSocketFileHandle, jbyteArray jbarr, jint off, jint len){
    ssize_t count;
    jbyte *cbarr = (*jEnv)->GetByteArrayElements(jEnv, jbarr, NULL);
    ASSERTNOERR(cbarr == NULL, "nativeRead: GetByteArrayElements", jEnv);

    // read up to len bytes from the socket into the buffer
    count = read(jSocketFileHandle, &cbarr[off], len);
    ASSERTNOERR(count == -1, "nativeRead: read", jEnv);

    (*jEnv)->ReleaseByteArrayElements(jEnv, jbarr, cbarr, 0);

    // return the number of bytes read
    return count;
}

// nativeWrite
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeWrite(JNIEnv * jEnv, jclass jClass, jint jSocketFileHandle, jbyteArray jbarr, jint off, jint len){
    ssize_t count;
    jbyte *cbarr = (*jEnv)->GetByteArrayElements(jEnv, jbarr, NULL);
    ASSERTNOERR(cbarr == NULL, "nativeWrite: GetByteArrayElements", jEnv);

    // try to write len bytes from the buffer to the socket
    count = write(jSocketFileHandle, &cbarr[off], len);
    ASSERTNOERR(count == -1, "nativeWrite: write", jEnv);

    (*jEnv)->ReleaseByteArrayElements(jEnv, jbarr, cbarr, JNI_ABORT);

    // return the number of bytes written
    return count;
}

// nativeTimeout
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeTimeout(JNIEnv * jEnv, jclass jClass, jint jSocketFileHandle, jint milis){
    struct timeval timeout;
    timeout.tv_sec = milis / 1000;
    timeout.tv_usec = (milis % 1000) * 1000;
    return setsockopt(jSocketFileHandle, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(timeout));
}

// nativeClose
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeClose(JNIEnv * jEnv, jclass jClass, jint jSocketFileHandle){
    shutdown(jSocketFileHandle, SHUT_RDWR);
    return close(jSocketFileHandle);
}

// nativeCloseInput
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeCloseInput(JNIEnv * jEnv, jclass jClass, jint jSocketFileHandle){
    // close the socket input stream
    return shutdown(jSocketFileHandle, SHUT_RD);
}

// nativeCloseOutput
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeCloseOutput(JNIEnv * jEnv, jclass jClass, jint jSocketFileHandle){
    // close the socket output stream
    return shutdown(jSocketFileHandle, SHUT_WR);
}

// nativeUnlink
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeUnlink(JNIEnv * jEnv, jclass jClass, jstring jSocketFile){
    int ret;
    const char *socketFile =
        (*jEnv)->GetStringUTFChars(jEnv, jSocketFile, NULL);

    // unlink socket file
    ret = unlink(socketFile);

    (*jEnv)->ReleaseStringUTFChars(jEnv, jSocketFile, socketFile);

    return ret;
}