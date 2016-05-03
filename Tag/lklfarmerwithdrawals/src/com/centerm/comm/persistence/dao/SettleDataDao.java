package com.centerm.comm.persistence.dao;

import java.util.List;

import com.centerm.comm.persistence.entity.SettleData;
import com.centerm.comm.persistence.entity.TransRecord;

/**
 * @author zhouhui @da2013-7-6
 *
 */
public interface SettleDataDao {

	/**
	 * 获取结算信息
	 * 
	 * @return
	 */
	public List<SettleData> getSettleData();

	/**
	 * 获取结算交易明细
	 * 
	 * @return
	 */
	public List<SettleData> getTXNData();

	/**
	 * 获取上送不成功交易
	 * 
	 * @return
	 */
	public List<SettleData> getUnSuccesssFulData();

	/**
	 * 获取被拒绝的交易
	 * 
	 * @return
	 */
	public List<SettleData> getDeniedData();

	/**
	 * 保存结算信息
	 * 
	 * @param settledata
	 * @return
	 */
	public int save(SettleData settledata);

	/**
	 * 由transRecord表转存到SettleData表
	 * 
	 * @return
	 */
	public int fromTranToSettle();

	/**
	 * 清除结算信息表
	 * 
	 * @return
	 */
	public int delete();

	/**
	 * 删除非本批次的结算信息
	 * 
	 * @param batchno
	 * @return
	 */
	public int deleteOtherBatch(String batchno);

	/**
	 * 删除单条记录
	 * 
	 * @param settleData
	 * @return
	 */
	public int delete(SettleData settleData);

	/**
	 * 根据交易批次凭证号，更新对应记录的制定字段的值
	 * 
	 * @param batchbillno
	 * @param key
	 * @param value
	 * @return
	 */
	public int update(String batchbillno, String key, String value);

}
