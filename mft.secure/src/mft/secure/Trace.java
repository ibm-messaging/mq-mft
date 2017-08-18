/**
 * 
 */
package mft.secure;

/**
 * ==================================================================================
 * Copyright (c) IBM Corporation 2017
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
 * ==================================================================================
 *
 */
public class Trace {

	/**
	 * Write log
	 * @param method
	 * @param params
	 */
	public static void logInfo(final String method, String... params){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(method);
		
		for(String param : params){
			sb.append("[");
			sb.append(param);
			sb.append("]");
		}
		System.out.println(sb.toString());			
	}
	
	/**
	 * Writes an exception log
	 * @param method
	 * @param ex
	 */
	public static void logException(final String method, final Exception ex) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(method);
		sb.append(" ");
		sb.append(ex);
		System.out.println(sb.toString());
	}
}
