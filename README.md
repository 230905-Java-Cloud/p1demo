# p1demo - A Demo CRUD API with Spring Boot 

## User Stories:

- Get all employees
- Insert employee
- Get employee by Id
- Get employee by username
- Update entire employee (PUT)
- Update employee username (PATCH)
- Get Pokemon from PokeAPI by name (RestTemplate example)
  
  (Check the P1DemoWithJWT repo for a version of this app with Login using JWT and Spring Security!)

  ## Addons

  - We added a Dockerfile in order to create Docker images of this API (which we can use to spin up a containerized P1Demo)
  - We added a deployment.yaml and a service.yaml for the sake of container orchestration with Minikube
  - Logs with Logback in week 7...
  - Again, JWT is not included in this repo! Check P1DemoWithJWT for that.
