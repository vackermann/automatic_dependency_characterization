# Research project on "Blackbox learning of parametric dependencies for performance models from monitoring data"

This Java project holds classes for:
- creating runtime data sets for multiple algorithms from different domains (e.g., basic Java functionalities, image processing) in order to emulate real monitoring data, 
- learning and predicting the runtime of an application in an online scenario based on observable input parameter values,
- evaluating the prediction performance of various Weka Classifiers for regression in this special scenario
- building a meta-classifier in order to recommend the best fitting prediction technique depending on observable charateristics on the training data set.

# Research outcomes:
- Developed 2-tier machine learning approach with performance prediction accuracy of 92.8\% in case study,
- Achieved proof of concept that automatic on-the-fly performance modelling can be used to optimize server utilization without any knowledge about the running application,
- Results published at 13th International Workshop on Models@run.time (Oct 2018). Find paper [here](http://ceur-ws.org/Vol-2245/mrt_paper_5.pdf).
