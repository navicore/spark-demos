# spark-demos
a scratch pad of spark utils and examples

used to get a handle on how to pas env vars to both driver and executor
```
sbt assembly && kubectl exec -i bdas-spark-master-controller-yu4ok -- /bin/bash -c 'cat > my.jar && DRIVER_PASSENGER_VAR=alice DRIVER_VAR=ed /opt/spark/bin/spark-submit --master spark://bdas-spark-master:7077 --conf spark.executorEnv.EXECUTOR_VAR=artie --deploy-mode client --class onextent.sparkdemos.HelloEnv ./my.jar' < target/scala-2.11/*.jar
```

note, the executor code is looking up `EXECUTOR_VAR`

