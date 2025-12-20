package com.organization.query_engine.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "users")
public class UserEntity implements Serializable {
    @Id
    private UUID id;

    private String username;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;

    private Gender gender;

    @Field(type = FieldType.Date)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private Integer age;
    private List<Role> roles;
    private Language language;
    private String timezone;

    @Field(type = FieldType.Object)
    private Location location;

    @Field(type = FieldType.Object)
    private Contacts contacts;

    @Field(type = FieldType.Object)
    private Channels channels;

    @Field(type = FieldType.Object)
    private Subscriptions subscriptions;

    @Field(type = FieldType.Object)
    private Preferences preferences;

    private List<Segment> segments;
    @JsonProperty("custom_tags")
    private List<CustomTag> customTags;

    @JsonProperty("account_status")
    private AccountStatus accountStatus;

    @Field(type = FieldType.Object)
    private Stats stats;

    @Field(type = FieldType.Object)
    private Metadata metadata;

    // --- Nested classes ---
    @Data
    public static class Location {
        private String city;
        private String country;
    }

    @Data
    public static class Contacts {
        private String email;
        @JsonProperty("phone_number")
        private String phoneNumber;
        @JsonProperty("telegram_id")
        private String telegramId;
        @JsonProperty("telegram_username")
        private String telegramUsername;
        @JsonProperty("fcm_token")
        private String fcmToken;
    }

    @Data
    public static class Channel {
        private Boolean enabled;
        private String provider;

        @JsonProperty("last_sent_at")
        private OffsetDateTime lastSentAt;
    }

    @Data
    public static class Channels {
        private Channel email;
        private Channel sms;
        private Channel telegram;
        private Channel push;
    }

    @Data
    public static class Subscriptions {
        private Boolean marketing;
        @JsonProperty("system_alerts")
        private Boolean systemAlerts;
        private Boolean promo;
        private Boolean educational;
    }

    @Data
    public static class Preferences {
        private List<String> categories;
        private List<String> authors;
    }

    @Data
    public static class Stats {
        @JsonProperty("books_purchased")
        private Integer booksPurchased;
        @JsonProperty("hours_spent_reading")
        private Integer hoursSpentReading;

        @JsonProperty("last_login_at")
        private OffsetDateTime lastLoginAt;
    }

    @Data
    public static class Metadata {
        @JsonProperty("created_at")
        private OffsetDateTime createdAt;

        @JsonProperty("last_updated")
        private OffsetDateTime lastUpdated;
    }

    public enum Gender {
        MALE, FEMALE
    }

    public enum CustomTag {
        VERIFIED, VIP, PREMIUM
    }

    public enum AccountStatus {
        ACTIVE, DEACTIVATED, PAUSED, DELETED
    }

    public enum Role {
        ADMIN,
        MODERATOR,
        USER
    }

    public enum Segment {
        WEB_USER, MOBILE_USER
    }

    public enum Language {
        AZ, ENG, DE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
