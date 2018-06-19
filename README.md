# Welcome to PhotonchainJ

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ethereum/ethereumj?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/ethereum/ethereumj.svg?branch=master)](https://travis-ci.org/ethereum/ethereumj)
[![Coverage Status](https://coveralls.io/repos/ethereum/ethereumj/badge.png?branch=master)](https://coveralls.io/r/ethereum/ethereumj?branch=master)


# About
PhotonchainJ is a pure-Java implementation of the PhotonChain protocol. For high-level information about PhotonChain and its goals, visit [photonchain.vip](http://photonchain.vip).

# Running PhotonchainJ

```
 find springboot application class, run it.
```

##### Building an executable JAR
```
git clone https://github.com/photonchain/java-photonchain
cd java-photonchain
cp photon-chain/photon-chain-interfaces/src/main/resources/example.properties photon-chain/photon-chain-interfaces/src/main/resources/testnet.properties
vim photon-chain/photon-chain-interfaces/src/main/resources/testnet.properties # edit to your needs
./mvn clean package -DskipTests
java -jar photon-chain/photon-chain-interfaces/photon-chain-interfaces-1.0-SNAPSHOT.jar
```

##### Running from command line:
```
> git clone https://github.com/photonchain/java-photonchain
> cd java-photonchain
> ./mvn spring-boot:run
```


##### Importing project to IntelliJ IDEA: 
```
> git clone https://github.com/photonchain/java-photonchain
> cd java-photonchain
> import project
```
  IDEA: 
* File -> New -> Project from existing sources…
* Select java-photonchain/pom.xml
* Dialog “Import Project from maven”: press “OK”
* After building run photonchain `com.photon.photonchain.InesvchainApplication`, one of `org.photonchain.samples.*` or create your own main. 

# Configuring PhotonchainJ

For reference on all existing options, their description and defaults you may refer to the default config `photonchainj.conf` (you may find it in either the library jar or in the source tree `photon-chain/photon-chain-interfaces/src/main/resources`) 
To override needed options you may use one of the following ways: 
* put your options to the `<working dir>/config/photonchainj.conf` file
* put `user.conf` to the root of your classpath (as a resource) 
* put your options to any file and supply it via `-Dphotonchainj.conf.file=<your config>`
* programmatically by using `SystemProperties.CONFIG.override*()`
* programmatically using by overriding Spring `SystemProperties` bean 

Note that don’t need to put all the options to your custom config, just those you want to override. 

# Special thanks
YourKit for providing us with their nice profiler absolutely for free.

YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.

![YourKit Logo](https://www.yourkit.com/images/yklogo.png)

# Contact
Chat with us .

# License
photonchainj is released under the [LGPL-V3 license](LICENSE).

