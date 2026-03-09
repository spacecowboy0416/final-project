##Mysql 마이그레이션 코드##
##RDS 연결 DB 컬럼 수정 시 명시하고 여기에 넣으시면 됩니다##
##넘버링이 되어 있는데, 쓰지 않으셔도 무방합니다##

-- 1) 카테고리
CREATE TABLE category (
  category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  sort_order INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) 태그
CREATE TABLE tag (
  tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(30) NOT NULL,
  name VARCHAR(50) NOT NULL,
  UNIQUE KEY uk_tag_type_name (type, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) 사용자
CREATE TABLE users (
  user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  provider VARCHAR(20) NOT NULL,
  provider_user_id VARCHAR(120) NOT NULL,
  email VARCHAR(120) NULL,
  nickname VARCHAR(50) NULL,
  profile_image_url VARCHAR(500) NULL,
  role VARCHAR(20) DEFAULT 'USER',
  status VARCHAR(20) DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  last_login_at DATETIME NULL,
  UNIQUE KEY uk_provider_user (provider, provider_user_id),
  INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3-1) 날씨 스냅샷 (recommendation에서 참조하므로 먼저 생성)
CREATE TABLE weather_snapshot (
  weather_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  temp DOUBLE,
  condition_text VARCHAR(50),
  humidity INT,
  wind_speed DOUBLE,
  recorded_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4) 옷장(내 옷)
CREATE TABLE closet_item (
  item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  name VARCHAR(100),
  color VARCHAR(50),
  season VARCHAR(30),
  thickness TINYINT,
  image_url VARCHAR(500),
  is_active BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_closet_user (user_id),
  INDEX idx_closet_user_category (user_id, category_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (category_id) REFERENCES category(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5) 내 옷 태그 매핑
CREATE TABLE closet_item_tag (
  item_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (item_id, tag_id),
  FOREIGN KEY (item_id) REFERENCES closet_item(item_id) ON DELETE CASCADE,
  FOREIGN KEY (tag_id) REFERENCES tag(tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6) 추천 결과
CREATE TABLE recommendation (
  rec_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  weather_id BIGINT NULL,
  input_mode VARCHAR(20) NOT NULL,
  input_text VARCHAR(500),
  product_option VARCHAR(20) NOT NULL,
  is_saved BOOLEAN DEFAULT FALSE,
  ai_blueprint JSON,
  ai_explanation TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_rec_user_time (user_id, created_at),
  INDEX idx_rec_user_saved (user_id, is_saved),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (weather_id) REFERENCES weather_snapshot(weather_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7) 추천 결과 태그 매핑
CREATE TABLE recommendation_tag (
  rec_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (rec_id, tag_id),
  FOREIGN KEY (rec_id) REFERENCES recommendation(rec_id) ON DELETE CASCADE,
  FOREIGN KEY (tag_id) REFERENCES tag(tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8) 상품 카탈로그
CREATE TABLE product (
  product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  source VARCHAR(30) DEFAULT 'NAVER',
  external_id VARCHAR(100),
  category_id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  brand VARCHAR(100),
  price INT,
  image_url VARCHAR(500),
  link VARCHAR(800),
  fetched_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_product_external (source, external_id),
  INDEX idx_product_category (category_id),
  FOREIGN KEY (category_id) REFERENCES category(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8-1) 상품 태그 매핑
CREATE TABLE product_tag (
  product_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (product_id, tag_id),
  FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
  FOREIGN KEY (tag_id) REFERENCES tag(tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9) 추천 결과 아이템 상세
CREATE TABLE recommendation_item (
  rec_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rec_id BIGINT NOT NULL,
  source_type VARCHAR(20) NOT NULL,
  closet_item_id BIGINT NULL,
  product_id BIGINT NULL,
  category_id BIGINT NOT NULL,
  position_no INT DEFAULT 0,
  reason TEXT,
  INDEX idx_rec_item_rec (rec_id),
  FOREIGN KEY (rec_id) REFERENCES recommendation(rec_id) ON DELETE CASCADE,
  FOREIGN KEY (closet_item_id) REFERENCES closet_item(item_id),
  FOREIGN KEY (category_id) REFERENCES category(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10) 코디 공유 게시판
CREATE TABLE board_post (
  post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  rec_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  content TEXT,
  is_public BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_post_time (created_at),
  INDEX idx_post_user (user_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (rec_id) REFERENCES recommendation(rec_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11) 댓글
CREATE TABLE board_comment (
  comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_comment_post (post_id, created_at),
  FOREIGN KEY (post_id) REFERENCES board_post(post_id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 2026-03-05, taeju, 
-- =========================================================

-- 12) 에러로그
CREATE TABLE system_error_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    error_hash VARCHAR(64) UNIQUE NOT NULL,
    error_type VARCHAR(100),
    message TEXT,
    stack_trace TEXT,
    ai_solution TEXT,
    occurrence_count INT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_occurred_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 2026-03-09, jin, 코디 추천 로직 고도화에 따른 스키마 변경
-- =========================================================

-- 13) 날씨 스냅샷 보강 (CoordinationRequestDto + KakaoMapPort 연계)
ALTER TABLE weather_snapshot
  ADD COLUMN feels_like DOUBLE NULL AFTER temp,
  ADD COLUMN weather_status VARCHAR(20) NULL AFTER condition_text,
  ADD COLUMN precipitation_type VARCHAR(20) NULL AFTER weather_status,
  ADD COLUMN rain_probability VARCHAR(20) NULL AFTER precipitation_type,
  ADD COLUMN latitude DECIMAL(10,7) NULL AFTER wind_speed,
  ADD COLUMN longitude DECIMAL(10,7) NULL AFTER latitude,
  ADD COLUMN place_name VARCHAR(120) NULL AFTER longitude,
  ADD COLUMN address_name VARCHAR(255) NULL AFTER place_name,
  ADD COLUMN resolved_by VARCHAR(20) NULL AFTER address_name;

CREATE INDEX idx_weather_recorded_at ON weather_snapshot (recorded_at);
CREATE INDEX idx_weather_geo_time ON weather_snapshot (latitude, longitude, recorded_at);

-- 14) recommendation 응답 메타 보강 (CoordinationResponseDto)
ALTER TABLE recommendation
  ADD COLUMN coordination_id VARCHAR(64) NULL AFTER rec_id,
  ADD COLUMN status VARCHAR(20) NULL AFTER product_option,
  ADD COLUMN blueprint_source VARCHAR(20) NULL AFTER status,
  ADD COLUMN tpo_type VARCHAR(20) NULL AFTER blueprint_source,
  ADD COLUMN style_type VARCHAR(20) NULL AFTER tpo_type;

CREATE UNIQUE INDEX uk_recommendation_coordination_id ON recommendation (coordination_id);

-- 15) recommendation_item 상세 보강 (RecommendationItemDto / mapper SQL 정합)
ALTER TABLE recommendation_item
  ADD COLUMN slot_key VARCHAR(20) NULL AFTER rec_id,
  ADD COLUMN item_name VARCHAR(200) NULL AFTER category_id,
  ADD COLUMN search_query VARCHAR(300) NULL AFTER item_name,
  ADD COLUMN attributes_json JSON NULL AFTER search_query,
  ADD COLUMN temp_min INT NULL AFTER attributes_json,
  ADD COLUMN temp_max INT NULL AFTER temp_min,
  ADD COLUMN priority VARCHAR(20) NULL AFTER temp_max,
  ADD COLUMN selection_stage VARCHAR(30) NULL AFTER priority,
  ADD COLUMN match_score DECIMAL(6,4) NULL AFTER selection_stage,
  ADD COLUMN style_score DECIMAL(6,4) NULL AFTER match_score,
  ADD COLUMN color_score DECIMAL(6,4) NULL AFTER style_score,
  ADD COLUMN temp_score DECIMAL(6,4) NULL AFTER color_score,
  ADD COLUMN scoring_details_json JSON NULL AFTER temp_score;

ALTER TABLE recommendation_item
  ADD CONSTRAINT fk_recommendation_item_product
  FOREIGN KEY (product_id) REFERENCES product(product_id);

CREATE INDEX idx_rec_item_slot ON recommendation_item (rec_id, slot_key);
CREATE INDEX idx_rec_item_product ON recommendation_item (product_id);
CREATE INDEX idx_rec_item_priority ON recommendation_item (priority);

-- 16) 업로드 이미지 메타데이터 (ItemMetadataRecorder 연계)
CREATE TABLE recommendation_image_metadata (
  image_meta_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rec_id BIGINT NULL,
  user_id BIGINT NULL,
  mime_type VARCHAR(100) NOT NULL,
  image_size_bytes INT NULL,
  storage_provider VARCHAR(30) NOT NULL DEFAULT 'S3',
  storage_url VARCHAR(800) NOT NULL,
  storage_path VARCHAR(255) NULL,
  checksum_sha256 CHAR(64) NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_image_meta_rec (rec_id),
  INDEX idx_image_meta_user_time (user_id, created_at),
  INDEX idx_image_meta_checksum (checksum_sha256),
  CONSTRAINT fk_image_meta_rec FOREIGN KEY (rec_id) REFERENCES recommendation(rec_id),
  CONSTRAINT fk_image_meta_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17) enum/category/tag 코드 정합성 시드
INSERT INTO category (code, name, sort_order) VALUES
  ('top', '상의', 10),
  ('pants', '하의', 20),
  ('outerwear', '아우터', 30),
  ('shoes', '신발', 40),
  ('accessory', '액세서리', 50),
  ('dress', '원피스', 60),
  ('jacket', '자켓', 70)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  sort_order = VALUES(sort_order);

INSERT INTO tag (type, name) VALUES
  ('STYLE', 'minimal'),
  ('STYLE', 'comfortable'),
  ('STYLE', 'street'),
  ('STYLE', 'sporty'),
  ('STYLE', 'classic'),
  ('STYLE', 'glam'),
  ('COLOR', 'black'),
  ('COLOR', 'white'),
  ('COLOR', 'gray'),
  ('COLOR', 'navy'),
  ('COLOR', 'blue'),
  ('COLOR', 'beige'),
  ('COLOR', 'brown'),
  ('COLOR', 'khaki'),
  ('COLOR', 'green'),
  ('COLOR', 'red'),
  ('COLOR', 'pink'),
  ('COLOR', 'purple'),
  ('COLOR', 'yellow'),
  ('COLOR', 'orange'),
  ('MATERIAL', 'cotton'),
  ('MATERIAL', 'denim'),
  ('MATERIAL', 'wool'),
  ('MATERIAL', 'leather'),
  ('MATERIAL', 'knit'),
  ('MATERIAL', 'linen'),
  ('MATERIAL', 'nylon'),
  ('MATERIAL', 'polyester'),
  ('FIT', 'slim'),
  ('FIT', 'regular'),
  ('FIT', 'relaxed'),
  ('FIT', 'oversized'),
  ('FIT', 'wide'),
  ('TPO', 'date'),
  ('TPO', 'work'),
  ('TPO', 'casual'),
  ('TPO', 'exercise'),
  ('TPO', 'travel'),
  ('TPO', 'formal'),
  ('TPO', 'funeral'),
  ('TPO', 'wedding')
ON DUPLICATE KEY UPDATE
  name = VALUES(name);

