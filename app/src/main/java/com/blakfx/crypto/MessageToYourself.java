package com.blakfx.crypto;

import java.math.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.inf.*;
import net.sourceforge.argparse4j.impl.*;

/**
 * Allows to send a message to yourself, using the Java Helix wrappers.
 * Will attempt to load the HelixForJava library, and if it succeeds keep a single instance of it across all MessageToYourself objects.
 * The "heavy lifting" is done by method <code>test_load</code>, with <code>main</code> simply parsing CLI arguments and calling it.
 */
public class MessageToYourself {
	static {
		try {
			System.loadLibrary("HelixForJAVA");
		} catch (UnsatisfiedLinkError e) {
			System.err.println("Native code library failed to load. See the chapter on Dynamic Linking Problems in the SWIG Java documentation for help.\n" + e);
			System.exit(1);
		}
	}
	
	/**
	 * Utility function to convert a SWIG promise to BigInteger
	 * @param aPromise The invokeStatus_t with the value of the promise to convert
	 * @return BigInteger with the promise value
	 */
	public static BigInteger convertP2B(invokeStatus_t aPromise) {
		return BigInteger.valueOf( aPromise.swigValue() );
	}

	/**
	 * Utility function to write bytes to a file
	 * @param aFileName The name of the file to write to
	 * @param fileContents The bytes to write into the file
	 * @return true if successful, false if not successful
	 */
	private static boolean writeToFile(final String aFileName, byte[] fileContents) {
		boolean result = false;
		try {
			final Path fullPath = Paths.get(aFileName);
			Files.write(fullPath, fileContents);
			result = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }		
		return result;
	}

	/**
	 * Utility function to read bytes from a file
	 * @param aFileName The name of the file to read from
	 * @return The bytes read from the file if successful, or null if not successful
	 */
	private static byte[] readFromFile(final String aFileName) {
		byte[] fileContents = null;
		try {
			final Path fullPath= Paths.get(aFileName);
			fileContents = Files.readAllBytes(fullPath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
		return fileContents;
	}

	/**
	 * Entry point of the program, will parse arguments from CLI, and call <code>test_load</code> with them
	 * @param args The CLI args passed in, can be one of the following: <code>server</code>, 
	 * <code>port</code>, <code>username</code>, <code>message</code>, <code>file</code>
	 */
	public static void main(String[] args) {		
		ArgumentParser parser = ArgumentParsers.newFor("test_load").build()
						.defaultHelp(true)
						.description("Demo Helix library encryption and decryption");

		parser.addArgument("-s", "--server").required(true).nargs("?").type(String.class)
                .help("IP address of Helix Key Server");
		parser.addArgument("-p", "--port").required(true).nargs("?").type(Integer.class).setDefault(8886)
				.choices(Arguments.range(1025, 65535))
                .help("Port to connect to Helix Key Server [default 8886] [valid range between 1,025-65,535]");
		parser.addArgument("-u", "--username").required(true).nargs("?").type(String.class).setDefault("HELIX-JAVA")
                .help("Handle of the Recipient User");
		parser.addArgument("-m", "--message").required(false).nargs("?").type(String.class)
                .help("Message to encrypt and decrypt");

		parser.addArgument("-f", "--file").required(false).nargs("?").type(String.class)
                .help("File with contents to encrypt and decrypt");
	
		Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
			
		final String server = ns.getString("server");
		int port = ns.getInt("port");
		final String userName = ns.getString("username"); 		//"HELIX-JAVA"; //"local";
		final String PLAINTEXT_MESSAGE = ns.getString("message"); //"Hello HELIX Crypto Module from Java Wrapper";
		final String filename = ns.getString("file");
			
		test_load(server, port, userName, PLAINTEXT_MESSAGE, filename);
	}

	/**
	 * Performs all Helix related tasks involved with encryption and decryption of a simple message in a file.
	 * Source code is commented with each of the steps in order to achieve both goals
	 * @param server The IP address of the Helix Key Server
	 * @param port The port of the Helix Key Server
	 * @param userName The username of the recipient/sender
	 * @param PLAINTEXT_MESSAGE The contents of a message to encrypt/decrypt
	 * @param aFileName The name of a file to encrypt/decrypt
	 */
	private static void test_load(String server, int port, String userName, String PLAINTEXT_MESSAGE, String aFileName) {
		System.out.printf("HELIX_INTERFACE_VERSION = %d%n", helix.HELIX_INTERFACE_VERSION);
	
		// System.out.printf("Setting log level to: %d%n", logLevel_t.DEBUG_LEVEL.swigValue());
		// helix.logger_setLogLevel(logLevel_t.DEBUG_LEVEL);

		System.out.printf("Loading plain file from: %s%n", aFileName);
		byte[] plainData = readFromFile(aFileName);
		System.out.printf("Loaded as %d bytes%n", plainData.length);
	
		System.out.printf("Starting to connect to Helix key-server at: [%s:%d]%n", server, port);
		
		//////////////////////////// START HELIX MODULE	///////////////////////
		System.out.println("Calling jCrypto_apiStartup");
		final invokeStatus_t promisedStartup = helix.jCrypto_apiStartup(server, port, 0L);
		System.out.printf("Finished jCrypto_apiStartup with return code: %d%n", promisedStartup.swigValue());
				
		if( invokeStatus_t.INVOKE_STATUS_TRUE == promisedStartup ) {
			System.out.println("SUCCESS: Initialised local Helix Crypto Module");
		} else {
			System.err.println("FAILURE: Could not initialise local Helix Crypto Module - abort");
			return;
		}
		
		
		//////////////////////// CONNECT TO HELIX KEY SERVER //////////////////
		System.out.println("Calling jCrypto_serverConnect");
		final invokeStatus_t promisedServerConnect = helix.jCrypto_serverConnect();
		System.out.printf("Finished jCrypto_serverConnect with return code: %d%n", promisedServerConnect.swigValue());
		if( invokeStatus_t.INVOKE_STATUS_TRUE == promisedServerConnect ) {
			System.out.println("SUCCESS: Connected to Helix Key Server");
		} else {
			System.err.println("FAILURE: Could not connect to Helix key Server - abort");
			return;
		}



		//////////// RE-CREATE TEST ACCOUNT ON HELIX KEY SERVER ///////////////
		//final String userName = "CaFeBaBe-JAVA"; //"local";
		//Create a user for the local device (delete if already existing)
		System.out.println("Calling jCrypto_accountDelete");
		final long resultOfUserDelete = helix.jCrypto_accountDelete(userName);
		System.out.printf("Finished jCrypto_accountDelete with return code: %d%n", resultOfUserDelete);

		System.out.println("Calling jCrypto_accountCreate");
		final invokeStatus_t resultOfUserCreate = helix.jCrypto_accountCreate(userName);
		System.out.printf("Finished jCrypto_accountCreate with return code: %d%n", resultOfUserCreate.swigValue());


		//////////////// LOGIN TO HELIX KEY SERVER ////////////////////////////
		System.out.println("Calling jCrypto_accountLogin");
		final invokeStatus_t resultOfUserLogin = helix.jCrypto_accountLogin(userName);
		System.out.printf("Finished jCrypto_accountLogin with return code: %d%n", resultOfUserLogin.swigValue());
		
		
		//////// TEST LOCAL MODULE IS CONNECTED TO HELIX KEY SERVER	///////////
		System.out.println("Checking if we are connected to the Helix Key Server");

		System.out.println("Calling jCrypto_serverIsConnected");
		final invokeStatus_t isConnected = helix.jCrypto_serverIsConnected();	
		System.out.printf("Finished jCrypto_serverIsConnected with return code: %d%n", isConnected.swigValue());

		
				
		//////////////////////////// ENCRYPT CLIENT DATA 	///////////////////
		long ms_timeout = 5000;
		final String remote_name = userName;
		
		System.out.println("Calling jCrypto_simpleSearchForRecipientByName");		
		final BigInteger foundRecipientID = helix.jCrypto_simpleSearchForRecipientByName(remote_name, ms_timeout);
		System.out.printf("Finished jCrypto_simpleSearchForRecipientByName with return code: %d%n", foundRecipientID.longValue());
		
		//helix.jCrypto_waitEvent(foundRecipientID, helix.SERVER_DELAY_RESPONSE_TIME *2);
		
		final promiseStatusAndFlags_t wes2 = helix.jCrypto_waitEventStatus(foundRecipientID);
		System.out.printf("Wait Event Status for 'foundRecipientID' (%d) with return code: %d%n", foundRecipientID.longValue(), wes2.swigValue());
		if (promiseStatusAndFlags_t.PROMISE_DATA_AVAILABLE != wes2) {
			System.out.println("Unknown User, or operation did not complete");
			return;
		}

		final String password = null;  // "H3lix!"; // This should be disabled for "sender-recipient" case
		//final String PLAINTEXT_MESSAGE = "Hello HELIX Crypto Module from Java Wrapper";
		
		System.out.printf("Starting to encrypt original payload data of %d bytes)%n", plainData.length);
			
		// byte[] _dataIn = PLAINTEXT_MESSAGE.getBytes();
		// System.out.println(String.format("in Java before encrypt, payload data (%d)=", _dataIn.length ));
		// for(int i=0; i < _dataIn.length; ++i) {
			// System.out.print(String.format("%02X ", _dataIn[i]));
		// }
		// System.out.println();
		
		String tempString = new String(plainData);		
		//public static java.math.BigInteger jCrypto_encryptStart(java.math.BigInteger user_id, String clearData, long dataSize, String password);
        final BigInteger encryptionHandle = helix.jCrypto_encryptStart(foundRecipientID, tempString, (long)tempString.length(), password);
        helix.jCrypto_waitEvent(encryptionHandle, promiseStatusAndFlags_t.PROMISE_INFINITE.swigValue());

        if (helix.jCrypto_waitEventStatus(encryptionHandle) != promiseStatusAndFlags_t.PROMISE_DATA_AVAILABLE) {
            System.out.println("Encryption Failed - abort");
			return;
        } else {
            System.out.println("SUCCESS: Call to encrypt returned flag of completion");
		}

        byte[] sendersMessageEncrypted = helix.jCrypto_encryptGetOutputData(encryptionHandle);

		// ***************************** write out to file captured encrypted blob ************* //
		final String outFilename = aFileName + "-encrypted.helix";
		boolean writeSuccess = writeToFile( outFilename, sendersMessageEncrypted);
		if(!writeSuccess) {
			System.out.printf("Error writing data to file: [%s]\n", outFilename);
		}
		// *************** *********************************************** ********************* //

		//////////////////////////// DECRYPT CLIENT DATA 	///////////////////
		// Simulate getting the 'cipher blob' from some external communication channel.
		byte[] cipherBlobFromSender = Arrays.copyOf(sendersMessageEncrypted, sendersMessageEncrypted.length);
		
		assert Arrays.equals(cipherBlobFromSender, sendersMessageEncrypted) : "Sent and Receive byte data must be identical";


		System.out.println("Calling jCrypto_decryptPayloadSerialized");
		final BigInteger decryptionHandle = helix.jCrypto_decryptStart(cipherBlobFromSender, password);
		System.out.printf("\tFinished execution of jCrypto_decryptPayloadSerialized with promise id: [%d]%n", decryptionHandle.longValue());
		
		System.out.println("Calling jCrypto_waitEvent");
		helix.jCrypto_waitEvent(decryptionHandle, promiseStatusAndFlags_t.PROMISE_INFINITE.swigValue());
		System.out.println("\tFinished execution of jCrypto_waitEvent");


		
		promiseStatusAndFlags_t wes7 = helix.jCrypto_waitEventStatus(decryptionHandle);
        if ( promiseStatusAndFlags_t.PROMISE_DATA_AVAILABLE != wes7 ) {
            System.out.printf("Decryption Failed with wait-result: %d - abort\n", wes7.swigValue());
			return;
        } else {
            System.out.println("SUCCESS: Call to decrypt returned flag of completion");
    	}
		
		// check if decryption was "valid"
		//cryptoResult_t op_res1 = helix.jCrypto_decryptOutputExists(decryptionHandle);
		//System.out.println(String.format("jCrypto_decryptIsValid returned with result: %d", op_res1));
		
		
		
        byte[] decryptedPayload = helix.jCrypto_decryptGetOutputData(decryptionHandle);
		if( decryptedPayload == null || decryptedPayload.length == 0 ) {
			System.err.println("Decryption Failed - aborting");
			return;
		}
		
		boolean result2 = writeToFile( aFileName + "-restored.helix", decryptedPayload);
		
		
		//final String decryptedMessageFromSender = new String(decryptedPayload);
		System.out.println();		
		System.out.printf("Decrypted payload data size [%d] (in bytes)%n", decryptedPayload.length);
			
		System.out.println();
		System.out.printf("Original payload data size [%d] (in bytes)%n", plainData.length);
		System.out.println();

		// Potential start of unit test
		assert Arrays.equals(decryptedPayload, sendersMessageEncrypted) : "Original and Decrypted byte data must be identical";
		//assert PLAINTEXT_MESSAGE == decryptedMessageFromSender : "Original and Decrypted String data must be identical";

		//write decrypted file back out


		//////////////// DISCONNECT FROM HELIX KEY SERVER  	///////////////////
		final invokeStatus_t promisedServerDisconnect = helix.jCrypto_serverDisconnect();
		System.out.printf("Finished jCrypto_serverDisconnect with return code: %d%n", promisedServerDisconnect.swigValue());
		if( invokeStatus_t.INVOKE_STATUS_TRUE == promisedServerDisconnect ) {
			System.out.println("SUCCESS: Disconnected from Helix Key Server");
		} else {
			System.err.println("FAILURE: Disconnect was not complete - ERROR");
			return;
		}


		//////////////////////////// FREE ALL LOCAL RESOURCES /////////////////
		helix.jCrypto_encryptConclude(encryptionHandle);
		helix.jCrypto_decryptConclude(decryptionHandle);
		helix.jCrypto_userRelease(foundRecipientID);
		System.out.println("Async Tests Complete\n");
			

		//////////////////////////// SHUTDOWN HELIX MODULE	///////////////////
		System.out.println("Starting to shutdown local Helix Crypto Module");
		helix.jCrypto_apiShutdown();
		System.out.println("SUCCESS: local Helix Crypto Module has been gracefully shutdown");
		
	}
}
