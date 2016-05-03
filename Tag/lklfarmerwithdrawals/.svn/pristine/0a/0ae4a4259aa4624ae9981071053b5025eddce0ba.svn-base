package com.centerm.lklcpos.http;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.centerm.lklcpos.util.Config;
import com.centerm.lklcpos.util.Utility;

public class HttpRequestHandler {
	/*
	 * private static final Logger log =
	 * Logger.getLogger(HttpRequestHandler.class);
	 * 
	 * private String ip = "9.250.249.10"; private static final String
	 * httpresponse = "httpresponse"; private int port = 443; private String
	 * protocol = "http"; private String keystorepath =
	 * "/data/mtms/apps/com.centerm.lklcpos/cer/media_test1.p12"; private String
	 * keystorepass= "media_test1"; private String parentCerPath =
	 * "/data/mtms/apps/com.centerm.lklcpos/cer/lklca_chain.cer"; private String
	 * cn = "term.lakala.com"; private String encoding ="utf-8"; private Handler
	 * handler; private Context context; private int timeout = 60000;
	 * 
	 * // public static HttpRequestHandler getInstance(Handler hanlder,Context
	 * context){ // if(hqh ==null){ // hqh = new
	 * HttpRequestHandler(hanlder,context ); // } // return hqh; // }
	 * 
	 * public HttpRequestHandler(Handler hanlder,Context context){ this.handler
	 * = hanlder; this.context = context; this. ip = Utility.getIp(context,
	 * this.ip); this.port =Utility.getPort(context, port); this.timeout =
	 * Integer.parseInt(Config.getInstance(context).getConfig("httptimeout",
	 * String.valueOf(timeout))); // this.keystorepath
	 * =Config.getInstance(context).getConfig("keystore", this.keystorepath); //
	 * this.keystorepass=Config.getInstance(context).getConfig("keystorepass",
	 * keystorepass); // this.parentCerPath
	 * =Config.getInstance(context).getConfig("cerpath", this.parentCerPath); //
	 * this.cn = Config.getInstance(context).getConfig("cerCn", cn);
	 * this.protocol = Config.getInstance(context).getConfig("protocol",
	 * this.protocol); }
	 * 
	 * private String getURL(String uri){ return
	 * this.protocol+"://"+ip+":"+port+uri; }
	 * 
	 * public void handleHttpGetRequest(final String uri,final String callback){
	 * final JsResponse response = new JsResponse();
	 * 
	 * new Thread(){ public void run() { try { log.debug("获取IP地址 = "
	 * +Utility.getLocalIpAddress());
	 * handler.sendMessage(Utility.createMesage(Constant.msg.msg_http_begin));
	 * String result = getUrl(getURL(uri), encoding); JSONObject jj = new
	 * JSONObject(result); response.setSuc(true, "http请求成功处理");
	 * response.addData(httpresponse, jj); log.debug("http返回数据:["+result+"]"); }
	 * catch (Exception e) { dealException(e, response); } log.debug(
	 * "callback 参数:["+response.toJson()+"]"); String tempJson =
	 * convertToJsString( response.toJson());
	 * handler.sendMessage(Utility.createCallbackMsg( Constant.msg.msg_http_ok,
	 * callback,tempJson)); }; }.start(); }
	 * 
	 * public void handleHttpRequest(final String uri,final String packet,final
	 * String callback){ final JsResponse response = new JsResponse(); new
	 * Thread(){ public void run() { try { log.debug("获取IP地址 = "
	 * +Utility.getLocalIpAddress());
	 * handler.sendMessage(Utility.createMesage(Constant.msg.msg_http_begin));
	 * log.debug("请求JSON数据包["+packet+"]"); String result = postUrl(getURL(uri),
	 * Utility.jsonToMap(packet), encoding); JSONObject jj = new
	 * JSONObject(result); response.setSuc(true, "http请求成功处理");
	 * response.addData(httpresponse, jj); log.debug("http返回数据:["+result+"]"); }
	 * catch (Exception e) { dealException(e, response); } log.debug(
	 * "callback 参数:["+response.toJson()+"]"); String tempJson =
	 * convertToJsString( response.toJson());
	 * handler.sendMessage(Utility.createCallbackMsg( Constant.msg.msg_http_ok,
	 * callback,tempJson)); }; }.start(); } /* /** 以json格式 psot 访问
	 * 
	 * @param uri
	 * 
	 * @param packet
	 * 
	 * @param callback
	 */
	/*
	 * public void handleJsonHttpRequest(final String uri,final String
	 * packet,final String callback){ final JsResponse response = new
	 * JsResponse(); new Thread(){ public void run() { try {
	 * handler.sendMessage(Utility.createMesage(Constant.msg.msg_http_begin));
	 * log.debug("请求JSON数据包["+packet+"]"); String result = postJson(getURL(uri),
	 * packet, encoding); JSONObject jj = new JSONObject(result);
	 * response.setSuc(true, "http请求成功处理"); response.addData(httpresponse, jj);
	 * log.debug("http返回数据:["+result+"]"); } catch (Exception e) {
	 * dealException(e, response); } log.debug("callback 参数:["
	 * +response.toJson()+"]"); String tempJson = convertToJsString(
	 * response.toJson()); handler.sendMessage(Utility.createCallbackMsg(
	 * Constant.msg.msg_http_ok, callback,tempJson)); }; }.start(); }
	 */
	/**
	 * 自定义以ip端口路径post访问
	 * 
	 * @param uri
	 * @param packet
	 * @param callback
	 * @param isUrlAll
	 */
	/*
	 * public void handleHttpRequest(final String uri,final String packet,final
	 * String callback ,final String isUrlAll){ final JsResponse response = new
	 * JsResponse(); new Thread(){ public void run() { try {
	 * handler.sendMessage(Utility.createMesage(Constant.msg.msg_http_begin));
	 * log.debug("请求JSON数据包["+packet+"]"); String result = postUrl(uri,
	 * Utility.jsonToMap(packet), encoding); JSONObject jj = new
	 * JSONObject(result); response.setSuc(true, "http请求成功处理");
	 * response.addData(httpresponse, jj); log.debug("http返回数据:["+result+"]"); }
	 * catch (Exception e) { dealException(e, response); } log.debug(
	 * "callback 参数:["+response.toJson()+"]"); String tempJson =
	 * convertToJsString( response.toJson());
	 * handler.sendMessage(Utility.createCallbackMsg( Constant.msg.msg_http_ok,
	 * callback,tempJson)); }; }.start(); }
	 * 
	 * private static String getCurrentDate(){ SimpleDateFormat format = new
	 * SimpleDateFormat("yyyy-MM-dd HH:mm"); return format.format(new Date()); }
	 * 
	 * public String getUrl(String url,String encoding) throws Exception {
	 * log.debug("http请求地址:["+url+"]"); HttpGet request = new HttpGet(url); //
	 * HttpClient client =
	 * HttpsUtil.getHttpClient(HttpsUtil.getHttpScheme(port), //
	 * HttpsUtil.getHttpsScheme(keystorepath, keystorepass, port,
	 * parentCerPath,cn), timeout); HttpClient client =
	 * HttpsUtil.getHttpClient(HttpsUtil.getHttpScheme(port), null, timeout);
	 * try{ HttpResponse response = client.execute(request); if
	 * (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	 * HttpEntity resEntity = response.getEntity(); return EntityUtils.toString(
	 * resEntity ); } else { throw new
	 * HttpStatusException("HTTP返回错误状态,状态码["+response.getStatusLine().
	 * getStatusCode()+"]"); }
	 * 
	 * }catch(Exception e){ throw e; }finally{ request.abort();
	 * client.getConnectionManager().closeExpiredConnections();
	 * client.getConnectionManager().shutdown(); } } public String
	 * postUrl(String url, Map<String, String> params, String encoding) throws
	 * Exception { log.debug("http请求地址:["+url+"]");
	 * log.info("http请求信息:["+params.toString()+"]"); List<NameValuePair> param =
	 * new ArrayList<NameValuePair>(); Iterator<Entry<String, String>> iterator
	 * = params.entrySet().iterator(); while (iterator.hasNext()) {
	 * Entry<String, String> entry = iterator.next(); param.add(new
	 * BasicNameValuePair(entry.getKey(), entry.getValue())); } HttpPost request
	 * = new HttpPost(url); HttpEntity entity = new UrlEncodedFormEntity(param,
	 * Config.getInstance(context).getConfig("httpEncode", encoding));
	 * request.setEntity(entity); // HttpClient client =
	 * HttpsUtil.getHttpClient(HttpsUtil.getHttpScheme(port), //
	 * HttpsUtil.getHttpsScheme(keystorepath, keystorepass, port,
	 * parentCerPath,cn), timeout); HttpClient client =
	 * HttpsUtil.getHttpClient(HttpsUtil.getHttpScheme(port), null, timeout);
	 * try{ HttpResponse response = client.execute(request);
	 * log.debug("http请求返回response状态:["+
	 * response.getStatusLine().getStatusCode() +"]" );
	 * 
	 * if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	 * HttpEntity resEntity = response.getEntity(); return
	 * EntityUtils.toString(resEntity); } else { throw new
	 * HttpStatusException("HTTP返回错误状态,状态码["+response.getStatusLine().
	 * getStatusCode()+"]"); } }catch(Exception e){ throw e; }finally{
	 * request.abort(); client.getConnectionManager().closeExpiredConnections();
	 * client.getConnectionManager().shutdown(); } }
	 * 
	 * 
	 * public String postJson(String url, String params, String encoding) throws
	 * Exception { log.debug("http请求地址:["+url+"]");
	 * log.info("http请求信息:["+params.toString()+"]");
	 * 
	 * HttpPost request = new HttpPost(url); HttpEntity entity = new
	 * StringEntity( params, encoding);
	 * 
	 * request.setEntity(entity); HttpClient client =
	 * HttpsUtil.getHttpClient(HttpsUtil.getHttpScheme(port), null, timeout);
	 * try{ HttpResponse response = client.execute(request);
	 * 
	 * log.debug("http请求返回response状态:["+
	 * response.getStatusLine().getStatusCode() +"]" ); if
	 * (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	 * HttpEntity resEntity = response.getEntity(); return
	 * EntityUtils.toString(resEntity); } else { throw new
	 * HttpStatusException("HTTP返回错误状态,状态码["+response.getStatusLine().
	 * getStatusCode()+"]"); } }catch(Exception e){ throw e; }finally{
	 * request.abort(); client.getConnectionManager().closeExpiredConnections();
	 * client.getConnectionManager().shutdown(); } }
	 * 
	 * private void dealException(Exception e,JsResponse response){ try {
	 * Thread.sleep(3000); log.error(e.getMessage(), e); String errMsg =
	 * Constant.tips.tip_net_error; if(e instanceof JSONException){ errMsg =
	 * Constant.tips.tip_data_error; }else if(e instanceof HttpStatusException){
	 * errMsg = e.getMessage(); }else if(e instanceof SocketTimeoutException){
	 * errMsg = Constant.tips.tip_socket_timeout; }else if(e instanceof
	 * ConnectTimeoutException){ errMsg = Constant.tips.tip_connect_timeout; }
	 * response.setSuc(false,errMsg); JSONObject kk = new JSONObject();
	 * JSONObject nn = new JSONObject(); try {
	 * nn.put("retCode",Constant.tips.tip_err_code); nn.put("errMsg", errMsg);
	 * kk.putOpt("retStatus", nn); response.addData(httpresponse, kk); } catch
	 * (JSONException e1) { e1.printStackTrace(); } } catch
	 * (InterruptedException e2) { e2.printStackTrace(); } }
	 * 
	 * 
	 * 
	 * private String convertToJsString(String paramString) { return
	 * paramString.replace("\\", "\\\\").replace("'", "\\'").replace("\r", "
	 * \\r").replace("\n", "\\n"); }
	 */
}