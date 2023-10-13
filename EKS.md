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

# Helm 

Brief aside about Helm is a package manager for Kubernetes applications, simplifying the deployment and management of applications on Kubernetes clusters. It enables you to define, install, and upgrade even the most complex Kubernetes applications.

[Helm Documentation](https://helm.sh/docs/)

## Key Concepts

### Chart

- A Helm package is called a **Chart**.
- A Chart is a collection of pre-configured Kubernetes resources that define a set of microservices.
- Charts can be shared and reused across different projects.

### Repository

- A **Repository** is a place where Helm Charts can be stored and retrieved.
- Helm Charts can be stored in public repositories like Helm Hub or private repositories.

### Release

- An instance of a Helm Chart installed in a Kubernetes cluster is called a **Release**.
- Multiple releases of the same chart can coexist in a single cluster.

### Values

- **Values** are parameters that can be passed to a Helm Chart during installation.
- They allow customization of the configuration settings in the Chart.

### Template

- Helm uses Go templates to render YAML files dynamically.
- **Templates** enable parameterization and reuse of YAML configurations across different environments.

## **How Helm Works**

**Client-Server Architecture:**
- Helm follows a client-server architecture where the Helm client interacts with the Tiller server deployed in the Kubernetes cluster.

**Chart Packaging:**
- Charts are packaged into a compressed archive that includes the chart's YAML files, templates, and metadata.

**Tiller Server:**
- Tiller is the server-side component of Helm installed in the Kubernetes cluster.
- It manages the release lifecycle and interacts with the Kubernetes API server.

**Helm Client:**
- Helm CLI is the client-side component that runs on the developer's machine.
- It sends commands to Tiller to manage Helm releases.

## **Helm Charts Anatomy**

**Chart.yaml:**
- Contains metadata about the chart, including version, description, and dependencies.

**Values.yaml:**
- Default configuration values used by the templates.

**templates/:**
- Directory containing Go templates for Kubernetes YAML files.

## **Helm Common Commands**

```bash
# Install a chart to the current namespace
helm install {release-name} {chart-name}

# Uninstall a chart to the current namespace
helm uninstall {release-name} {chart-name}

# List all releases for a specified namespace (uses current namespace by default)
helm list

# Search for a repo with helm
helm search repo {keyword}
```

# Lambda with AWS Controller for Kubernetes (ACK)

## Install the ACK service controller for Lambda

Log into the Helm registry that stores the ACK charts:

```bash
# Note this command ONLY works if you specify us-east-1 as your region, all other regions are locked from public access to the ECR public repo. This is an AWS thing.
aws ecr-public get-login-password --region us-east-1 | helm registry login --username AWS --password-stdin public.ecr.aws
```

Deploy the ACK service controller for Amazon Lambda using the [lambda-chart Helm chart](https://gallery.ecr.aws/aws-controllers-k8s/lambda-chart). This example creates resources in the `us-east-1` region, but you can use any other region supported in AWS.

```bash
# The following command will install and create a space on our cluster to house our ack-system on our kubernetes cluster. This will pull the chart from aws's ecr.
helm install --create-namespace -n ack-system oci://public.ecr.aws/aws-controllers-k8s/lambda-chart --version=1.3.4 --generate-name --set=aws.region=us-east-1
```

For a full list of available values to the Helm chart, please [review the values.yaml file](https://github.com/aws-controllers-k8s/lambda-controller/blob/main/helm/values.yaml).

## Configure IAM permissions

Once the service controller is deployed [configure the IAM permissions](https://aws-controllers-k8s.github.io/community/docs/user-docs/irsa/) for the
controller to invoke the Lambda API. For full details, please review the AWS Controllers for Kubernetes documentation
for [how to configure the IAM permissions](https://aws-controllers-k8s.github.io/community/docs/user-docs/irsa/). If you follow the examples in the documentation, use the
value of `lambda` for `SERVICE`.

# IAM Policy

## OIDC Identity provider

The OpenIDConnect (OIDC) identity provider for you EKS clusteris an IAM entity that describes an external identity provider service.This is useful when creating a mobile app or web application that requires access to AWS resources, but you don't want to create custom sign-in code or manage your own user identities. 

```bash
# Project specific
eksctl utils associate-iam-oidc-provider --cluster p1-demo --region us-east-1 --approve
```

## lambda-ack-policy
The below JSON is found from the aws-controller-k8s github page for their [recommended inline policy](https://github.com/aws-controllers-k8s/lambda-controller/blob/main/config/iam/recommended-inline-policy). We must go to IAM, creating the below policy called `lambda-ack-policy`. 

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "lambda:*",
                "s3:Get*",
                "ecr:Get*",
                "ecr:BatchGet*",
                "ec2:DescribeSecurityGroups",
                "ec2:DescribeSubnets",
                "ec2:DescribeVpcs"
            ],
            "Resource": "*"
        },
        {
            "Action": "iam:PassRole",
            "Condition": {
                "StringEquals": {
                    "iam:PassedToService": "lambda.amazonaws.com"
                }
            },
            "Effect": "Allow",
            "Resource": "*"
        }
    ]
}
```

Once the above is done, we can attach this policy to our IAM role `ack-lambda-controller`. Within the Trust Relationshipo of our role here, we should provide the following for our specific project:

### Project Specific
We can find out our `oidc-provider` by executing the following:

```bash
aws eks describe-cluster --name p1-demo --region us-east-1 --query "cluster.identity.oidc.issuer" --output text | sed -e "s/^https:\/\///"

# Result should look like: 
#      oidc.eks.us-east-1.amazonaws.com/id/6A8EFD31DB95967B09554C72A53A2BA7
```

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::296271419447:oidc-provider/oidc.eks.us-east-1.amazonaws.com/id/6A8EFD31DB95967B09554C72A53A2BA7"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "oidc.eks.us-east-1.amazonaws.com/id/6A8EFD31DB95967B09554C72A53A2BA7:sub": "system:serviceaccount:ack-system:ack-lambda-controller"
        }
      }
    },
    {
      "Effect": "Allow",
      "Principal": {
          "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

Here is a generalized variant to replace in the sections wrapped with `{}`

### Generalized

Find out your `oidc-provider` by running the following command:

```bash
aws eks describe-cluster --name {app-name} --region {your-aws-region} --query "cluster.identity.oidc.issuer" --output text | sed -e "s/^https:\/\///"

# Result should look like: 
#      oidc.eks.us-east-1.amazonaws.com/id/6A8EFD31DB95967B09554C72A53A2BA7
```

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::{AWS_ACCOUNT_ID}:oidc-provider/{oidc-provider}"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "{oidc-provider}:sub": "system:serviceaccount:ack-system:ack-lambda-controller"
        }
      }
    },
    {
      "Effect": "Allow",
      "Principal": {
          "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

### Associate IAM Role to a Service 

Finally we must annotate the service account for our ack-system to include the newly created role. Once that's complete we must restart this service so those new environment variables are accessible to the service.

```bash
# This adds the annotation of our ack-lambda-controller role so the service knows to integrate with it when started
kubectl annotate serviceaccount -n ack-system ack-lambda-controller eks.amazonaws.com/role-arn=arn:aws:iam::296271419447:role/ack-lambda-controller

# This refreshes the system to allow for the lambda-chart deployment to be connected to the IAM role it needs to deploy a Lambda Function
kubectl -n ack-system rollout restart deployment lambda-chart-1697222412
```

## Create Lambda function handler - SpringWithLambda

### Dockerfile

***IMPORTANT*** Prior to running out `docker build` Run `mvn clean package` on the singular function you wish to keep, this will generate the aws.jar we need to include within our docker image.

```dockerfile
FROM public.ecr.aws/lambda/java:11

#the docker build command is running at my project root directory
COPY target/*-aws.jar ${LAMBDA_TASK_ROOT}/lib/

CMD ["org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest"]
```

```bash
# Project Specific
# Create our Lambda Image, best testing would be to manually pull the image on AWS as this is specific to AWS 
docker build -t spring-lambda .

# Create ECR Repo on AWS, one should be made per image
aws ecr create-repository --repository-name spring-lambda

# Authenticate Your Terminal to access the ECR
# NOTE: If you're following a long from above most likely you won't need to do this again
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 296271419447.dkr.ecr.us-east-1.amazonaws.com

# Tag your image to the ECR Repo
docker tag spring-lambda:latest 296271419447.dkr.ecr.us-east-1.amazonaws.com/spring-lambda:latest

# Push to the ECR Repo
docker push 296271419447.dkr.ecr.us-east-1.amazonaws.com/spring-lambda

###################################################################

# Generalized
# Create our Lambda Image, best testing would be to manually pull the image on AWS as this is specific to AWS 
docker build -t {name-of-project-image} .

# Create ECR Repo on AWS, one should be made per image
aws ecr create-repository --repository-name {name-of-project-image}

# Authenticate Your Terminal to access the ECR
aws ecr get-login-password --region {your-aws-region} | docker login --username AWS --password-stdin {aws-account-id}.dkr.ecr.{your-aws-region}.amazonaws.com

# Tag your image to the ECR Repo
docker tag {name-of-project-image}:latest {aws-account-id}.dkr.ecr.us-{your-aws-region}.amazonaws.com/{name-of-project-image}:latest

# Push to the ECR Repo
docker push {aws-account-id}.dkr.ecr.{your-aws-region}.amazonaws.com/{name-of-project-image}
```
## Lambda Function & FunctionURLConfig YAML

[Lambda Function YAML Docs](https://aws-controllers-k8s.github.io/community/reference/lambda/v1alpha1/function/)

[Lambda FunctionURLConfig YAML Docs](https://aws-controllers-k8s.github.io/community/reference/lambda/v1alpha1/functionurlconfig/)

### Project Specific 

```yaml
apiVersion: lambda.services.k8s.aws/v1alpha1
kind: FunctionURLConfig
metadata:
  name: url-config
spec:
  authType: NONE
  functionName: lambda-oci-ack
---
apiVersion: lambda.services.k8s.aws/v1alpha1
kind: Function
metadata:
  name: lambda-oci-ack
  annotations:
    services.k8s.aws/region: us-east-1
spec:
  name: lambda-oci-ack
  packageType: Image
  code:
    imageURI: 296271419447.dkr.ecr.us-east-1.amazonaws.com/lamba-get-items:latest
  role: arn:aws:iam::296271419447:role/ack-lambda-controller
  description: function created by ACK lambda-controller e2e tests
  memorySize: 256
```

### Generalized 

```yaml
apiVersion: lambda.services.k8s.aws/v1alpha1
kind: FunctionURLConfig
metadata:
  name: url-config
spec:
  authType: NONE
  functionName: lambda-oci-ack
---
apiVersion: lambda.services.k8s.aws/v1alpha1
kind: Function
metadata:
  name: lambda-oci-ack
  annotations:
    services.k8s.aws/region: {your-aws-region}
spec:
  name: lambda-oci-ack
  packageType: Image
  code:
    imageURI: {aws-account-id}.dkr.ecr.{your-aws-region}.amazonaws.com/lamba-get-items:latest
  role: arn:aws:iam::{aws-account-id}:role/ack-lambda-controller
  description: function created by ACK lambda-controller e2e tests
  memorySize: 256
```

### Generate AWS Lambda through Kubernetes
Once you've generated this file, navigate to it in your terminal so we can begin using our `kubectl` command again to let kubernete control AWS Services!!!

```bash
# Command creates the function on AWS Lambda
kubectl create -f function.yaml

# Command will give all the details including the Function URL Endpoint
kubectl describe function/lambda-oci-ack

## NOTE: The Function URL will fail until you proceed forward to the next step
```

### Expose URL to public

In this step, we utilize `aws lambda` command to add permissions to our function that allow our Function URL endpoint to have public access, so it can handle traffic from the world-wide web. 

```bash
# Command to add new permissions to the Lambda Function
aws lambda add-permission --function-name lambda-oci-ack --statement-id FunctionURLAllowPublicAccess --principal "*" --action "lambda:InvokeFunctionUrl" --function-url-auth-type NONE
```

You can now make requests to the Function URL Endpoint! Enjoy!

### Deletion of Function

```bash
# Command will kill the function
kubectl delete -f function.yaml
```
