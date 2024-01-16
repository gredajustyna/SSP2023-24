from mininet.topo import Topo, MinimalTopo
from mininet.net import Mininet
from mininet.cli import CLI
from mininet.node import RemoteController
import random
import string
import time

class MyTopo(Topo):
    def __init__ (self):
        Topo.__init__(self)

        h1 = self.addHost('h1')
        h2 = self.addHost ('h2')
        h3 = self.addHost('h3')
        h4 = self.addHost('h4')
        h5 = self.addHost ('h5')
        s1 = self.addSwitch( 's1')
        s3 = self.addSwitch('s3')
        s2 = self.addSwitch('s2')
        s4 = self.addSwitch('s4')
        s5 = self.addSwitch('s5')
        s6 = self.addSwitch( 's6')
        h6 = self.addHost ('h6')
        h7 = self.addHost('h7')
        h8 = self.addHost('h8')
        h9 = self.addHost('h9')
        h10 = self.addHost('h10')
        self.addLink(h1,s1, port2=1)
        self.addLink(h2,s1, port2=2)
        self.addLink(h3,s1, port2=3)
        self.addLink(h4,s3, port2=1)
        self.addLink(h5,s3, port2=2)
        self.addLink(s1,s2, port1=4)
        self.addLink(s3,s2, port1=4)
        self.addLink(s2,s4)
        self.addLink(s4,s5, port2=4)
        self.addLink(s4,s6, port2=4)
        self.addLink(s5,h6, port1=1)
        self.addLink(s5,h7, port1=2)
        self.addLink(s5,h8, port1=3)
        self.addLink(s6,h9, port1=1)
        self.addLink(s6,h10, port1=2)

        print()

def topo():
    random.seed(0)
    topos = { 'mytopo': (lambda: MyTopo())}
    net = Mininet(topo=MyTopo(), controller=lambda name: RemoteController( name, ip='192.168.1.7', port=6653 ))
    hosts = net.hosts
    left = range(0,5)
    right = range(5,10)
    print(hosts)

    threads = []

    net.start()
    for i in range(2):
        source = random.choice(left)
        dest = random.choice(right)
        left.remove(source)
        right.remove(dest)
        source = hosts[source]
        dest = hosts[dest]

        source.cmd("cd /home/floodlight/D-ITG-2.8.1-r1023/bin")
        dest.cmd("cd /home/floodlight/D-ITG-2.8.1-r1023/bin")

        send_log = "send_{}_{}.log".format(source, dest).replace(" ", "_")
        recv_log = "recv_{}_{}.log".format(source, dest).replace(" ", "_")

        send_dbg_log = "dbg_send_{}_{}.txt".format(source, dest).replace(" ", "_")
        recv_dbg_log = "dbg_recv_{}_{}.log".format(source, dest).replace(" ", "_")

        print("Sending traffic from {} to {}".format(source, dest))
        dest.cmd("./ITGRecv -l {} >> {} 2>> {} &".format(recv_log, recv_dbg_log, recv_dbg_log))
        time.sleep(1)
        source.cmd("./ITGSend -a {} -l {} >> {} 2>> {} &".format(dest.IP(), send_log, send_dbg_log, send_dbg_log))


    CLI( net )
    net.stop()


if __name__ == "__main__":
    topo()
