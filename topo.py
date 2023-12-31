from mininet.topo import Topo

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
        self.addLink(h1,s1)
        self.addLink(h2,s1)
        self.addLink(h3,s1)
        self.addLink(h4,s3)
        self.addLink(h5,s3)
        self.addLink(s1,s2)
        self.addLink(s3,s2)
        self.addLink(s2,s4)
        self.addLink(s4,s5)
        self.addLink(s4,s6)
        self.addLink(s5,h6)
        self.addLink(s5,h7)
        self.addLink(s5,h8)
        self.addLink(s6,h9)
        self.addLink(s6,h10)

topos = { 'mytopo': (lambda: MyTopo())}
