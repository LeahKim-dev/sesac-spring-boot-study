package com.example.shorten_url_service.infrastructure;

import com.example.shorten_url_service.domain.ShortenUrl;
import com.example.shorten_url_service.domain.ShortenUrlRepository;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class DatabaseShortenUrlRepository implements ShortenUrlRepository {

    private final ShortenUrlJpaRepository shortenUrlJpaRepository;

    public DatabaseShortenUrlRepository(
            ShortenUrlJpaRepository shortenUrlJpaRepository
    ) {
        this.shortenUrlJpaRepository = shortenUrlJpaRepository;
    }

    @Override
    public ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey) {
        Optional<ShortenUrlEntity> result =
                shortenUrlJpaRepository.findByShortenUrlKey(shortenUrlKey);

        if (result.isEmpty()) {
            return null;
        }

        ShortenUrlEntity entity = result.get();
        return new ShortenUrl(
                entity.getOriginalUrl(),
                entity.getShortenUrlKey(),
                entity.getRedirectCount()
        );
    }

    private ShortenUrl toShortenUrl(ResultSet row, int rowNum) throws SQLException {
        String originalUrl = row.getString("original_url");
        String shortenUrlKey = row.getString("shorten_url_key");
        long redirectCount = row.getLong("redirect_count");

        return new ShortenUrl(originalUrl, shortenUrlKey, redirectCount);
    }

    @Override
    public void insertShortenUrl(ShortenUrl shortenUrl) {
        ShortenUrlEntity entity = new ShortenUrlEntity(
                shortenUrl.getOriginalUrl(),
                shortenUrl.getShortenUrlKey(),
                shortenUrl.getRedirectCount()
        );

        shortenUrlJpaRepository.save(entity);
    }

    @Override
    public void increaseRedirectCount(String shortenUrlKey) {
        shortenUrlJpaRepository.increaseRedirectCount(shortenUrlKey);
    }

}