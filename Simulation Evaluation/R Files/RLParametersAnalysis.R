#import packages
library(tidyverse)

#load data
data10 <- read.csv("ThesisSimulation-Helsinki-SprayLearnWaitRouter-Group.nrofHosts10-Scenario.endTime172800.csv")
data20 <- read.csv("ThesisSimulation-Helsinki-SprayLearnWaitRouter-Group.nrofHosts20-Scenario.endTime172800.csv")
data30 <- read.csv("ThesisSimulation-Helsinki-SprayLearnWaitRouter-Group.nrofHosts30-Scenario.endTime172800.csv")
data40 <- read.csv("ThesisSimulation-Helsinki-SprayLearnWaitRouter-Group.nrofHosts40-Scenario.endTime172800.csv")
data50 <- read.csv("ThesisSimulation-Helsinki-SprayLearnWaitRouter-Group.nrofHosts50-Scenario.endTime172800.csv")


#aggregate data in 24 intervalls of simclock
meanValueBySimClock <- function(feature, interval) {
  a <- data10%>%
    group_by(grp = findInterval(SimClock, seq(min(SimClock), max(SimClock), {{interval}}))) %>%
    summarise(feature = mean({{feature}}))
  
  b <- data20%>%
    group_by(grp = findInterval(SimClock, seq(min(SimClock), max(SimClock), {{interval}}))) %>%
    summarise(feature = mean({{feature}}))
  
  c <- data30%>%
    group_by(grp = findInterval(SimClock, seq(min(SimClock), max(SimClock), {{interval}}))) %>%
    summarise(feature = mean({{feature}}))
  
  d <- data40%>%
    group_by(grp = findInterval(SimClock, seq(min(SimClock), max(SimClock), {{interval}}))) %>%
    summarise(feature = mean({{feature}}))
  
  e <- data50%>%
    group_by(grp = findInterval(SimClock, seq(min(SimClock), max(SimClock), {{interval}}))) %>%
    summarise(feature = mean({{feature}}))
  
  combined_df <- dplyr::bind_rows(data10, data20, data30, data40, data50)
  f <- combined_df %>%
    group_by(grp = findInterval(SimClock, seq(min(SimClock), max(SimClock), {{interval}}))) %>%
    summarise(feature = mean({{feature}}))
  
  df <- data.frame('10'=a$feature,'20'=b$feature,'30'=c$feature,'40'=d$feature,'50'=e$feature)
  return(df)
}

#select features to be plotted
features <- c("waitTime","clusterHeight","avgCluster","avgNodesPerCluster","FreeBufferSpacePercent","reward")
features <- c("waitTime")
#plot line graph
for (feature in features) {
  ylab <- str_replace(feature, "^\\w{1}", toupper)
  plotlist <- meanValueBySimClock(.data[[feature]],172800/24)
  plotlist <- plotlist %>%   mutate(time = row_number()*172800/24) %>%  gather(nodes, feature, -time)
  plot <- ggplot(data=plotlist, aes(x = time, y = feature,color = nodes)) +
    geom_smooth(se=FALSE, size=1.5, alpha=0.8)+
    scale_color_discrete(name = "Number of Nodes", labels = c("36", "66", "96","126","156"))+
    ggtitle(paste("Mean", str_replace(feature, "^\\w{1}", toupper),"over Simulation Time",sep=" ")) +
    theme_publish()+
    ylab(ylab)+
    theme(text = element_text(size = 20),
          plot.title = element_text(size=15),
          axis.text.x = element_text(size = 15),
          axis.text.y = element_text(size = 15),  
          axis.title.x = element_text(size = 18),
          axis.title.y = element_text(size = 18))
  plot(plot)
}

# generate linear models
data.total <- rbind(data10,data20,data30,data40,data50)

summary(lm(reward ~ SimClock, data10))
summary(lm(reward ~ SimClock, data20))
summary(lm(reward ~ SimClock, data30))
summary(lm(reward ~ SimClock, data40))
summary(lm(reward ~ SimClock, data50))
summary(lm(reward ~ SimClock, data.total))
summary(lm(reward ~ iteration, data10))
summary(lm(reward ~ iteration, data20))
summary(lm(reward ~ iteration, data30))
summary(lm(reward ~ iteration, data40))
summary(lm(reward ~ iteration, data50))
summary(lm(reward ~ iteration, data.total))

summary(lm(waitTime ~ SimClock, data10))
summary(lm(waitTime ~ SimClock, data20))
summary(lm(waitTime ~ SimClock, data30))
summary(lm(waitTime ~ SimClock, data40))
summary(lm(waitTime ~ SimClock, data50))
summary(lm(waitTime ~ SimClock, data.total))
summary(lm(waitTime ~ iteration, data10))
summary(lm(waitTime ~ iteration, data20))
summary(lm(waitTime ~ iteration, data30))
summary(lm(waitTime ~ iteration, data40))
summary(lm(waitTime ~ iteration, data50))
summary(lm(waitTime ~ iteration, data.total))

