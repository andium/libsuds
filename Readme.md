# libSUDS

libSUDS is a set Java classes providing Unix Domain Sockets, using JNI wrappers around a very simple C implementation.

It provides a simple **blocking** socket which is appropriate for small scale uses of socket architecture. It is advised that if you want to use high performance, high throughput, non-blocking Unix domain sockets, that you investigate the use of Netty (which uses epoll / kqueue to allow non-blocking IO) or libEvent.

Requires Java 7 or above. 

## Releases

Check the releases tab to grab the latest version. 

The release JAR will contain pre-compiled native libraries for the following platforms/archs
64-bit macOS
64-bit Linux-x86_64
32-bit Linux-ARM

This adds a total of 85kb to the resulting release but covers the majority of uses cases for the library. If your needs differ, please open an issue and let us know. 

### Maven / Gradle

We are currently awaiting our namespace to be created in Maven Central. Once it has been created, this section will be updated with the appropriate information.

## Dependencies 

Apart from the native library, libSUDS contains a single dependency, which is the SLF4J logging facade. libSUDS only provides the API jar, allowing callers to provide their own logging implementation. 

## Usage

**Server**  
Below is a quick way of setting up a Unix domain socket server using libSUDS.
```
int conn = 3;
boolean running = true;
SimpleUnixDomainSocketServer server;
ExecutorService mainService = Executors.newSingleThreadExecutor();
ExecutorService connService = Executors.newFixedThreadPool(conn);
 
mainService.submit(()->{
    server = new SimpleUnixDomainSocketServer("socket.sock", SimpleUnixDomainSocket.SOCK_STREAM, conn);
    while (running){
        try {
            SimpleUnixDomainSocket socket = server.accept(); //blocking
            connService.submit(()->{
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())){
                    String message;
                    while ((message = reader.readLine()) != null){
                        System.out.println(message);
                    }
                }
            });
        } catch (IOException e){
            // handle exception
        }
    }
});
```

**Client**  
This example demonstrates a writing a single 'Hello world!' message to socket and then disconnecting. 
```
try (SimpleUnixDomainSocketClient client = new SimpleUnixDomainSocketClient("sock.sock", SOCK_STREAM);
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))){
        writer.write("Hello world!");
        writer.newLine();
        writer.flush();
    } catch (Exception e){
        // handle exception
    }
}
```

## Compiling

In order to compile the native code into the appropriate target, please see the Readme in /src/main/c/.

## Acknowledgments 
libSUDS picks up from JUDS, and by extension from J-BUDS - both of which are licenses under the LGPLv2.1 as well.
libSUDS uses the MIT licensed Dockcross scripts in order to cross compile the native code. 

## License
This library is free software; you can redistribute it and/or modify it under the terms of version 2.1 of the GNU Lesser General Public License as published by the Free Software Foundation.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details:

http://www.opensource.org/licenses/lgpl-license.html http://www.gnu.org/copyleft/lesser.html

To obtain a written copy of the GNU Lesser General Public License, please write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA