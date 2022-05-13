import random_generator as rg

def write_start_loc():
	file = open("data.csv", "wt")
	arr1 = rg.start_loc()
	for set in arr1:
		str_set = str(set)
		str_set = str_set.replace("[", " ")
		str_set = str_set.replace("]", " ")
		file.write(str_set + "\n")
	file.close()
write_start_loc()