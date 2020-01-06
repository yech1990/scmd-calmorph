#!/usr/bin/env pythonDouble
# -*- coding: utf-8 -*-
#
# Copyright © 2019 yech <yech1990@gmail.com>
# Distributed under terms of the MIT license.
#
# Created: 2019-11-29 01:25

"""
parse tif image from hcs of yeast
"""

import cv2
import numpy as np
import sys
from skimage import restoration


def normalize_image(img, q1=0.1, q2=0.99):
    """for 16 bits image.

    normalized_img = np.zeros((raw_img.shape))
    normalized_img = cv2.normalize(
        raw_img, normalized_img, 0, 2 ** 16 - 1, cv2.NORM_MINMAX
    )
    """
    cmin = np.quantile(img, q1)
    cmax = np.quantile(img, q2)
    img[img < cmin] = cmin
    img[img > cmax] = cmax
    img = (img - cmin) / (cmax - cmin) * (2 ** 16 - 1)
    img = img.astype("uint16")
    return img


def deconvolve_image(img):
    psf = np.ones((200, 200)) / 1
    #  deconvolved, _ = restoration.unsupervised_wiener(img, psf)
    deconvolved = restoration.richardson_lucy(img, psf, 10)
    return deconvolved


if __name__ == "__main__":
    raw_img = cv2.imread(sys.argv[1], -1)

    normalized_img = normalize_image(raw_img, 0.800, 0.999)
    #  normalize_image = deconvolve_image(normalized_img)

    #  kernel = np.array([[0, 0, 0], [0, 1, 0], [0, 0, 0]])
    #  normalized_img = cv2.filter2D(normalized_img, -1, kernel)

    # 16 bits to 8 bits
    normalized_img = (normalized_img / 256).astype("uint8")

    # hist normalize (only support 8 bits)
    #  normalized_img = cv2.equalizeHist(normalized_img)

    # show image
    cv2.namedWindow("image", cv2.WINDOW_NORMAL)
    cv2.resizeWindow("image", 800, 800)
    cv2.imshow("image", normalized_img)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

    # save image
    cv2.imwrite(sys.argv[2], normalized_img)
