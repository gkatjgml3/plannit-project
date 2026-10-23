package com.planit.event;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventChecklistItemRepository extends JpaRepository<EventChecklistItem, Long> {

	List<EventChecklistItem> findByEventIdOrderBySortOrder(Long eventId);

	List<EventChecklistItem> findByEventIdInOrderByEventIdAscSortOrderAsc(Collection<Long> eventIds);

	/** 입력값: 일정 id / 출력값: 지운 행 수 / 기능: 준비물 목록을 통째로 바꾸기 전에 기존 항목을 지운다. */
	@Modifying
	@Query("delete from EventChecklistItem i where i.eventId = :eventId")
	int deleteByEventId(@Param("eventId") Long eventId);

}
