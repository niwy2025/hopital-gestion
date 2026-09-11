package com.hopital.accounting.billing;

import java.util.*;
import java.sql.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Repository
public class BillingStore {
    private final NamedParameterJdbcTemplate jdbc;
    public BillingStore(NamedParameterJdbcTemplate jdbc){this.jdbc=jdbc;}
    public static MapSqlParameterSource p(Object...args){var p=new MapSqlParameterSource();for(int i=0;i<args.length;i+=2)p.addValue(args[i].toString(),args[i+1]);return p;}
    public List<Map<String,Object>> rows(String sql,MapSqlParameterSource p){return jdbc.query(sql,p,this::map);}
    public Map<String,Object> one(String sql,MapSqlParameterSource p){var rows=rows(sql,p);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Élément de facturation introuvable.");return rows.get(0);}
    public long count(String sql,MapSqlParameterSource p){return jdbc.queryForObject(sql,p,Long.class);}
    public int update(String sql,MapSqlParameterSource p){return jdbc.update(sql,p);}
    public static UUID id(Map<String,Object> r,String field){return r.get(field)==null?null:UUID.fromString(r.get(field).toString());}
    private Map<String,Object> map(ResultSet rs,int n)throws SQLException{
        var row=new LinkedHashMap<String,Object>();var meta=rs.getMetaData();
        for(int i=1;i<=meta.getColumnCount();i++){
            var parts=meta.getColumnLabel(i).split("_");var key=new StringBuilder(parts[0]);
            for(int j=1;j<parts.length;j++)key.append(Character.toUpperCase(parts[j].charAt(0))).append(parts[j].substring(1));
            Object value=rs.getObject(i);
            if(value instanceof Timestamp t)value=t.toInstant().toString();
            else if(value instanceof java.sql.Date d)value=d.toLocalDate().toString();
            else if(value instanceof UUID u)value=u.toString();
            row.put(key.toString(),value);
        }return row;
    }
}
