from retrieve_ec2_instances import *
from clean_database import *
from subprocess import call

get_clients()
get_middlewares()

# clean database
databases = get_databases()
host = databases[0][0]
username = "ubuntu"
password = "mepas$1$2$3$"
dbname = "mepas"

#recreate_database(host, dbname, username, password)
#initialize_database(host, dbname, username, password, 5, 5, "../src/main/resources/auxiliary_functions.sql")

# create the jar
call(["cd", "..", ";", "ant", "jar"])

