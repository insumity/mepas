#!/usr/bin/python

# retrieves the instances public and private IPs from the amazon EC2
import boto
import boto.ec2
import time


class EC2Instantiator:
    def __init__(self, access_key, secret_access):
        self.conn = boto.ec2.connect_to_region("us-west-2", aws_access_key_id=access_key,
                                               aws_secret_access_key=secret_access)

    def createInstance(self, AMI, instanceType):
        reservation = self.conn.run_instances(
            AMI,
            instance_type=instanceType,
            security_groups=['everythingIsFine'],
            key_name='mepas',
            placement='us-west-2c'
        )

        instance = reservation.instances[0]

        while instance.state != 'running':
            time.sleep(5)
            instance.update()

        return instance

    def createDatabase(self, instanceType):
        databaseAMI = 'ami-cb450dfb'
        inst = self.createInstance(databaseAMI, instanceType)
        inst.add_tag("Name", "database")
        return inst

    def createClient(self, instanceType):
        generalAMI = 'ami-3db2fb0d'
        inst = self.createInstance(generalAMI, instanceType)
        inst.add_tag("Name", "client")
        return inst

    def createMiddleware(self, instanceType):
        generalAMI = 'ami-3db2fb0d'
        inst = self.createInstance(generalAMI, instanceType)
        inst.add_tag("Name", "middleware")
        return inst

    def terminateInstance(self, inst):
        self.conn.terminate_instances(instance_ids=[inst.id])