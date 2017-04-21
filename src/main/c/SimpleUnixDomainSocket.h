/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_andium_unix_socket_SimpleUnixDomainSocket */

#ifndef _Included_com_andium_unix_socket_SimpleUnixDomainSocket
#define _Included_com_andium_unix_socket_SimpleUnixDomainSocket
#ifdef __cplusplus
extern "C" {
#endif
#undef com_andium_unix_socket_SimpleUnixDomainSocket_SOCK_DGRAM
#define com_andium_unix_socket_SimpleUnixDomainSocket_SOCK_DGRAM 0L
#undef com_andium_unix_socket_SimpleUnixDomainSocket_SOCK_STREAM
#define com_andium_unix_socket_SimpleUnixDomainSocket_SOCK_STREAM 1L
/*
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeCreate
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeCreate
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeListen
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeListen
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeAccept
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeAccept
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeOpen
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeOpen
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeRead
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeRead
  (JNIEnv *, jclass, jint, jbyteArray, jint, jint);

/*
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeWrite
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeWrite
  (JNIEnv *, jclass, jint, jbyteArray, jint, jint);

/*
 * update
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeTimeout
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeTimeout
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeClose
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeClose
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeCloseInput
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeCloseInput
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeCloseOutput
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeCloseOutput
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_andium_unix_socket_SimpleUnixDomainSocket
 * Method:    nativeUnlink
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_andium_unix_socket_SimpleUnixDomainSocket_nativeUnlink
  (JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif
#endif