============================================
Edition for GWT - dependencies between JAR files
============================================


Below is a list of the dependencies between Restlet libraries. You need to ensure 
that all the dependencies of the libraries that you are using are on the classpath
of your Restlet program, otherwise ClassNotFound exceptions will be thrown.

A minimal Restlet application requires the org.restlet JAR.

To configure connectors such as HTTP server or HTTP client connectors, please refer
to the Restlet User Guide: http://wiki.restlet.org/docs_2.1/

org.restlet (Restlet Core - API and Engine)
-----------
 - com.google.gwt_2.2

org.restlet.ext.json (Restlet Extension - JSON)
--------------------
 - com.google.gwt_2.2

org.restlet.ext.xml (Restlet Extension - XML)
-------------------
 - nothing beside org.restlet JAR.
