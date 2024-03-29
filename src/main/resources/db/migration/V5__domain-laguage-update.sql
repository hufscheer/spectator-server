-- comment -> cheer_talk
ALTER TABLE comments RENAME TO cheer_talks;
ALTER TABLE reports CHANGE comment_id cheer_talk_id BIGINT;

-- administrator -> manager
ALTER TABLE games CHANGE administrator_id manager_id BIGINT;
ALTER TABLE teams CHANGE administrator_id manager_id BIGINT;
ALTER TABLE leagues CHANGE administrator_id manager_id BIGINT;
ALTER TABLE members CHANGE is_administrator is_manager BIT(1);

-- lineup_players
ALTER TABLE game_team_players RENAME TO lineup_players;
ALTER TABLE records CHANGE game_team_player_id lineup_player_id BIGINT;

-- team -> leagueTeam
ALTER TABLE teams RENAME TO league_teams;
ALTER TABLE game_teams CHANGE team_id league_team_id BIGINT;

-- team_player -> league_team_player
ALTER TABLE team_players RENAME TO league_team_players;
ALTER TABLE league_team_players CHANGE team_id league_team_id BIGINT;
