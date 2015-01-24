import pexpect
from os import system

def getSSHAddress(username, host):
    return username + "@" + host

# assumes "logs" directory is in the home directory
def cleanMachine(username, host):
    child = pexpect.spawn("ssh " + getSSHAddress(username, host))
    child.expect("Last login:*")
    child.sendline("rm -rf ~/logs")
    child.expect("ubuntu@*")
    child.sendline("rm -f *_errors.out")
    child.expect("ubuntu@*")
    child.sendline("rm -f *.properties")
    child.expect("ubuntu@*")
    child.sendline("rm *errors.out")
    child.expect("ubuntu@*")

def executeCommand(username, host, command):
    system("ssh " + getSSHAddress(username, host) + " " + command)

# copies the file in the home directory of host
def scpTo(file, newFileName, username, host):
    system("scp " + file + " " + getSSHAddress(username, host) + ":" + newFileName)

# copies the file from the host to the current directory
def scpFrom(file, where, username, host):
    system("scp -C " + getSSHAddress(username, host) + ":" + file + " " + where)

def createPropertiesFile(fileName, properties):
    fo = open(fileName, "w+")

    for property in properties:
        fo.write(property[0] + "=" + str(property[1]) + "\n")

    fo.close()

def createPath(directories, fileName):
    path = ""

    firstTime = True
    for directory in directories:
        if not firstTime:
            path = path + "/" + directory
        else:
            firstTime = False
            path = directory
    return path + "/" + fileName
