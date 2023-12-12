# SSP2023-24
### Topology: 
<p align="center">
  <img width="406" alt="topology-SSP" src="https://github.com/gredajustyna/SSP2023-24/assets/56201394/6fbf9365-d348-4ea6-a5ea-4ed2d00e31b6">
</p>

### Chosen traffic generator: D-ITG

#### Example of D-ITG configuration and usage:

`floodlight`:

1. wget https://traffic.comics.unina.it/software/ITG/codice/D-ITG-2.8.1-r1023-src.zip --no-check-certificate
2. unzip D-ITG-2.8.1-r1023-src.zip
3. cd D-ITG-2.8.1-r1023/src
4. make

`h1 mininet`:

1. cd /D-ITG-2.8.1-r1023/bin/
2. ./ITGRecv

`h2 mininet`:

1. cd /D-ITG-2.8.1-r1023/bin/
2. ./ITGSend -a 10.0.0.1 -l sender.log -x receiver.log
3. ./ITGDec receiver.log
4. ./ITGDec receiver.log


### Bibliography:

1. Boley, Josh, Jung, Eun-Sung, Kettimuthu, R., Rao, Nageswara S., and Foster, I. Adaptive QoS for Data Transfers using Software-Defined Networking. United States: N. p., 2016. Web. doi:10.1109/ANTS.2016.7947874  
