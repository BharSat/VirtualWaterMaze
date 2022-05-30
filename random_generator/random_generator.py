import numpy as np
from numpy import random

def start_loc():
	arr1 = np.arange(3)
	arr2 = np.tile(arr1, [10, 1])
	arr3 = []
	for i in arr2:
		new_i = list(np.append(i, random.randint(0, 3)))
		random.shuffle(new_i)
		arr3.append(list(new_i))
	print(str(arr3).replace("[", "{").replace("]", "}"))
	return arr3

def platform_locations():
	radius = 5
	