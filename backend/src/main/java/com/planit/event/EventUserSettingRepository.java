package com.planit.event;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventUserSettingRepository extends JpaRepository<EventUserSetting, EventUserSetting.Id> {

	default Optional<EventUserSetting> findSetting(Long eventId, Long userId) {
		return findById(new EventUserSetting.Id(eventId, userId));
	}

	List<EventUserSetting> findByIdUserIdAndIdEventIdIn(Long userId, Collection<Long> eventIds);

	List<EventUserSetting> findByIdEventIdAndReminderAtIsNotNull(Long eventId);

}
