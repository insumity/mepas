#!/usr/bin/python

# retrieves the instances public and private IPs from the amazon EC2
import boto
import boto.ec2


class EC2InstancesRetriever:
    def __init__(self, access_key, secret_access):
        conn = boto.ec2.connect_to_region("us-west-2", aws_access_key_id=access_key,
                                          aws_secret_access_key=secret_access)

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
                        self.databases.append((inst.ip_address, inst.private_ip_address, inst.instance_type))
                    elif name.find("client") != -1:
                        self.clients.append((inst.ip_address, inst.private_ip_address, inst.instance_type))
                    elif name.find("middleware") != -1:
                        self.middlewares.append((inst.ip_address, inst.private_ip_address, inst.instance_type))
            else:
                print "There are instances with no names!"
                exit(1)

    def getDatabaseIP(self, numberOfDatabasesToRetrieve):
        return self.databases[0:numberOfDatabasesToRetrieve]

    def getClientsIPs(self, numberOfClientsToRetrieve):
        return self.clients[0:numberOfClientsToRetrieve]

    def getMiddlewaresIPs(self, numberOfMiddlewaresToRetrieve):
        return self.middlewares[0:numberOfMiddlewaresToRetrieve]
