import math
import numpy
import pylab
from pprint import pprint

f = open("rssidata")

linenum = 0

dist = [0,1,2,3,4,5,7,10,15,20,25,30,40,50,60,70,80,90,100,120,140,160,180,200]

a1 = [] 
a2 = []
for l in f:
  
  a1.append(dist[int(math.floor(linenum / 10))])
  a2.append(int(l))
  linenum += 1
  
z = numpy.polyfit(a1,a2, 4)
p = numpy.poly1d(z)

pprint(z)

#pylab.figure()
#pylab.plot(a1, a2)

pylab.figure()
pylab.scatter(a1, a2, label="measured values")
pylab.plot(a1, p(a1), 'r', label="fitted function")
pylab.legend()
pylab.xlabel("Distance (cm)")
pylab.ylabel("Signal strength (rssi)")
pylab.show()


