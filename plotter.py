import sys
import numpy as np
import matplotlib.pyplot as plt

# parsing args
pname = sys.argv[1]

# reading ./tempfile
values_file = open('.tempfile')
data = [[float(x) for x in line.split(' ')] for line in values_file.read().splitlines() if line != '']

# spliting data
mid = int(len(data)/2)
sat, hsp = data[:mid], data[mid:]
problems = [i for i in range(len(sat))]

# sorting data by time
sat.sort()
hsp.sort()

# orgnizing for plotting
sat = np.array(sat)
hsp = np.array(hsp)

print(sat, hsp)
satTime, satSteps = sat[:,0], sat[:,1]
hspTime, hspSteps = hsp[:,0], hsp[:,1]

print(satTime, satSteps)
print(hspTime, hspSteps)

# plotting
fig, (p1, p2) = plt.subplots(1, 2)

p1.plot(problems, satTime)
p1.plot(problems, hspTime)
p1.set(xlabel='problems', ylabel='time')
p1.set_title('Time plot')

p2.plot(problems, satSteps)
p2.plot(problems, hspSteps)
p2.set(xlabel='problems', ylabel='steps')
p2.set_title('Steps plot')

# setting title
fig.suptitle('Performance of SAT based Planner vs HSP on {} problem'.format(pname))
plt.legend(['SAT', 'HSP'])

# plt.savefig('plot.pgf')

plt.show()
