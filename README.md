```

                      \VW/
                    .::::::.
                    ::::::::
                    '::::::'
                     '::::'
                       `"`
  ____  _                      _                           
 / ___|| |_ _ __ __ ___      _| |__   ___ _ __ _ __ _   _  
 \___ \| __| '__/ _` \ \ /\ / / '_ \ / _ \ '__| '__| | | | 
  ___) | |_| | | (_| |\ V  V /| |_) |  __/ |  | |  | |_| | 
 |____/ \__|_|  \__,_| \_/\_/ |_.__/ \___|_|  |_|   \__, | 
                                                    |___/


```
## Strawberry Real-Time Streaming Events Processing Platform ##
Strawberry is a platform that helps developers to easily write applications to process streams of events coming in at a rapid pace, real time. The main objective of this platform is to keep the developer's focus out of plumbing activities and only make them focus on their business logic. 
In a typical Big Data world of events processing, there are quite a lot of challenges. A few main ones (in the context of what Strawberry solves), are listed here.

 - Choosing the right technology stack.
 - Horizontal scalability.
 - Real time processing.
 - Ability to scale fast based on business needs.
 - Cloud friendly design.
 - Enabling a lot of business opportunities outside the platform (eg: Machine Learning, Analytics etc).
 - Operational tools to monitor the platform (Throughput, Performance etc).
 - Easy to add support for more events in the future.
 - Developer Productivity.
 
 ### Principles ###
 - Developer Productivity - Strawberry platform provides a solid foundation built using the right technology stack for processing large volumes of real-time streaming events. Typically, in an event processing application a lot of plumbing activities are required even to write a simple "Hello World" app. Strawberry relieves all the pain by giving the developers a solid platform to only write their business logic and all the rest is taken care of.
 - Scaleable Design - Strawberry is built using Microservices Architecture which is a modern architecture for large scale cloud friendly apps. The principle of microservices is much like the unix philosophy - "Do one thing and do it well".
 - Effective Utilisation of Resources - Strawberry is built for utilizing all the available cores in the system effectively by applying modern reactive principles.
 - Distributed/Fault Tolerant by Design - Strawberry uses Kafka, MongoDB, Elasticsearch which are built for distributed apps and have been put to production in a large number of enterprises. They have built in support for resiliency by employing replication.
 - We haven't forgotten our Ops team - Strawberry offers straight out of the box dashboards that gives a lot of KPIs on systems performance and health.
 -   Declarative syntax - Strawberry offers "JSON" as a format to define the event processing instruction. It is also possible for the app developers to write a custom java code with their business logic and allow it to be called by the platform. Employs the famous Hollywood principle - "Don't call us, We'll call you".
 
## Logical High Level Architecture ##
![Alt text](HighLevelLogicalArch.png?raw=true "High Level Logical Architecture")

## Layers in Strawberry ##
![Alt text](Layered.png?raw=true "Layers")

* *Data ingestion layer* - Highly scalable, fault tolerant layer that collects the events from different sources asynchronously. This is typically a Kafka Topic or Rest API for easier integration. 

* *Data transformation/enrichment layer* - Often, the raw event data needs enriching or transformation based on business logic.

* *Notifications layer* - The enriched data passes through the notifications engine in which various rules are applied to the data. If the data matches the rules, then the notification is sent else the notificication is not sent.


## Technology Stack ##
![Alt text](tech_stack_1.png?raw=true "Tech stack")


## This is what happens inside the platform ##
![Alt text](Strawberry_Dissection.png?raw=true "This is what happens inside the platform")

## PREREQUISITES (FOR A DEV SETUP)##
* Java 8
* Maven 3
* Docker (Latest)

## MODULES ##
 - api - Java API (Model objects).
 - config-service - Microservice that exposes all the configuration to the platform (db url, elasticsearch url etc etc).
 - configs - File system based configuration files which will be exposed by the config-service.
 - event-processor - The strawberry platform. Remember, the platform cannot run on it's own. It is bundled along with the app.
 - app-banking-txn-anomaly - A sample APP that runs on the platform. This app has a config file in the resources directory. This file is the bible for the platform to follow the instructions to process the event. com.sai.app.banking.txn.EventReceiver is a java class that contains the business logic to enrich the incoming event data to be later used in the notification framework.
 - ui-console - A simple UI to view the configurations of a running app and also a few utilities to quicky create a template of an app for rapid development.

## STEPS TO BUILD ##
* Open the config-service/src/main/resources/application.properties - Change the file location defined in the spring.```cloud.config.server.native.searchLocations``` property to point the correct location inside configs module. 
* Open a Docker Terminal (If you're using a Non-Linux OS), Use Docker Tookbox. It is simple to use. Refer to it's website for installation instructions specific to your OS.
* All the below commands must be run on this shell only.
* Go to config-service directory and run - `mvn clean install`
* Go to api directory and run - `mvn clean install`
* Go to event-processor directory and run - `mvn clean install`
* Go to ui-console directory and run - `mvn clean install`
* Go to app-banking-txn-anomaly directory and run - `mvn clean install`

You've built all the modules now.

## STEPS TO RUN ##
* On the same Docker Terminal shell, navigate to the project root directory (strawberry), run this first:  ``` docker-compose --file docker-compose-config-service.yml up -d ```
* The above command will start the config service first. This must be the first step. 
* Check this URL:  http://192.168.99.100:8888/app-banking-txn-anomaly/default/    (You should see the config JSON).
* On the same Docker Terminal shell, navigate to the project root directory (strawberry), run this:  ``` docker-compose up -d ```
* After 2 mins or so - http://192.168.99.100:9999/swagger-ui.html
* You should see the swagger ui.
* Go to docker-utils directory and run this command. ``` ./kafka-create-topic.sh card_txns_1 ```
* Back to swagger ui - Go to the resource, POST /eventstream/{eventStreamConfigId}, set the eventStreamConfigId as card_txns_1 and the payload as the contents of card_txns_data.json and click the "try now" button.
* Go to Kibana: http://192.168.99.100:5601/ Navigate to the strawberryopsindex and make it as default.
* Go to http://192.168.99.100:9090/strawberry-ui-console/homePage.do - Click on the 'ops dashboard' link and you should see the kibana dashboards.






















