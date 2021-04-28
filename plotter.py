import sys
import numpy as np
import matplotlib.pyplot as plt

# parsing args
pname = sys.argv[1]

# reading ./tempfile
values_file = open('.tempfile')
data = [[float(x) for x in line.split(' ')] for line in values_file.read().splitlines() if line != '']

# spliting data
#mid = int(len(data)/2)

# sorting data by time

print(data)
data.sort()
print(data)
problems = [i for i in range(len(data))]

# orgnizing for plotting
hspTime = [e[0] for e in data]
hspSteps = [e[1] for e in data]
satTime = [e[2] for e in data]
satSteps = [e[3] for e in data]

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
