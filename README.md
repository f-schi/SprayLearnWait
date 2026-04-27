# Spray-Learn-Wait: Reinforcement Learning and Movement Prediction for Adaptive Routing in Opportunistic Networks

This repository contains the implementation and evaluation framework for the paper:

**Reinforcement learning and movement prediction for adaptive routing in opportunistic networks**
Frederick Schindlegger, Thomas Hupperich
*Computer Networks, Volume 278 (2026), Article 112084*
DOI: [https://doi.org/10.1016/j.comnet.2026.112084](https://doi.org/10.1016/j.comnet.2026.112084)

---

## 📄 Abstract (Paper)

Opportunistic Networks are mobile ad-hoc networks operating in intermittently connected environments, where nodes exchange data only upon contact. Efficient routing must adapt to highly dynamic topologies.

We propose **Spray-Learn-Wait**, a routing protocol that combines:

* clustering-based movement prediction
* reinforcement learning for adaptive decision-making

Compared to Epidemic, First Contact, and ProPHET, the approach improves delivery probability while reducing overhead and dropped messages, while maintaining minimal data sharing for privacy and sustainability.

---

## 📁 Repository Structure

```
Plots and Figures/
    ├── Visualization outputs used in the paper
    └── R scripts for statistical analysis and plotting

Simulation Evaluation/
    ├── Parameter tuning studies
    ├── Protocol comparisons
    └── Reinforcement learning experiments

the-one-1.6.0/
    ├── Modified ONE simulator environment
    ├── core/DTNHost
    ├── routing/SprayLearnWaitRouter
    └── R/ analysis scripts
```

---

## ⚙️ Spray-Learn-Wait Routing Protocol

The implementation extends the ONE simulator with a custom routing protocol that integrates mobility prediction and reinforcement learning.

### 🔧 Logging

* `executionLogging` *(Boolean)*: Enables internal process logging for debugging and validation
* `loggingPath` *(String)*: Output directory for logs

### 🧭 Movement Prediction

* `directionMode` *(1–3)*:

  * `1`: Running average of movement direction
  * `2`: Direction toward average location
  * `3`: Direction toward destination
* `runningAvgWeight` *(Double)*: Weight of new observations in running average

### 🧩 Clustering & Waiting Strategy

* `clusterHeight` *(Double)*: Initial clustering threshold
* `sleepTime` *(Double)*: Initial waiting time (ms)

### 🧠 Reinforcement Learning State Space

Defines discretization boundaries:

* `minSleepTime`, `maxSleepTime`, `incSleepTime`
* `minClusterHeight`, `maxClusterHeight`, `incClusterHeight`

### 📊 Q-Learning Parameters

* `rlLearningRate` (α)
* `rlDiscountFactor` (γ)
* `rlExplorationFactor` (ε)

---

## 🧪 Example Configuration

```text
SprayLearnWaitRouter.directionMode = 1
SprayLearnWaitRouter.runningAvgWeight = 100.0
SprayLearnWaitRouter.clusterHeight = 1.0  
SprayLearnWaitRouter.sleepTime = 2000.0

SprayLearnWaitRouter.executionLogging = true
SprayLearnWaitRouter.loggingPath = C:\\Users\\fschi\\Desktop

SprayLearnWaitRouter.minSleepTime = 100.0
SprayLearnWaitRouter.maxSleepTime = 4000.0
SprayLearnWaitRouter.incSleepTime = 100.0

SprayLearnWaitRouter.minClusterHeight = 0.5
SprayLearnWaitRouter.maxClusterHeight = 1.25
SprayLearnWaitRouter.incClusterHeight = 0.05

SprayLearnWaitRouter.rlLearningRate = 0.8
SprayLearnWaitRouter.rlDiscountFactor = 0.7
SprayLearnWaitRouter.rlExplorationFactor = 0.5
```

---

## 🚀 How to Run

1. Install the **ONE Simulator (v1.6.0)**
2. Replace/merge the provided `the-one-1.6.0` directory into your simulator installation
3. Compile the simulator:

   ```bash
   ant
   ```
4. Run simulations using predefined configuration files in the evaluation folder:

   ```bash
   java -jar one.jar <scenario_file>
   ```

---

## 📊 Reproducing Results

To reproduce results from the paper:

* Use scenarios in `Simulation Evaluation/`
* Enable logging via `executionLogging = true`
* Run batch simulations for statistical averaging
* Use R scripts in `Plots and Figures/` for:

  * performance visualization
  * clustering analysis
  * RL parameter evaluation

---

## 📚 Citation

If you use this work in academic research, please cite:

```bibtex
@article{SCHINDLEGGER2026112084,
  title   = {Reinforcement learning and movement prediction for adaptive routing in opportunistic networks},
  journal = {Computer Networks},
  volume  = {278},
  pages   = {112084},
  year    = {2026},
  issn    = {1389-1286},
  doi     = {https://doi.org/10.1016/j.comnet.2026.112084},
  url     = {https://www.sciencedirect.com/science/article/pii/S1389128626000964},
  author  = {Frederick Schindlegger and Thomas Hupperich},
  keywords = {Opportunistic networks, Routing, Machine learning}
}
```
