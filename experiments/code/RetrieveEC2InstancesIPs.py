#!/usr/bin/python

# retrieves the instances public and private IPs from the amazon EC2
import boto
import boto.ec2

access_key = "AKIAIV45ZYABLMV25HBQ";
secret_access = "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";
conn = boto.ec2.connect_to_region("us-west-2", aws_access_key_id=access_key, aws_secret_access_key=secret_access)

reservations = conn.get_all_instances()

databases = []
databasesP = []
middlewares = []
serversP = []
clients = []

for r in reservations:
    inst = r.instances[0]

    name = inst.tags["Name"]

    if str(inst.state) == "running":
        if name.find("database") != -1:
            databases.append((inst.public_dns_name, inst.private_ip_address))
        elif name.find("client") != -1:
            clients.append((inst.public_dns_name, inst.private_ip_address))
        elif name.find("middleware") != -1:
            middlewares.append((inst.public_dns_name, inst.private_ip_address))

def getDatabaseIP():
    return databases

def getClientsIPs():
    return clients

def getMiddlewaresIPs():
    return middlewares