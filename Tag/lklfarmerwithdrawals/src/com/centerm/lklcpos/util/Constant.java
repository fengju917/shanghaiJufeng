﻿package com.centerm.lklcpos.util;

import java.util.HashMap;
import java.util.Map;

public final class Constant {
	public static final class msg {
		public static final int DEVICE_MSG = 0x0A;
		public static String msg = "msg";
		public static String msg_return_main = "msg_return_main";
		public static String msg_go_indexpath = "msg_go_indexpath";
		public static String page_index = "page_index";

		public static String msg_page_back = "msg_page_back";
		public static String msg_gototms = "msg_gototms";
		public static String msg_page_forward = "msg_page_forward";
		public static String msg_page_ad_forward = "msg_page_ad_forward";
		public static String page_forward_url = "page_forward_url";
		public static String page_forward_extra_data = "page_forward_extra_data";
		public static String page_forward_canback = "page_forward_canback";
		public static String isstay = "isstay";
		public static String preLoad_WebView = "preloadwebview";
		public static String play_video = "play_video";
		public static String msg_dialog_hide = "msg_dialog_hide";
		public static String msg_dialog_show = "msg_dialog_show";
		public static String msg_dialog_show_tips = "msg_dialog_show_tips";
		public static String msg_reset_scrreen_timer = "msg_reset_scrreen_timer";
		public static String msg_swipe_card = "msg_swipe_card";
		public static String msg_swipe_card_data = "msg_swipe_card_data";
		public static String callback = "callback";
		public static String jsresponse = "jsresponse";

		public static String msg_dev_error = "msg_dev_error";
		public static String dev_position = "statusPos ";
		public static String dev_status = "statusMark ";
		public static String msg_pin_key = "msg_pin_key";
		public static String key_press_nomal = "key_press_nomal";
		public static String key_press_confirm = "key_press_confirm";
		public static String key_press_del = "key_press_del";
		public static String key_press_cancel = "key_press_cancel";
		public static String key_press_reinput = "key_press_reinput";
		public static String key_press_data = "key_press_data";
		public static String key_press_number = "key_press_number";
		public static String msg_print_data = "msg_print_data";
		public static String msg_http_begin = "msg_http_begin";
		public static String msg_http_ok = "msg_http_ok";

		public static String msg_getpin_begin = "msg_getpin_begin"; // 外接密码键盘
		public static String msg_getpin_end = "msg_getpin_end";

		public static String socket_err = "socket_err";// add by xrh 20130402
		public static String param_config = "param_config";// add by xrh
															// 20130402

		public static String msg_cashup_startproc = "msg_cashup_startproc"; // add
																			// by
																			// chenkehui
																			// @20130910
		public static String msg_cashup_kernelProc = "msg_cashup_kernelProc"; // 接触式电子现金脱机第二步
		public static String msg_cashup_completeProc = "msg_cashup_completeProc"; // 接触式电子现金脱机第三步
		public static String msg_check_card = "msg_check_card"; // 检卡
		public static String msg_read_card = "msg_read_card"; // 读卡信息
		public static String msg_start_pboc = "msg_start_pboc"; // 接触式PBOC第一步接口
		public static String msg_kernel_pboc = "msg_kernel_pboc"; // 接触式PBOC第二步接口
		public static String msg_inputOnlineRespData = "msg_inputOnlineRespData"; // PBOC第三步接口
		public static String msg_comple_pboc = "msg_comple_pboc"; // PBOC第四步接口
		public static String msg_rf_pre_progress = "msg_rf_pre_progress"; // 非接预处理
		public static String msg_emv_interaction = "msg_emv_interaction"; // 内核交互
		public static String msg_readCardOfflineBalance = "msg_readCardOfflineBalance"; // 读卡片余额
		public static String msg_readCardLog = "msg_readCardLog"; // 读卡片日志
	}

	public static final class tips {
		public static final String tip_net_error = "通讯异常，请稍候再试！";
		public static final String tip_socket_timeout = "网络响应超时，请稍候再试！";
		public static final String tip_connect_timeout = "网络连接超时，请稍候再试！";
		public static final String tip_data_error = "数据解析异常,请联系客服！";
		public static final String tip_file_notfound = "找不到证书文件,请联系客服！";
		public static final String tip_err_code = "ERR";
		public static final String tip_err_nodefined = "该错误未定义,请联系客服！";
	}

	public static final class config {

		public static final String tag_service_ip = "Service_ip";
		public static final String tag_service_port = "Service_port";
		public static final String tag_http_timeout = "http_timeout";
		public static final String tag_http_tryagain = "http_try_again";
		public static final String tag_swipecard_timeout = "Swipecard_timeout";
		public static final String tag_dev_opt_timeout = "Dev_opt_timeout";
		public static final String tag_main_page = "Main_page";
		public static final String tag_pswd_max_length = "pswd_max_length";

		public static final Map<String, String> defaultValueMap = new HashMap();

		static {
			defaultValueMap.put(tag_service_ip, "1.202.150.4");
			defaultValueMap.put(tag_service_port, "8980");
			defaultValueMap.put(tag_pswd_max_length, "6");
		}

	}

	public static final class broadcast {
		public final static String bizAppStatusbroadcast = "com.centerm.lklcpos.broadcast.bizAppStatusbroadcast";
		public final static int app_busyStatus = 0;
		public final static int app_validStatus = 1;
		public final static String devErrorBoradCast = "com.centerm.lklcpos.broadcast.devErrorBroadCast";
		public final static String mtmsupdateBroadcase = "com.centerm.lklcpos.broadcast.mtmsupdateBroadcase";
		public final static String jobStartBroadCast = "com.centerm.lklcpos.broadcast.jobStartBroadCast";
	}

	/************************************** 网络类型 **********************************/
	public static final int CHINA_UNICOME = 1;
	public static final int CHINA_TELECOME = 2;
	public static final int CHINA_CMCC = 3;
	public static final int UNKNOWN_NET = 0;
	/*********************************************************************************/

}
