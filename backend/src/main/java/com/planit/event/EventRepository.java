package com.planit.event;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

	Optional<Event> findByIdAndDeletedAtIsNull(Long id);

	Optional<Event> findByCreatedByAndRequestId(Long createdBy, String requestId);

	/**
	 * 캘린더 조회: 내 개인 일정 + 내가 속한 팀의 일정 중 [from, to)와 겹치는 것.
	 * 삭제한 일정과 내가 숨긴 일정은 빠진다.
	 */
	@Query("""
			select e from Event e
			where e.deletedAt is null
			  and e.startsAt < :to and e.endsAt > :from
			  and ((e.teamId is null and e.createdBy = :userId)
			       or e.teamId in (select m.id.teamId from TeamMember m where m.id.userId = :userId))
			  and not exists (select s from EventUserSetting s
			                  where s.id.eventId = e.id and s.id.userId = :userId and s.hidden = true)
			order by e.startsAt, e.id
			""")
	List<Event> findCalendar(@Param("userId") Long userId, @Param("from") Instant from, @Param("to") Instant to);

}
