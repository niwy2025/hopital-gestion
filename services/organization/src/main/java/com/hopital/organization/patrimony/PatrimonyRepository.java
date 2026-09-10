package com.hopital.organization.patrimony;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

@Repository
public class PatrimonyRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public PatrimonyRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<Map<String,Object>> list(String sql, MapSqlParameterSource p) { return jdbc.query(sql, p, this::row); }
    public Map<String,Object> one(String sql, MapSqlParameterSource p) {
        var rows = list(sql, p);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Élément introuvable.");
        return rows.get(0);
    }
    public int update(String sql, MapSqlParameterSource p) { return jdbc.update(sql, p); }
    public long count(String sql, MapSqlParameterSource p) { return jdbc.queryForObject(sql, p, Long.class); }
    public static MapSqlParameterSource params(Object... values) {
        var p = new MapSqlParameterSource();
        for (int i=0;i<values.length;i+=2) p.addValue(values[i].toString(),values[i+1]);
        return p;
    }
    public static UUID id(Map<String,Object> row, String field) { return row.get(field) == null ? null : UUID.fromString(row.get(field).toString()); }
    private Map<String,Object> row(ResultSet rs, int index) throws SQLException {
        var result = new LinkedHashMap<String,Object>();
        var metadata = rs.getMetaData();
        for (int i=1;i<=metadata.getColumnCount();i++) {
            String key = metadata.getColumnLabel(i);
            var parts = key.split("_"); var name = new StringBuilder(parts[0]);
            for (int j=1;j<parts.length;j++) name.append(Character.toUpperCase(parts[j].charAt(0))).append(parts[j].substring(1));
            Object value = rs.getObject(i);
            if (value instanceof java.sql.Timestamp time) value = time.toInstant().toString();
            else if (value instanceof java.sql.Date date) value = date.toLocalDate().toString();
            else if (value instanceof UUID uuid) value = uuid.toString();
            result.put(name.toString(), value);
        }
        return result;
    }
}
