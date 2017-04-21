# SimpleUnixDomainSocket        

This folder contains the native code to run a Unix Domain Socket from within Java. Two C files (`SimpleUnixDomainSocket.c` and `SimpleUnixDomainSocket.h`) are contained within this directory, along with some Dockerfiles containing cross compilers for different platforms (courtesy of the [Dockercross Project](https://github.com/dockcross/dockcross))

We're using Docker containers, with preconfigured cross compiler toolchains to compile the code into platform specific libraries. The follwing platforms have been defined:
* linux_x86_x64
* linux ARMv7
* macOS

The code is compatible with OS X, however due to licensing terms, we can't provide an OS X cross compiler. However, if you're compiling the code on a mac, the flags should all be set correctly. 



## Compiling

If you'd just like to compile for your target platform, and not cross compile for other platforms, then you only need to include the JNI C headers. E.g.

`gcc -g -O2 -dynamiclib -fPIC -I $JAVA_HOME/include -I $JAVA_HOME/include/darwin/ SimpleUnixDomainSocket.c -o libsuds-darwin-x86_64.dylib`

The output library should be named using the following format:

`libsuds`-`<platform>`-`<arch>`.`<ext>`

Platform can either be `darwin` or `linux`  
Arch can either be `x86_64` or `arm`  
Ext can either be `dylib` or `so`

You are welcome to add more arch or platform types, but you'll need to add the logic in `SimpleUnixDomainSocket.java` to load the appropriate library.

### Cross Compiling
You will need to create the appropriate Docker containers in order to use the Dockcross scripts to cross compile. 

First, you will need to build the base image - this is the `Dockerfile` found in the root of the Docker folder.   
`cd src/main/c/docker`  
`docker build -t andium/base`

Then you will be need to pipe the results of running a target `Dockerfile` into a bash script  
`docker run --rm linux-armv7 > ./armv7`  
`chmod +x ./armv7`

You can then pass in `$CC` to the script and compile, for example:  
`./armv7 bash -c "$CC -shared -I$JAVA_HOME/include -I$JAVA_HOME/include/linux SimpleUnixDomainSocket.c -o libsuds-linux-arm.so"`

See [the Dockcross github repo](https://github.com/dockcross/dockcross) for more detailed instructions on usage of the docker containers.

All artifacts should be placed in the `src/main/resources/` folder, as this is where the library loader will check.

## Windows support 

Since Windows doesn't have the concept of a Domain Socket, this won't run on Windows in it's current state. There are two ways we can approach this:
* Wrap libevent in JNI and use libEvent for event processing on local only sockets (`AF_LOCAL` rather than `AF_UNIX`)
* Detect when we're running on Windows and bind a local socket to 127.0.0.1 which won't accept any outside connections. Currently unsure whether this approach will be viable for file based sockets - need to investigate the WinSock API


## License and Credit
Everything contained within the docker folder is licensed under MIT (see LICENSE). All credit goes to Steeve Morin, Rob Burns, Matthew McCormick and the [Dockcross project](https://github.com/dockcross/dockcross)