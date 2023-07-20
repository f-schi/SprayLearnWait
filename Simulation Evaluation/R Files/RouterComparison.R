barchartplotter <- function(in.data, time, feature) {
  data <- in.data %>% filter(time == SimTime)
  if (feature == "delivery_prob") {
    plot = ggplot(data=data, aes(fill=Router, y=.data[[paste(feature,"mean",sep=".")]], x=as.factor(NoHosts))) +
      ylim(0, 1)+
      xlab("Network Participants")+
      ylab("Mean Delivery Probability")+
      ggtitle(paste("Average Message Delivery Probability over",time,sep=" ")) +
      geom_bar(position=position_dodge(), stat="identity", color="black") +
      geom_errorbar(aes(ymin=(.data[[paste(feature,"mean",sep=".")]]) - (.data[[paste(feature,"sd",sep=".")]]), ymax=(.data[[paste(feature,"mean",sep=".")]])+(.data[[paste(feature,"sd",sep=".")]])), width=.2,position=position_dodge(.9))+
      theme_publish()+
      theme(text = element_text(size = 20))
    print(plot)
    
  } else if (feature == "overhead_ratio") {
    
    plot = ggplot(data=data, aes(fill=Router, y=.data[[paste(feature,"mean",sep=".")]], x=as.factor(NoHosts))) +
      scale_y_sqrt(limits = c(0,2000))+
      xlab("Network Participants")+
      ylab("Mean Overhead Ratio")+
      ggtitle(paste("Average Network Overhead Ratio over",time,sep=" ")) +
      geom_bar(position=position_dodge(), stat="identity", color="black") +
      geom_errorbar(aes(ymin=(.data[[paste(feature,"mean",sep=".")]]) - (.data[[paste(feature,"sd",sep=".")]]), ymax=(.data[[paste(feature,"mean",sep=".")]])+(.data[[paste(feature,"sd",sep=".")]])), width=.2,position=position_dodge(.9))+
      theme_publish()+
      theme(text = element_text(size = 20))
    print(plot)
    
  } else if (feature == "latency_avg") {
    plot = ggplot(data=data, aes(fill=Router, y=.data[[paste(feature,"mean",sep=".")]], x=as.factor(NoHosts))) +
      scale_y_sqrt(limits = c(0,5000))+
      xlab("Network Participants")+
      ylab("Mean Message Delivery Latency")+
      ggtitle(paste("Average Latency of Message Delivery over",time,sep=" ")) +
      geom_bar(position=position_dodge(), stat="identity", color="black") +
      geom_errorbar(aes(ymin=(.data[[paste(feature,"mean",sep=".")]]) - (.data[[paste(feature,"sd",sep=".")]]), ymax=(.data[[paste(feature,"mean",sep=".")]])+(.data[[paste(feature,"sd",sep=".")]])), width=.2,position=position_dodge(.9))+
      theme_publish()+
      theme(text = element_text(size = 20))
    print(plot)
  } else {
    plot = ggplot(data=data, aes(fill=Router, y=.data[[paste(feature,"mean",sep=".")]], x=as.factor(NoHosts))) +
      xlab("Network Participants")+
      ylab("Mean Dropped and Removed Messages")+
      scale_y_sqrt()+
      ggtitle(paste("Average Dropped or Removed Messages over",time,sep=" ")) +
      geom_bar(position=position_dodge(), stat="identity", color="black") +
      geom_errorbar(aes(ymin=(.data[[paste(feature,"mean",sep=".")]]) - (.data[[paste(feature,"sd",sep=".")]]), ymax=(.data[[paste(feature,"mean",sep=".")]])+(.data[[paste(feature,"sd",sep=".")]])), width=.2,position=position_dodge(.9))+
      theme_publish()+
      theme(text = element_text(size = 20))
    print(plot)
  }
}

library(tidyverse)

data <- read.csv("RouterComparisonAverages.csv")

days <- c('2 days')
features <- c("delivery_prob", "overhead_ratio", "latency_avg", "dropped_removed_messages")

for (f in features) {
  for (d in days) {
    print(d)
    print(f)
    barchartplotter(data, d, f)
  }
}











ggsave("mtcars.png")