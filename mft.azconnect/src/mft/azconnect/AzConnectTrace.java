/**
 * Copyright (c) IBM Corporation 2018
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *  Contributors:
 *    Shashikanth Rao T - Initial Contribution
 *
 ***************************************************************************
 */
package mft.azconnect;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class AzConnectTrace {
    static private FileHandler fileHTML;
    static private AzConnectTraceFormatter formatterHTML;
    static Logger logger = null;
    
    static public void setup() throws IOException {

        // get the logger to configure it
        logger = Logger.getLogger("mft.azconnect");

        logger.setLevel(Level.FINEST);
        fileHTML = new FileHandler("Logging.html");

        // create an HTML formatter
        formatterHTML = new AzConnectTraceFormatter();
        fileHTML.setFormatter(formatterHTML);
        logger.addHandler(fileHTML);
    }

	public static void entry(final Object clsObj, final String methodName, Object... objects) {
		LogRecord log = new LogRecord(Level.FINEST, "ENTRY");
		log.setParameters(objects);
		log.setMillis(System.currentTimeMillis());
		log.setThreadID((int) Thread.currentThread().getId());
		log.setSourceClassName(getClassName(clsObj));
		log.setSourceMethodName(methodName);
		logger.log(log);
	}
	
	private static String getClassName(Object clsObj) {
		Class<?> enclosingClass = clsObj.getClass().getEnclosingClass();
		String className = "";
		if (enclosingClass != null) {
			className = enclosingClass.getName();
			} else {
				className = clsObj.getClass().getName();
			}
		return className;
	}
	
	public static void exit(final Object clsObj, final String methodName, Object... objects) {
		LogRecord log = new LogRecord(Level.FINEST, "EXIT");
		log.setParameters(objects);
		log.setThreadID((int) Thread.currentThread().getId());
		log.setSourceClassName(getClassName(clsObj));
		log.setSourceMethodName(methodName);
		logger.log(log);
	}
	
	public static void data(final Object clsObj, final String methodName, final Object... data ) {
		LogRecord log = new LogRecord(Level.FINEST, "DATA");
		log.setSourceClassName(getClassName(clsObj));
		log.setThreadID((int) Thread.currentThread().getId());
		log.setSourceMethodName(methodName);
		log.setParameters(data);
		logger.log(log);
	}
    
	public static void throwing(final Object clsObj, final String methodName, final Throwable th ) {
		LogRecord log = new LogRecord(Level.FINEST, "THROWING");
		log.setThreadID((int) Thread.currentThread().getId());
		log.setSourceClassName(getClassName(clsObj));
		log.setSourceMethodName(methodName);
		log.setThrown(th);
		logger.log(log);		
	}
}
