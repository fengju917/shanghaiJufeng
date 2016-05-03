package com.centerm.comm.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.entity.TransRecord;
import com.centerm.comm.persistence.helper.DBOpenHelper;
import com.centerm.mid.util.M3Utility;

/**
 * 访问交易记录 的DAO实现层 作者：cxy 时间：2013.03.04
 */
public class TransRecordDaoImpl implements TransRecordDao {

	private final Logger logger = Logger.getLogger(TransRecordDaoImpl.class);

	private Context context;
	private DBOpenHelper openHelper;

	public TransRecordDaoImpl() {
		openHelper = DBOpenHelper.getInstance(this.context);
	}

	public TransRecordDaoImpl(Context context) {
		this.context = context;
		openHelper = DBOpenHelper.getInstance(this.context);
	}

	@Override
	public synchronized TransRecord getTransRecordByPage(int pageNum) {
		TransRecord record = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();

		Cursor cursor = null;
		try {
			cursor = db.rawQuery(
					"select * from transrecord where transprocode <> '900000' and (statuscode <> 'AC' or statuscode is null) order by translocaldate desc,translocaltime desc limit 1 offset "
							+ pageNum,
					new String[] {}); // 按时间倒序排序
			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				record = new TransRecord();
				record.setId(cursor.getInt(0));
				record.setPriaccount(cursor.getString(1));
				record.setTransprocode(cursor.getString(2));
				record.setTransamount(cursor.getString(3));
				record.setSystraceno(cursor.getString(4));
				record.setTranslocaltime(cursor.getString(5));
				record.setTranslocaldate(cursor.getString(6));
				record.setExpireddate(cursor.getString(7));
				record.setEntrymode(cursor.getString(8));
				record.setSeqnumber(cursor.getString(9));
				record.setConditionmode(cursor.getString(10));
				record.setUpdatecode(cursor.getString(11));
				record.setTrack2data(cursor.getString(12));
				record.setTrack3data(cursor.getString(13));
				record.setRefernumber(cursor.getString(14));
				record.setIdrespcode(cursor.getString(15));
				record.setRespcode(cursor.getString(16));
				record.setTerminalid(cursor.getString(17));
				record.setAcceptoridcode(cursor.getString(18));
				record.setAcceptoridname(cursor.getString(19));
				record.setAddrespkey(cursor.getString(20));
				record.setAdddataword(cursor.getString(21));
				record.setTranscurrcode(cursor.getString(22));
				record.setPindata(cursor.getString(23));
				record.setSecctrlinfo(cursor.getString(24));
				record.setBalanceamount(cursor.getString(25));
				record.setIcdata(cursor.getString(26));
				record.setAdddatapri(cursor.getString(27));
				record.setPbocdata(cursor.getString(28));
				record.setLoadparams(cursor.getString(29));
				record.setCardholderid(cursor.getString(30));
				record.setBatchbillno(cursor.getString(31));
				record.setSettledata(cursor.getString(32));
				record.setMesauthcode(cursor.getString(33));
				record.setStatuscode(cursor.getString(34));
				record.setReversetimes(cursor.getString(35));
				record.setReserve1(cursor.getString(36));
				record.setReserve2(cursor.getString(37));
				record.setReserve3(cursor.getString(38));
				record.setReserve4(cursor.getString(39));
				record.setReserve5(cursor.getString(40));
			}
		} catch (Exception e) {
			logger.error("获取最后一笔交易记录发生异常", e);
			e.printStackTrace();
		}
		cursor.close();
		db.close();
		return record;
	}

	public synchronized List<TransRecord> getEntities() {
		List<TransRecord> list = new ArrayList();
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from transrecord where transprocode <> '900000' and (statuscode <> 'AC' or statuscode is null) order by translocaldate desc,translocaltime desc ",
				new String[] {}); // 按时间倒序排序
		if (cursor != null && cursor.getCount() > 0) {
			list = new ArrayList<TransRecord>();
			while (cursor.moveToNext()) {
				TransRecord record = new TransRecord();
				record.setId(cursor.getInt(0));
				record.setPriaccount(cursor.getString(1));
				record.setTransprocode(cursor.getString(2));
				record.setTransamount(cursor.getString(3));
				record.setSystraceno(cursor.getString(4));
				record.setTranslocaltime(cursor.getString(5));
				record.setTranslocaldate(cursor.getString(6));
				record.setExpireddate(cursor.getString(7));
				record.setEntrymode(cursor.getString(8));
				record.setSeqnumber(cursor.getString(9));
				record.setConditionmode(cursor.getString(10));
				record.setUpdatecode(cursor.getString(11));
				record.setTrack2data(cursor.getString(12));
				record.setTrack3data(cursor.getString(13));
				record.setRefernumber(cursor.getString(14));
				record.setIdrespcode(cursor.getString(15));
				record.setRespcode(cursor.getString(16));
				record.setTerminalid(cursor.getString(17));
				record.setAcceptoridcode(cursor.getString(18));
				record.setAcceptoridname(cursor.getString(19));
				record.setAddrespkey(cursor.getString(20));
				record.setAdddataword(cursor.getString(21));
				record.setTranscurrcode(cursor.getString(22));
				record.setPindata(cursor.getString(23));
				record.setSecctrlinfo(cursor.getString(24));
				record.setBalanceamount(cursor.getString(25));
				record.setIcdata(cursor.getString(26));
				record.setAdddatapri(cursor.getString(27));
				record.setPbocdata(cursor.getString(28));
				record.setLoadparams(cursor.getString(29));
				record.setCardholderid(cursor.getString(30));
				record.setBatchbillno(cursor.getString(31));
				record.setSettledata(cursor.getString(32));
				record.setMesauthcode(cursor.getString(33));
				record.setStatuscode(cursor.getString(34));
				record.setReversetimes(cursor.getString(35));
				record.setReserve1(cursor.getString(36));
				record.setReserve2(cursor.getString(37));
				record.setReserve3(cursor.getString(38));
				record.setReserve4(cursor.getString(39));
				record.setReserve5(cursor.getString(40));
				list.add(record);
			}
		}
		cursor.close();
		db.close();
		return list;
	}

	// 获取最后一笔交易记录
	public synchronized TransRecord getLastTransRecord() {
		TransRecord record = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();

		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from transrecord where transprocode <> '900000' "
					+ "and (statuscode <> 'AC' or statuscode is null) "
					+ "order by translocaldate desc,translocaltime desc ", new String[] {});
			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				record = new TransRecord();
				record.setId(cursor.getInt(0));
				record.setPriaccount(cursor.getString(1));
				record.setTransprocode(cursor.getString(2));
				record.setTransamount(cursor.getString(3));
				record.setSystraceno(cursor.getString(4));
				record.setTranslocaltime(cursor.getString(5));
				record.setTranslocaldate(cursor.getString(6));
				record.setExpireddate(cursor.getString(7));
				record.setEntrymode(cursor.getString(8));
				record.setSeqnumber(cursor.getString(9));
				record.setConditionmode(cursor.getString(10));
				record.setUpdatecode(cursor.getString(11));
				record.setTrack2data(cursor.getString(12));
				record.setTrack3data(cursor.getString(13));
				record.setRefernumber(cursor.getString(14));
				record.setIdrespcode(cursor.getString(15));
				record.setRespcode(cursor.getString(16));
				record.setTerminalid(cursor.getString(17));
				record.setAcceptoridcode(cursor.getString(18));
				record.setAcceptoridname(cursor.getString(19));
				record.setAddrespkey(cursor.getString(20));
				record.setAdddataword(cursor.getString(21));
				record.setTranscurrcode(cursor.getString(22));
				record.setPindata(cursor.getString(23));
				record.setSecctrlinfo(cursor.getString(24));
				record.setBalanceamount(cursor.getString(25));
				record.setIcdata(cursor.getString(26));
				record.setAdddatapri(cursor.getString(27));
				record.setPbocdata(cursor.getString(28));
				record.setLoadparams(cursor.getString(29));
				record.setCardholderid(cursor.getString(30));
				record.setBatchbillno(cursor.getString(31));
				record.setSettledata(cursor.getString(32));
				record.setMesauthcode(cursor.getString(33));
				record.setStatuscode(cursor.getString(34));
				record.setReversetimes(cursor.getString(35));
				record.setReserve1(cursor.getString(36));
				record.setReserve2(cursor.getString(37));
				record.setReserve3(cursor.getString(38));
				record.setReserve4(cursor.getString(39));
				record.setReserve5(cursor.getString(40));
			}
		} catch (Exception e) {
			logger.error("获取最后一笔交易记录发生异常", e);
			e.printStackTrace();
		}
		cursor.close();
		db.close();
		return record;
	}

	/**
	 * 按交易类型查询
	 * 
	 * @return add by chenkehui
	 */
	public synchronized List<TransRecord> getCountTransRecord(String transprocode, String conditionmode,
			String reserve1) {
		List<TransRecord> list = null;
		TransRecord record = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(
					"select * from transrecord where transprocode = ? and conditionmode = ? and reserve1 = ?"
							+ "and (statuscode <> 'AC' or statuscode is null) order by translocaldate desc,translocaltime desc ",
					new String[] { transprocode, conditionmode, reserve1 });
			if (cursor != null && cursor.getCount() > 0) {
				list = new ArrayList<TransRecord>();
				while (cursor.moveToNext()) {
					record = new TransRecord();
					record.setId(cursor.getInt(0));
					record.setPriaccount(cursor.getString(1));
					record.setTransprocode(cursor.getString(2));
					record.setTransamount(cursor.getString(3));
					record.setSystraceno(cursor.getString(4));
					record.setTranslocaltime(cursor.getString(5));
					record.setTranslocaldate(cursor.getString(6));
					record.setExpireddate(cursor.getString(7));
					record.setEntrymode(cursor.getString(8));
					record.setSeqnumber(cursor.getString(9));
					record.setConditionmode(cursor.getString(10));
					record.setUpdatecode(cursor.getString(11));
					record.setTrack2data(cursor.getString(12));
					record.setTrack3data(cursor.getString(13));
					record.setRefernumber(cursor.getString(14));
					record.setIdrespcode(cursor.getString(15));
					record.setRespcode(cursor.getString(16));
					record.setTerminalid(cursor.getString(17));
					record.setAcceptoridcode(cursor.getString(18));
					record.setAcceptoridname(cursor.getString(19));
					record.setAddrespkey(cursor.getString(20));
					record.setAdddataword(cursor.getString(21));
					record.setTranscurrcode(cursor.getString(22));
					record.setPindata(cursor.getString(23));
					record.setSecctrlinfo(cursor.getString(24));
					record.setBalanceamount(cursor.getString(25));
					record.setIcdata(cursor.getString(26));
					record.setAdddatapri(cursor.getString(27));
					record.setPbocdata(cursor.getString(28));
					record.setLoadparams(cursor.getString(29));
					record.setCardholderid(cursor.getString(30));
					record.setBatchbillno(cursor.getString(31));
					record.setSettledata(cursor.getString(32));
					record.setMesauthcode(cursor.getString(33));
					record.setStatuscode(cursor.getString(34));
					record.setReversetimes(cursor.getString(35));
					record.setReserve1(cursor.getString(36));
					record.setReserve2(cursor.getString(37));
					record.setReserve3(cursor.getString(38));
					record.setReserve4(cursor.getString(39));
					record.setReserve5(cursor.getString(40));
					list.add(record);
				}
			}
		} catch (Exception e) {
			logger.error("获取" + transprocode + "交易类型发生异常", e);
			e.printStackTrace();
		}
		cursor.close();
		db.close();
		return list;
	}

	/**
	 * 根据批次号和票据号获取单个消费
	 */
	public synchronized TransRecord getTransRecordByCondition(String batchno, String billno) {
		logger.info("<按票据凭证号查询>batchno=[" + batchno + "],billno=[" + billno + "]");
		TransRecord record = null;
		if (batchno == null || billno == null) {
			return record;
		}
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from transrecord where batchbillno like ?",
					new String[] { batchno + billno + "%" });

			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				record = new TransRecord();
				record.setId(cursor.getInt(0));
				record.setPriaccount(cursor.getString(1));
				record.setTransprocode(cursor.getString(2));
				record.setTransamount(cursor.getString(3));
				record.setSystraceno(cursor.getString(4));
				record.setTranslocaltime(cursor.getString(5));
				record.setTranslocaldate(cursor.getString(6));
				record.setExpireddate(cursor.getString(7));
				record.setEntrymode(cursor.getString(8));
				record.setSeqnumber(cursor.getString(9));
				record.setConditionmode(cursor.getString(10));
				record.setUpdatecode(cursor.getString(11));
				record.setTrack2data(cursor.getString(12));
				record.setTrack3data(cursor.getString(13));
				record.setRefernumber(cursor.getString(14));
				record.setIdrespcode(cursor.getString(15));
				record.setRespcode(cursor.getString(16));
				record.setTerminalid(cursor.getString(17));
				record.setAcceptoridcode(cursor.getString(18));
				record.setAcceptoridname(cursor.getString(19));
				record.setAddrespkey(cursor.getString(20));
				record.setAdddataword(cursor.getString(21));
				record.setTranscurrcode(cursor.getString(22));
				record.setPindata(cursor.getString(23));
				record.setSecctrlinfo(cursor.getString(24));
				record.setBalanceamount(cursor.getString(25));
				record.setIcdata(cursor.getString(26));
				record.setAdddatapri(cursor.getString(27));
				record.setPbocdata(cursor.getString(28));
				record.setLoadparams(cursor.getString(29));
				record.setCardholderid(cursor.getString(30));
				record.setBatchbillno(cursor.getString(31));
				record.setSettledata(cursor.getString(32));
				record.setMesauthcode(cursor.getString(33));
				record.setStatuscode(cursor.getString(34));
				record.setReversetimes(cursor.getString(35));
				record.setReserve1(cursor.getString(36));
				record.setReserve2(cursor.getString(37));
				record.setReserve3(cursor.getString(38));
				record.setReserve4(cursor.getString(39));
				record.setReserve5(cursor.getString(40));
			}
		} catch (Exception e) {
			logger.error("根据批次号和流水号获取单个交易记录对象发生异常", e);
			e.printStackTrace();
		}
		cursor.close();
		db.close();
		return record;
	}

	/**
	 * 根据批次号和票据号获取单个消费、预授权或预授权完成撤销
	 */
	public synchronized TransRecord getConsumeByCondition(String batchno, String billno) {
		Log.i("ckh", "<按票据凭证号查询>batchno=[" + batchno + "],billno=[" + billno + "]");
		TransRecord record = null;
		if (batchno == null || billno == null) {
			return record;
		}
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			// cursor = db.rawQuery("select * from transrecord where
			// transprocode='000000' and conditionmode='00' and batchbillno like
			// ?",
			// new String[]{batchno+billno });
			cursor = db.rawQuery(
					"select * from transrecord where (transprocode='000000' or transprocode='030000' or transprocode='200000' or "
							+ "transprocode='630000') and (statuscode <> 'AC' or statuscode is null) and batchbillno like ?",
					new String[] { batchno + billno + "%" });
			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				record = new TransRecord();
				record.setId(cursor.getInt(0));
				record.setPriaccount(cursor.getString(1));
				record.setTransprocode(cursor.getString(2));
				record.setTransamount(cursor.getString(3));
				record.setSystraceno(cursor.getString(4));
				record.setTranslocaltime(cursor.getString(5));
				record.setTranslocaldate(cursor.getString(6));
				record.setExpireddate(cursor.getString(7));
				record.setEntrymode(cursor.getString(8));
				record.setSeqnumber(cursor.getString(9));
				record.setConditionmode(cursor.getString(10));
				record.setUpdatecode(cursor.getString(11));
				record.setTrack2data(cursor.getString(12));
				record.setTrack3data(cursor.getString(13));
				record.setRefernumber(cursor.getString(14));
				record.setIdrespcode(cursor.getString(15));
				record.setRespcode(cursor.getString(16));
				record.setTerminalid(cursor.getString(17));
				record.setAcceptoridcode(cursor.getString(18));
				record.setAcceptoridname(cursor.getString(19));
				record.setAddrespkey(cursor.getString(20));
				record.setAdddataword(cursor.getString(21));
				record.setTranscurrcode(cursor.getString(22));
				record.setPindata(cursor.getString(23));
				record.setSecctrlinfo(cursor.getString(24));
				record.setBalanceamount(cursor.getString(25));
				record.setIcdata(cursor.getString(26));
				record.setAdddatapri(cursor.getString(27));
				record.setPbocdata(cursor.getString(28));
				record.setLoadparams(cursor.getString(29));
				record.setCardholderid(cursor.getString(30));
				record.setBatchbillno(cursor.getString(31));
				record.setSettledata(cursor.getString(32));
				record.setMesauthcode(cursor.getString(33));
				record.setStatuscode(cursor.getString(34));
				record.setReversetimes(cursor.getString(35));
				record.setReserve1(cursor.getString(36));
				record.setReserve2(cursor.getString(37));
				record.setReserve3(cursor.getString(38));
				record.setReserve4(cursor.getString(39));
				record.setReserve5(cursor.getString(40));
			}
		} catch (Exception e) {
			logger.error("根据批次号和流水号获取单个交易记录对象发生异常", e);
			e.printStackTrace();
		}
		cursor.close();
		db.close();
		return record;
	}

	/**
	 * 根据批次号和票据号获取消费撤销记录
	 */
	public synchronized TransRecord getTransRevokeByCondition(String batchno, String billno) {
		logger.info("<按票据凭证号查询>batchno=[" + batchno + "],billno=[" + billno + "]");
		TransRecord record = null;
		if (batchno == null || billno == null) {
			return record;
		}
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from transrecord where batchbillno like ?",
					new String[] { batchno + "______" + billno });

			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				record = new TransRecord();
				record.setId(cursor.getInt(0));
				record.setPriaccount(cursor.getString(1));
				record.setTransprocode(cursor.getString(2));
				record.setTransamount(cursor.getString(3));
				record.setSystraceno(cursor.getString(4));
				record.setTranslocaltime(cursor.getString(5));
				record.setTranslocaldate(cursor.getString(6));
				record.setExpireddate(cursor.getString(7));
				record.setEntrymode(cursor.getString(8));
				record.setSeqnumber(cursor.getString(9));
				record.setConditionmode(cursor.getString(10));
				record.setUpdatecode(cursor.getString(11));
				record.setTrack2data(cursor.getString(12));
				record.setTrack3data(cursor.getString(13));
				record.setRefernumber(cursor.getString(14));
				record.setIdrespcode(cursor.getString(15));
				record.setRespcode(cursor.getString(16));
				record.setTerminalid(cursor.getString(17));
				record.setAcceptoridcode(cursor.getString(18));
				record.setAcceptoridname(cursor.getString(19));
				record.setAddrespkey(cursor.getString(20));
				record.setAdddataword(cursor.getString(21));
				record.setTranscurrcode(cursor.getString(22));
				record.setPindata(cursor.getString(23));
				record.setSecctrlinfo(cursor.getString(24));
				record.setBalanceamount(cursor.getString(25));
				record.setIcdata(cursor.getString(26));
				record.setAdddatapri(cursor.getString(27));
				record.setPbocdata(cursor.getString(28));
				record.setLoadparams(cursor.getString(29));
				record.setCardholderid(cursor.getString(30));
				record.setBatchbillno(cursor.getString(31));
				record.setSettledata(cursor.getString(32));
				record.setMesauthcode(cursor.getString(33));
				record.setStatuscode(cursor.getString(34));
				record.setReversetimes(cursor.getString(35));
				record.setReserve1(cursor.getString(36));
				record.setReserve2(cursor.getString(37));
				record.setReserve3(cursor.getString(38));
				record.setReserve4(cursor.getString(39));
				record.setReserve5(cursor.getString(40));
			}
		} catch (Exception e) {
			logger.error("根据批次号和流水号获取单个交易记录对象发生异常", e);
			e.printStackTrace();
		}
		cursor.close();
		db.close();
		return record;
	}

	@Override
	public synchronized TransRecord getTransRecordByCondition(String transprocode, String systraceno, String terminalid,
			String acceptoridcode) {
		TransRecord record = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(
					"select * from transrecord where transprocode = ? and systraceno = ? and terminalid = ? and acceptoridcode = ?",
					new String[] { transprocode, systraceno, terminalid, acceptoridcode });

			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				record = new TransRecord();
				record.setId(cursor.getInt(0));
				record.setPriaccount(cursor.getString(1));
				record.setTransprocode(cursor.getString(2));
				record.setTransamount(cursor.getString(3));
				record.setSystraceno(cursor.getString(4));
				record.setTranslocaltime(cursor.getString(5));
				record.setTranslocaldate(cursor.getString(6));
				record.setExpireddate(cursor.getString(7));
				record.setEntrymode(cursor.getString(8));
				record.setSeqnumber(cursor.getString(9));
				record.setConditionmode(cursor.getString(10));
				record.setUpdatecode(cursor.getString(11));
				record.setTrack2data(cursor.getString(12));
				record.setTrack3data(cursor.getString(13));
				record.setRefernumber(cursor.getString(14));
				record.setIdrespcode(cursor.getString(15));
				record.setRespcode(cursor.getString(16));
				record.setTerminalid(cursor.getString(17));
				record.setAcceptoridcode(cursor.getString(18));
				record.setAcceptoridname(cursor.getString(19));
				record.setAddrespkey(cursor.getString(20));
				record.setAdddataword(cursor.getString(21));
				record.setTranscurrcode(cursor.getString(22));
				record.setPindata(cursor.getString(23));
				record.setSecctrlinfo(cursor.getString(24));
				record.setBalanceamount(cursor.getString(25));
				record.setIcdata(cursor.getString(26));
				record.setAdddatapri(cursor.getString(27));
				record.setPbocdata(cursor.getString(28));
				record.setLoadparams(cursor.getString(29));
				record.setCardholderid(cursor.getString(30));
				record.setBatchbillno(cursor.getString(31));
				record.setSettledata(cursor.getString(32));
				record.setMesauthcode(cursor.getString(33));
				record.setStatuscode(cursor.getString(34));
				record.setReversetimes(cursor.getString(35));
				record.setReserve1(cursor.getString(36));
				record.setReserve2(cursor.getString(37));
				record.setReserve3(cursor.getString(38));
				record.setReserve4(cursor.getString(39));
				record.setReserve5(cursor.getString(40));
			}
		} catch (Exception e) {
			logger.error("根据处理代码、POS流水号、终端号、商户号查询交易记录发生异常", e);
			e.printStackTrace();
		}
		cursor.close();
		db.close();
		return record;
	}

	// 通过“状态码”查找
	public synchronized List<TransRecord> getTransRecordsByStatuscode(String code) {
		List<TransRecord> list = new ArrayList();
		SQLiteDatabase db = openHelper.getReadableDatabase();
		String status1 = code;
		String status2 = code;
		if (code.equals("AACorARPC")) { // AAC\ARPC交易信息上送
			status1 = "AC";
			status2 = "AR";
		}
		Cursor cursor = db.rawQuery(
				"select * from transrecord where statuscode = ? or statuscode = ? order by translocaldate asc,translocaltime asc ",
				new String[] { status1, status2 }); // 按时间正序排序
		if (cursor != null && cursor.getCount() > 0) {
			list = new ArrayList<TransRecord>();
			while (cursor.moveToNext()) {
				TransRecord record = new TransRecord();
				record.setId(cursor.getInt(0));
				record.setPriaccount(cursor.getString(1));
				record.setTransprocode(cursor.getString(2));
				record.setTransamount(cursor.getString(3));
				record.setSystraceno(cursor.getString(4));
				record.setTranslocaltime(cursor.getString(5));
				record.setTranslocaldate(cursor.getString(6));
				record.setExpireddate(cursor.getString(7));
				record.setEntrymode(cursor.getString(8));
				record.setSeqnumber(cursor.getString(9));
				record.setConditionmode(cursor.getString(10));
				record.setUpdatecode(cursor.getString(11));
				record.setTrack2data(cursor.getString(12));
				record.setTrack3data(cursor.getString(13));
				record.setRefernumber(cursor.getString(14));
				record.setIdrespcode(cursor.getString(15));
				record.setRespcode(cursor.getString(16));
				record.setTerminalid(cursor.getString(17));
				record.setAcceptoridcode(cursor.getString(18));
				record.setAcceptoridname(cursor.getString(19));
				record.setAddrespkey(cursor.getString(20));
				record.setAdddataword(cursor.getString(21));
				record.setTranscurrcode(cursor.getString(22));
				record.setPindata(cursor.getString(23));
				record.setSecctrlinfo(cursor.getString(24));
				record.setBalanceamount(cursor.getString(25));
				record.setIcdata(cursor.getString(26));
				record.setAdddatapri(cursor.getString(27));
				record.setPbocdata(cursor.getString(28));
				record.setLoadparams(cursor.getString(29));
				record.setCardholderid(cursor.getString(30));
				record.setBatchbillno(cursor.getString(31));
				record.setSettledata(cursor.getString(32));
				record.setMesauthcode(cursor.getString(33));
				record.setStatuscode(cursor.getString(34));
				record.setReversetimes(cursor.getString(35));
				record.setReserve1(cursor.getString(36));
				record.setReserve2(cursor.getString(37));
				record.setReserve3(cursor.getString(38));
				record.setReserve4(cursor.getString(39));
				record.setReserve5(cursor.getString(40));
				list.add(record);
			}
		}
		cursor.close();
		db.close();
		return list;

	}

	public synchronized int save(TransRecord record) {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL(
					"insert into transrecord(" + "priaccount,transprocode,transamount,systraceno,translocaltime,"
							+ "translocaldate,expireddate,entrymode,seqnumber,conditionmode,"
							+ "updatecode,track2data,track3data,refernumber,idrespcode,"
							+ "respcode,terminalid,acceptoridcode,acceptoridname,addrespkey,"
							+ "adddataword,transcurrcode,pindata,secctrlinfo,balanceamount,"
							+ "icdata,adddatapri,pbocdata,loadparams,cardholderid,"
							+ "batchbillno,settledata,mesauthcode,statuscode,reversetimes,"
							+ "reserve1,reserve2,reserve3,reserve4,reserve5)"
							+ "values(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)",
					new Object[] { record.getPriaccount(), record.getTransprocode(), record.getTransamount(),
							record.getSystraceno(), record.getTranslocaltime(), record.getTranslocaldate(),
							record.getExpireddate(), record.getEntrymode(), record.getSeqnumber(),
							record.getConditionmode(), record.getUpdatecode(), record.getTrack2data(),
							record.getTrack3data(), record.getRefernumber(), record.getIdrespcode(),
							record.getRespcode(), record.getTerminalid(), record.getAcceptoridcode(),
							record.getAcceptoridname(), record.getAddrespkey(), record.getAdddataword(),
							record.getTranscurrcode(), record.getPindata(), record.getSecctrlinfo(),
							record.getBalanceamount(), record.getIcdata(), record.getAdddatapri(), record.getPbocdata(),
							record.getLoadparams(), record.getCardholderid(), record.getBatchbillno(),
							record.getSettledata(), record.getMesauthcode(), record.getStatuscode(),
							record.getReversetimes(), record.getReserve1(), record.getReserve2(), record.getReserve3(),
							record.getReserve4(), record.getReserve5() });

			result = 1;

			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			logger.error("保存transrecord异常", e);
		}
		db.close();
		return result;
	}

	public synchronized int update(TransRecord record) {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL(
					"priaccount=?,transprocode=?,transamount=?,systraceno=?,translocaltime=?,"
							+ "translocaldate=?,expireddate=?,entrymode=?,seqnumber=?,conditionmode=?,"
							+ "updatecode=?,track2data=?,track3data=?,refernumber=?,idrespcode=?,"
							+ "respcode=?,terminalid=?,acceptoridcode=?,acceptoridname=?,addrespkey=?,"
							+ "adddataword=?,transcurrcode=?,pindata=?,secctrlinfo=?,balanceamount=?,"
							+ "icdata=?,adddatapri=?,pbocdata=?,loadparams=?,cardholderid=?,"
							+ "batchbillno=?,settledata=?,mesauthcode=?,statuscode=?,reversetimes=?"
							+ "reserve1=?,reserve2=?,reserve3=?,reserve4=?,reserve5=? where id=?",
					new Object[] { record.getPriaccount(), record.getTransprocode(), record.getTransamount(),
							record.getSystraceno(), record.getTranslocaltime(), record.getTranslocaldate(),
							record.getExpireddate(), record.getEntrymode(), record.getSeqnumber(),
							record.getConditionmode(), record.getUpdatecode(), record.getTrack2data(),
							record.getTrack3data(), record.getRefernumber(), record.getIdrespcode(),
							record.getRespcode(), record.getTerminalid(), record.getAcceptoridcode(),
							record.getAcceptoridname(), record.getAddrespkey(), record.getAdddataword(),
							record.getTranscurrcode(), record.getPindata(), record.getSecctrlinfo(),
							record.getBalanceamount(), record.getIcdata(), record.getAdddatapri(), record.getPbocdata(),
							record.getLoadparams(), record.getCardholderid(), record.getBatchbillno(),
							record.getSettledata(), record.getMesauthcode(), record.getStatuscode(),
							record.getReversetimes(), record.getReserve1(), record.getReserve2(), record.getReserve3(),
							record.getReserve4(), record.getReserve5(), record.getId() });
			result = 1;

			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			logger.error("更新transrecord异常", e);
		}
		db.close();
		return result;
	}

	public synchronized int update(int id, String key, String value) {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("update transrecord set " + key + "=? where id=?", new Object[] { value, id });
			result = 1;

			M3Utility.sync(); // 同步命令

		} catch (Exception e) {
			result = -1;
			logger.error("更新transrecord异常", e);
		}
		db.close();
		return result;
	}

	public synchronized int delete(TransRecord record) {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("delete from transrecord where id=?", new Object[] { record.getId() });
			result = 1;

			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			logger.error("删除transrecord异常", e);
		}
		db.close();
		return result;
	}

	public synchronized int deleteAll() {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("delete from transrecord", new Object[] {});
			logger.info("清除交易流水记录...");
			result = 1;

			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			logger.error("清除交易流水记录异常..", e);
		}
		db.close();
		return result;
	}

	public int getTransCount() {
		int count = 0;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from transrecord where transprocode <> '900000' ", new String[] {});
			count = cursor.getCount();
		} catch (Exception e) {
			logger.error("获取交易记录数异常", e);
		} finally {
			cursor.close();
			db.close();
		}
		return count;
	}

	public int getConsumeCount() {
		int count = 0;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from transrecord where transprocode='000000' and conditionmode='00'",
					new String[] {});
			count = cursor.getCount();
		} catch (Exception e) {
			logger.error("获取消费交易记录数异常", e);
		} finally {
			cursor.close();
			db.close();
		}
		return count;
	}

	public int getRevokeCount() {
		int count = 0;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from transrecord where transprocode='200000' and conditionmode='00'",
					new String[] {});
			count = cursor.getCount();
		} catch (Exception e) {
			logger.error("获取消费撤销的交易记录数异常", e);
		} finally {
			cursor.close();
			db.close();
		}
		return count;
	}

	// 获取结算信息
	public synchronized TransRecord getSettle() {
		TransRecord record = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();

		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from transrecord where transprocode='900000' and conditionmode='00' ",
					new String[] {});
			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				record = new TransRecord();
				record.setId(cursor.getInt(0));
				record.setPriaccount(cursor.getString(1));
				record.setTransprocode(cursor.getString(2));
				record.setTransamount(cursor.getString(3));
				record.setSystraceno(cursor.getString(4));
				record.setTranslocaltime(cursor.getString(5));
				record.setTranslocaldate(cursor.getString(6));
				record.setExpireddate(cursor.getString(7));
				record.setEntrymode(cursor.getString(8));
				record.setSeqnumber(cursor.getString(9));
				record.setConditionmode(cursor.getString(10));
				record.setUpdatecode(cursor.getString(11));
				record.setTrack2data(cursor.getString(12));
				record.setTrack3data(cursor.getString(13));
				record.setRefernumber(cursor.getString(14));
				record.setIdrespcode(cursor.getString(15));
				record.setRespcode(cursor.getString(16));
				record.setTerminalid(cursor.getString(17));
				record.setAcceptoridcode(cursor.getString(18));
				record.setAcceptoridname(cursor.getString(19));
				record.setAddrespkey(cursor.getString(20));
				record.setAdddataword(cursor.getString(21));
				record.setTranscurrcode(cursor.getString(22));
				record.setPindata(cursor.getString(23));
				record.setSecctrlinfo(cursor.getString(24));
				record.setBalanceamount(cursor.getString(25));
				record.setIcdata(cursor.getString(26));
				record.setAdddatapri(cursor.getString(27));
				record.setPbocdata(cursor.getString(28));
				record.setLoadparams(cursor.getString(29));
				record.setCardholderid(cursor.getString(30));
				record.setBatchbillno(cursor.getString(31));
				record.setSettledata(cursor.getString(32));
				record.setMesauthcode(cursor.getString(33));
				record.setStatuscode(cursor.getString(34));
				record.setReversetimes(cursor.getString(35));
				record.setReserve1(cursor.getString(36));
				record.setReserve2(cursor.getString(37));
				record.setReserve3(cursor.getString(38));
				record.setReserve4(cursor.getString(39));
				record.setReserve5(cursor.getString(40));
			}
		} catch (Exception e) {
			logger.error("获取结算信息发生异常", e);
			e.printStackTrace();
		}
		cursor.close();
		db.close();
		return record;
	}

	/**
	 * 获取未上送的脱机交易笔数
	 */
	@Override
	public int getOfflineSaleCount() {
		// TODO Auto-generated method stub
		int count = 0;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from transrecord where transprocode='000000' and statuscode='OF'",
					new String[] {});
			count = cursor.getCount();
		} catch (Exception e) {
			logger.error("获取脱机消费的交易记录数异常", e);
		} finally {
			cursor.close();
			db.close();
		}
		return count;
	}
}
