#clusterFeatures to vectors
speed <- unlist(clusterFeatures[1])
direction <- unlist(clusterFeatures[2])

#transform direction angle(degree) to sin(),cos() representation
dirSin = sin(direction * (pi / 180))
dirCos = cos(direction * (pi) / 180)

#normalize speed to [-1,1] (from Simulation Range 0 to 15)
speed <- sapply(speed, function(v) (2*(v/13.9)-1))

#build dataframe from features
df <- data.frame(speed=speed,
                 dirSin=dirSin,
                 dirCos=dirCos)

#compute distance matrix
dmat <- dist(df)
#perform cluster analysis
tree <- hclust(d=dmat, method="average")
#cut tree of clusters at var clusterHeight
cut <- cutree(tree, h=clusterHeight)

