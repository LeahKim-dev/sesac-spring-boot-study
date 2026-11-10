package com.example.personal_project.application;

import com.example.personal_project.domain.EntityNotFoundException;
import com.example.personal_project.domain.ItemRepository;
import com.example.personal_project.infrastructure.MemoryItemRepository;
import com.example.personal_project.presentation.ItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ItemServiceTest {
    @Autowired
    ItemService itemService;

    @Test
    @DisplayName("아이템 등록 시 이름 공백을 제거하고 실제 id로 조회한다")
    void 정상_등록_id_조회() {
        ItemDto itemDto = new ItemDto(null, "    노트  ", 12000);
        ItemDto savedItemDto = itemService.add(itemDto);
        Long savedItemId = savedItemDto.id();

        ItemDto foundItemDto = itemService.findById(savedItemId);

        assertNotNull(savedItemId);
        assertEquals("노트", savedItemDto.name());
        assertEquals(savedItemDto, foundItemDto);
    }

    @Test
    @DisplayName("없는 아이템 id로 조회하면 예외가 발생한다")
    void 없는_id_조회시_예외_발생() {
        Long notExistId = -1L;

        assertThrows(EntityNotFoundException.class, () -> {
            itemService.findById(notExistId);
        });
    }

    @Test
    @DisplayName("이름이 null이면 등록을 거부한다")
    void null_이름_등록시_예외_발생() {
        ItemDto itemDto = new ItemDto(null, null, 1000);

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.add(itemDto);
        });
    }

    @Test
    @DisplayName("이름이 빈 문자열이면 등록을 거부한다")
    void 빈_이름_등록시_예외_발생() {
        ItemDto itemDto = new ItemDto(null, "", 1000);

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.add(itemDto);
        });
    }

    @Test
    @DisplayName("공백만 있는 이름으로 등록하면 예외가 발생한다")
    void 공백_이름_등록시_예외_발생() {
        ItemDto itemDto = new ItemDto(null, "   ", 1000);

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.add(itemDto);
        });
    }

    @Test
    @DisplayName("음수 가격으로 등록하면 예외가 발생한다")
    void 음수_가격_등록시_예외_발생() {
        ItemDto itemDto = new ItemDto(null, "노트", -1);

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.add(itemDto);
        });
    }

    @Test
    @DisplayName("등록에 실패하면 기존 목록이 유지된다")
    void 등록_실패시_기존_목록_유지() {
        itemService.add(new ItemDto(null, "노트", 12000));
        List<ItemDto> before = itemService.findAll();

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.add(new ItemDto(null, "   ", 1000));
        });

        List<ItemDto> after = itemService.findAll();

        assertEquals(before, after);
    }
    @Test
    @DisplayName("음수 예산으로 추천하면 예외가 발생한다")
    void 음수_예산_추천시_예외_발생() {
        int budget = -1;

        assertThrows(IllegalArgumentException.class, () -> {
            itemService.recommend(budget);
        });
    }

    @Test
    @DisplayName("추천을 조회해도 원본 목록은 바뀌지 않는다")
    void 추천_조회_전후_원본_목록_유지() {
        itemService.add(new ItemDto(null, "노트", 12000));
        itemService.add(new ItemDto(null, "무료 샘플", 0));
        itemService.add(new ItemDto(null, "펜", 5000));

        List<ItemDto> before = itemService.findAll();

        itemService.recommend(5000);

        List<ItemDto> after = itemService.findAll();

        assertEquals(before, after);
    }

    @Test
    @DisplayName("검색어의 앞뒤 공백을 제거하고 이름 부분 검색을 한다")
    void 이름_부분_검색() {
        ItemDto savedItem = itemService.add(
                new ItemDto(null, "캠핑 랜턴", 20000)
        );

        List<ItemDto> result = itemService.findByNameContaining("  랜턴  ");

        assertTrue(result.contains(savedItem));
    }

    @Test
    @DisplayName("빈 검색어로 검색하면 예외가 발생한다")
    void 빈_검색어_검색시_예외_발생() {
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.findByNameContaining("");
        });
    }

    @Test
    @DisplayName("공백만 있는 검색어로 검색하면 예외가 발생한다")
    void 공백_검색어_검색시_예외_발생() {
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.findByNameContaining("   ");
        });
    }

    @Test
    @DisplayName("이름이 일치하는 아이템이 없으면 빈 목록을 반환한다")
    void 검색_결과가_없으면_빈_목록_반환() {
        List<ItemDto> result =
                itemService.findByNameContaining("존재하지않는검색어");

        assertEquals(List.of(), result);
    }
}
