FROM maven:3.8-jdk-11 AS buildweb

ADD pom.xml /source/pom.xml
RUN cd /source && mvn verify clean --fail-never

ADD ./src /source/src
RUN cd /source && mvn -B package -DskipTests

FROM maven:3.8-jdk-11 AS buildconv

RUN git clone --branch extension "https://github.com/fazleh2010/question-grammar-generator.git" /source

#alternative from local directory
#ADD ./grammar-generator/pom.xml /source/pom.xml
# ADD ./grammar-generator /source

RUN cd /source && mvn verify clean --fail-never

RUN cd /source && mvn -B package -DskipTests

FROM adoptopenjdk/openjdk15:jre-15.0.2_7-debian

COPY --from=buildweb /source/target/*.jar /app/webapp.jar
COPY --from=buildconv /source/target/QuestionGrammarGenerator.jar /app/generator.jar

CMD ["java", "-jar", "/app/webapp.jar"]
