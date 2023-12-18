# SSP2023-24
### Topology: 
<p align="center">
  <img width="406" alt="topology-SSP" src="https://github.com/gredajustyna/SSP2023-24/assets/56201394/6fbf9365-d348-4ea6-a5ea-4ed2d00e31b6">
</p>

### Chosen traffic generator: D-ITG 

https://traffic.comics.unina.it/software/ITG/documentation.php

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
4. ./ITGDec sender.log


### How to use startup script ###
1. Clone repo to floodlight VM home directory.
2. cd SSP2023-24
3. sudo python net.py

There might be a need to update python / install additional libraries (mininet).

### Pseudocode:
#### MANAGE-FLOWS(networkGraph, flowDB, networkMonitor)
```
for each flow in flowDB do
  ADD-FLOW-DATA(networkMonitor, flow.id, flow.history)
  CLASSIFY(flow.history)
end for
SEIZE-BW(networkGraph, flowDB)
RETURN-BW(networkGraph, flowDB)
SHARE-AVAILABLE-BW(networkGraph, flowDB)
for each flow in flowDB do
  COMMIT-DROP-RATE(flow)
end for
```

#### SEIZE-BW(networkGraph, flowDB)
```
for each flow in flowDB do
  lowerBound ← CALC-THRESHOLD(flow.tracking, flow.rate,
flow.history)
  if flow.class = DECREASE and flow.rate < lowerBound then
    decrease ← CALC-OPTIMAL-DECREASE(flow.tracking, flow.rate, flow.history)
    flow.tracking ← flow.tracking - decrease
    ADD-TO-AVAILABLE-POOLS(networkGraph, flow.route, decrease)
  end if
end for
```

#### RETURN-BW(networkGraph, flowDB)
```
for each flow in flowDB with flow not flow.flagged do
  if INSIDE-GROWTH-MARGIN(flow.rate, flow.margin) then
    available ← BW-ON-PATH(networkGraph, flow.route)
    optimal ← CALC-OPTIMAL-INCREASE(flow.tracking, flow.rate, flow.history)
    returned ← MIN(available, optimal)
    flow.tracking ← flow.tracking + returned
    CLAIM-BW(networkGraph, flow.route, returned)
    remaining ← optimal - returned
    if remaining > 0 then
      other ← GET-LOWEST-PRIORITY(flowDB, flow)
      while remaining > 0 do
        returned ← returned + TAKE-BW(networkGraph, flowDB, other, remaining)
        flow.tracking ← flow.tracking + returned
        remaining ← remaining − returned
        other ← GET-NEXT-LOWEST(flowDB, other, flow)
      end while
    end if
    flow.flagged ← GROWING
  end if
end for
```

#### SHARE-BW(networkGraph, flowDB)
```
for flow in flowDB from flow.priority = HIGHEST to
flow.priority = LOWEST with flow not flow.flagged do
  lowerBound ← flow.reserved − CALC-NOMINALWINDOW(flow.rate, flow.history)/2
  if flow.tracking ≥ lowerBound then
    available ← BW-ON-PATH(networkGraph, flow.route)
    optimal ← CALC-OPTIMALINCREASE(flow.tracking, flow.rate, flow.history)
    borrowed ← MIN(available, optimal)
    flow.tracking ← flow.tracking + borrowed
    CLAIM-BW(networkGraph, flow.route)
  end if
end for
```

### Bibliography:

1. Boley, Josh, Jung, Eun-Sung, Kettimuthu, R., Rao, Nageswara S., and Foster, I. Adaptive QoS for Data Transfers using Software-Defined Networking. United States: N. p., 2016. Web. doi:10.1109/ANTS.2016.7947874
