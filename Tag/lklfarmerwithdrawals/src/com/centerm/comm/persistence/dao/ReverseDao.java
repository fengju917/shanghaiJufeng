package com.centerm.comm.persistence.dao;

import java.util.List;

import com.centerm.comm.persistence.entity.Reverse;

public interface ReverseDao {
	/**
	 * 获取交易记录
	 */
	public List<Reverse> getEntities();

	/**
	 * 获取一条冲正记录
	 * 
	 * @return
	 */
	public Reverse getReverse();

	/**
	 * 根据批次号、流水号获取唯一一笔交易记录
	 * 
	 * @param batchno
	 *            批次号
	 * @param systraceno
	 *            流水号
	 * @return
	 */
	public Reverse getReverseByCondition(String batchno, String systraceno);

	/**
	 * 根据处理代码、POS流水号、终端号、商户号
	 * 
	 * @param transprocode
	 *            处理码
	 * @param systraceno
	 *            流水号
	 * @return
	 */
	public Reverse getReverseByCondition(String transprocode, String systraceno, String terminalid,
			String acceptoridcode);

	/**
	 * 保存交易记录
	 */
	public int save(Reverse record);

	/**
	 * 更新交易记录
	 */
	public int update(Reverse record);

	public int update(int id, String key, String value);

	/**
	 * 删除交易记录
	 */
	public int delete(Reverse record);

	public int deleteAll();

	public int getReverseCount();

}
