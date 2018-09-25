#!/bin/bash

REGION=$(curl 169.254.169.254/latest/meta-data/placement/availability-zone/ | sed 's/[a-z]$//')

sudo yum update -y

sudo yum install ruby wget -y

cd /home/ec2-user

wget https://aws-codedeploy-$REGION.s3.amazonaws.com/latest/install

chmod +x ./install

sudo ./install auto