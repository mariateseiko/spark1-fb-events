mvn clean package
$SPARK_HOME/bin/spark-submit 
--class "com.epam.training.spark1.FbSparkApp" 
--master yarn spark-fb-events-1.0-SNAPSHOT-jar-with-dependencies.jar
inputCityPath inputTagsPath logsInputPath tagsOutputPath eventsOutputPath attendiesOutputPath
