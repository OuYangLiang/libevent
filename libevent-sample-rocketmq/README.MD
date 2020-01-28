查询topic列表

```shell
./bin/mqadmin topicList -n localhost:9876
```


查询cluster列表

```shell
./bin/mqadmin clusterList -n localhost:9876
```

创建topic

```shell
./bin/mqadmin updateTopic -c DefaultCluster -n localhost:9876 -w 4 -r 4 -o true -t event_topic
```

删除topic

```shell
./bin/mqadmin deleteTopic -c DefaultCluster -n localhost:9876 -t event_topic
```


查询所有topic与consumer状态

```shell
./bin/mqadmin statsAll -n localhost:9876
```


