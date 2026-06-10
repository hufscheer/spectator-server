# Database ERD

> **생성 기준**: `db/migration/prod/` V1–V18을 MySQL 8.0에 순차 적용한 실제 스키마 (2026-06-10, develop `e9e07c85`)
>
> 스키마 변경 시 이 문서를 함께 갱신해 주세요. (tbls 자동화 도입 전까지 수동 관리)

## 전체 ERD

```mermaid
erDiagram
    organizations {
        bigint id PK
        varchar name
        int student_number_digits
        varchar logo_image_url
    }

    units {
        bigint id PK
        varchar name
        bigint organization_id FK
    }

    members {
        bigint id PK
        bigint organization_id FK "NULL 허용"
        varchar email
        varchar password
        boolean is_administrator
        datetime last_login
    }

    teams {
        bigint id PK
        varchar name
        varchar logo_image_url
        varchar team_color
        boolean is_deleted
        varchar sport_type
        bigint unit_id FK "NULL 허용"
    }

    players {
        bigint id PK
        varchar name
        varchar student_number UK "UQ(organization_id, student_number)"
        bigint organization_id FK "NULL 허용"
    }

    team_players {
        bigint id PK
        bigint team_id FK "UQ(team_id, player_id)"
        bigint player_id FK
        int jersey_number
    }

    leagues {
        bigint id PK
        bigint organization_id FK
        bigint administrator_id FK
        varchar name
        datetime start_at
        datetime end_at
        boolean is_deleted
        varchar max_round
        varchar in_progress_round
        varchar sport_type
    }

    league_teams {
        bigint id PK
        bigint league_id FK "UQ(league_id, team_id)"
        bigint team_id FK
        int total_cheer_count
        int total_talk_count
        int ranking
    }

    league_top_scorers {
        bigint id PK
        bigint league_id FK
        bigint player_id FK
        int ranking
        int goal_count
    }

    league_statistics {
        bigint id PK
        bigint league_id FK
        bigint first_winner_team_id FK "NULL 허용"
        bigint second_winner_team_id FK "NULL 허용"
        bigint most_cheered_team_id FK "NULL 허용"
        bigint most_cheer_talks_team_id FK "NULL 허용"
    }

    games {
        bigint id PK
        bigint administrator_id FK "NULL 허용"
        bigint league_id FK
        datetime start_time
        varchar name
        varchar video_id
        datetime quarter_changed_at
        varchar game_quarter
        varchar state
        varchar round
        boolean is_pk_taken
    }

    game_teams {
        bigint id PK
        bigint game_id FK "UQ(game_id, team_id)"
        bigint team_id FK
        int cheer_count
        int score
        int pk_score
        varchar result
    }

    lineup_players {
        bigint id PK
        bigint game_team_id FK "UQ(game_team_id, player_id)"
        bigint player_id FK
        int jersey_number
        boolean is_captain
        varchar state
        boolean is_playing
        bigint replaced_player_id FK "self-reference, NULL 허용"
    }

    cheer_talks {
        bigint id PK
        bigint game_team_id FK "NULL 허용, ON DELETE SET NULL"
        varchar content
        datetime created_at
        varchar block_status "ACTIVE / BLOCKED_BY_ADMIN / BLOCKED_BY_BOT"
        boolean is_ai_seed
    }

    pending_cheer_talks {
        bigint id PK
        varchar destination
        json cheer_talk
        datetime created_at "INDEX"
    }

    reports {
        bigint id PK
        bigint cheer_talk_id FK
        datetime reported_at
        varchar state
    }

    cheer_talk_bot_filter_history {
        bigint id PK
        bigint cheer_talk_id FK "NULL 허용, ON DELETE SET NULL"
        datetime filtered_at
        varchar cheer_talk_filter_result
        varchar bot_type
        json raw_bot_response
        int latency_ms
    }

    timelines {
        bigint id PK
        varchar type
        bigint game_id FK
        varchar recorded_quarter
        int recorded_at
        bigint scorer_id FK "NULL 허용"
        bigint assist_lineup_player_id FK "NULL 허용, ON DELETE CASCADE"
        bigint origin_lineup_player_id FK "NULL 허용"
        bigint replaced_lineup_player_id FK "NULL 허용"
        bigint game_team1_id FK "NULL 허용"
        bigint game_team2_id FK "NULL 허용"
        int score
        int snapshot_score1
        int snapshot_score2
        boolean is_success
        boolean is_foul_out
        varchar game_progress_type
        varchar previous_quarter
        datetime previous_quarter_changed_at
        varchar warning_card_type
    }

    %% 조직 계층: Organization → Unit → Team (V18에서 단일 계층화)
    organizations ||--o{ units : "단과대"
    organizations |o--o{ members : "소속 관리자"
    organizations |o--o{ players : "소속 선수"
    organizations ||--o{ leagues : "주최"
    units |o--o{ teams : "소속 팀"

    %% 리그
    members ||--o{ leagues : "리그 관리자"
    members |o--o{ games : "경기 관리자"
    leagues ||--o{ games : "경기"
    leagues ||--o{ league_teams : "참가팀"
    leagues ||--o{ league_top_scorers : "득점왕"
    leagues ||--o{ league_statistics : "통계"
    teams ||--o{ league_teams : ""
    teams |o--o{ league_statistics : "우승·최다응원 팀 (FK 4개)"
    players ||--o{ league_top_scorers : ""

    %% 팀/선수
    teams ||--o{ team_players : "로스터"
    players ||--o{ team_players : ""

    %% 경기
    games ||--o{ game_teams : "양 팀"
    teams ||--o{ game_teams : ""
    game_teams ||--o{ lineup_players : "라인업"
    players ||--o{ lineup_players : ""
    lineup_players |o--o| lineup_players : "교체 (replaced_player_id)"

    %% 타임라인
    games ||--o{ timelines : "기록"
    game_teams |o--o{ timelines : "game_team1/2 (FK 2개)"
    lineup_players |o--o{ timelines : "득점·어시스트·교체 (FK 4개)"

    %% 응원톡
    game_teams |o--o{ cheer_talks : "응원톡 (SET NULL)"
    cheer_talks ||--o{ reports : "신고"
    cheer_talks |o--o{ cheer_talk_bot_filter_history : "AI 필터 이력 (SET NULL)"
```

## 설계 노트

### FK 삭제 정책 (의도적 결정 — 변경 전 확인 필요)

| 관계 | 정책 | 배경 |
|------|------|------|
| `cheer_talks.game_team_id` | `ON DELETE SET NULL` | 게임팀이 삭제돼도 응원톡은 보존 (V5에서 변경) |
| `cheer_talk_bot_filter_history.cheer_talk_id` | `ON DELETE SET NULL` | 응원톡 삭제 후에도 필터링 이력 보존 |
| `timelines.assist_lineup_player_id` | `ON DELETE CASCADE` | 라인업 삭제 시 어시스트 기록도 삭제 (V7) — 다른 lineup FK들과 정책이 다름에 유의 |
| 나머지 전부 | `RESTRICT` (기본) | 부모 삭제 불가 |

### 독립 테이블

- **`pending_cheer_talks`** — FK 없음. WebSocket 전송 대기 응원톡을 JSON으로 보관하는 아웃박스성 테이블.

### 조직 계층 변천 (참고)

- V10: `teams.organization_id` 직접 FK 추가 → V14: `units` 엔티티 도입 → **V18: `teams.organization_id` 제거**.
  현재 팀의 소속 조직은 `teams.unit_id → units.organization_id` 경로로만 조회 가능.

### 유니크 제약

| 테이블 | 제약 |
|--------|------|
| `players` | `(organization_id, student_number)` — organization_id가 NULL인 레거시 행은 중복 허용 (V17) |
| `team_players` | `(team_id, player_id)` |
| `league_teams` | `(league_id, team_id)` |
| `game_teams` | `(game_id, team_id)` |
| `lineup_players` | `(game_team_id, player_id)` |
