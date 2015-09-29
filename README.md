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
