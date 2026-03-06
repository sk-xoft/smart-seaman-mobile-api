# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.0/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.0.0/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#web)
* [codecentric's Spring Boot Admin (Server)](https://codecentric.github.io/spring-boot-admin/current/#getting-started)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)


## Command docker
docker build -t xoftspace/smart-seaman-mobile-api:0.2 .

docker push xoftspace/smart-seaman-mobile-api:0.1 

docker pull xoftspace/smart-seaman-mobile-api:0.1

docker run --name smart-seaman-mobile-api \
-e COMPANY='smart-seaman' \
-e ENV='dev' \
-it -p 30000:8080/tcp \
xoftspace/smart-seaman-mobile-api:0.1


docker run --name smart-seaman-mobile-api -d \
-e COMPANY='smart-seaman' \
-e ENV='dev' \
-it -p 30000:8080/tcp \
xoftspace/smart-seaman-mobile-api:0.1


### Run docker on pord
docker run --name smart-seaman-mobile-api-0.2 -d \
-e COMPANY='smart-seaman' \
-e ENV='dev' \
-it -p 30000:8080/tcp \
-v /home/ssmuser/apps-logs-service/smart-seaman-mobile-api/logs:/apps-logs-service/smart-seaman-mobile-api/logs \
xoftspace/smart-seaman-mobile-api:0.2
 
### Run Docker logcal
docker run --name smart-seaman-mobile-api-0.2 -d \
-e COMPANY='smart-seaman' \
-e ENV='dev' \
-it -p 30000:8080/tcp \
-v /Users/sarunyook/workspaces/xoftspace/logs:/apps-logs-service/smart-seaman-mobile-api/logs \
xoftspace/smart-seaman-mobile-api:0.2


(https://mobile.smartseaman.com/)


###
docker exec -it <mycontainer> bash

### Docker command
&docker images 

&docker ps -all
&docker ps stop <container_iddfdf
docker ps stop 