# Evaluating Recommenders

In this assignment, LensKit's evaluator is used to conduct offline evaluations of several recommender algorithms. The relative performance of the various algorithms are explored according to predefined metrics. An additional metric is used to measure diversity of recommendations.


## Overview

The core of this assignment is doing a comparative offline evaluation of the following algorithms using the included data set:

- Global mean rating (predict only)
- Global popularity (number of ratings, recommend only)
- Item mean rating
- Personalized mean rating ( ![equation](http://latex.codecogs.com/gif.latex?%24%5Cmu%20&plus;%20b_i%20&plus;%20b_u%24) )
- Three variants of LensKit's user-based collaborative filtering implementation
- Two variants of LensKit's item-based collaborative filtering implementation
- Two variants of a content-based filter built on Apache Lucene

The following metrics are evaluated:

- Coverage (the fraction of test predictions that could actually be made)
- Per-user RMSE
- nDCG (over predictions, also called *Predict nDCG*; this is a *rank effectiveness* measure)
- nDCG (over top-N recommendation, also called *Top-N nDCG*)
- MRR
- MAP
- A diversity metric (entropy over item tags)

The evaluation uses 5-fold cross-validation over included set of ratings data. 
**For this assignment, the mean of the metric results for each algorithm configuration is considered.**



## Writing a Metric

There are many ways to measure diversity, but for this assignment we use the _entropy_ of the tags of the items in a top-10 recommendation list. Entropy is, roughly, a measurement of how complicated it is to say which one of several possibilities has been picked. If there are many different tags represented among the movies, they will have high entropy; if there are very few tags, entropy will be low. We will use high entropy as an indication that the set of movies is diverse.


The entropy of a set ![equation](http://latex.codecogs.com/gif.latex?%24X%24) of things ![equation](http://latex.codecogs.com/gif.latex?%24x%24), each of which has a probability ![equation](http://latex.codecogs.com/gif.latex?%24P%28x%29%24), is defined as

 ![equation](http://latex.codecogs.com/gif.latex?%24%24H%28X%29%20%3D%20%5Csum_%7Bx%20%5Cin%20X%7D%20-P%28x%29%20%5Cmathrm%7Blog%7D_2%20P%28x%29%24%24)

