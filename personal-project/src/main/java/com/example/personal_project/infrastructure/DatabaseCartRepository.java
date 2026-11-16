package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.Cart;
import com.example.personal_project.domain.CartItem;
import com.example.personal_project.domain.CartRepository;
import com.example.personal_project.domain.SourceType;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Profile("prod")
public class DatabaseCartRepository implements CartRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DatabaseCartRepository(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Cart getCart() {
        String stateSql = """
            SELECT next_sequence
            FROM cart_state
            WHERE id = :id
            """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource("id", 1L);

        Long nextSequence = namedParameterJdbcTemplate.queryForObject(
                stateSql,
                parameters,
                Long.class
        );

        String itemsSql = """
            SELECT id, item_id, name, unit_price,
                   source_type, stock, quantity
            FROM cart_items
            ORDER BY id ASC
            """;

        List<CartItem> items = namedParameterJdbcTemplate.query(
                itemsSql,
                cartItemRowMapper
        );

        Cart cart = new Cart(items, nextSequence);
        return cart;
    }

    @Override
    @Transactional
    public void save(Cart cart) {
        // 기존 저장 목록 삭제
        namedParameterJdbcTemplate.update(
                "DELETE FROM cart_items",
                new MapSqlParameterSource()
        );

        // 현재 장바구니 항목 저장
        String sql = """
            INSERT INTO cart_items
                (id, item_id, name, unit_price, source_type, stock, quantity)
            VALUES
                (:id, :itemId, :name, :unitPrice, :sourceType, :stock, :quantity)
            """;

        for (CartItem item : cart.getItems()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("id", item.getId())
                    .addValue("itemId", item.getItemId())
                    .addValue("name", item.getName())
                    .addValue("unitPrice", item.getUnitPrice())
                    .addValue("sourceType", item.getSourceType().name())
                    .addValue("stock", item.getStock())
                    .addValue("quantity", item.getQuantity());

            namedParameterJdbcTemplate.update(sql, parameters);
        }

        // 다음 행 번호 저장
        String stateSql = """
            UPDATE cart_state
            SET next_sequence = :nextSequence
            WHERE id = :id
            """;

        MapSqlParameterSource stateParameters = new MapSqlParameterSource()
                .addValue("id", 1L)
                .addValue("nextSequence", cart.getNextSequence());

        namedParameterJdbcTemplate.update(stateSql, stateParameters);
    }

    private final RowMapper<CartItem> cartItemRowMapper =
            (resultSet, rowNum) -> new CartItem(
                    resultSet.getLong("id"),
                    resultSet.getLong("item_id"),
                    resultSet.getString("name"),
                    resultSet.getLong("unit_price"),
                    SourceType.valueOf(resultSet.getString("source_type")),
                    resultSet.getObject("stock", Integer.class),
                    resultSet.getInt("quantity")
            );
}