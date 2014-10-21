#!/usr/bin/python

# retrieves the instances public and private IPs from the amazon EC2
import boto
import boto.ec2

class EC2InstancesRetriever:

    def __init__(self):
        access_key = "AKIAIV45ZYABLMV25HBQ";
        secret_access = "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";
        conn = boto.ec2.connect_to_region("us-west-2", aws_access_key_id=access_key, aws_secret_access_key=secret_access)

        reservations = conn.get_all_instances()

        self.databases = []
        self.databasesP = []
        self.middlewares = []
        self.serversP = []
        self.clients = []

        for r in reservations:
            inst = r.instances[0]

            if 'Name' in inst.tags:
                name = inst.tags["Name"]

                if str(inst.state) == "running":
                    if name.find("database") != -1:
                        self.databases.append((inst.ip_address, inst.private_ip_address))
                    elif name.find("client") != -1:
                        self.clients.append((inst.ip_address, inst.private_ip_address))
                    elif name.find("middleware") != -1:
                        self.middlewares.append((inst.ip_address, inst.private_ip_address))
            else:
                print "There are instances with no names!"
                exit(1)

    def getDatabaseIP(self):
        return self.databases

    def getClientsIPs(self):
        return self.clients

    def getMiddlewaresIPs(self):
        return self.middlewares
