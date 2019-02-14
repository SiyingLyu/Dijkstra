# dijkstra
Implement the dijkstra to do the image simplification.

## Vertical Cut and Image Resizing

Note that an image is 2-dimensional array of pixels

[98, 251, 246]  [34, 0, 246]  [255, 246, 127]  [21, 0, 231]
[25, 186, 221]  [43, 9, 127]  [128, 174, 100]  [88, 1, 143]
[46, 201, 132]  [23, 5, 217]  [186, 165, 147]  [31, 8, 251]

We calculated the distance from the pixels. From the distance matrix, we apply S2S (set of vertice to set of vertices) shortest path
algorithm to find the Min-Cut from the picture.
