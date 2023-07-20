# Code Submission - Bachelor Thesis Frederick Schindlegger

Welcome to the repository for my Bachelor's thesis on the topic of **Data Efficient Routing in Opportunistic Networks based on mobility prediction and reinforcement learning**. I have developed the routing protocol *SprayLearnWait* for use in opportunistic networks. This repository contains the implementation and embedding of the protocol in the ONE simulator, the reports of the conducted simulation series, and their analysis.

## Navigation
Here is a brief overview of the structure of this repository:
- **Plots and Figures**: Contains data visualization and graphics, as well as R scripts for generating graphics and statistical models.
- **Simulation Evaluation**: Includes all the reports of simulation series for parameter tuning, comparison of different protocols, and investigation of the reinforcement learning parameters of the proposed protocol.
- **the-one-1.6.0**: Contains the simulation environment with the embedded protocol.
Within the scope of the Bachelor's thesis, the most important areas are:
    - **.\core\DTNHost**: Functions for generating the required data for the protocol are added here.
    - **.\routing\SprayLearnWaitRouter**: Contains the specific routing logic of the proposed protocol.
    - **.\R**: Contains the R scripts for Clusternalaysis and Reinforcement learning.

## Router Settings
The proposed routing protocol for the Bachelor's thesis, *SprayLearnWait*, requires the following settings parameters for the simulation:

- **executionLogging**: Boolean value that determines whether processed data should be logged. This is also used to verify the correct information processing in the protocol.

- **directionMode** (Factor{1,2,3}): Determines in which direction the velocity should point.
    1 = Running average of movement direction
    2 = Direction towards the average location
    3 = Direction towards the destination

- **runningAvgWeight**: Sets the weighting of new readings in the calculation of the running average. The default value is 0, which corresponds to no weighting.

- Initial configuration of **clusterHeight** and **waitTime** (or **sleepTime**).

- The following parameters define the states of the Q-Table:
    **minSleepTime**, **maxSleepTime**, **incSleepTime**,
    **minClusterHeight**, **maxClusterHeight**, **incClusterHeight**.

- The following settings define the alpha, gamma, and epsilon of Q-learning:
    **rlLearningRate**
    **rlDiscountFactor**
    **rlExplorationFactor**