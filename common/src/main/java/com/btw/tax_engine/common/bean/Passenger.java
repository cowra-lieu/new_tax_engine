package com.btw.tax_engine.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Passenger {

    public String type = "ADT";    // ADT成人，CHD儿童，INF婴儿

    // yyyy-MM-dd，根据这个可选字段计算ADT成人呢，CHD儿童，INF婴儿
    // 英国 GB税对16岁以下免征；有的国家规定13岁以下是儿童
    public String birthday;

    public String id = "N";

    public char category;  // N公民, R常住居民, E航空公司雇员, default is N
    public char rtype = 'N';    // N国家, C城市, default is C
    public String region = "CN";
    public int age = -1;    // default is 20

    public int getAge() {
        if (age == -1) {
            switch (type) {
                case "ADT":
                    age = 20;
                    break;
                case "CHD":
                    age = 10;
                    break;
                case "INF":
                    age = 1;
                    break;
            }
        }
        return age;
    }

    public Passenger() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return category == passenger.category &&
                rtype == passenger.rtype &&
                age == passenger.age &&
                Objects.equals(type, passenger.type) &&
                Objects.equals(birthday, passenger.birthday) &&
                Objects.equals(id, passenger.id) &&
                Objects.equals(region, passenger.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, birthday, id, category, rtype, region, age);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Passenger.class.getSimpleName() + "[", "]")
                .add("type='" + type + "'")
                .add("birthday='" + birthday + "'")
                .add("id='" + id + "'")
                .add("category=" + category)
                .add("rtype=" + rtype)
                .add("region='" + region + "'")
                .add("age=" + age)
                .toString();
    }
}
