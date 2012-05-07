import numpy
import pylab
import math
from collections import defaultdict
from pprint import pprint

#Formula created by fitting data from measurements
#distance returned in centimeters
def rssi_to_distance(rssi):
  return 1.64581191e-07 * rssi ** 4 + -9.53330296e-05 * rssi ** 3 + \
         0.0183046719 * rssi ** 2 + -1.43727619 * rssi + 11.7808933

f = open("testdata3", "r")

d = defaultdict(list)

a = numpy.zeros(91)
for g in range(-90, 1):
  a[abs(g)] = rssi_to_distance(g)


for line in f:
  split = line.split(",")
  
  acc = [float(split[1]), float(split[2]), float(split[3])]
  d2 = defaultdict(int)
  d2['acc'] = acc
  d2['rssi'] = int(split[4])
  d[split[0]].append(d2)


size = len(d['0014.4F01.0000.3917'])

time_step = 1.0

#speed in m/s
old_speed = numpy.zeros(3)
speed = numpy.zeros(3)

old_pos = numpy.zeros(3)
pos = numpy.zeros(3)
filtered_distance = numpy.zeros(size)
dist_accel = numpy.zeros(size)
dist_rssi = numpy.zeros(size)
distance = 0

#balance factor: 1 is trust accelerometer completely, 0 is trust radio signal completely
B = 0.5


for k in range(1, size):
  
  #Estimate new distance and speed based on accelerometer
  speed[0] = old_speed[0] + (d['0014.4F01.0000.3917'][k]['acc'][0] * 9.81 * time_step)
  speed[1] = old_speed[1] + (d['0014.4F01.0000.3917'][k]['acc'][1] * 9.81 * time_step)
  speed[2] = old_speed[2] + (d['0014.4F01.0000.3917'][k]['acc'][2] * 9.81 * time_step)
  
  pos[0] = old_pos[0] + (speed[0] * time_step)
  pos[1] = old_pos[1] + (speed[1] * time_step)
  pos[2] = old_pos[2] + (speed[2] * time_step)
  
  #calc distance from 0
  distance = math.sqrt(numpy.sum([math.pow(x, 2) for x in pos]))
  #math.sqrt(math.pow(position[0], 2) + math.pow(position[1], 2) + math.pow(position[2], 2))
  
  #check nearby nodes (just the basestation for this version) for position and signal strength,
  # and see if the distance matches
  rssi = abs(d['0014.4F01.0000.3917'][k]['rssi'])
  pos_base = [0.0, 0.0, 0.0]
  distance_2 = math.sqrt(numpy.sum([math.pow(x, 2) for x in pos_base]))
  
  distance_between = abs(distance - distance_2)
  
  filtered_distance[k] = B * distance_between + (1 - B) * a[rssi]
  dist_rssi[k] = a[rssi]
  dist_accel[k] = distance_between
  ratio = filtered_distance[k] / distance_between 
  
  for l in range(3):
    pos[l] = ratio * pos[l] #TODO: base distance not used
    
    speed[l] = (pos[l] - old_pos[l]) * time_step
  pprint(speed)
  for u in range(3):
    old_pos[u] = pos[u]
    old_speed[u] = speed[u]
  
  #time update
  #xhatminus[k] = xhat[k-1]
  #P[k] = Pminus[k]
  
  #measurement update
  #K[k] = Pminus[k] / (Pminus[k] + R)
  #xhat[k] = xhatminus[k] + K[k] * (z[k] - xhatminus[k])
  #P[k] = (1-K[k])*Pminus[k]
pprint(filtered_distance)

pylab.figure()
pylab.plot(filtered_distance, 'b-', label="test data")
pylab.plot(dist_rssi, 'g', label="signal str. data")
pylab.plot(dist_accel, 'y', label="accel. data")
pylab.plot([0,28,56], [0, 200, 0], 'r-', label="truth value")
#pylab.axhline(a[14],color='g',label='truth value')
pylab.legend()
pylab.show()