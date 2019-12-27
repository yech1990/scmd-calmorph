#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright © 2019 yech <yech1990@gmail.com>
# Distributed under terms of the MIT license.
#
# Created: 2019-12-28 15:23

"""
remove background of image
"""

from sys import argv

import cv2
import numpy as np
from PIL import Image

# Init
I = Image.open(argv[1])
W, H = I.size
print(W, H)
A = W * H
D = [sum(c) for c in I.getdata()]
Bh = [0] * H
Ch = [0] * H
Bv = [0] * W
Cv = [0] * W

# Flood-fill
Background = 3 * 255 + 1
S = [0]
while S:
    i = S.pop()
    c = D[i]
    if c != Background:
        D[i] = Background
        Bh[i // W] += c
        Ch[i // W] += 1
        Bv[i % W] += c
        Cv[i % W] += 1
        S += [
            (i + o) % A for o in [1, -1, W, -W] if abs(D[(i + o) % A] - c) < 10
        ]

# Eliminate "trapped" areas
for i in range(H):
    Bh[i] /= float(max(Ch[i], 1))

for i in range(W):
    Bv[i] /= float(max(Cv[i], 1))

for i in range(A):
    a = (Bh[i // W] + Bv[i % W]) / 2
    if D[i] >= a:
        D[i] = Background


D = (np.reshape(D, [H, W]) / 3).astype("uint8")
print(D)
cv2.namedWindow("image", cv2.WINDOW_NORMAL)
cv2.resizeWindow("image", W, H)
cv2.imshow("image", D)
cv2.waitKey(0)
cv2.destroyAllWindows()
