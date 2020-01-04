#
# @since July 4, 2019 19:58 PM (creation date)
# @author Peter Withers <peter.withers@mpi.nl>
#
FROM openjdk:9
RUN apt-get update # --fix-missing
RUN apt-get -y upgrade # --fix-missing
RUN apt-get -y install maven vim 
#mysql-server
RUN git clone --depth 30000 https://github.com/PeterWithers/DomesticEnvironmentLogger.git
RUN sed -i 's|<packaging>war</packaging>|<packaging>jar</packaging>|g' /DomesticEnvironmentLogger/monitor/pom.xml
RUN sed -i 's|<exclusions>|<!--<exclusions>|g' /DomesticEnvironmentLogger/monitor/pom.xml
RUN sed -i 's|</exclusions>|</exclusions>-->|g' /DomesticEnvironmentLogger/monitor/pom.xml
RUN sed -i 's|1.4.2.RELEASE|2.1.11.RELEASE|g' /DomesticEnvironmentLogger/monitor/pom.xml
RUN sed -i 's|>1.8<|>1.9<|g' /DomesticEnvironmentLogger/monitor/pom.xml
RUN sed -i 's|@Autowired|//@Autowired|g' /DomesticEnvironmentLogger/monitor/src/main/java/com/bambooradical/monitor/repository/*.java
RUN sed -i 's|@PostConstruct|//@PostConstruct|g' /DomesticEnvironmentLogger/monitor/src/main/java/com/bambooradical/monitor/repository/*.java
RUN sed -i 's|@Bean|//@Bean|g' /DomesticEnvironmentLogger/monitor/src/main/java/com/bambooradical/monitor/Application.java
RUN sed -i 's|context.web|web.servlet.support|g' /DomesticEnvironmentLogger/monitor/src/main/java/com/bambooradical/monitor/Application.java
RUN sed -i 's|DataRecordService|DataRecordRepository|g' /DomesticEnvironmentLogger/monitor/src/main/java/com/bambooradical/monitor/controller/*Controller.java
RUN sed -i 's|EnergyRecordService|EnergyRecordRepository|g' /DomesticEnvironmentLogger/monitor/src/main/java/com/bambooradical/monitor/controller/Data*Controller.java
RUN sed -i 's|RadioDataService|RadioDataRepository|g' /DomesticEnvironmentLogger/monitor/src/main/java/com/bambooradical/monitor/controller/DataRecordController.java
RUN sed -i 's|MagnitudeRecordService|MagnitudeRecordRepository|g' /DomesticEnvironmentLogger/monitor/src/main/java/com/bambooradical/monitor/controller/DataRecordController.java
RUN sed -i 's|DayOfDataGcsFileStore|DayOfDataFileStore|g' /DomesticEnvironmentLogger/monitor/src/main/java/com/bambooradical/monitor/controller/DataRecordController.java

RUN rm /DomesticEnvironmentLogger/monitor/src/main/resources/application.properties

RUN cd /DomesticEnvironmentLogger/monitor/ \
    && git diff

RUN cd /DomesticEnvironmentLogger/monitor/ \
    && mvn install

#WORKDIR /target
VOLUME ["/data"]
CMD java -jar /DomesticEnvironmentLogger/monitor/target/monitor-1.0-SNAPSHOT.jar
