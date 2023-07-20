# load necessary packages
library(tidyverse)
library(hexbin)

# load datasets
data10 <- read.csv("ThesisSimulation-Helsinki-SprayLearnWaitRouter-Group.nrofHosts10-Scenario.endTime172800.csv")
data20 <- read.csv("ThesisSimulation-Helsinki-SprayLearnWaitRouter-Group.nrofHosts20-Scenario.endTime172800.csv")
data30 <- read.csv("ThesisSimulation-Helsinki-SprayLearnWaitRouter-Group.nrofHosts30-Scenario.endTime172800.csv")
data40 <- read.csv("ThesisSimulation-Helsinki-SprayLearnWaitRouter-Group.nrofHosts40-Scenario.endTime172800.csv")
data50 <- read.csv("ThesisSimulation-Helsinki-SprayLearnWaitRouter-Group.nrofHosts50-Scenario.endTime172800.csv")

# subset Coordinates
data.Coord.10 <- data.frame(x = data10$CoordX,y = data10$CoordY)
data.Coord.20 <- data.frame(x = data20$CoordX,y = data20$CoordY)
data.Coord.30 <- data.frame(x = data30$CoordX,y = data30$CoordY)
data.Coord.40 <- data.frame(x = data40$CoordX,y = data40$CoordY)
data.Coord.50 <- data.frame(x = data50$CoordX,y = data50$CoordY)


#set colors
color_scale <- scale_fill_gradient(low = "lightblue", high = "darkblue", limits = c(0,350))

# set size of hexagons
bins=15

# list of all datasets
data.Coords <- list(data.Coord.10,data.Coord.20,data.Coord.30,data.Coord.40,data.Coord.50)

# plot hexmaps
for (d in data.Coords ) {
  hexmap <- ggplot(d, aes(x = x, y = y)) +
    geom_hex(bins =bins,color = "white", size=1.5)+
    color_scale +
    theme_void() +
    theme(
      plot.background = element_rect(fill = "white"),
      panel.grid = element_blank(),
      axis.text = element_blank(),
      axis.title = element_blank(),
      axis.ticks = element_blank(),
      legend.position = "none")
  plot(hexmap)
}

