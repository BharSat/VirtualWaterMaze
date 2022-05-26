import numpy as np
from numpy import random

def start_loc():
	arr1 = np.arange(3)
	arr2 = np.tile(arr1, [280, 1])
	arr3 = []
	for i in arr2:
		new_i = list(np.append(i, random.randint(0, 3)))
		random.shuffle(new_i)
		arr3.append(new_i)
	print(arr3)
	return arr3

def platform_locations():
	radius = 5
	