CREATE TABLE IF NOT EXISTS shorten_urls (
    id BIGINT NOT NULL AUTO_INCREMENT,
    original_url LONGTEXT NOT NULL,
    shorten_url_key VARCHAR(8) NOT NULL,
    redirect_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_shorten_urls_key UNIQUE (shorten_url_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
