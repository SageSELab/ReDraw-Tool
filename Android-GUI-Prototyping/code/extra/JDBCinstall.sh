#!/bin/bash
curl JDBC.tar.gz https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.41.tar.gz
tar -xzf JDBC.tar.gz 
cd mysql-connector-java-5.1.41
PATH=$PWD/mysql-connector-java-5.1.41-bin