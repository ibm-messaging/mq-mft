/*
* (c) Copyright IBM Corporation 2025
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import java.nio.charset.StandardCharsets;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.lang.Process;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.BytesMessage;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;


import org.w3c.dom.*;

import javax.xml.parsers.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import org.xml.sax.InputSource;

public class XMLToJson{

    private static String host = "localhost";
    private static int port = 1414;
    private static String channel = null;
    private static String user = null;
    private static String password = null;
    private static String queueManagerName = null;
    private static String destinationName = null;
    private static boolean isTopic = false;
    private static boolean clientTransport = false;
    public static String name;
    public static String key1;
  
    private static int timeout = Integer.MAX_VALUE;

    private static int status = 1;
    private void consumeMessages(Connection connection) throws Exception {
		// Variables
		MessageConsumer consumer = null;
    Session consumerSession = null;

		System.out.println("==== Starting IBM MQ JMS Consumer ====");
		consumerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		System.out.println("Session created.");
		Destination destination = consumerSession.createTopic("topic://SYSTEM.FTE/#");
		consumer = consumerSession.createConsumer(destination);
		System.out.println("Consumer created.");
		System.out.println("Waiting for messages... from " + destination.toString());

		// Now receive messages synchronously or asynchronously
		// Create and register a new MessageListener for this consumer
		consumer.setMessageListener(new MessageListener() {
			public void onMessage(Message msg) {
				try {
					if (msg instanceof TextMessage) {
						TextMessage textMsg = (TextMessage)msg;
						System.out.println(textMsg);
					} else if (msg instanceof BytesMessage) {
						BytesMessage bytesMsg = (BytesMessage)msg;
						int bodyLen = (int)bytesMsg.getBodyLength();
						byte [] body = new byte[bodyLen];
						bytesMsg.readBytes(body, bodyLen);
						String bodyStr = new String(body, StandardCharsets.UTF_8);
            //fetching Name
            int start_index = bodyStr.indexOf("xsi:noNamespaceSchemaLocation=\"");
            int end_index = bodyStr.indexOf(".xsd");
            if(start_index != -1 && end_index != -1){
            name = bodyStr.substring(start_index + 31, end_index);
            // System.out.println("XML format message for " + bodyStr.substring(start_index + 31, end_index));
						// System.out.println("Body:" + bodyStr);
            System.out.println("Json formant message for " +  bodyStr.substring(start_index + 31, end_index));
            }
            else{
              name = "monitorList";
              // System.out.println("XML format message for Monitor list");
						  // System.out.println("Body:" + bodyStr);
              System.out.println("Json formant message for Monitor list");
            }
            String rawJson = convertXmlToJson(bodyStr).trim();
            // Split multiple JSON blocks correctly
            String[] jsonObjects = rawJson.split("(?<=\\})\\s*(?=\\{)");
            StringBuilder arrayOutput = new StringBuilder();
            arrayOutput.append("[\n");
            for (String obj : jsonObjects) {

              String cleanedObj = obj
              .replaceAll("\"(xmlns(:[^\"]*)?)\"\\s*:\\s*\"[^\"]*\",?", "") // removes xmlns:* entries
              .replaceAll("\"xsi:[^\"]*\"\\s*:\\s*\"[^\"]*\",?", ""); // removes xsi:* entries

                arrayOutput.append("  {\n")
                          .append("    \"").append(name).append("\": ")
                          .append(cleanedObj.trim()).append("\n")
                          .append("  },\n");
            }
            // Remove the trailing comma
            if (arrayOutput.toString().endsWith(",\n")) {
                arrayOutput.setLength(arrayOutput.length() - 2);
                arrayOutput.append("\n");
            }
            arrayOutput.append("]");
            String final_output = arrayOutput.toString();

            // FileWriter myWriter = new FileWriter("file.json", true);
            // myWriter.append(final_output);
            // myWriter.close();
            
            
            System.out.println(final_output);
            //run the java program that will push the json data into prometheus
              try{
                RunPrometheusProgram(final_output);
              }catch (Exception e) {
                e.printStackTrace();
                } //end try

					}	            	
				} // end try
				catch (Exception e) {
					System.out.println("Exception caught in onMessage():\n" + e);
				}
				return;
			} // end onMessage()
		}); // end setMessageListener				
	}

  // Recieve xml data and pass it for parsing
  public static String convertXmlToJson(String xml) throws Exception{
    if(xml == null || xml.trim().isEmpty()){
      throw new IllegalArgumentException("XML input is empty");
    }
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();

    ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
    Document doc = builder.parse(new InputSource(input));
    Element root = doc.getDocumentElement();
    return beautifyJson(xmlToJson(root, 0));
  }

  // Structurally convert the xml into json
  private static String xmlToJson(Element element, int indent) {
    StringBuilder json = new StringBuilder();
    String indentStr = " ".repeat(indent);
    json.append(indentStr).append("{\n");
    String childIndent = " ".repeat(indent + 2);
    // Add attributes
    NamedNodeMap attributes = element.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
        Node attr = attributes.item(i);
        json.append(childIndent)
            .append("\"").append(attr.getNodeName()).append("\": ")
            .append("\"").append(attr.getNodeValue()).append("\",\n");
    }
    // Handle <properties> metadata block
    if (element.getTagName().equals("properties")) {
        NodeList children = element.getChildNodes();
        json.append(childIndent).append("\"metaData\": {\n");
        String metaIndent = " ".repeat(indent + 4);
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element entry && entry.getTagName().equals("entry")) {
                String key = entry.getAttribute("key");
                if (key.startsWith("com.ibm.wmqfte.")) {
                    key = key.substring("com.ibm.wmqfte.".length());
                }
                String value = entry.getTextContent().trim();
                if (key.equals("agentTraceLevel") && value.startsWith("<?xml")) {
                    try {
                        Element embeddedRoot = parseXmlStringToElement(value);
                        String embeddedJson = xmlToJson(embeddedRoot, indent + 6);
                        json.append(metaIndent).append("\"").append(key).append("\": ")
                            .append(embeddedJson).append(",\n");
                    } catch (Exception e) {
                        json.append(metaIndent).append("\"").append(key).append("\": ")
                            .append("\"").append(beautifyJson(value)).append("\",\n");
                    }
                } else {
                    json.append(metaIndent).append("\"").append(key).append("\": ")
                        .append("\"").append(beautifyJson(value)).append("\",\n");
                }
            }
        }
        json.setLength(json.length() - 2);
        json.append("\n").append(childIndent).append("},\n");
    } else {
        // Group children by tag name
        Map<String, List<Element>> groupedChildren = new LinkedHashMap<>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element e) {
                groupedChildren.computeIfAbsent(e.getTagName(), k -> new ArrayList<>()).add(e);
            }
        }
        // Add each group
        for (Map.Entry<String, List<Element>> entry : groupedChildren.entrySet()) {
            String tag = entry.getKey();
            List<Element> elems = entry.getValue();
            json.append(childIndent).append("\"").append(tag).append("\": ");
            if (elems.size() > 1) {
                json.append("[\n");
                for (Element e : elems) {
                    json.append(xmlToJson(e, indent + 4)).append(",\n");
                }
                json.setLength(json.length() - 2);
                json.append("\n").append(childIndent).append("],\n");
            } else {
                Element single = elems.get(0);
                String text = single.getTextContent().trim();
                boolean hasAttributes = single.getAttributes().getLength() > 0;
                boolean hasChildElements = single.getElementsByTagName("*").getLength() > 0;
                // Handle embedded XML tags like taskXML or agentTraceLevel
                if ((tag.equals("taskXML") || tag.equals("agentTraceLevel")) && text.startsWith("<?xml")) {
                    try {
                        Element embeddedRoot = parseXmlStringToElement(text);
                        String embeddedJson = xmlToJson(embeddedRoot, indent + 4);
                        json.append(embeddedJson).append(",\n");
                    } catch (Exception e) {
                        json.append("\"").append(beautifyJson(text)).append("\",\n");
                    }
                } else if (!hasChildElements && !hasAttributes) {
                    json.append("\"").append(beautifyJson(text)).append("\",\n");
                } else if (!hasChildElements && hasAttributes) {
                    json.append("{\n");
                    NamedNodeMap childAttrs = single.getAttributes();
                    for (int i = 0; i < childAttrs.getLength(); i++) {
                        Node attr = childAttrs.item(i);
                        if(attr.getNodeValue().startsWith("com.ibm.wmqfte.")){
                          key1 = attr.getNodeValue().substring("com.ibm.wmqfte.".length());
                        }
                        json.append(" ".repeat(indent + 4))
                            .append("\"").append(attr.getNodeName()).append("\": ")
                            .append("\"").append(attr.getNodeValue()).append("\",\n");
                    }
                    if (!text.isEmpty()) {
                        json.append(" ".repeat(indent + 4))
                            .append("\"value\": \"").append(beautifyJson(text)).append("\"\n");
                    } else {
                        json.setLength(json.length() - 2);
                        json.append("\n");
                    }
                    json.append(childIndent).append("},\n");
                } else {
                    json.append(xmlToJson(single, indent + 2)).append(",\n");
                }
            }
        }
        // If no children, add text content
        if (groupedChildren.isEmpty()) {
            String textContent = element.getTextContent().trim();
            if (!textContent.isEmpty()) {
                json.append(childIndent).append("\"value\": \"").append(beautifyJson(textContent)).append("\",\n");
            }
        }
    }
    // Remove trailing comma
    if (json.toString().endsWith(",\n")) {
        json.setLength(json.length() - 2);
        json.append("\n");
    }
    json.append(indentStr).append("}");
    return json.toString();
}

// Provides the indent and adds readability to json data
private static String beautifyJson(String Json){
  return Json.replaceAll("\n\\s*\n", "\n").trim();
}

// Parse the xml strings into element
private static Element parseXmlStringToElement(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setIgnoringElementContentWhitespace(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(new InputSource(new StringReader(xml)));
    return doc.getDocumentElement();
}

//  Push the json data into Prometheus
public static void RunPrometheusProgram(String final_output) {
  try {
      String projectRoot = "C:\\Users\\Administrator\\Sample JMS files";
      String filePath = "xml_metrics_exporter\\JsonToPrometheus.java";
      String className = "xml_metrics_exporter.JsonToPrometheus";
      // Compile the Java file
      ProcessBuilder compileProcessBuilder = new ProcessBuilder("javac", "-cp", "lib/*", filePath);
      compileProcessBuilder.directory(new File(projectRoot)); // Set working directory
      Process compileProcess = compileProcessBuilder.start();
      int compileExitCode = compileProcess.waitFor();
      if (compileExitCode != 0) {
          System.err.println("Compilation failed with exit code: " + compileExitCode);
          BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
          String line;
          while ((line = errorReader.readLine()) != null) {
              System.err.println(line);
          }
          return;
      }
      // Run the compiled Java class
      ProcessBuilder runProcessBuilder = new ProcessBuilder("java", "-cp", ".;lib/*", className);
      runProcessBuilder.directory(new File(projectRoot)); // Set working directory
      Process runProcess = runProcessBuilder.start();
      // Send input
      BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()));
      processWriter.write(final_output);
      processWriter.flush();
      processWriter.close();
      // Read output
      BufferedReader outputReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
      String outputLine;
      System.out.println("Program output:");
      while ((outputLine = outputReader.readLine()) != null) {
          System.out.println(outputLine);
      }
      // Read errors
      BufferedReader runErrorReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
      String errorLine;
      System.err.println("Program error output:");
      while ((errorLine = runErrorReader.readLine()) != null) {
          System.err.println(errorLine);
      }
      int runExitCode = runProcess.waitFor();
      System.out.println("Program finished with exit code: " + runExitCode);
  } catch (IOException | InterruptedException e) {
      e.printStackTrace();
  }
}
    public static void main(String[] args){
        parseArgs(args);
        XMLToJson Jc = new XMLToJson();

        Connection connection = null;
        Session session = null;
        Destination destination = null;
        MessageConsumer consumer = null;
    try{
      JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
      JmsConnectionFactory cf = ff.createConnectionFactory();

      cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
      cf.setIntProperty(WMQConstants.WMQ_PORT, port);
      cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);

        if(clientTransport){
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
        }
        else{
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_BINDINGS);
        }
        cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManagerName);
      if (user != null) {
        cf.setStringProperty(WMQConstants.USERID, user);
        cf.setStringProperty(WMQConstants.PASSWORD, password);
        cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
      }
      connection = cf.createConnection();
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      

      if(isTopic){
        destination = session.createTopic(destinationName);
      }
      else{
        destination = session.createQueue(destinationName);
      }
      consumer = session.createConsumer(destination);

      connection.start();
      try {
        Jc.consumeMessages(connection);
      } catch (Exception e){
        e.printStackTrace();
      }

      Message message;
      do{
        message = consumer.receive(timeout);
        if(message != null){
            System.out.println("Recieved Message : " + message);
        }
      }while(message != null);
      System.out.format("No message received in %d seconds!\n", timeout / 1000);
      recordSuccess();
    }
    catch (JMSException jmsex){
        recordFailure(jmsex);
    }
    finally{
        if(consumer != null){
            try{
                consumer.close();
            }
            catch(JMSException jmsex){
                System.out.println("Consuner could not be closed");
                recordFailure(jmsex);
            }
        }
        if (session != null){
            try{
                session.close();
            }
            catch(JMSException jmsex){
                System.out.println("Session could not be closed");
                recordFailure(jmsex);
            }
        }
        if (connection != null){
            try{
                connection.close();
            }
            catch(JMSException jmsex){
                System.out.println("Connection could not be closed");
                recordFailure(jmsex);
            }
        }
    }
    
    System.exit(status);
    return;

    }
    private static void ProcessJMSException(JMSException jmsex){
        System.out.println(jmsex);

        Throwable innerException = jmsex.getLinkedException();
        if (innerException != null) {
        System.out.println("Inner exception(s):");
        }
        while (innerException != null) {
        System.out.println(innerException);
        innerException = innerException.getCause();
        }
        return;
    }

    private static void recordSuccess() {
        System.out.println("SUCCESS");
        status = 0;
        return;
      }

      private static void recordFailure(Exception ex) {
        if (ex != null) {
          if (ex instanceof JMSException) {
            ProcessJMSException((JMSException) ex);
          }
          else {
            System.out.println(ex);
          }
        }
        System.out.println("FAILURE");
        status = -1;
        return;
      }
      private static void parseArgs(String[] args) {
        try {
          int length = args.length;
          if (length == 0) {
            throw new IllegalArgumentException("No arguments! Mandatory arguments must be specified.");
          }
          if ((length % 2) != 0) {
            throw new IllegalArgumentException("Incorrect number of arguments!");
          }
    
          int i = 0;
    
          while (i < length) {
            if ((args[i]).charAt(0) != '-') {
              throw new IllegalArgumentException("Expected a '-' character next: " + args[i]);
            }
    
            char opt = (args[i]).toLowerCase().charAt(1);
    
            switch (opt) {
              case 'h' :
                host = args[++i];
                clientTransport = true;
                break;
              case 'p' :
                port = Integer.parseInt(args[++i]);
                break;
              case 'l' :
                channel = args[++i];
                break;
              case 'm' :
                queueManagerName = args[++i];
                break;
              case 'd' :
                destinationName = args[++i];
                break;
              case 'u' :
                user = args[++i];
                break;
              case 'w' :
                password = args[++i];
                break;
              case 't' :
                try {
                  int timeoutSeconds = Integer.parseInt(args[++i]);
                  timeout = timeoutSeconds * 1000;
                }
                catch (NumberFormatException nfe) {
                  throw new IllegalArgumentException("Timeout must be a whole number of seconds");
                }
                break;
              default : {
                throw new IllegalArgumentException("Unknown argument: " + opt);
              }
            }
    
            ++i;
          }
    
          if (queueManagerName == null) {
            throw new IllegalArgumentException("A queueManager name must be specified.");
          }
    
          if (destinationName == null) {
            throw new IllegalArgumentException("A destination name must be specified.");
          }
    
          if (((user == null) && (password != null)) || 
              ((user != null) && (password == null))) {
            throw new IllegalArgumentException("A userid and password must be specified together");
          }
    
          if (destinationName.startsWith("topic://")) {
            isTopic = true;
          }
          else {
            isTopic = false;
          }
        }
        catch (Exception e) {
          System.out.println(e.getMessage());
          printUsage();
          System.exit(-1);
        }
        return;
      }

      private static void printUsage(){
        System.out.println("\nUsage:");
        System.out.println("JmsConsumer -m queueManagerName -d destinationName [-h host -p port -l channel] [-u user -w passWord] [-t timeout_seconds]");
        return;
      }
} 
    