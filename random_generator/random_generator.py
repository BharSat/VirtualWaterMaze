import importlib

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
	return str(arr3).replace("[", "{").replace("]", "}")


def platform_locations():
	radius = 9
	rng = random.default_rng(69813)
	set1 = rng.normal(0, radius, 20)
	tmp = []
	set2 = []
	for i in set1:
		tmp.append(round(i, 2))
		if len(tmp) == 2:
			set2.append(tmp)
			tmp = []
	return str(set2).replace("[", "{").replace("]", "}")
