CREATE TABLE IF NOT EXISTS movies (
    id SERIAL PRIMARY KEY,
    tmdb_id INTEGER UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    overview TEXT,
    release_date DATE,
    vote_average DECIMAL(3, 1),
    poster_path VARCHAR(512),
    created_at TIMESTAMP DEFAULT NOW()
);