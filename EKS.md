# Deploying a Kubernetes Cluster to AWS EKS

## AWS Getting Started Guide - CLI Configuration
The [Getting Started Guide](https://docs.aws.amazon.com/eks/latest/userguide/getting-started.html) from AWS is a great resource to make sure you have all the necessary components to begin working from the CLI on your system for ease of use to build AWS resources through repeatable commands. While this may seem like a lot to download and configure in your terminal, these tools help streamline the process of provisioning AWS resources & handling those resources.

**NOTE** Helm is not included here, but will be necessary for this project.

### AWS CLI
[AWS CLI Documentation](https://docs.aws.amazon.com/cli/latest/)

If you didn't go through the `Getting Started Guide` above please utilize this [Installation Doc](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html). Making sure to select the appropriate operating system, Windows folks have an installer to click through which is nice. 


### eksctl 
[eksctl Documentation](https://eksctl.io/usage/creating-and-managing-clusters/)

If you didn't go through the `Getting Started Guide` above please utilize this [Installation Doc](https://eksctl.io/installation/). Windows users can download a zip or follow the script with the checksum in PowerShell.

### kubectl

[kubectl Documentation](https://kubernetes.io/docs/tasks/tools/#kubectl)

If you didn't go through the `Getting Started Guide` above please utilize this [Installation Doc](https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/). Windows users can download a zip or follow the script with the checksum in PowerShell.

### Helm
[helm Documentation](https://helm.sh/docs/)

**NOTE** Given some recent issues with the recent version PLEASE download this version [3.12.3](https://github.com/helm/helm/releases/tag/v3.12.3) of helm. 

Once download is complete, please extract the folder, open the folder where `helm` executable is present. Once there, copy the address to this location and open up your systems environment variables to edit your `Path` variable to contain a new location that you copied. Save this and remember to RESTART your terminals for the path to be updated.

#### AWS Tutorial through Browser Cloud Shell

If you want to try something else out that [AWS provides at your own pace with some additional tooling, please check out this site](https://www.eksworkshop.com/). Have fun exploring Elastic Kubernetes Service on AWS!!!!

## Generating a EKS Cluster hosting a Spring Boot Application

Now let's finally get started by working with our application that we know and love. Below I will be referencing all the work done by Ben P for the [P1Demo](https://github.com/230905-Java-Cloud/p1demo), but I will also include generalized commands for you to fill in the blanks.

### Containerized Application & Push to AWS ECR

First and foremost we need to make sure our docker image is prepared and ready to work with which requires us to take a look at our Docker file for this application. Which for most with Spring Boot applications should look like below:

```Dockerfile

# A Dockerfile is used to describe the image of the application so we can reliably recreate it anywhere we want

# First thing we need to do is to provide a BASE IMAGE to start FROM
FROM amazoncorretto:11

# We'll need to COPY our packaged JAR file from here to our containers storage
COPY target/app.jar app.jar

# Next, we need to EXPOSE a port for HTTP traffic. The container will sit here.
EXPOSE 8080

# The final thing we need to do is provide the CMD to start the application itself
CMD ["java", "-jar", "app.jar"]
```
### Runing App Locally

As long as the above docker represents something simular to your application, you can begin to build and run this application through docker locally first to ensure functionality.

```bash
# Project Specific
docker build -t p1-demo .
docker run -d -p 4444:8080 p1-demo 
# Request http://localhost:4444/p1/employee to validate docker is running containerized application appropriately

###################################################################

# Generalized
docker build -t {name-of-project-image} .
docker run -d -p {host-port}:{container-exposed-port} {name-of-project-image}
```

## Create ECR and push the above image
Once the endpoint calls as expected we can begin to work with our `aws cli` commands to begin provisioning ECR resources to allow us to push these images. 

```bash
# Project Specific
# Create ECR Repo on AWS, one should be made per image
aws ecr create-repository --repository-name p1-demo

# Authenticate Your Terminal to access the ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 296271419447.dkr.ecr.us-east-1.amazonaws.com

# Tag your image to the ECR Repo
docker tag p1-demo:latest 296271419447.dkr.ecr.us-east-1.amazonaws.com/p1-demo:latest

# Push to the ECR Repo
docker push 296271419447.dkr.ecr.us-east-1.amazonaws.com/p1-demo

###################################################################

# Generalized
# Create ECR Repo on AWS, one should be made per image
aws ecr create-repository --repository-name {name-of-project-image}

# Authenticate Your Terminal to access the ECR
aws ecr get-login-password --region {your-aws-region} | docker login --username AWS --password-stdin {aws-account-id}.dkr.ecr.{your-aws-region}.amazonaws.com

# Tag your image to the ECR Repo
docker tag {name-of-project-image}:latest {aws-account-id}.dkr.ecr.us-{your-aws-region}.amazonaws.com/{name-of-project-image}:latest

# Push to the ECR Repo
docker push {aws-account-id}.dkr.ecr.{your-aws-region}.amazonaws.com/{name-of-project-image}
```

## Create your first EKS Cluster

Now that you have your image pushed up to the ECR, we can begin by creating and working with our kubernetes hosted on AWS Elastic Kubernetes Service (EKS). To get started we can begin by creating our cluster through `eksctl` command in our terminal. This will take 10-15 min, so in the meanwhile we can make sure our `kubernetes-manifest.yaml` file is up-to-date.

```bash
# Project Specific
eksctl create cluster --name p1-demo --region us-east-1

###################################################################

# Generalized
eksctl create cluster --name {cluster-name} --region {your-aws-region}
```

### Project Manifest Yaml
While this is running, as mentioned above let's look at our yaml file for the p1demo project `Kubernetes/kubernetes-manifest.yaml`.

```yaml
apiVersion: apps/v1
kind: Deployment #The kind of object I'm creating
metadata:
  name: p1 #arbitrary but important name
spec:
  replicas: 3 #3 replica pods will be made for this deployment
  selector: #the selector and labels should match the ones in the service.yaml to create a pairing
    matchLabels:
      app: p1
  template:
    metadata:
      labels:
        app: p1
    spec:
      containers: #here's where we define info about the container that this deployment will spin up
        - name: p1 #this will be the name of the container
          image: 296271419447.dkr.ecr.us-east-1.amazonaws.com/p1-demo:latest #The image we want to base the container off of
          ports:
            - containerPort: 8080 #the container will run on port 8080 in the cluster
---
apiVersion: v1
kind: Service
metadata:
  name: api
spec:
  type: LoadBalancer #This time we'll provision a LoadBalancer (instead of NodePort). Better for production!
  selector:
    app: p1 # FOR EKS we change this app to be p1 so it knows what deployment to apply to our services
  ports:
    - port: 80 # FOR EKS change to port 80
      targetPort: 5000 #in accordance with the application.properties
```

### Generalized Manifest Yaml
While this is running, as mentioned above let's look at our yaml file generalized for a  project `Kubernetes/kubernetes-manifest.yaml`.

```yaml
apiVersion: apps/v1
kind: Deployment #The kind of object I'm creating
metadata:
  name: {app-name} #arbitrary but important name
spec:
  replicas: 3 
  selector: 
    matchLabels:
      app: {app-name} # must match to the selector.app in the service below
  template:
    metadata:
      labels:
        app: {app-name}
    spec:
      containers: #here's where we define info about the container that this deployment will spin up
        - name: {app-name} #this will be the name of the container
          image: {uri-to-ecr-repo-latest-image} #The image we want to base the container off of
          ports:
            - containerPort: {docker-container-exposed-port} # Fill this with the port exposed by your docker container
---
apiVersion: v1
kind: Service
metadata:
  name: {service-specific-name} # api is good default, but could be anything as well
spec:
  type: LoadBalancer 
  selector:
    app: {app-name} #must match to the select.matchLabels from the deployment above
  ports:
    - port: 80 # FOR EKS change to port 80, this is the default for http (https = port 443)
      targetPort: {exposed-application-port} #in accordance with the application.properties
```

### Apply the Manifest to your running EKS and watch the magic happen

Once our terminal is available to use again after our `eksctl create cluster` command has conluded, we can finally apply our manifest to the cluster and see our pods getting generated and exposed to the world-wide web all using the `kubectl` commands. Along with this we can begin to inspect our pods & services to get the information we need, such as the url to access our LoadBalancer exposed to the internet.

```bash
# Project Specific
# Apply the manifest to allow the cluster to begin pulling images to begin building our Application
kubectl apply -f Kubernetes/kubernetes-manifest.yaml

# Find the pods id, getting status and other important information
kubectl get pods
kubectl logs p1-69f5b6578b-2kmkt # specifically obtain the EKS logs from this running pod


# Find the services, this is were we find our external IP to the LoadBalancer to access our app
kubectl get svc
kubectl describe svc api # Obtain more detailed information about everything applied to this service

# Take the External IP from the LoadBalance to request to URI /p1/employee to see if this is working as expected

###################################################################

# Generalized
# Apply the manifest to allow the cluster to begin pulling images to begin building our Application
kubectl apply -f {path-to}/kubernetes-manifest.yaml

# Find the pods id, getting status and other important information
kubectl get pods
kubectl logs {pod-id-printed-above} # specifically obtain the EKS logs from this running pod


# Find the services, this is were we find our external IP to the LoadBalancer to access our app
kubectl get svc
kubectl describe svc {service-specific-name} # Obtain more detailed information about everything applied to this service

# Take the External IP from the LoadBalance to request to URI /p1/employee to see if this is working as expected

```

### Shut that cluster down

While it may only be a few dollars per day to keep this running, ideally shut this down as soon as you know you're not going to utilize it by the following `eksctl` command. You can always re-deploy this all after you've done it once. 

***Also a good idea to keep records of any hiccups so you can make a detailed `README.md` in your application repo on github incase anyone else wants to attempt to build your app.***

```bash
# eksctl 
eksctl delete cluster --name p1-demo

###################################################################

eksctl delete cluster --name {cluster-name}
```

# Lambda with AWS Controller for Kubernetes (ACK)

***Update coming***



