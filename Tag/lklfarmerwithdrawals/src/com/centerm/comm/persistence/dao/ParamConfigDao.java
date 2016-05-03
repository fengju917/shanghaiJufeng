package com.centerm.comm.persistence.dao;

import java.util.List;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;

import com.centerm.comm.persistence.entity.ParamConfig;

public interface ParamConfigDao {

	/**
	 * 获取参数配置信息
	 */
	public List<ParamConfig> getEntities();

	public Map<String, String> get();

	public String get(String tagname);

	/**
	 * 保存参数配置信息
	 */
	public int save(ParamConfig config);

	public int save(List<ParamConfig> params);

	public int save(Map<String, String> map);

	public int save(String tagname, String tagval);

	/**
	 * 更新参数配置信息
	 */
	public int update(ParamConfig param);

	public int update(Map<String, String> map);

	public int update(String tagname, String tagval);

	/**
	 * 删除参数配置信息
	 */
	public int delete(ParamConfig param);

	public int delete(String tagname);

	/**
	 * 判断是否存在
	 */
	public boolean isExist(String tagname);

	public boolean isExist(SQLiteDatabase db, String tagname);

	public int dropTable();

	int syncXmlUpdate(List<ParamConfig> params);
}
