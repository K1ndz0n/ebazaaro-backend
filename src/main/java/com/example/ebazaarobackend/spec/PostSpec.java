package com.example.ebazaarobackend.spec;

import com.example.ebazaarobackend.dto.PostFilter;
import com.example.ebazaarobackend.model.Category;
import com.example.ebazaarobackend.model.City;
import com.example.ebazaarobackend.model.Post;
import com.example.ebazaarobackend.model.User;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class PostSpec {
    public static Specification<Post> withFilters(PostFilter f, Float lat, Float lng) {
        return Specification
                .where(search(f.getSearch()))
                .and(condition(f.getCondition()))
                .and(priceFrom(f.getPriceFrom()))
                .and(priceTo(f.getPriceTo()))
                .and(category(f.getCategory()))
                .and(withinRadius(lat, lng, f.getRadius()))
                .and(cityFilter(f.getCityId(), lat, lng, f.getRadius()));
    }

    private static Specification<Post> search(String search) {
        return (root, query, cb) -> {
            if (search == null) return null;
            String like = "%" + search.toLowerCase() + "%";

            Predicate byName = cb.like(cb.lower(root.get("name")), like);
            Predicate byCondition = cb.like(cb.lower(root.get("condition")), like);

            Join<Post, Category> cat = root.join("category", JoinType.LEFT);
            Predicate byCategory = cb.like(cb.lower(cat.get("name")), like);

            Join<Post, User> user = root.join("user", JoinType.LEFT);
            Predicate byUser = cb.like(cb.lower(user.get("username")), like);

            Join<Post, City> city = root.join("city", JoinType.LEFT);
            Predicate byCity = cb.or(
                    cb.like(cb.lower(city.get("name")), like),
                    cb.like(cb.lower(city.get("voivodeship")), like)
            );

            query.distinct(true);
            return cb.or(byName, byCondition, byCategory, byUser, byCity);
        };
    }

    private static Specification<Post> condition(String condition) {
        return (root, query, cb) ->
                condition == null ? null : cb.equal(root.get("condition"), condition);
    }

    private static Specification<Post> priceFrom(Float price) {
        return (root, query, cb) ->
                price == null ? null : cb.greaterThanOrEqualTo(root.get("price"), price);
    }

    private static Specification<Post> priceTo(Float price) {
        return (root, query, cb) ->
                price == null ? null : cb.lessThanOrEqualTo(root.get("price"), price);
    }

    private static Specification<Post> category(String category) {
        return (root, query, cb) -> {
            if (category == null) return null;
            Join<Post, Category> cat = root.join("category", JoinType.LEFT);
            return cb.equal(cat.get("name"), category);
        };
    }

    private static Specification<Post> withinRadius(Float lat, Float lng, Float radius) {
        return (root, query, cb) -> {
            if (lat == null || lng == null || radius == null || radius == 0) return null;

            Join<Post, City> city = root.join("city", JoinType.LEFT);

            Expression<Double> lat1 = city.get("latitude");
            Expression<Double> lon1 = city.get("longitude");

            Expression<Double> lat2 = cb.literal(lat.doubleValue());
            Expression<Double> lon2 = cb.literal(lng.doubleValue());

            Expression<Double> radLat1 = cb.function("radians", Double.class, lat1);
            Expression<Double> radLon1 = cb.function("radians", Double.class, lon1);
            Expression<Double> radLat2 = cb.function("radians", Double.class, lat2);
            Expression<Double> radLon2 = cb.function("radians", Double.class, lon2);

            Expression<Double> cosLat2 = cb.function("cos", Double.class, radLat2);
            Expression<Double> cosLat1 = cb.function("cos", Double.class, radLat1);
            Expression<Double> sinLat2 = cb.function("sin", Double.class, radLat2);
            Expression<Double> sinLat1 = cb.function("sin", Double.class, radLat1);

            Expression<Double> cosLonDiff =
                    cb.function("cos",
                            Double.class,
                            cb.diff(radLon1, radLon2));

            Expression<Double> acosInput =
                    cb.sum(
                            cb.prod(cosLat2, cb.prod(cosLat1, cosLonDiff)),
                            cb.prod(sinLat2, sinLat1)
                    );

            Expression<Double> distance =
                    cb.prod(
                            cb.literal(6371.0),
                            cb.function("acos", Double.class, acosInput)
                    );

            return cb.lessThanOrEqualTo(distance, radius.doubleValue());
        };
    }

    private static Specification<Post> cityFilter(Long cityId, Float lat, Float lng, Float radius) {
        return (root, query, cb) -> {
            if (lat != null && lng != null && radius != null && radius > 0) return null;
            if (cityId == null) return null;
            return cb.equal(root.get("city").get("id"), cityId);
        };
    }
}
