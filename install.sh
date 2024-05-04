#!/bin/sh
# install sw needed
echo "Starting install procedure from shell script..."
sudo apt-get -y update >/dev/null 2>&1
sudo apt-get -y upgrade >/dev/null 2>&1
sudo apt-get -y install zip >/dev/null 2>&1
sudo apt-get -y install unzip >/dev/null 2>&1
sudo apt-get -y install fail2ban >/dev/null 2>&1
sudo apt-get -y install ufw >/dev/null 2>&1
sudo apt-get -y install certbot >/dev/null 2>&1
sudo apt-get -y install nginx-full >/dev/null 2>&1
sudo apt-get -y install nodejs >/dev/null 2>&1
sudo apt-get -y install npm >/dev/null 2>&1
sudo apt-get -y install openjdk-17-jre >/dev/null 2>&1
sudo apt-get -y install openjdk-17-jdk >/dev/null 2>&1
sudo apt-get -y install gradle >/dev/null 2>&1
# start main procedure
sudo ./gradlew install
echo "Done."
# done
