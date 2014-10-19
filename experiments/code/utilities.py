import pexpect
from os import system

def ssh_address(username, host):
    return username + "@" + host

# assumses logs directory is in the home directory
def clean_machine(username, host):
    child = pexpect.spawn("ssh " + ssh_address(username, host))
    child.expect("Last login:*")
    child.sendline("rm -rf ~/logs")
    child.expect("ubuntu@*")


def execute_command(username, host, command):
    system("ssh " + ssh_address(username, host) + " " + command)


# copies the file in the home directory of host
def scp_to(file, username, host):
    system("scp " + file + " " + ssh_address(username, host) + ":")


# copies the file from the host to the current directory
def scp_from(file, where, username, host):
    system("scp  " + ssh_address(username, host) + ":" + file + " " + where)

def create_properties_file(fileName, properties):
    fo = open(fileName, "w+")

    for property in properties:
        fo.write(property[0] + "=" + str(property[1]) + "\n")

    fo.close()
