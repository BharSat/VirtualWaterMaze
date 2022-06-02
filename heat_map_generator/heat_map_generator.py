#!/usr/bin/python3
import tkinter as tk
import tkinter.ttk as ttk
from tkinter.filedialog import askopenfile

import cv2
import numpy as np


class heat_map_generator:
    def __init__(self, master=None):
        # build ui
        self.window = ttk.Frame(master)
        self.GoButton = ttk.Button(self.window)
        self.GoButton.configure(
            cursor="diamond_cross",
            default="normal",
            takefocus=False,
            text="Geanerate Map",
        )
        self.GoButton.grid(column="1", padx="5", pady="5", row="20", sticky="s")
        _wcmd = lambda wid="GoButton": self.generateMap(wid)
        self.GoButton.configure(command=_wcmd)
        self.radiusL = ttk.Label(self.window)
        self.radiusL.configure(font="TkHeadingFont", takefocus=False, text="Radius:")
        self.radiusL.grid(column="0", padx="2", pady="2", row="0")
        self.radiusE = ttk.Entry(self.window)
        self.radiusTV = tk.StringVar(value="")
        self.radiusE.configure(
            exportselection="true", takefocus=False, textvariable=self.radiusTV
        )
        self.radiusE.grid(column="1", row="0")
        self.radiusE.bind("<Enter>", self.enterRadius, add="")
        self.radiusE.bind("<Leave>", self.leaveRadius, add="")
        self.funcTV = tk.StringVar(value="")
        self.funcL = ttk.Label(self.window)
        self.funcL.configure(font="TkHeadingFont", text="Function(<function> int)")
        self.funcL.grid(column="0", row="1")
        self.funcE = ttk.Entry(self.window, textvariable=self.funcTV)
        self.funcE.configure(font="TkDefaultFont")
        self.funcE.grid(column="1", row="1")
        self.funcE.bind("<Enter>", self.enterFunction, add="")
        self.funcE.bind("<Leave>", self.leaveFunction, add="")
        self.previewL = ttk.Label(self.window)
        self.previewL.configure(text="Preview Before Saving:")
        self.previewL.grid(column="0", row="3")
        self.previewC = ttk.Checkbutton(self.window)
        self.previewC.grid(column="1", row="3")
        self.helpM = tk.Message(self.window)
        self.helpTV = tk.StringVar(
            value="Radius: The radius of the watermaze.\nFunction: the function to be run on every x/y Value before running.\nEg: + 50"
        )
        self.helpM.configure(
            background="#8942ff",
            cursor="arrow",
            foreground="#42ff59",
            text="Radius: The radius of the watermaze.\nFunction: the function to be run on every x/y Value before running.\nEg: + 50",
        )
        self.helpM.configure(textvariable=self.helpTV)
        self.helpM.grid(
            column="0",
            columnspan="2",
            ipadx="0",
            ipady="0",
            padx="0",
            pady="5",
            row="21",
        )
        self.window.configure(height="200", width="200")
        self.window.grid(column="0", row="0")
        self.dTV = tk.StringVar(value="")
        self.dL = ttk.Label(self.window)
        self.dL.configure(font="TkHeadingFont", text="Delimiter:")
        self.dL.grid(column="0", row="4")
        self.dE = ttk.Entry(self.window, textvariable=self.dTV)
        self.dE.configure(font="TkDefaultFont")
        self.dE.grid(column="1", row="4")

        # Main widget
        self.mainwindow = self.window

    def run(self):
        self.mainwindow.mainloop()

    def generateMap(self, widget_id):
        r = int(self.radiusTV.get())
        func = self.funcE.get().lower().split(" ")
        func.remove("")
        func.remove(" ")
        if len(func) != 2:
            raise ValueError("Invalid Function")
        zeroes = np.zeros((r, r, 3))
        cv2.circle(zeroes, r // 2, r, (255, 255, 255))
        file = askopenfile()
        try:
            file.read()
        except Exception as e:
            print("Failed: " + str(e))

    def enterRadius(self, event=None):
        self.helpTV.set("The radius of the watermaze.")

    def leaveRadius(self, event=None):
        self.helpTV.set(
            "Radius: The radius of the watermaze.\nFunction: the function to be run on every x/y Value before running.\nEg: + 50")

    def enterFunction(self, event=None):
        self.helpTV.set("<function> int")

    def leaveFunction(self, event=None):
        self.helpTV.set(
            "Radius: The radius of the watermaze.\nFunction: the function to be run on every x/y Value before running.\nEg: + 50")


if __name__ == "__main__":
    root = tk.Tk()
    app = heat_map_generator(root)
    app.run()
