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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class AzConnectTraceFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		StringBuffer buf = new StringBuffer(1000);
        buf.append("<tr>\n");

        buf.append("\t<td>");
        buf.append(calcDate(record.getMillis()));
        buf.append("</td>\n");

        buf.append("\t<td>");
        buf.append(record.getThreadID());
        buf.append("</td>\n");

        buf.append("\t<td>");
        buf.append(record.getSourceClassName());
        buf.append("</td>\n");
        
        buf.append("\t<td>");
        String msg = record.getMessage();
        String traceObjects = "";
        
        if(msg.equalsIgnoreCase("ENTRY")) {
        	traceObjects = " { " + record.getSourceMethodName();
        } else if(msg.equalsIgnoreCase("EXIT")) {
        	traceObjects = " } " + record.getSourceMethodName();
        } else if(msg.equalsIgnoreCase("THROWING")) {
        	traceObjects = " E " + record.getSourceMethodName();        	
            if(record.getThrown() != null) {
            	buf.append(record.getThrown());
            }        	
        } else {
        	traceObjects = " D " + record.getSourceMethodName();
        }
        
        Object [] params = record.getParameters();
        for(Object obj : params) {
        	traceObjects += "[" + obj + "]";
        }
        
        if(traceObjects != null)
        	buf.append(traceObjects);
        
        buf.append("</td>\n");
        buf.append("</tr>\n");

        return buf.toString();
    }

    private String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }

    // this method is called just after the handler using this
    // formatter is created
    public String getHead(Handler h) {
        return "<!DOCTYPE html>\n<head>\n<style>\n"
            + "table { width: 100% }\n"
            + "th { font:bold 10pt Tahoma; }\n"
            + "td { font:normal 10pt Tahoma; }\n"
            + "h1 {font:normal 11pt Tahoma;}\n"
            + "</style>\n"
            + "</head>\n"
            + "<body>\n"
            + "<h1>" + (new Date()) + "</h1>\n"
            + "<table border=\"0\" cellpadding=\"5\" cellspacing=\"3\">\n"
            + "<tr align=\"left\">\n"
            + "\t<th style=\"width:10%\">Time</th>\n"
            + "\t<th style=\"width:5%\">TID</th>\n"
            + "\t<th style=\"width:25%\">Class</th>\n"
            + "\t<th style=\"width:60%\">Log Message</th>\n"
            + "</tr>\n";
      }

    // this method is called just after the handler using this
    // formatter is closed
    public String getTail(Handler h) {
        return "</table>\n</body>\n</html>";
    }

}
