# Deploying to App Engine

1. Update [src/main/appengine/app.yaml](src/main/appengine/app.yaml)
2. Possible increment application version number in [pom.xml](pom.xml)
3. `mvn clean package appengine:deploy -P cloud-gcp`
