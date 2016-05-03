package com.centerm.comm.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.centerm.comm.persistence.dao.SettleDataDao;
import com.centerm.comm.persistence.entity.SettleData;
import com.centerm.comm.persistence.helper.DBOpenHelper;
import com.centerm.mid.util.M3Utility;

/**
 * @author zhouhui @da2013-7-6
 *
 */
public class SettleDataDaoImpl implements SettleDataDao {
	private static Logger logger = Logger.getLogger(SettleDataDaoImpl.class);

	private Context context;
	private DBOpenHelper openHelper;

	/*
	 * public SettleDataDaoImpl(){ openHelper =
	 * DBOpenHelper.getInstance(this.context); }
	 */

	public SettleDataDaoImpl(Context context) {
		this.context = context;
		openHelper = DBOpenHelper.getInstance(this.context);
	}

	/*
	 * 获取结算信息
	 */
	@Override
	public List<SettleData> getSettleData() {
		List<SettleData> list = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from settledata order by translocaldate desc,translocaltime desc", null);
			Log.d("ckh", "zhixing chaxun ");
			if (cursor != null && cursor.getCount() > 0) {
				Log.i("ckh", "you shuju ");
				list = new ArrayList<SettleData>();
				while (cursor.moveToNext()) {
					SettleData record = new SettleData();
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
			e.printStackTrace();
			logger.error("数据库操作异常", e);
		}
		cursor.close();
		db.close();
		return list;
	}

	/*
	 * 保存结算信息
	 */
	@Override
	public int save(SettleData settledata) {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL(
					"insert into settledata(" + "priaccount,transprocode,transamount,systraceno,translocaltime,"
							+ "translocaldate,expireddate,entrymode,seqnumber,conditionmode,"
							+ "updatecode,track2data,track3data,refernumber,idrespcode,"
							+ "respcode,terminalid,acceptoridcode,acceptoridname,addrespkey,"
							+ "adddataword,transcurrcode,pindata,secctrlinfo,balanceamount,"
							+ "icdata,adddatapri,pbocdata,loadparams,cardholderid,"
							+ "batchbillno,settledata,mesauthcode,statuscode,reversetimes,"
							+ "reserve1,reserve2,reserve3,reserve4,reserve5)"
							+ "values(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)",
					new Object[] { settledata.getPriaccount(), settledata.getTransprocode(),
							settledata.getTransamount(), settledata.getSystraceno(), settledata.getTranslocaltime(),
							settledata.getTranslocaldate(), settledata.getExpireddate(), settledata.getEntrymode(),
							settledata.getSeqnumber(), settledata.getConditionmode(), settledata.getUpdatecode(),
							settledata.getTrack2data(), settledata.getTrack3data(), settledata.getRefernumber(),
							settledata.getIdrespcode(), settledata.getRespcode(), settledata.getTerminalid(),
							settledata.getAcceptoridcode(), settledata.getAcceptoridname(), settledata.getAddrespkey(),
							settledata.getAdddataword(), settledata.getTranscurrcode(), settledata.getPindata(),
							settledata.getSecctrlinfo(), settledata.getBalanceamount(), settledata.getIcdata(),
							settledata.getAdddatapri(), settledata.getPbocdata(), settledata.getLoadparams(),
							settledata.getCardholderid(), settledata.getBatchbillno(), settledata.getSettledata(),
							settledata.getMesauthcode(), settledata.getStatuscode(), settledata.getReversetimes(),
							settledata.getReserve1(), settledata.getReserve2(), settledata.getReserve3(),
							settledata.getReserve4(), settledata.getReserve5() });

			result = 1;
			M3Utility.sync(); // 同步命令

		} catch (Exception e) {
			result = -1;
			e.printStackTrace();
			logger.error("数据库操作异常", e);
		}
		db.close();
		return result;
	}

	/*
	 * 清除结算表
	 */
	@Override
	public int delete() {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("delete from settledata");
			result = 1;
			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			e.printStackTrace();
			logger.error("数据库操作异常", e);
		}
		db.close();
		return result;
	}

	/**
	 * 删除非本批次的所有结算记录
	 */
	public int deleteOtherBatch(String batchno) {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("delete from settledata where batchbillno not like ?", new Object[] { batchno + "%" });
			result = 1;
			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			e.printStackTrace();
			logger.error("数据库操作异常", e);
		}
		db.close();
		return result;
	}

	/*
	 * 由transRecord表转存到SettleData表
	 */
	@Override
	public int fromTranToSettle() {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("insert into settledata select * from transrecord");
			result = 1;
			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			e.printStackTrace();
			logger.error("数据库操作异常", e);
		}
		db.close();
		return result;
	}

	@Override
	public int delete(SettleData settleData) {
		// TODO Auto-generated method stub
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("delete from settledata where batchbillno=?", new Object[] { settleData.getBatchbillno() });
			result = 1;
			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			logger.error("数据库操作异常", e);
		} finally {
			db.close();
		}
		return result;
	}

	@Override
	public int update(String batchbillno, String key, String value) {
		// TODO Auto-generated method stub
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("update settledata set " + key + "=? where batchbillno=?", new Object[] { value, batchbillno });
			result = 1;

			M3Utility.sync(); // 同步命令

		} catch (Exception e) {
			result = -1;
			logger.error("数据库操作异常", e);
		}
		db.close();
		return result;
	}

	@Override
	public List<SettleData> getTXNData() {
		// TODO Auto-generated method stub
		List<SettleData> list = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(
					"select * from settledata where statuscode is null order by translocaldate desc,translocaltime desc",
					null);
			if (cursor != null && cursor.getCount() > 0) {
				list = new ArrayList<SettleData>();
				while (cursor.moveToNext()) {
					SettleData record = new SettleData();
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
			e.printStackTrace();
			logger.error("数据库操作异常", e);
		}
		cursor.close();
		db.close();
		return list;
	}

	@Override
	public List<SettleData> getUnSuccesssFulData() {
		// TODO Auto-generated method stub
		List<SettleData> list = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(
					"select * from settledata where statuscode = ? order by translocaldate desc,translocaltime desc",
					new String[] { "1" });
			if (cursor != null && cursor.getCount() > 0) {
				list = new ArrayList<SettleData>();
				while (cursor.moveToNext()) {
					SettleData record = new SettleData();
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
			e.printStackTrace();
			logger.error("数据库操作异常", e);
		}
		cursor.close();
		db.close();
		return list;
	}

	@Override
	public List<SettleData> getDeniedData() {
		// TODO Auto-generated method stub
		List<SettleData> list = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(
					"select * from settledata where statuscode = ? order by translocaldate desc,translocaltime desc",
					new String[] { "2" });
			if (cursor != null && cursor.getCount() > 0) {
				list = new ArrayList<SettleData>();
				while (cursor.moveToNext()) {
					SettleData record = new SettleData();
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
			e.printStackTrace();
			logger.error("数据库操作异常", e);
		}
		cursor.close();
		db.close();
		return list;
	}

}
