package com.centerm.comm.persistence.dao;

import java.util.List;

import com.centerm.comm.persistence.entity.TransRecord;

/**
 * 访问交易记录 的DAO层 作者：cxy 时间：2013.03.04
 */
public interface TransRecordDao {
	/**
	 * 获取交易记录
	 */
	public List<TransRecord> getEntities();

	/**
	 * 获取第几条数据
	 * 
	 * @param pageNum
	 * @return
	 */
	public TransRecord getTransRecordByPage(int pageNum);

	/**
	 * 获取最后一笔交易记录
	 * 
	 * @return
	 */
	public TransRecord getLastTransRecord();

	/**
	 * 根据批次号、票据号获取唯一一笔交易记录
	 * 
	 * @param batchNo
	 *            批次号
	 * @param billno
	 *            流水号
	 * @return
	 */
	public TransRecord getTransRecordByCondition(String batchno, String billno);

	public TransRecord getConsumeByCondition(String batchno, String billno);

	public TransRecord getTransRevokeByCondition(String batchno, String billno);

	/**
	 * 根据处理代码、POS流水号、终端号、商户号
	 * 
	 * @param transprocode
	 *            处理码
	 * @param systraceno
	 *            流水号
	 * @return
	 */
	public TransRecord getTransRecordByCondition(String transprocode, String systraceno, String terminalid,
			String acceptoridcode);

	/**
	 * 根据状态码查找记录
	 * 
	 * @param code
	 * @return
	 */
	public List<TransRecord> getTransRecordsByStatuscode(String code);

	/**
	 * 保存交易记录
	 */
	public int save(TransRecord record);

	/**
	 * 更新交易记录
	 */
	public int update(TransRecord record);

	public int update(int id, String key, String value);
	// public int updatePart(String jsonStr,String batchNo,String sysTraNo);

	/**
	 * 删除交易记录
	 */
	public int delete(TransRecord record);

	public int deleteAll();

	public int getTransCount();

	public int getConsumeCount();

	public int getRevokeCount();

	public int getOfflineSaleCount();

	// 获取结算
	public TransRecord getSettle();

	public List<TransRecord> getCountTransRecord(String transprocode, String conditionmode, String reserve1);
}
