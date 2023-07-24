#Build States Matrix

waitTime <- seq(minWaitTime, maxWaitTime, by = incWaitTime)
clusterHeight <- seq(minClusterHeight,maxClusterHeight, by=incClusterHeight)

statesTable <- data.frame(clusterHeight= merge(clusterHeight,waitTime)[1]$x,
                          waitTime = merge(clusterHeight,waitTime)[2]$y)
statesMatrix <- matrix(seq(1,nrow(statesTable),1), 
                       nrow = length(clusterHeight), 
                       ncol= length(waitTime))

#Returns all possible states reachable from current state with possible actions
computeNextReachableStates <- function(c, tab, mat) {
  l = dim(mat)[1]
  smax = dim(mat)[1]*dim(mat)[2]
  #Compute all States in 1-Hop Distance to currentState
  n <- sapply(c(-l-1,-l,-l+1,-1,0,1,l-1, l, l+1), function(x) x+c)

  #Filter cases, where currentState is at edge of matrix
  if (tab[c,]$clusterHeight == min(tab$clusterHeight)) {
    n <- sort(n[n > 0 & n <= smax & !(n %in% c(c-1,c+l-1,c-l-1))])
  }
  else if (tab[c,]$clusterHeight == max(tab$clusterHeight)) {
    n <- sort(n[n > 0 & n <= smax & !(n %in% c(c+1,c+l+1,c-l+1))])
  }
  else {
    n <- sort(n[n > 0 & n <= smax])
  }
  return(n)
}

#load Qtable and State of past routing phase
Q <- matrix(unlist(Qtable), nrow = length(clusterHeight), ncol= length(waitTime))
currentState <- which(statesTable$clusterHeight == currentClusterHeight
                      & statesTable$waitTime == currentWaitTime)

#compute list of possible next states
nextStates <- computeNextReachableStates(currentState, tab=statesTable, mat=statesMatrix)

#decide between exploration and exploitation via a coin toss according to exploration factor
coinToss = runif(1)
if (coinToss < explorationFactor) {
  # exploration: choose random reachable state
  nextState <- sample(nextStates, 1)
} else {
  # exploitation: choose reachable state with max Q-Value
  nextState <- nextStates[which(Q[nextStates] == max(Q[nextStates]))]
  if(length(nextState) > 1) {
    nextState <- sample(nextState, 1)
  }
}
print("Test5")
# Update Q values for next states.
Q[currentState] <- Q[currentState] + learningRate * (reward + discountFactor * max(Q[computeNextReachableStates(nextState, tab=statesTable, mat=statesMatrix)])-Q[currentState])

# output updated Qtable, waitTime and clusterHeight values
output <- append(as.vector(Q),c(statesTable[nextState,]$clusterHeight,statesTable[nextState,]$waitTime))


