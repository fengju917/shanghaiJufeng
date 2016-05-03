package com.centerm.comm.persistence.dao;

import java.util.List;

import com.centerm.comm.persistence.entity.ScriptNotity;

public interface ScriptNotityDao {
	/**
	 * 获取交易记录
	 */
	public List<ScriptNotity> getEntities();

	/**
	 * 获取一条脚本记录
	 * 
	 * @return
	 */
	public ScriptNotity getScriptNotity();

	/**
	 * 根据批次号、流水号获取唯一一笔交易记录
	 * 
	 * @param batchno
	 *            批次号
	 * @param systraceno
	 *            流水号
	 * @return
	 */
	public ScriptNotity getScriptNotityByCondition(String batchno, String systraceno);

	/**
	 * 根据处理代码、POS流水号、终端号、商户号
	 * 
	 * @param transprocode
	 *            处理码
	 * @param systraceno
	 *            流水号
	 * @return
	 */
	public ScriptNotity getScriptNotityByCondition(String transprocode, String systraceno, String terminalid,
			String acceptoridcode);

	/**
	 * 保存交易记录
	 */
	public int save(ScriptNotity record);

	/**
	 * 更新交易记录
	 */
	public int update(ScriptNotity record);

	public int update(int id, String key, String value);

	/**
	 * 删除交易记录
	 */
	public int delete(ScriptNotity record);

	public int deleteAll();

	public int getScriptNotityCount();

}
