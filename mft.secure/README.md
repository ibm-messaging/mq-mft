# IBM MQ Managed File Transfer - Encrypt/Decrypt Exit

## Introduction
IBM MQ Managed File Transfer (IBM MQ MFT) transfers files between systems over an IBM MQ network in a managed and auditable way, regardless of file size or the operating systems used. Using IBM MQ MFT customers can build a customized, secure, scalable and automated file transfer solutions. More details on IBM MQ MFT are here https://www.ibm.com/support/knowledgecenter/SSFKSJ_9.0.0/com.ibm.wmqfte.doc/wmqfte_intro.htm

IBM MQ MFT can securely transfer files over a TLS enabled MQ network. IBM MQ Advanced Message Security (AMS) can also be used for more protection to ensure confidential file data is not visible when transfer of data between two IBM MQ Managed File Transfer agents is interrupted. In both of the scenarios, TLS enabled MQ network or AMS, after completion of the transfer, file at destination end point will be in a decrypted format. However there are scenarios where it is required to encrypt file(s) at source before they are transferred to a destination agent. The encrypted files are then either decrypted at destination or kept in encrypted form to be decrypted at a later time. Such encrypted files can be transferred over a TLS enabled secure as well as non-secure MQ network.

IBM MQ MFT provides a very useful feature wherein functionality of MFT agents can be extended by developing custom code (a.k.a Exits). This article decribes an exit that extends MFT functionality by providing the ability to encrypt file(s) at source end point and decrypt those file(s) at destination endpoint using PGP protocol. This exit uses BouncyCastle PGP implementation and is based on sample described here: https://github.com/damico/OpenPgp-BounceCastle-Example/blob/master/src/org/jdamico/bc/openpgp/utils/PgpHelper.java

## Building Exit
The following environment was used to build exit:

JDK: v1.8

MFT: com.ibm.wmqfte.exitroutines.api.jar (v9.0. Earlier version of the API can also be used)

BouncyCastle: bcpg-jdk15on-157.jar,bcpg-jdk15on-157.jar, bcpkix-jdk15on-157.jar, bcprov-ext-jdk15on-157.jar, bcprov-jdk15on-157.jar, bctls-jdk15on-157.jar. Download these from BouncyCastle website.

Eclipse: Neon

Source files:
1) CryptDecryptUtil.java - PGP Encrypt/Decrypt files using BouncyCastle API.
2) EncryptAtSource - Implements SourceTransferStartExit exit for encrypting files before transfer begins.
3) DecryptAtDestination - Implements DestinationTransferEndExit for decrypting files at destination agent.
4) Trace.java - Implements simple tracing with System.out.println. Trace will be written to agent's output0.log file.
5) CryptDecryptException.java - Simple extension of Exception class.

After compiling, it would be useful to jar the class files, for example mft.secure.jar.

## Configuration for the using exits
1) Create source and destination agents. 
2) The exit uses a configuration file, decenc.properties, for enabling/disabling decryption/encryption. The file must be located along side the mft.secure.jar under agent's exit directory.
   The contents of decenc.properties for source agent:
   
   encryptAtSource=true <- Enable encryption of files at source agent. If false, no encryption will be done
   
   enableDebugLog=true  <- Write trace log. If false, no logs are written except for any exceptions
   
   publicKeyFile=<location of public key file> for example /PGPKeys/publickey.txt

   The contents of decenc.properties for destination agent
   
   decryptAtDestination=true <- Enable decryption of files at destination agent. If false, no decryption will be done
   
   enableDebugLog=true       <- Write trace log. If false, no logs are written except for any exceptions.
   
   privateKeyFile=<location of private key file> for example /PGPKeys/privatekey.txt
   
   For decryption, exit requires a passphrase also. The passphrase, in plain text, must be provided via file called "cryptdecrypt.pwd" located in logged in user's home directory. This is to ensure passphrase is secured to some extent.
   
   Sample contents of cryptdecrypt.pwd file:
   passphrase=passw0rd
   
3) After exit is compile and jared, copy the mft.secure.jar to exit directory of both source and destination agents.
4) Copy decenc.properties file containing required configuration properties to exit directory of source and destination agents.
5) Generate Private and Public Keys. They can be generated from igolder website @https://www.igolder.com/PGP/generate-key/. Follow the instructions on the website and place the public and private key files in suitable directories. Update the decenc.properties file with correct path to these key files.
6) Add the following properties to agent.properties file of source and destination agent as below:
   Source agent:
   sourceTransferStartExitClasses=mft.secure.EncryptAtSource
   exitClassPath=/bcpgp/bcpg-jdk15on-157.jar;/bcpgp/bcpkix-jdk15on-157.jar;/bcpgp/bcprov-ext-jdk15on-157.jar;/bcpgp/bcprov-jdk15on-157.jar;/bcpgp/bctls-jdk15on-157.jar
   
   Destination agent:
   destinationTransferEndExitClasses=mft.secure.DecryptAtDestination
   exitClassPath=/bcpgp/bcpg-jdk15on-157.jar;/bcpgp/bcpkix-jdk15on-157.jar;/bcpgp/bcprov-ext-jdk15on-157.jar;/bcpgp/bcprov-jdk15on-157.jar;/bcpgp/bctls-jdk15on-157.jar

   Note: Change path BouncyCastle jars appropriately.

7) Start source and destination agents.
8) Run transfer. 
   Example: fteCreateTransfer -rt -1 -sa SRC -sm SRCQM -da DEST -dm DESTQM -sd delete -de overwrite -dd "/dest/files/" "/src/files"

