package com.example.personal_project.application;

import com.example.personal_project.domain.Item;
import com.example.personal_project.domain.ItemRepository;
import com.example.personal_project.presentation.ItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ValidationService validationService;
    @InjectMocks
    private ItemService itemService;

    @Test
    @DisplayName("등록한 아이템을 반환한다")
    void 정상_아이템_등록() {
        when(validationService.validateName("노트")).thenReturn("노트");
        when(validationService.validatePrice(12000)).thenReturn(12000);

        Item savedItem = new Item(1L, "노트", 12000);
        // any(Item.class): 어떤 Item 객체가 인자로 들어와도
        // Repository의 add에 어떤 Item 객체가 들어오든, 준비해 둔 savedItem을 반환
        when(itemRepository.add(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.add(new ItemDto(null, "노트", 12000));
        assertEquals(ItemDto.from(savedItem), result);
    }

    @Test
    @DisplayName("이름 검증에 실패하면 저장하지 않는다")
    void 이름_검증_실패시_저장하지_않음() {
        when(validationService.validateName("   "))
                .thenThrow(new IllegalArgumentException("이름을 입력해주세요."));

        ItemDto itemDto = new ItemDto(null, "   ", 1000);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.add(itemDto));

        verifyNoInteractions(itemRepository);// 저장소에 아무 요청도 하지 않았나?
    }

    @Test
    @DisplayName("예산이 0원이면 무료 아이템만 추천한다")
    void 예산_0원시_무료_아이템_추천() {
        // 가짜 검증 객체가 예산 0을 받으면, 검증을 통과한 값 0을 반환
        when(validationService.validateBudget(0)).thenReturn(0);

        List<Item> items = List.of(
                new Item(1L, "노트", 12000),
                new Item(2L, "무료 샘플", 0)
        );
        // Service가 가짜 Repository에 전체 목록을 요청하면, 두 아이템의 목록을 반환
        when(itemRepository.findAll()).thenReturn(items);

        List<ItemDto> result = itemService.recommend(0);

        List<ItemDto> expected = List.of(
                new ItemDto(2L, "무료 샘플", 0)
        );
        // 실제 추천 결과가 무료 샘플 하나뿐인 목록과 같은가?
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("예산 이하의 아이템을 가격순으로 최대 3개 추천한다")
    void 예산_이하_가격순_최대_3개_추천() {
        when(validationService.validateBudget(5000)).thenReturn(5000);

        List<Item> items = List.of(
                new Item(1L, "노트", 12000),
                new Item(2L, "무료 샘플", 0),
                new Item(3L, "펜", 5000),
                new Item(4L, "스티커", 5000),
                new Item(5L, "책갈피", 1000)
        );
        when(itemRepository.findAll()).thenReturn(items);

        List<ItemDto> result = itemService.recommend(5000);

        List<ItemDto> expected = List.of(
                new ItemDto(2L, "무료 샘플", 0),
                new ItemDto(5L, "책갈피", 1000),
                new ItemDto(3L, "펜", 5000)
        );
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("예산 이하의 아이템이 없으면 빈 목록을 반환한다")
    void 추천_후보가_없으면_빈_목록_반환() {
        when(validationService.validateBudget(1000)).thenReturn(1000);

        List<Item> items = List.of(
                new Item(1L, "노트", 12000),
                new Item(2L, "펜", 5000)
        );
        when(itemRepository.findAll()).thenReturn(items);

        List<ItemDto> result = itemService.recommend(1000);

        assertEquals(List.of(), result);
    }
}
