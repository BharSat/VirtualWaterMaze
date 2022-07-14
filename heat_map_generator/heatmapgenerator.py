#!/usr/bin/python3
import tkinter as tk
import tkinter.ttk as ttk
from tkinter.filedialog import askopenfile, asksaveasfilename

import cv2
import numpy as np


class HeatMapGenerator:
    def __init__(self, master=None):
        # build ui
        self.window = ttk.Frame(master)
        self.GoButton = ttk.Button(self.window)
        self.GoButton.configure(
            cursor="diamond_cross",
            default="normal",
            takefocus=False,
            text="Generate Map",
        )
        self.GoButton.grid(column=1, padx=5, pady=5, row=20, sticky="s")
        self.GoButton.configure(command=self.generate_map)
        self.radiusL = ttk.Label(self.window)
        self.radiusL.configure(font="TkHeadingFont", takefocus=False, text="Radius:")
        self.radiusL.grid(column=0, padx=2, pady=2, row=0)
        self.radiusE = ttk.Entry(self.window)
        self.radiusTV = tk.StringVar(value="")
        self.radiusE.configure(
            exportselection=True, takefocus=False, textvariable=self.radiusTV
        )
        self.radiusE.grid(column=1, row=0)
        self.radiusE.bind("<Enter>", self.enter_radius, add="")
        self.radiusE.bind("<Leave>", self.leave_radius, add="")
        self.previewL = ttk.Label(self.window)
        self.previewL.configure(text="Preview Before Saving:")
        self.previewL.grid(column=0, row=3)
        self.previewC = ttk.Checkbutton(self.window)
        self.previewC.grid(column=1, row=3)
        self.helpM = tk.Message(self.window)
        self.helpTV = tk.StringVar(
            value="Radius: The radius of the water maze."
        )
        self.helpM.configure(
            background="#636281",
            cursor="arrow",
            foreground="#aaffaa",
            text="Radius: The radius of the water maze.",
        )
        self.helpM.configure(textvariable=self.helpTV)
        self.helpM.grid(
            column=0,
            columnspan=3,
            ipadx=0,
            ipady=0,
            padx=0,
            pady=5,
            row=21,
        )
        self.window.configure(height="200", width="200")
        self.window.grid(column=0, row=0)
        self.dTV = tk.StringVar(value="")
        self.dL = ttk.Label(self.window)
        self.dL.configure(font="TkHeadingFont", text="Delimiter:")
        self.dL.grid(column=0, row=4)
        self.dE = ttk.Entry(self.window, textvariable=self.dTV)
        self.dE.configure(font="TkDefaultFont")
        self.dE.grid(column=1, row=4)

        # Main widget
        self.main_window = self.window

    def run(self):
        self.main_window.mainloop()

    def generate_map(self, widget_id=None):
        try:
            r = int(self.radiusTV.get())
            print(r)
            zeroes = np.zeros((2 * r, 2 * r, 3))
            cv2.circle(zeroes, (r, r), r - 2, (255, 255, 255))
            file = askopenfile()
            data = file.readlines()
            l = 0
            for set in data:
                if set.lower() == "initializing":
                    print("yes")
                    continue
                set_split = set.split(self.dTV.get().replace("/t", "\t"))
                if len(set_split) != 2:
                    raise ValueError(f"Illegal set '{set}'")
                try:
                    x, y = set_split
                except ValueError as e:
                    raise Exception(str(e))

                print(int(x) - 1, int(y) - 1)
                zeroes[int(x) - 1, int(y) - 1] += 1
            cv2.imshow("Final", zeroes)
            cv2.waitKey(0)
            cv2.destroyAllWindows()
            file_save = asksaveasfilename(confirmoverwrite=True, defaultextension=".png")
            cv2.imwrite(file_save, zeroes)


        except Exception as e:
            print("Failed due to " + str(type(e)) + ": " + str(e))
            self.helpTV.set("Failed due to " + str(type(e)) + ": " + str(e))
            self.window.after(10000, lambda: self.helpTV.set("Radius: The radius of the water maze."))
            return

    def enter_radius(self, event=None):
        self.helpTV.set("The radius of the water maze.")

    def leave_radius(self, event=None):
        self.helpTV.set("Radius: The radius of the water maze.")


if __name__ == "__main__":
    root = tk.Tk()
    app = HeatMapGenerator(root)
    app.run()
