ALTER TABLE games ADD INDEX idx_games_state_league (state, league_id);
ALTER TABLE cheer_talks ADD INDEX idx_cheer_talks_gameteam_status_created (game_team_id, block_status, created_at DESC, id DESC);
ALTER TABLE leagues ADD INDEX idx_leagues_deleted_org_sport_start_end (is_deleted, organization_id, sport_type, start_at, end_at);
ALTER TABLE timelines ADD INDEX idx_timelines_game_type (game_id, type);
