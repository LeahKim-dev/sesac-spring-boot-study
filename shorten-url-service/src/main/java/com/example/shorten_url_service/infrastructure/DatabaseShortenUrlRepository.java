package com.example.shorten_url_service.infrastructure;

import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.domain.ShortenUrlRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DatabaseShortenUrlRepository implements ShortenUrlRepository {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseShortenUrlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey) {
        String sql = """
                SELECT original_url, shorten_url_key, redirect_count
                FROM shorten_urls
                WHERE shorten_url_key = ?
                """;

        List<ShortenUrl> results = jdbcTemplate.query(
                sql, this::toShortenUrl, shortenUrlKey
        );

        if (results.isEmpty()) {
            return null;
        }

        return results.getFirst();
    }

    private ShortenUrl toShortenUrl(ResultSet row, int rowNum) throws SQLException {
        String originalUrl = row.getString("original_url");
        String shortenUrlKey = row.getString("shorten_url_key");
        long redirectCount = row.getLong("redirect_count");

        return new ShortenUrl(originalUrl, shortenUrlKey, redirectCount);
    }

    public void insertShortenUrl(ShortenUrl shortenUrl) {
        String sql = """
            INSERT INTO shorten_urls (original_url, shorten_url_key, redirect_count)
            VALUES (?, ?, ?)
            """;

        jdbcTemplate.update(
                sql,
                shortenUrl.getOriginalUrl(),
                shortenUrl.getShortenUrlKey(),
                shortenUrl.getRedirectCount()
        );
    }

    public void increaseRedirectCount(String shortenUrlKey) {
        String sql = """
            UPDATE shorten_urls
            SET redirect_count = redirect_count + 1
            WHERE shorten_url_key = ?
            """;

        jdbcTemplate.update(sql, shortenUrlKey);
    }

}