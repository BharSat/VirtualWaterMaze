import csv

import matplotlib.pyplot as plt
import numpy as np

with open('locs.csv', newline='') as csvfile:
    spamreader = csv.reader(csvfile, delimiter=' ', quotechar='"')
    final = []
    for row in spamreader:
        ch = [float(n.replace(",", "").replace("f", "")) for n in row]
        final.append(ch[0])
        final.append(ch[1])
    y = final

# make data:
y = np.array(y)
x = np.linspace(0, 10, len(y))

# plot
fig, ax = plt.subplots()

ax.plot(x, y)

plt.show()
