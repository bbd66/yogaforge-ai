-- Myfitpet schema (PostgreSQL)
-- Core tables: users, pets, exercise_sessions, user_metrics

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  email VARCHAR(128) NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  avatar_url TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pets (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR(64) NOT NULL,
  species VARCHAR(64),
  level INT DEFAULT 1,
  exp INT DEFAULT 0,
  gold INT DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS exercise_sessions (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  pet_id BIGINT REFERENCES pets(id) ON DELETE SET NULL,
  device_id BIGINT,
  type VARCHAR(32) NOT NULL, -- e.g., 'yoga','rope','treadmill'
  start_time TIMESTAMPTZ NOT NULL,
  end_time TIMESTAMPTZ,
  duration_seconds INT,
  pose_count INT DEFAULT 0,
  quality_score DOUBLE PRECISION,
  calories INT DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_metrics (
  user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  total_calories BIGINT DEFAULT 0,
  total_sessions INT DEFAULT 0,
  total_yoga_seconds BIGINT DEFAULT 0,
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- touch updated_at on update
CREATE OR REPLACE FUNCTION touch_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at := NOW();
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'users_touch_updated_at'
  ) THEN
    CREATE TRIGGER users_touch_updated_at BEFORE UPDATE ON users
      FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'pets_touch_updated_at'
  ) THEN
    CREATE TRIGGER pets_touch_updated_at BEFORE UPDATE ON pets
      FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'exercise_sessions_touch_updated_at'
  ) THEN
    CREATE TRIGGER exercise_sessions_touch_updated_at BEFORE UPDATE ON exercise_sessions
      FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
  END IF;
END $$;

-- compute duration on end_time set
CREATE OR REPLACE FUNCTION compute_duration() RETURNS TRIGGER AS $$
BEGIN
  IF NEW.end_time IS NOT NULL THEN
    NEW.duration_seconds := GREATEST(0, EXTRACT(EPOCH FROM (NEW.end_time - NEW.start_time))::INT);
  END IF;
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'exercise_sessions_compute_duration'
  ) THEN
    CREATE TRIGGER exercise_sessions_compute_duration BEFORE UPDATE ON exercise_sessions
      FOR EACH ROW EXECUTE FUNCTION compute_duration();
  END IF;
END $$;

-- simple calories heuristic: duration_minutes * 4 for yoga
CREATE OR REPLACE FUNCTION compute_calories() RETURNS TRIGGER AS $$
BEGIN
  IF NEW.type = 'yoga' AND NEW.duration_seconds IS NOT NULL THEN
    NEW.calories := COALESCE(NEW.calories, 0) + (NEW.duration_seconds / 60) * 4;
  END IF;
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'exercise_sessions_compute_calories'
  ) THEN
    CREATE TRIGGER exercise_sessions_compute_calories BEFORE UPDATE ON exercise_sessions
      FOR EACH ROW EXECUTE FUNCTION compute_calories();
  END IF;
END $$;

-- seed metrics rows on user insert
CREATE OR REPLACE FUNCTION init_user_metrics() RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO user_metrics(user_id) VALUES(NEW.id) ON CONFLICT DO NOTHING;
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'users_init_metrics'
  ) THEN
    CREATE TRIGGER users_init_metrics AFTER INSERT ON users
      FOR EACH ROW EXECUTE FUNCTION init_user_metrics();
  END IF;
END $$;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_exercise_sessions_user ON exercise_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_exercise_sessions_type ON exercise_sessions(type);
-- PostgreSQL 简化版 Schema 满足最新需求：
-- 需求要点：
-- 1. 用户：用户名、邮箱、密码、头像。
-- 2. 宠物：固定前端形象，只存状态数值：金币、经验、能量、心情（可选等级）。
-- 3. 对话：与外接大模型的交互记录（支持会话分组、顺序、角色、模型信息、令牌统计）。
-- 4. 设备：编号、使用总时长、使用次数；保留单次会话日志用于统计回溯。
-- 保留最小可扩展设计，去除成就/商城/运动会话等旧需求表。

-- =============================
-- 用户
-- =============================
CREATE TABLE users (
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(128) UNIQUE NOT NULL,
    username       VARCHAR(64) NOT NULL,
    password_hash  TEXT NOT NULL, -- 后端散列
    avatar_url     TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_users_email ON users(email);

-- =============================
-- 宠物（单用户可支持多宠物，若仅 1 个也可在逻辑层限制）
-- level 与 exp: 经验换算升级可选，若只需要经验可移除 level。
-- mood / energy 范围 0-100，前端操作由后端校验。
-- coins: 与宠物绑定的货币池（若希望所有宠物共享可改存在 users）。
-- last_interaction_at: 最近一次对话或交互，可用于活跃度展示。
-- =============================
CREATE TABLE pets (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name              VARCHAR(64) NOT NULL,
    species           VARCHAR(32) DEFAULT 'fixed', -- 前端固定形象标识
    level             INT NOT NULL DEFAULT 1,
    exp               INT NOT NULL DEFAULT 0,
    mood              INT NOT NULL DEFAULT 50 CHECK (mood BETWEEN 0 AND 100),
    energy            INT NOT NULL DEFAULT 50 CHECK (energy BETWEEN 0 AND 100),
    coins             INT NOT NULL DEFAULT 0,
    last_interaction_at TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_pets_user ON pets(user_id);

-- 宠物状态快照日志（可选，用于趋势/审计；若不需要可删除此表）
CREATE TABLE pet_status_history (
    id          BIGSERIAL PRIMARY KEY,
    pet_id      BIGINT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    level       INT NOT NULL,
    exp         INT NOT NULL,
    mood        INT NOT NULL CHECK (mood BETWEEN 0 AND 100),
    energy      INT NOT NULL CHECK (energy BETWEEN 0 AND 100),
    coins       INT NOT NULL,
    reason      VARCHAR(32), -- feed/conversation/upgrade/manual
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_pet_status_history_pet ON pet_status_history(pet_id);
CREATE INDEX idx_pet_status_history_time ON pet_status_history(created_at);

-- =============================
-- 宠物与模型对话记录
-- conversation_id 用于分组一段连续会话；
-- role: user / pet / assistant / system；
-- seq: 在同一 conversation 内的顺序（防乱序）；
-- tokens_*：可选埋点统计模型花费，便于成本与长度限制控制；
-- model_name：保存调用模型标识，后续分析不同模型质量；
-- reply_to_message_id：用于引用或多轮关联（可选）。
-- =============================
CREATE TABLE pet_conversations (
    id                   BIGSERIAL PRIMARY KEY,
    conversation_id      UUID NOT NULL,
    pet_id               BIGINT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    user_id              BIGINT REFERENCES users(id) ON DELETE SET NULL,
    seq                  INT NOT NULL,
    role                 VARCHAR(16) NOT NULL, -- user/pet/assistant/system
    message              TEXT NOT NULL,
    model_name           VARCHAR(64),
    tokens_in            INT,
    tokens_out           INT,
    reply_to_message_id  BIGINT REFERENCES pet_conversations(id) ON DELETE SET NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(conversation_id, seq)
);
CREATE INDEX idx_pet_conv_pet ON pet_conversations(pet_id);
CREATE INDEX idx_pet_conv_conversation ON pet_conversations(conversation_id);
CREATE INDEX idx_pet_conv_created ON pet_conversations(created_at);

-- 会话摘要表（可选，提升列表查询性能）
CREATE TABLE pet_conversation_sessions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet_id           BIGINT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    user_id          BIGINT REFERENCES users(id) ON DELETE SET NULL,
    title            VARCHAR(128), -- 自动摘要或用户命名
    message_count    INT NOT NULL DEFAULT 0,
    last_message_at  TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_conv_sessions_pet ON pet_conversation_sessions(pet_id);

-- =============================
-- 设备与使用统计
-- total_usage_seconds 与 usage_count 作为聚合字段，
-- 通过 device_sessions 结束时触发器自动更新，减少实时聚合开销。
-- =============================
CREATE TABLE devices (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_code         VARCHAR(64) NOT NULL, -- 设备编号（前端或硬件提供）
    total_usage_seconds BIGINT NOT NULL DEFAULT 0,
    usage_count         INT NOT NULL DEFAULT 0,
    status              VARCHAR(16) NOT NULL DEFAULT 'active', -- active/inactive
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, device_code)
);
CREATE INDEX idx_devices_user ON devices(user_id);

-- 单次设备使用会话日志
CREATE TABLE device_sessions (
    id             BIGSERIAL PRIMARY KEY,
    device_id      BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_time     TIMESTAMPTZ NOT NULL,
    end_time       TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (end_time IS NULL OR end_time >= start_time)
);
CREATE INDEX idx_device_sessions_device ON device_sessions(device_id);
CREATE INDEX idx_device_sessions_user ON device_sessions(user_id);

-- =============================
-- 触发器：更新时间戳 + 设备聚合 + 宠物最近交互
-- =============================
CREATE OR REPLACE FUNCTION touch_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_pets_updated BEFORE UPDATE ON pets FOR EACH ROW EXECUTE FUNCTION touch_updated_at();
CREATE TRIGGER trg_devices_updated BEFORE UPDATE ON devices FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

-- 设备会话结束后聚合更新设备统计
CREATE OR REPLACE FUNCTION finalize_device_session() RETURNS TRIGGER AS $$
DECLARE
  session_seconds BIGINT;
BEGIN
  IF (NEW.end_time IS NOT NULL) THEN
    session_seconds := EXTRACT(EPOCH FROM (NEW.end_time - NEW.start_time));
    UPDATE devices
      SET total_usage_seconds = total_usage_seconds + session_seconds,
          usage_count = usage_count + 1,
          updated_at = NOW()
    WHERE id = NEW.device_id;
  END IF;
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_device_session_finalize
  AFTER UPDATE OF end_time ON device_sessions
  FOR EACH ROW
  WHEN (OLD.end_time IS NULL AND NEW.end_time IS NOT NULL)
  EXECUTE FUNCTION finalize_device_session();

-- 宠物对话插入后刷新最近交互时间 & 可选心情微调（示例：每次对话 mood+1 上限100）
CREATE OR REPLACE FUNCTION pet_conversation_after_insert() RETURNS TRIGGER AS $$
BEGIN
  UPDATE pets
    SET last_interaction_at = NEW.created_at,
        mood = LEAST(100, mood + CASE WHEN NEW.role = 'assistant' OR NEW.role = 'pet' THEN 1 ELSE 0 END),
        updated_at = NOW()
  WHERE id = NEW.pet_id;
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pet_conv_insert
  AFTER INSERT ON pet_conversations
  FOR EACH ROW
  EXECUTE FUNCTION pet_conversation_after_insert();

-- 会话消息计数刷新到会话摘要表
CREATE OR REPLACE FUNCTION update_conv_session_stats() RETURNS TRIGGER AS $$
BEGIN
  UPDATE pet_conversation_sessions
    SET message_count = message_count + 1,
        last_message_at = NEW.created_at
  WHERE id = NEW.conversation_id;
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pet_conv_session_stats
  AFTER INSERT ON pet_conversations
  FOR EACH ROW
  EXECUTE FUNCTION update_conv_session_stats();

-- =============================
-- 升级与公式（逻辑层实现，不直接写入数据库）：
-- required_exp(level) = 100 + (level - 1) * 50
-- 经验/金币换算（示例）: exp_gain = floor(usage_seconds/60 * 0.5) ; coins_gain = floor(usage_seconds/60 * 0.2)
-- 喂食或互动：后端校验后写入 pet_status_history；若产生经验/金币则直接更新 pets。
-- 能量与心情都达到阈值可在业务层触发升级：level++, exp 重置或递减。

-- =============================
-- 前端仅缓存：
-- 1. 当前宠物状态快照（与后端定期同步）。
-- 2. 最近对话列表（分页或增量加载）。
-- 3. 当前设备实时使用计时（结束后写后端）。
-- 4. 临时未发送的对话消息（网络波动时 IndexedDB）。

-- 结束
-- =============================
-- 新增：通用运动会话与跳绳细粒度计数
-- 支持类型：rope(跳绳)/yoga(瑜伽)/treadmill(跑步机)
-- 跳绳：保存总跳数 + 可选细粒度事件（每次传感器上传增量）
-- 瑜伽/跑步机：以时长计算卡路里
-- calories 由应用层或触发器计算（可替换公式）
-- =============================

CREATE TABLE exercise_sessions (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pet_id           BIGINT REFERENCES pets(id) ON DELETE SET NULL,
    device_id        BIGINT REFERENCES devices(id) ON DELETE SET NULL,
    type             VARCHAR(16) NOT NULL CHECK (type IN ('rope','yoga','treadmill')),
    start_time       TIMESTAMPTZ NOT NULL,
    end_time         TIMESTAMPTZ, -- 结束前可为空
    duration_seconds INT GENERATED ALWAYS AS (
        CASE WHEN end_time IS NOT NULL THEN EXTRACT(EPOCH FROM (end_time - start_time)) ELSE NULL END
    ) STORED,
    total_jumps      INT, -- 仅 rope 使用；其它类型置 NULL
  pose_count       INT DEFAULT 0, -- 瑜伽：识别的有效体式/动作次数
  quality_score    NUMERIC(5,2), -- 瑜伽：整体质量/平均评分（结束时写入）
  calories         INT, -- 结束或更新后计算（触发器）
    raw_data         JSONB, -- 可选：存放传感器原始片段/统计
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (end_time IS NULL OR end_time >= start_time),
    CHECK ((type <> 'rope' AND total_jumps IS NULL) OR (type='rope'))
);
CREATE INDEX idx_ex_sessions_user ON exercise_sessions(user_id);
CREATE INDEX idx_ex_sessions_pet ON exercise_sessions(pet_id);
CREATE INDEX idx_ex_sessions_device ON exercise_sessions(device_id);
CREATE INDEX idx_ex_sessions_type ON exercise_sessions(type);
CREATE INDEX idx_ex_sessions_start ON exercise_sessions(start_time);
-- 复合索引：提升按类型与时间范围查询效率
CREATE INDEX idx_ex_sessions_type_start ON exercise_sessions(type, start_time);

-- 细粒度跳绳计数事件（可选使用；若不需要增量可只用 total_jumps）
CREATE TABLE jump_rope_events (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT NOT NULL REFERENCES exercise_sessions(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id       BIGINT REFERENCES devices(id) ON DELETE SET NULL,
    increment       INT NOT NULL CHECK (increment > 0), -- 本次上传新增跳数
    cumulative      INT NOT NULL, -- 上传时的累计总数（便于校验与对账）
    sensor_payload  JSONB, -- 可存加速度/时间戳组装后的数据
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_jump_events_session ON jump_rope_events(session_id);
CREATE INDEX idx_jump_events_user ON jump_rope_events(user_id);

-- 结束运动后自动计算卡路里触发器（示例公式，可在应用层实现更灵活逻辑）
-- rope: calories = floor(total_jumps * 0.1)
-- yoga: calories = floor(duration_seconds/60 * 5)  (示例 MET 转换)
-- treadmill: calories = floor(duration_seconds/60 * 8)
-- 可替换为基于用户体重、心率等的更精确算法

CREATE OR REPLACE FUNCTION compute_exercise_calories() RETURNS TRIGGER AS $$
DECLARE
  dur INT;
  jumps INT;
  cals INT;
BEGIN
  IF (NEW.end_time IS NOT NULL) THEN
    dur := NEW.duration_seconds; -- 秒
    IF NEW.type = 'rope' THEN
      jumps := COALESCE(NEW.total_jumps,0);
      cals := FLOOR(jumps * 0.1);
    ELSIF NEW.type = 'yoga' THEN
      cals := FLOOR(COALESCE(dur,0)/60 * 5);
    ELSIF NEW.type = 'treadmill' THEN
      cals := FLOOR(COALESCE(dur,0)/60 * 8);
    ELSE
      cals := 0;
    END IF;
    NEW.calories := cals;
    NEW.updated_at := NOW();
  END IF;
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_exercise_finalize
  BEFORE UPDATE OF end_time ON exercise_sessions
  FOR EACH ROW
  WHEN (OLD.end_time IS NULL AND NEW.end_time IS NOT NULL)
  EXECUTE FUNCTION compute_exercise_calories();

-- 当新增跳绳事件时自动更新会话的 total_jumps（若使用事件表）
CREATE OR REPLACE FUNCTION accumulate_jump_events() RETURNS TRIGGER AS $$
DECLARE
  current_total INT;
BEGIN
  SELECT total_jumps INTO current_total FROM exercise_sessions WHERE id = NEW.session_id FOR UPDATE;
  IF current_total IS NULL THEN
    current_total := 0;
  END IF;
  current_total := current_total + NEW.increment;
  UPDATE exercise_sessions SET total_jumps = current_total, updated_at = NOW() WHERE id = NEW.session_id;
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_jump_event_insert
  AFTER INSERT ON jump_rope_events
  FOR EACH ROW
  EXECUTE FUNCTION accumulate_jump_events();

-- 若需要在运动结束时自动将经验/金币奖励写入宠物，可在 finalize 之后单独业务层调用；
-- 或添加 AFTER UPDATE 触发器读取 NEW.calories -> 计算经验/金币 -> 更新 pets。
-- 出于可控性和公式易变性，此处不直接写奖励触发器，推荐应用层事务处理：
-- 1) 结束会话 -> 更新 exercise_sessions(end_time) -> 返回 calories
-- 2) 在服务层 computeRewardFromUsage(duration_seconds or total_jumps)
-- 3) 调用 applyRewardTransactional(petId,...)

-- =============================
-- 用户聚合统计表（方案 A）
-- =============================
CREATE TABLE user_metrics (
  user_id            BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  total_calories     BIGINT NOT NULL DEFAULT 0,
  total_sessions     INT    NOT NULL DEFAULT 0,
  total_yoga_seconds BIGINT NOT NULL DEFAULT 0,
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_user_metrics_total_calories ON user_metrics(total_calories);
CREATE INDEX idx_user_metrics_total_sessions ON user_metrics(total_sessions);

