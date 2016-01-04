# Azure-Toolkit-for-IntelliJ

The Azure Toolkit for IntelliJ IDEA provides templates and functionality that allow you to easily create, develop, configure, test, and deploy arbitrarily complex, multi-tier, highly available and scalable cloud services and applications to Microsoft Azure using the IntelliJ IDEA development environment running on Mac OS, Linux (tested on Ubuntu) and Windows. It also contains all the key Azure service API clients for Java, exposed as Eclipse libraries, including the Azure Management SDK, Storage SDK, Application Insights SDK, JDBC (for SQL Server and Azure SQL Database) and a supported distro of Apache QPid for Azure AMQP messaging support. 

Other key features of the Azure Toolkit for Eclipse include: 

* deploy any JVM to the cloud, including the OpenJDK (e.g. Azul Zulu) or the Oracle JDK
* deploy any application server, including Tomcat, Jetty, JBoss or GlassFish; or rely on a version of Tomcat or Jetty made available in Azure by Microsoft 
* architect your n-tier application for multiple, independently scalable, highly available clusters ("roles") within an Azure cloud service 
* navigate and manage your Azure storage accounts and their contents (blobs, tables, queue) using the Azure Services Explorer 
* enable session affinity ("sticky sessions") and SSL offloading transparently, without worrying about configuring your application server 
* program against Azure services such as Storage, Service Bus (including AMQP), Caching, Azure SQL Database and more, using the Azure SDK APIs for Java 
* generation of build and publish scripts for automated deployment via Ant outside of the IDE (for example, using Jenkins or Hudson)
* customize and extend the out-of-the-box functionality of the Toolkit easily with startup scripts, templates, components, etc. 
* enable automated telemetry for your applications using Application Insights logging directly into the Azure cloud, regardless of where your application is running

> :warning: Note: the IntelliJ-specific documentation is under construction, so in the meantime, you may refer to the documentation for the [Azure Toolkit for Eclipse](http://go.microsoft.com/fwlink/?LinkID=699529), which is equivalent in functionality, just expressed and exposed using the Eclipse UI model.

## How to install

Pre-requisites:

* Java 7+
* IntelliJ Community supported but Ultimate is recommended for full functionality
* :warning: [Azure Services Explorer plugin](https://plugins.jetbrains.com/plugin/8052?pr=idea) must be installed first
