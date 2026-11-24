#!/bin/bash
set -e

mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<SQL
CREATE DATABASE IF NOT EXISTS finance_dev;
CREATE DATABASE IF NOT EXISTS finance_test;

GRANT ALL PRIVILEGES ON finance_dev.* TO '$MYSQL_USER'@'%';
GRANT ALL PRIVILEGES ON finance_test.* TO '$MYSQL_USER'@'%';
SQL