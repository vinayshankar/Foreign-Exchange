Homework 1 - PrepData.java
  All the csv files in the directory path that is provided are retrieved.
  Each csv file is then independently read, processed and a file with prepared data is written. 

  Note: Mixing of the currency pairs data to ensure correct prediction in the future

Homework 2 - DecisionTree.java
  All the prepared files are retrieved. 
  Each record in each file is retrieved into a list.
  The list with all the records is then fed to the constructor of the Decision tree.
  The decision tree the selects the best feature for the root node and splits the records.
  The two split datasets are then again given to the constructor of the left node and right node.
  This is done recursively until all the features are exhausted.
  
  Once the decision tree is a trained "learned" tree, we test the tree's predication capability with test data.
  The stats corresponding to true positives, true negatives, false positives and false negatives are captured.

Homework 3 - RandomForest.java
  All the prepared files are retrieved. 
  Each record in each file is retrieved into a list.
  The list with all the records is then fed to the constructor of the Random Forest along with number of trees that needs to   be in the forest.
  The random forest randomly selects 2/3 records, randomly selects 3 features out of the 9 features and feeds this data to     constructor of the decision tree.
  The random forest is now trained with data.
  The random forest also has the query label value function that queries the label value for a given record against all the    trees in the forest and returns the majority.
  The random forest in then queried against every test record and the stats corresponding to true positives, true negatives,   false positives and false negatives are captured.
  
Homework 4 - Cassandra.java
  The prepared training data is stored in a cassandra table 'records' in the keyspace 'trainingdata'.
  The prepared testing data is stored in a cassandra table 'records' in the kepspace 'testingdata'.
  The training data is read from the cassandra table and fed to the constructor of the random forest.
  The testing data is again read from the cassandra table and tested against the random forest. 
