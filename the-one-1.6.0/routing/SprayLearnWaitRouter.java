package routing;

import core.*;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Thesis Router of F-SCHI
 */
public class SprayLearnWaitRouter extends ActiveRouter {

    /**
     * Constructor. Creates a new message router based on the settings in
     * the given Settings object.
     * @param s The settings object
     */

    /**
     * F-Schi
     * Adjustable Parameters
     */

    public static final String PROJECT_ROUTER = "SprayLearnWaitRouter";

    //Movement Parameter
    protected Integer directionMode;

    //Cluster Analysis Parameter
    protected Double clusterHeight;

    //Hibernation Parameters
    protected Double sleepTime;
    protected Double wakeTime;

    //RL Reward Parameters
    protected Double averageClusters;
    protected Double averageNodesPerCluster;
    protected Integer runningAvgCounter;

    //Logging Parameter
    protected Boolean executionLogging;

    //RL Q-Table Parameters
    protected List<Double> Qtable;
    protected Double minSleepTime;
    protected Double maxSleepTime;
    protected Double incSleepTime;
    protected Double minClusterHeight;
    protected Double maxClusterHeight;
    protected Double incClusterHeight;
    protected Double sizeQTable;
    protected Double rlLearningRate;
    protected Double rlDiscountFactor;
    protected Double rlExplorationFactor;

    //Renjin Parameter
    private final ScriptEngine renjin;


    public SprayLearnWaitRouter(Settings s) {
        super(s);
        Settings projectSettings = new Settings(PROJECT_ROUTER);

        //Load Parameters from Settings
        this.directionMode = projectSettings.getInt("directionMode");
        this.runningAvgWeight = projectSettings.getDouble("runningAvgWeight");
        this.clusterHeight = projectSettings.getDouble("clusterHeight");
        this.sleepTime = projectSettings.getDouble("sleepTime");
        this.executionLogging = projectSettings.getBoolean("executionLogging");
        this.minSleepTime = projectSettings.getDouble("minSleepTime");
        this.maxSleepTime = projectSettings.getDouble("maxSleepTime");
        this.incSleepTime = projectSettings.getDouble("incSleepTime");
        this.minClusterHeight = projectSettings.getDouble("minClusterHeight");
        this.maxClusterHeight = projectSettings.getDouble("maxClusterHeight");
        this.incClusterHeight = projectSettings.getDouble("incClusterHeight");
        this.rlLearningRate = projectSettings.getDouble("rlLearningRate");
        this.rlDiscountFactor = projectSettings.getDouble("rlDiscountFactor");
        this.rlExplorationFactor = projectSettings.getDouble("rlExplorationFactor");

        //initialize other parameters
        this.wakeTime = SimClock.getTime() + sleepTime;
        this.sizeQTable = (((maxSleepTime-minSleepTime)/incSleepTime)+1)
                * (((maxClusterHeight-minClusterHeight)/incClusterHeight)+1);
        this.Qtable = new ArrayList<Double>(Collections.nCopies(sizeQTable.intValue(), 0.0));
        this.averageClusters = 0.0;
        this.averageNodesPerCluster = 0.0;
        this.runningAvgCounter = 0;

        //initialize renjin
        RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
        this.renjin = factory.getScriptEngine();
    }

    /**
     * Copy constructor.
     * @param r The router prototype where setting values are copied from
     */
    protected SprayLearnWaitRouter(SprayLearnWaitRouter r) {
        super(r);
        this.directionMode = r.directionMode;
        this.runningAvgWeight = r.runningAvgWeight;
        this.clusterHeight = r.clusterHeight;
        this.sleepTime = r.sleepTime;
        this.minSleepTime = r.minSleepTime;
        this.maxSleepTime = r.maxSleepTime;
        this.incSleepTime = r.incSleepTime;
        this.minClusterHeight = r.minClusterHeight;
        this.maxClusterHeight = r.maxClusterHeight;
        this.incClusterHeight = r.incClusterHeight;
        this.rlLearningRate = r.rlLearningRate;
        this.rlDiscountFactor = r.rlDiscountFactor;
        this.rlExplorationFactor = r.rlExplorationFactor;
        this.wakeTime = r.wakeTime;
        this.Qtable = r.Qtable;
        this.averageClusters = r.averageClusters;
        this.averageNodesPerCluster = r.averageNodesPerCluster;
        this.runningAvgCounter = r.runningAvgCounter;
        RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
        this.renjin = factory.getScriptEngine();
        this.executionLogging = r.executionLogging;
    }


    @Override
    public void update() {
        super.update();

        if (isTransferring() || !canStartTransfer()) {
            return; // transferring, don't try other connections yet
        }

        //Try first the messages that can be delivered to final recipient
        if (exchangeDeliverableMessages() != null) {
            return; // started a transfer, don't try others (yet)
        }

        //Perform Routing Logic if wakeTime is reached
        if (SimClock.getTime() >= wakeTime) {

            // Get Node Features to be included in Cluster Analysis
            List <List> clusterFeatures = this.getClusterFeatures();

            // Perform Cluster Analysis
            List <Integer> clusterResults = this.performClusterAnalysis(clusterFeatures, this.clusterHeight);

            // Deliver Messages according to cluster analysis
            this.deliverMessagesToClusters(clusterResults);

            //Calculate RL Rewards
            Double rlReward = this.computeRewards();

            //Perform Reinforcement Learning: Update Q-Table, select next actions and update local parameters
            this.performRL(rlReward);

            // Set wakeTime for next iteration
            this.wakeTime = SimClock.getTime() + this.sleepTime;

            //Write internal parameters to CSV (Only for Analysis Purposes)
            writeParametersToCSV(rlReward);
        }
    }

    private List getClusterFeatures() {

        //list of connected Hosts
        List<DTNHost> hosts = this.getConnectedHosts();

        //list of Name per Host
        List<String> hostsName = this.getConnectedHostsNames(hosts);

        //list of RunningAvgOfSpeedWelford per Host
        List<Double> hostsSpeed = this.getConnectedHostsSpeed(hosts);

        //list of Directions per Host depending on DirectionMode
        List<Double> hostsDirection = this.getConnectedHostsDirectionByDirMode(hosts, this.directionMode);

        if(this.executionLogging) {
            System.out.println("--------------------");
            System.out.println("-Delivery Phase-");
            System.out.println("Name:" + hostsName);
            System.out.println("Speed:" + hostsSpeed);
            System.out.println("Direction:" + hostsDirection);
        }

        //Merge Features to Output ArrayList
        List<List> clusterFeatures = new ArrayList<>();
        clusterFeatures.add(hostsSpeed);
        clusterFeatures.add(hostsDirection);
        return clusterFeatures;
    }

    public List performClusterAnalysis(List<List> clusterFeatures, Double clusterHeight) {

        Vector resultsClusterAnalysis = null;
        //Execute ClusterAnalysis.R and capture Output Vector
        String filePathRCode = "R" + File.separator + "ClusterAnalysis.R";
        try {
            renjin.put("clusterFeatures", clusterFeatures);
            renjin.put("clusterHeight", clusterHeight);
            resultsClusterAnalysis = (Vector) renjin.eval(new FileReader(filePathRCode));
        } catch (FileNotFoundException | ScriptException e) {
            System.out.println("An error occurred while executing DataAnalysis.R");
        }
        //Transform Output Vector to ArrayList
        List <Integer> clusters = new ArrayList<>();
        for (int i = 0; i < resultsClusterAnalysis.length(); i++) {
            clusters.add(resultsClusterAnalysis.getElementAsInt(i) - 1); //Let the result start at 0
        }
        //Update Clustering Perfomance Metrics
        this.updateClusteringPerformanceMetrics(clusters);
        if(this.executionLogging) {System.out.println("Cluster Analysis Result:" + clusters);}

        return clusters;
    }

    private void deliverMessagesToClusters(List <Integer> clusters) {

        //list of connections
        List<Connection> connections = this.getHostConnections();

        //list of connected Hosts
        List<DTNHost> hosts = this.getConnectedHosts();

        //list of Name per Host
        List<String> hostsName = this.getConnectedHostsNames(hosts);

        //list of messages
        List<Message> messages = this.getHostMessages();

        //index list of hosts
        List <Integer> indices = IntStream.range(0, hostsName.size()).boxed().toList();

        // List of FreeBufferSizes of all connected hosts
        List<Integer> hostsFBS = this.getConnectedHostsFBS(hosts);

        //Initialize Array storing which Messages are to be delivered to which Host
        List <List> messagesDestinations = new ArrayList<List>();
        for(DTNHost h: hosts){
            messagesDestinations.add(new ArrayList<Message>());
        }

        for(int cluster : IntStream.range(0, Collections.max(clusters)+1).toArray()) {

            if(this.executionLogging) {System.out.println("Evaluating Cluster Nr.: " + cluster);}


            //current cluster batch indices
            List <Integer> indicesCurrentBatch = new ArrayList<>(indices);
            //batch of hosts of current cluster
            List <DTNHost> hostsCurrentBatch = new ArrayList<>(hosts);
            //batch of hostsFBS of current cluster
            List <Integer> hostsFBSCurrentBatch = new ArrayList<>(hostsFBS);

            //remove each element of index, where index of clusters != cluster) aka. remove non-cluster members
            int shift = 0;
            for(int i: indices) {
                if(!clusters.get(i).equals(cluster)) {
                    indicesCurrentBatch.remove(i - shift);
                    hostsCurrentBatch.remove(i - shift);
                    hostsFBSCurrentBatch.remove(i - shift);
                    shift += 1;
                }
            }


            if(clusters.get(0) == cluster) {
                //Case: Active Host is member of current cluster itself
                messagesDestinations = deliveryHomeCluster(hostsCurrentBatch,
                        indicesCurrentBatch,
                        hostsFBSCurrentBatch,
                        messages,
                        messagesDestinations,
                        hosts);

            } else {
                //Case: Active Host is not member of current cluster itself
                messagesDestinations = deliveryForeignCluster(hostsCurrentBatch,
                        indicesCurrentBatch,
                        hostsFBSCurrentBatch,
                        messages,
                        messagesDestinations,
                        hosts);
            }
        }

        if(this.executionLogging) {System.out.println("MessageDestinations: " + messagesDestinations);}

        Integer iterator = 0;
        for(List messageBundle: messagesDestinations) {
            Boolean noMessages = false;
            if (messageBundle.size() == 0) {
                noMessages = true;
            }
            if (noMessages == false) {
                if (iterator >= 1) {
                    Connection recipient = connections.get(iterator-1);
                    List <Connection> recipientAsList = new ArrayList<>();
                    recipientAsList.add(recipient);
                    tryMessagesToConnections(messageBundle, recipientAsList);
                    if(this.executionLogging) {
                        System.out.println("Sending Messages: " + messageBundle.toString() + " to Host: " + hostsName.get(iterator) );
                    }
                }
            }
            iterator += 1;
        }
    }

    private List<List> deliveryHomeCluster(List<DTNHost> hostsCurrentBatch,
                                           List<Integer> indicesCurrentBatch,
                                           List<Integer> hostsFBSCurrentBatch,
                                           List<Message> messages,
                                           List<List> messagesDestinations,
                                           List<DTNHost> hosts){
        if(this.executionLogging) {System.out.println("Cluster Members:" + hostsCurrentBatch);}

        for (Message message : messages) {
            if(this.executionLogging) {
                System.out.println("Evaluating Message: " + message.getId() +  " with size: " + message.getSize());
            }
            //Check if message already exists in Cluster
            Boolean messageAlreadyExists = false;
            for(Integer i: IntStream.range(0, indicesCurrentBatch.size()).toArray()) {
                if(hostsCurrentBatch.get(i).getRouter().getMessageCollection().contains(message) && i != 0) {
                    if(this.executionLogging) {System.out.println("Message already exists with Clustermember!");}
                    messageAlreadyExists = true;
                }
            }
            if(messageAlreadyExists) {
                //delete message locally
                this.getHost().deleteMessage(message.getId(), false);
                if (!this.getHost().getRouter().getMessageCollection().contains(message)) {
                    if(this.executionLogging) {System.out.println("Deleted Message locally!");}
                }
            } else  {
                //add current MessageSize from active Host FreeBufferSpace
                hostsFBSCurrentBatch.set(0, hostsFBSCurrentBatch.get(0) + message.getSize());
                if(this.executionLogging) {System.out.println("Free Buffersizes in batch: " + hostsFBSCurrentBatch);}

                if(Collections.max(hostsFBSCurrentBatch)<message.getSize()) {
                    if(this.executionLogging) {System.out.println("No BufferSpace left for any MessageCopies");}

                } else {
                    // Select Node with maximum FreeBufferSpace
                    int indexSelectedNodeInCurrentBatch = hostsFBSCurrentBatch.indexOf(Collections.max(hostsFBSCurrentBatch));
                    int indexSelectedNodeConnection = indicesCurrentBatch.get(indexSelectedNodeInCurrentBatch)-1;
                    if(this.executionLogging) {System.out.println("Chosen Node: " + hosts.get(indexSelectedNodeConnection+1));}
                    if( indexSelectedNodeConnection == -1) {
                        // best message carrier is node itself
                        if(this.executionLogging) {System.out.println("Chosen Node = active Host");}

                    } else {
                        // best message carrier is other recipient
                        messagesDestinations.get(indicesCurrentBatch.get(indexSelectedNodeInCurrentBatch)).add(message);
                        if(this.executionLogging) {
                            System.out.println("Added " + message + " to index " + indicesCurrentBatch.get(indexSelectedNodeInCurrentBatch)+" in messageDestinations");
                        }
                        //Connection recipient = connections.get(indexSelectedNodeConnection);
                        // subtract current MessageSize from recipients' FreeBufferSpace
                        hostsFBSCurrentBatch.set(indexSelectedNodeInCurrentBatch, hostsFBSCurrentBatch.get(indexSelectedNodeInCurrentBatch) - message.getSize());
                        //delete Message from host
                        this.getHost().deleteMessage(message.getId(), false);
                        hostsFBSCurrentBatch.set(0, hostsFBSCurrentBatch.get(0) + message.getSize());
                        if (!this.getHost().getRouter().getMessageCollection().contains(message)) {
                            if(this.executionLogging) {System.out.println("Deleted Message locally!");}

                        }
                    }
                }
                //subtract current MessageSize from active Host FreeBufferSpace
                hostsFBSCurrentBatch.set(0, hostsFBSCurrentBatch.get(0) - message.getSize());
            }
        }
        return messagesDestinations;
    }
    private List<List> deliveryForeignCluster(List<DTNHost> hostsCurrentBatch,
                                              List<Integer> indicesCurrentBatch,
                                              List<Integer> hostsFBSCurrentBatch,
                                              List<Message> messages,
                                              List<List> messagesDestinations,
                                              List<DTNHost> hosts){

        if(this.executionLogging) {System.out.println("cluster batch members:" + hostsCurrentBatch);}

        for (Message message : messages) {
            if(this.executionLogging) {
                System.out.println("Evaluating Message: " + message.getId() +  " with size: " + message.getSize());
            }
            //Check if message already exists in Cluster
            Boolean messageAlreadyExists = false;
            for(Integer i: IntStream.range(0, indicesCurrentBatch.size()).toArray()) {
                if(hostsCurrentBatch.get(i).getRouter().getMessageCollection().contains(message)) {
                    if(this.executionLogging) {System.out.println("Message already exists in Cluster!");}
                    messageAlreadyExists = true;
                }
            }
            if(messageAlreadyExists) {
                //break loop, message doesn't need to be sent
            } else {
                if(this.executionLogging) {System.out.println("Free Buffersizes in batch: " + hostsFBSCurrentBatch);}

                if(Collections.max(hostsFBSCurrentBatch)<message.getSize()) {
                    if(this.executionLogging) {System.out.println("No BufferSpace left for any MessageCopies");}

                } else {
                    // Select Node with maximum FreeBufferSpace
                    int indexSelectedNodeInCurrentBatch = hostsFBSCurrentBatch.indexOf(Collections.max(hostsFBSCurrentBatch));
                    // subtract current MessageSize from recipients' FreeBufferSpace
                    int indexSelectedNodeConnection = indicesCurrentBatch.get(indexSelectedNodeInCurrentBatch)-1;
                    hostsFBSCurrentBatch.set(indexSelectedNodeInCurrentBatch, hostsFBSCurrentBatch.get(indexSelectedNodeInCurrentBatch) - message.getSize());
                    messagesDestinations.get(indicesCurrentBatch.get(indexSelectedNodeInCurrentBatch)).add(message);
                    if(this.executionLogging) {
                        System.out.println("Chosen Node: " + hosts.get(indexSelectedNodeConnection+1));
                        System.out.println("Added " + message + " to index " + indicesCurrentBatch.get(indexSelectedNodeInCurrentBatch)+" in messageDestinations");
                    }
                }
            }
        }
        return messagesDestinations;
    }
    private Double computeRewards() {
        if(this.executionLogging) {System.out.println("-Evaluation Phase-");}

        //Reward Delivery Frequency
        double load = this.getHost().getBufferOccupancy()/100;
        if(load > 1) load = 1;
        double rewardDF;
        /**
         * OLD REWARD FUNCTION (DEPCRECATED)
         *         double rewardDF = -1.023595 + 16.14673*load - 77.26101*Math.pow(load, 2) +
         *                 183.0286*Math.pow(load, 3) - 190.181*Math.pow(load, 4) + 68.26667*Math.pow(load, 5);
         */

        if(load > 0.125 && load <= 0.75) {
            rewardDF = 1.6*load-0.2;
        } else if(load > 0.75 && load < 0.875) {
            rewardDF = -8*load+7;
        } else {
            rewardDF = -1;
        }

        //Reward Cluster Analysis
        double rewardCA;

        /**
         * Optional Module, to wait for mean to reach statistical significance before start rewarding:
         *         if(this.runningAvgCounter > XX) {REWARDFUNCTION HERE} else {rewardCA = 0;}
         */

        if(this.averageClusters <= 1.1 && this.averageNodesPerCluster <= 1.1) {
            rewardCA = -1;
        } else if (this.averageClusters > 1.1 && this.averageNodesPerCluster > 1.1){
            rewardCA = 0;
        } else {
            rewardCA = -0.5;
        }


        //add rewards to final result
        double reward = rewardCA+rewardDF;

        //Execution Logging

        if(this.executionLogging) {
            System.out.println("RewardDF: " + rewardDF + " with Occupancy: " + load);
            System.out.println("RewardCA: " + rewardCA + " with avgCluster: " + this.averageClusters + " and avgNodes: " + this.averageNodesPerCluster);
            System.out.println("Total Reward: " + reward);
        }

        return reward;
    }

    private void performRL(Double routingPhaseReward) {

        //execute ReinforcementLearning.R script
        Vector resultsReinforcementLearning = null;
        String filePathRCode = "R" + File.separator + "ReinforcementLearning.R";

        if(this.executionLogging) {
            System.out.println("RL Input QTable with size " + this.Qtable.size() + ": " + this.Qtable);
            System.out.println("RL Input ClusterHeight: " + this.clusterHeight);
            System.out.println("RL Input SleepTime: " + this.sleepTime);
            System.out.println("RL InputReward: " + routingPhaseReward);
        }


        try {
            renjin.put("Qtable", this.Qtable);
            renjin.put("currentSleepTime", this.sleepTime);
            renjin.put("currentClusterHeight", this.clusterHeight);
            renjin.put("reward", routingPhaseReward);
            renjin.put("minSleepTime", this.minSleepTime);
            renjin.put("maxSleepTime", this.maxSleepTime);
            renjin.put("incSleepTime", this.incSleepTime);
            renjin.put("minClusterHeight", this.minClusterHeight);
            renjin.put("maxClusterHeight", this.maxClusterHeight);
            renjin.put("incClusterHeight", this.incClusterHeight);
            renjin.put("learningRate", this.rlLearningRate);
            renjin.put("discountFactor", this.rlDiscountFactor);
            renjin.put("explorationFactor", this.rlExplorationFactor);

            resultsReinforcementLearning = (Vector) renjin.eval(new FileReader(filePathRCode));
            //System.out.println("R Output Vector: " + resultsReinforcementLearning);
        } catch (FileNotFoundException | ScriptException e) {
            System.out.println("An error occurred while executing ReinforcementLearning.R");
        }

        //Transform Output Vector from .R file to local parameter type
        List <Double> newQtable = new ArrayList<>();
        Double newClusterHeight = 0.0;
        Double newSleepTime = 0.0;
        for (int i = 0; i < resultsReinforcementLearning.length(); i++) {
            if(i == resultsReinforcementLearning.length()-2) {
                newClusterHeight = resultsReinforcementLearning.getElementAsDouble(i);
            }
            else if(i == resultsReinforcementLearning.length()-1) {
                newSleepTime = resultsReinforcementLearning.getElementAsDouble(i);
            } else {
                newQtable.add(resultsReinforcementLearning.getElementAsDouble(i));
            }
        }

        if(this.executionLogging) {
            System.out.println("RL Output QTable with size " + newQtable.size() + ": " + newQtable );
            System.out.println("RL Output ClusterHeight: " + newClusterHeight);
            System.out.println("RL Output SleepTime: " + newSleepTime);

        }

        //Update local Parameters according to new Q-State
        this.Qtable = newQtable;
        this.clusterHeight = newClusterHeight;
        this.sleepTime = newSleepTime;
    }



    private double calcRunningAvgWelford(double average, double element, int counter, double weight){
        if (counter == 1){
            average = element;
        }
        else if (weight == 0.0) {
            average = average + (element-average) / counter;
        }
        else {
            average = average + (element-average) / weight;
        }
        return(average);
    }

    private void updateClusteringPerformanceMetrics(List <Integer> clusters){
        double currentClusterCount = clusters.stream().distinct().count();
        double currentNodesPerCluster = clusters.size() / currentClusterCount;
        this.runningAvgCounter += 1;
        this.averageClusters = calcRunningAvgWelford(this.averageClusters, currentClusterCount, this.runningAvgCounter, 0.0);
        this.averageNodesPerCluster = calcRunningAvgWelford(this.averageNodesPerCluster, currentNodesPerCluster, this.runningAvgCounter, 0.0);
    }

    private List<DTNHost> getConnectedHosts(){
        List<DTNHost> hosts = new ArrayList<>();
        hosts.add(this.getHost());
        for(Connection c: this.getConnections()) {
            hosts.add(c.getOtherNode(this.getHost()));
        }
        return hosts;
    }
    private List<String> getConnectedHostsNames(List<DTNHost> hosts){
        List<String> hostsName = new ArrayList<>();
        for (DTNHost h : hosts) {
            hostsName.add(h.toString());
        }
        return hostsName;
    }
    private List<Double> getConnectedHostsSpeed(List<DTNHost> hosts){
        List<Double> hostsSpeed = new ArrayList<>();
        for (DTNHost h : hosts) {
            hostsSpeed.add(h.getRunningAvgOfSpeed());
        }
        return hostsSpeed;
    }

    private List<Double> getConnectedHostsDirectionByDirMode(List<DTNHost> hosts, Integer directionMode){
        //list of Directions per Host depending on DirectionMode
        List<Double> hostsDirection = new ArrayList<>();

        if (directionMode == 1) {
            //Get RunningAvgOfDirectionOfMovement per Host
            for (DTNHost h : hosts) {
                hostsDirection.add(h.getRunningAvgOfDirectionOfMovement());
            }
        }
        else if (directionMode == 2) {
            //Get DirectionOfAvgLocationper Host
            for (DTNHost h : hosts) {
                hostsDirection.add(h.getDirectionOfAvgLocation());
            }

        }
        else if (directionMode == 3) {
            //Get DirectionOfDestination per Host
            for (DTNHost h : hosts) {
                hostsDirection.add(h.getDirectionOfDestination());
            }
        }
        return hostsDirection;
    }

    private List<Connection> getHostConnections(){
        List<Connection> connections= new ArrayList<>();
        connections = this.getConnections();
        return connections;
    }

    private List<Message> getHostMessages(){
        List<Message> messages= new ArrayList<Message>(this.getMessageCollection());
        this.sortByQueueMode(messages);
        return messages;
    }
    private List<Integer> getConnectedHostsFBS(List<DTNHost> hosts){
        List<Integer> hostsFBS = new ArrayList<>();
        for (DTNHost h : hosts) {
            hostsFBS.add(h.getRouter().getFreeBufferSize());
        }
        return hostsFBS;
    }

    private void writeParametersToCSV(Double reward) {
        SimScenario simScenario = SimScenario.getInstance();
        String filePath = "C:/Users/fschi/Documents/BachelorArbeit/the-one-1.6.0/reports/SprayLearnWaitRouterInternalAnalysis/20nodes/"+simScenario.getName()+".csv";
        File yourFile = new File(filePath);
        if(!yourFile.isFile()){
            try {
                yourFile.createNewFile();
                Files.write(Paths.get(filePath), "Node,SimClock,CoordX,CoordY,sleepTime,clusterHeight,avgCluster, avgNodesPerCluster, FreeBufferSpacePercent, reward, iteration".getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Files.write(Paths.get(filePath),
                    ("\n"
                        + this.getHost().toString() + ","
                        + SimClock.getTime() + ","
                        + this.getHost().getLocation().getX() + ","
                        + this.getHost().getLocation().getY() + ","
                        + this.sleepTime + ","
                        + this.clusterHeight + ","
                        + this.averageClusters + ","
                        + this.averageNodesPerCluster + ","
                        + this.getHost().getBufferOccupancy() + ","
                        + reward + ","
                        + this.runningAvgCounter
                    ).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SprayLearnWaitRouter replicate() {
        return new SprayLearnWaitRouter(this);
    }

}
