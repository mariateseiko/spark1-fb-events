package com.epam.bigdata.spark1;

import com.epam.bigdata.spark1.model.*;
import com.restfb.*;
import com.restfb.batch.BatchRequest;
import com.restfb.batch.BatchResponse;
import com.restfb.types.Event;
import com.restfb.types.Location;
import com.restfb.types.Place;
import com.restfb.types.User;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.*;

import org.apache.spark.sql.*;
import scala.Tuple2;
import scala.collection.mutable.Seq;

import static org.apache.spark.sql.functions.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FbSparkApp {
    private static final String token = "EAACEdEose0cBAAKQ4TszfZAmLoGsaFridfawYjYtqiwssg4ZBpQ0VHM8C9eIN1JeGA0Pyke7toHNWxTYvY5DuO6DcSnJHpVLOaS3nSknBdNF6Il049ud8wfr91m29XFNxOrZAmV2roIYwLty1ZBZA98AI6RvgZCqhBJmcDclz428lbLsdyu1A3";
    private static final String UNKNOWN = "unknown";
    private static SparkSession spark;

    public static void main(String[] args) throws IOException {
        spark = SparkSession
                .builder()
                .appName("Spark FB events")
                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .config("spark.kryo.classesToRegister",
                        "com.epam.bigdata.spark1.model.Attendee")
                .enableHiveSupport()
                .getOrCreate();
        process(args[0], args[1], args[2], args[3], args[4], args[5]);
    }

    private static void process(String cityInputPath, String tagsInputPath, String logsInputPath,
                                String keywordsOutputPath, String eventsOutputPath, String attendeiesOutputPath) throws IOException {
        Dataset<String> citiesLines = spark.read().textFile(cityInputPath);
        Dataset<City> cities = citiesLines
                .map((MapFunction<String, City>) x -> new City(x.split("\\t")[0], x.split("\\t")[1]),
                        Encoders.bean(City.class));
        Dataset<String> keywordLines = spark.read().textFile(tagsInputPath);
        Dataset<KeywordId> keywords = keywordLines
                .map((MapFunction<String, KeywordId>) x -> new KeywordId(x.split("\\t")[0], x.split("\\t")[1]),
                        Encoders.bean(KeywordId.class));
        Dataset<String> logsLines = spark.read().textFile(logsInputPath);
        Dataset<LogsEntry> logs = logsLines
                .map((MapFunction<String, LogsEntry>)
                                x -> new LogsEntry(x.split("\\t")[1].substring(0, 8), x.split("\\t")[6], x.split("\\t")[20]),
                        Encoders.bean(LogsEntry.class));

        Dataset<DateCityKeyword> dateCityKeywordDataset = logs
                .join(cities, logs.col("cityId").equalTo(cities.col("cityId")))
                .join(keywords, logs.col("tagsId").equalTo(keywords.col("keywordsId")))
                .map((MapFunction<Row, DateCityKeyword>)
                                row -> new DateCityKeyword(row.getString(row.fieldIndex("date")),
                                        row.getString(row.fieldIndex("cityName")), row.getString(row.fieldIndex("keywords"))),
                        Encoders.kryo(DateCityKeyword.class))
                .flatMap((FlatMapFunction<DateCityKeyword, DateCityKeyword>) x ->{
                    List<DateCityKeyword> rows = new ArrayList<>();
                    Arrays.stream(x.getKeyword().split(",")).forEach(word -> {
                        x.setKeyword(word);
                        rows.add(x);
                    });
                    return rows.iterator();
                }, Encoders.bean(DateCityKeyword.class));

        dateCityKeywordDataset.cache();

        Dataset<Row> uniqueKeywordsByCityDate = dateCityKeywordDataset
                .groupBy("date", "cityName")
                .agg(collect_set("keyword").alias("keywords"));
        uniqueKeywordsByCityDate.coalesce(1).toJavaRDD().saveAsTextFile(keywordsOutputPath);

        Dataset<String> uniqueTags = dateCityKeywordDataset.map((MapFunction<DateCityKeyword, String>) DateCityKeyword::getKeyword, Encoders.STRING()).distinct();

        Dataset<FBEvent> fbEvents = uniqueTags.mapPartitions((MapPartitionsFunction<String, FBEvent>) iterator -> {
            List<FBEvent> fb = new ArrayList<>();
            DefaultFacebookClient facebookClient = new DefaultFacebookClient(token, Version.LATEST);
            iterator.forEachRemaining(keyword -> {
                Connection<Event> connections = facebookClient.fetchConnection(
                        "search",
                        Event.class,
                        Parameter.with("type", "event"),
                        Parameter.with("q", keyword),
                        Parameter.with("fields", "id,name,description,attending_count,start_time,place"),
                        Parameter.with("since", System.currentTimeMillis() / 1000),
                        Parameter.with("until", System.currentTimeMillis() / 1000 + 24 * 3600 * 5)
                );
                connections.getData().forEach(event -> {
                        String city = Optional.of(event).map(Event::getPlace)
                                .map(Place::getLocation).map(Location::getCity)
                                .orElse(UNKNOWN);
                        fb.add(new FBEvent(event.getId(), event.getStartTime(), event.getName(), event.getDescription(), keyword, city, event.getAttendingCount()));
                    });
            });
            return fb.iterator();
        }, Encoders.bean(FBEvent.class)).cache();

        Dataset<FBEventsAgg> aggregatedEvents = fbEvents.withColumn("description", split(col("description"), " "))
                .groupBy("date", "keyword", "city")
                .agg(sum("attendingCount").alias("totalAttending"), collect_list("description").alias("descriptions"))
                .map(new MapFunction<Row, FBEventsAgg>() {
                    @Override
                    public FBEventsAgg call(Row row) throws Exception {
                        Map<String, Integer> wordcount = new HashMap<>();
                        List<Seq<String>> descriptions = scala.collection.JavaConversions.seqAsJavaList(row.getSeq(row.fieldIndex("descriptions")));
                        descriptions.forEach(x -> scala.collection.JavaConversions.seqAsJavaList(x).forEach(y -> {
                            Integer count = wordcount.putIfAbsent(y, 1);
                            if (count != null) {
                                wordcount.put(y, count + 1);
                            }
                        }));
                        Map<String, Integer> topTen = wordcount.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                .limit(10).collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (e1, e2) -> e1,
                                        LinkedHashMap::new
                                ));
                        ArrayList<String> topTenArray = new ArrayList<>();
                        topTen.entrySet().forEach(x -> {
                            topTenArray.add(x.getKey());
                            topTenArray.add(x.getValue().toString());
                        });
                        return new FBEventsAgg(row.getString(row.fieldIndex("keyword")),
                                row.getString(row.fieldIndex("city")),
                                row.getString(row.fieldIndex("date")),
                                row.getLong(row.fieldIndex("totalAttending")),
                                topTenArray.toArray(new String[0]));
                    }
                }, Encoders.bean(FBEventsAgg.class));

        aggregatedEvents.toJavaRDD().coalesce(1).saveAsTextFile(eventsOutputPath);

        Dataset<Attendee> attendeeDataset = fbEvents
                .mapPartitions((MapPartitionsFunction<FBEvent, Attendee>) partition -> {
                    List<BatchRequest> batch = new ArrayList<>();
                    partition.forEachRemaining(event -> {
                        String id = event.getId();
                        batch.add(new BatchRequest.BatchRequestBuilder(id + "/attending").parameters(
                                Parameter.with("fields", "name")
                        ).build());
                    });
                    List<Attendee> keywordsEvents = new ArrayList<>();
                    if (batch.size() > 0) {
                            DefaultFacebookClient facebookClient = new DefaultFacebookClient(token, Version.LATEST);
                            List<BatchResponse> resp = facebookClient.executeBatch(batch);
                            JsonMapper jsonMapper = new DefaultJsonMapper();
                            resp.forEach(response ->
                                    jsonMapper.toJavaList(response.getBody(), User.class)
                                            .forEach(x -> keywordsEvents.add(new Attendee(x.getName(), x.getId()))));
                    }
                    return keywordsEvents.iterator();
                }, Encoders.bean(Attendee.class));

        JavaRDD<Attendee> attendeeJavaRDD = attendeeDataset.toJavaRDD()
                .mapToPair(x -> new Tuple2<>(x.getName(), 1))
                .reduceByKey((x, y) -> (x + y))
                .map((Function<Tuple2<String, Integer>, Attendee>) attendee -> new Attendee(attendee._1(), attendee._2()))
                .sortBy(Attendee::getNumOccurencies, false, 1);

        attendeeJavaRDD.saveAsTextFile(attendeiesOutputPath);
    }
}