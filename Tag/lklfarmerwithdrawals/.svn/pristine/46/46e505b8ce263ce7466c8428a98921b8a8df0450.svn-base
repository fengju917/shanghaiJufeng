package com.centerm.comm.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.centerm.comm.persistence.dao.ReverseDao;
import com.centerm.comm.persistence.entity.Reverse;
import com.centerm.comm.persistence.helper.DBOpenHelper;
import com.centerm.mid.util.M3Utility;

public class ReverseDaoImpl implements ReverseDao {

	private final Logger logger = Logger.getLogger(ReverseDaoImpl.class);

	private Context context;
	private DBOpenHelper openHelper;

	public ReverseDaoImpl(Context context) {
		this.context = context;
		openHelper = DBOpenHelper.getInstance(this.context);
	}

	@Override
	public synchronized List<Reverse> getEntities() {
		List<Reverse> list = new ArrayList();
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from reverse order by translocaldate desc,translocaltime desc",
				new String[] {});
		if (cursor != null && cursor.getCount() > 0) {
			list = new ArrayList<Reverse>();
			while (cursor.moveToNext()) {
				Reverse record = new Reverse();
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
		return list;
	}

	@Override
	public synchronized Reverse getReverse() {
		Reverse record = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from reverse order by translocaldate desc,translocaltime desc",
					new String[] {});
			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				record = new Reverse();
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
			logger.error("获取冲正记录发生异常!", e);
			e.printStackTrace();
		}
		cursor.close();
		return record;
	}

	@Override
	public synchronized Reverse getReverseByCondition(String batchno, String systraceno) {
		Reverse record = null;
		if (batchno == null || "".equals(batchno) || systraceno == null || "".equals(systraceno)) {
			return record;
		}
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from reverse where systraceno = ? and batchno like ?",
				new String[] { systraceno, "%" + batchno + "%" });
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				record = new Reverse();
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
		}
		cursor.close();
		return record;
	}

	@Override
	public synchronized Reverse getReverseByCondition(String transprocode, String systraceno, String terminalid,
			String acceptoridcode) {
		Reverse record = null;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(
				"select * from reverse where transprocode = ? and systraceno = ? and terminalid = ? and acceptoridcode = ?",
				new String[] { transprocode, systraceno, terminalid, acceptoridcode });

		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				record = new Reverse();
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
		}
		cursor.close();
		return record;
	}

	@Override
	public synchronized int save(Reverse record) {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL(
					"insert into reverse(" + "priaccount,transprocode,transamount,systraceno,translocaltime,"
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
			logger.error("保存reverse异常", e);
		}
		return result;
	}

	@Override
	public synchronized int update(Reverse record) {
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
			logger.error("更新reverse异常", e);
		}
		return result;
	}

	@Override
	public synchronized int update(int id, String key, String value) {

		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("update reverse set " + key + "=? where id=?", new Object[] { value, id });
			result = 1;
			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			logger.error("更新reverse异常", e);
		}
		return result;
	}

	@Override
	public synchronized int delete(Reverse record) {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("delete from reverse where id=?", new Object[] { record.getId() });
			result = 1;
			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			logger.error("删除reverse异常", e);
		}
		return result;
	}

	@Override
	public synchronized int deleteAll() {
		int result = 0;
		SQLiteDatabase db = openHelper.getWritableDatabase();
		try {
			db.execSQL("delete from reverse", new Object[] {});
			result = 1;
			M3Utility.sync(); // 同步命令
		} catch (Exception e) {
			result = -1;
			logger.error("删除reverse异常", e);
		}
		return result;
	}

	@Override
	public int getReverseCount() {
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("select * from reverse", new String[] {});
		} catch (Exception e) {
			logger.error("获取冲正记录数异常", e);
		}
		// db.close();
		return cursor == null ? 0 : cursor.getCount();
	}
}
