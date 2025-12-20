package com.organization.query_engine.mapper;

import com.organization.query_engine.api.model.*;
import com.organization.query_engine.entity.UserEntity;

import java.util.stream.Collectors;

import static com.organization.query_engine.util.DateUtil.toLocalDateTime;
import static com.organization.query_engine.util.DateUtil.toOffsetDateTime;


public class UserMapper {

    public static UserDetail toDto(UserEntity entity) {
        if (entity == null) return null;
        UserDetail dto = new UserDetail();

        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setBirthday(entity.getBirthday());
        dto.setAge(entity.getAge());
        dto.setTimezone(entity.getTimezone());

        dto.setGender(entity.getGender() != null ? Gender.valueOf(entity.getGender().name()) : null);
        dto.setLanguage(entity.getLanguage() != null ? Language.valueOf(entity.getLanguage().name()) : null);
        dto.setAccountStatus(entity.getAccountStatus() != null ? AccountStatus.valueOf(entity.getAccountStatus().name()) : null);

        if (entity.getRoles() != null) {
            dto.setRoles(entity.getRoles().stream()
                    .map(role -> Role.valueOf(role.name()))
                    .collect(Collectors.toList()));
        }

        if (entity.getSegments() != null) {
            dto.setSegments(entity.getSegments().stream()
                    .map(segment -> Segment.valueOf(segment.name()))
                    .collect(Collectors.toList()));
        }

        if (entity.getCustomTags() != null) {
            dto.setCustomTags(entity.getCustomTags().stream()
                    .map(tag -> CustomTag.valueOf(tag.name()))
                    .collect(Collectors.toList()));
        }

        if (entity.getLocation() != null) {
            dto.setLocation(new Location()
                    .city(entity.getLocation().getCity())
                    .country(entity.getLocation().getCountry()));
        }

        if (entity.getContacts() != null) {
            dto.setContacts(new Contacts()
                    .email(entity.getContacts().getEmail())
                    .phoneNumber(entity.getContacts().getPhoneNumber())
                    .telegramId(entity.getContacts().getTelegramId())
                    .telegramUsername(entity.getContacts().getTelegramUsername())
                    .fcmToken(entity.getContacts().getFcmToken()));
        }

        if (entity.getChannels() != null) {
            dto.setChannels(new Channels()
                    .email(mapChannel(entity.getChannels().getEmail()))
                    .sms(mapChannel(entity.getChannels().getSms()))
                    .telegram(mapChannel(entity.getChannels().getTelegram()))
                    .push(mapChannel(entity.getChannels().getPush())));
        }

        if (entity.getSubscriptions() != null) {
            dto.setSubscriptions(new Subscriptions()
                    .marketing(entity.getSubscriptions().getMarketing())
                    .systemAlerts(entity.getSubscriptions().getSystemAlerts())
                    .promo(entity.getSubscriptions().getPromo())
                    .educational(entity.getSubscriptions().getEducational()));
        }

        if (entity.getPreferences() != null) {
            dto.setPreferences(new Preferences()
                    .categories(entity.getPreferences().getCategories())
                    .authors(entity.getPreferences().getAuthors()));
        }

        if (entity.getStats() != null) {
            dto.setStats(new Stats()
                    .booksPurchased(entity.getStats().getBooksPurchased())
                    .hoursSpentReading(entity.getStats().getHoursSpentReading())
                    .lastLoginAt(toLocalDateTime(entity.getStats().getLastLoginAt())));
        }

        if (entity.getMetadata() != null) {
            dto.setMetadata(new Metadata()
                    .createdAt(toLocalDateTime(entity.getMetadata().getCreatedAt()))
                    .lastUpdated(toLocalDateTime(entity.getMetadata().getLastUpdated())));
        }

        return dto;
    }

    public static UserEntity toEntity(UserDetail dto) {
        if (dto == null) return null;

        UserEntity entity = new UserEntity();

        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setBirthday(dto.getBirthday());
        entity.setAge(dto.getAge());
        entity.setTimezone(dto.getTimezone());

        entity.setGender(dto.getGender() != null ? UserEntity.Gender.valueOf(dto.getGender().name()) : null);
        entity.setLanguage(dto.getLanguage() != null ? UserEntity.Language.valueOf(dto.getLanguage().name()) : null);
        entity.setAccountStatus(dto.getAccountStatus() != null ? UserEntity.AccountStatus.valueOf(dto.getAccountStatus().name()) : null);

        if (dto.getStats() != null) {
            var stats = new UserEntity.Stats();
            stats.setBooksPurchased(dto.getStats().getBooksPurchased());
            stats.setHoursSpentReading(dto.getStats().getHoursSpentReading());
            stats.setLastLoginAt(toOffsetDateTime(dto.getStats().getLastLoginAt()));
            entity.setStats(stats);
        }

        if (dto.getMetadata() != null) {
            var metadata = new UserEntity.Metadata();
            metadata.setCreatedAt(toOffsetDateTime(dto.getMetadata().getCreatedAt()));
            metadata.setLastUpdated(toOffsetDateTime(dto.getMetadata().getLastUpdated()));
            entity.setMetadata(metadata);
        }

        return entity;
    }

    private static Channel mapChannel(UserEntity.Channel src) {
        if (src == null) return null;
        return new Channel()
                .enabled(src.getEnabled())
                .provider(src.getProvider())
                .lastSentAt(toLocalDateTime(src.getLastSentAt()));
    }
}
