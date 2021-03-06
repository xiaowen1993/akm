package cn.zsmy.akm.doctor.chat.im;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import cn.zsmy.akm.doctor.chat.bean.Attachment;
import cn.zsmy.akm.doctor.chat.bean.Message;
import cn.zsmy.akm.doctor.chat.bean.MessageStatus;
import cn.zsmy.akm.doctor.chat.utils.Constants;
import cn.zsmy.akm.doctor.chat.utils.Trace;
import cn.zsmy.akm.doctor.home.MyApplication;
import cn.zsmy.akm.doctor.http.FileCallback;
import cn.zsmy.akm.doctor.http.FileEntity;
import cn.zsmy.akm.doctor.http.JsonParser;
import cn.zsmy.akm.doctor.http.Request;
import cn.zsmy.akm.doctor.http.RequestManager;
import cn.zsmy.akm.doctor.http.StringCallback;
import cn.zsmy.akm.doctor.utils.UrlHelper;

public class IMPushService extends Service  {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// TODO find wait handler message to handler
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Message message = (Message) intent.getSerializableExtra(Constants.KEY_MESSAGE_ENTITIES);
			if (message != null) {
				handlerMessage(message);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void handlerMessage(Message message) {
		switch (message.getType()) {
		case txt:
			sendTxtMsg(message);
			break;
		case img:
			sendImgMsg(message);
			break;
		case voice:
			sendVoiceMsg(message);
			break;
		case video:
			sendVideoMsg(message);
			break;
		default:
			break;
		}
	}

	/**
	 * 发送文字消息
	 * 
	 * @param oldMessage
	 */
	private void sendTxtMsg(final Message oldMessage) {
		
		Request request = new Request(UrlHelper.SEND_MSG, Request.RequestMethod.POST,this);
		Log.i("TAG>>>>>>sendTxtMsg",oldMessage.getContent());
		request.addHeader("Content-Type", "application/json;charset=UTF-8");
		request.postContent = JsonParser.serializeToJson(oldMessage);
		Trace.d(request.postContent);
		request.setCallback(new StringCallback() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jsonObject = new JSONObject(result);
					String code = jsonObject.optString("code");
					if (code.equals("SUCC")) {
						JSONObject obj = jsonObject.optJSONObject("data");
						JSONArray arrayJSON = jsonObject.optJSONArray("data");
						String data = "";
						if (arrayJSON != null) {
							data = jsonObject.getJSONArray("data").toString();
						} else {
							data = jsonObject.getJSONObject("data").toString();
						}

						Message message = JsonParser.deserializeFromJson(data, Message.class);
						if (message != null) {
							message.setStatus(MessageStatus.done);
							message.setSenderId(MyApplication.getProfile().getUserId());
							Log.i("TAG", message.getCaseId() + "" + message.getContent());
							IMPushManager.getInstance(getApplicationContext()).notifyDataChanged(oldMessage, message);
						}
					}
				} catch (Exception exception) {
//					throw new AppException(ExceptionStatu.ParseJsonException, "ParseJsonException:" + exception.getMessage());
				}

			}
		});
		RequestManager.getInstance().execute(this.toString(), request);
	}

	/**
	 * 发送图片消息
	 * 
	 * @param message
	 */
	private void sendImgMsg(final Message message) {
		Request request = new Request(UrlHelper.CHAT_SEND_PHOTO_URL+"?caseId="+message.getCaseId(), Request.RequestMethod.POST, this);
		final ArrayList<FileEntity> files = new ArrayList<FileEntity>() ;
		ArrayList<Attachment> attachments = message.getAttachments();
		for (Attachment attachment : attachments) {
			files.add(new FileEntity(attachment.getFile_name(),attachment.getFile_type(), attachment.getFile_path()));
		}
		Log.i("TAG>>>>>>>>>>>>>",files.get(0).getFilePath());
		request.put("","");
		request.fileEntities=files;
		request.setCallback(new FileCallback() {
			
//			@Override
//			public String preRequest() {
//				// TODO Auto-generated method stub
//
//				if (message.getType() == MessageType.img) {
//					FileEntity fileEntity = files.get(0);
//					Bitmap bitmap = BitmapFactory.decodeByteArray(BitmapUtil.decodeBitmap(fileEntity.getFilePath()), 0, BitmapUtil.decodeBitmap(fileEntity.getFilePath()).length);
//					bitmap = compressImage(bitmap);
//					if(bitmap.isRecycled() == true){
//						bitmap.recycle();   //回收图片所占的内存
//						   System.gc();
//					}
//					Log.i("IMpushService","bitmap现在的大小是---->"+bitmap.getByteCount());
//					String path = FileUtil.saveBitmap(bitmap, fileEntity.getFileName());
//					if(!TextUtils.isEmpty(path)){
//						fileEntity.setFilePath(path);
//					}else{
//						fileEntity.setFilePath(FileUtil.getImgDir()+fileEntity.getFileName()+".JPEG");
//					}
//
//
//				}
//				return super.preRequest();
//			}
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jsonObject = new JSONObject(result);
					String code = jsonObject.optString("code");
					if (code.equals("SUCC")) {
						JSONObject obj=jsonObject.optJSONObject("data");
						JSONArray  arrayJSON=jsonObject.optJSONArray("data");
						String data = "";
						if (arrayJSON!=null) {
							data = jsonObject.getJSONArray("data").toString();
						} else {
							data = jsonObject.getJSONObject("data").toString();
						}

						Message newMessage = JsonParser.deserializeFromJson(data, Message.class);
						if (message != null) {
							message.setStatus(MessageStatus.done);
							message.setSenderId(MyApplication.getProfile().getUserId());
							IMPushManager.getInstance(getApplicationContext()).notifyDataChanged(message, newMessage);
						}
					}
				} catch (Exception exception) {
//					throw new AppException(ExceptionStatu.ParseJsonException, "ParseJsonException:" + exception.getMessage());
				}
//					}else{
//						message.setStatus(MessageStatus.err);
//						IMPushManager.getInstance(getApplicationContext()).notifyDataChanged(message, null);
//					}
			}
			
//			@Override
//			public void onFailure(AppException exception) {
//				exception.printStackTrace();
//				message.setStatus(MessageStatus.err);
//				IMPushManager.getInstance(getApplicationContext()).notifyDataChanged(message, null);
//			}
//
//			@Override
//			public void onProgressUpdate(int curPos, int contentLength) {
//
//			}
		});
		RequestManager.getInstance().execute(this.toString(), request);
	}

	/**
	 * 发送文字消息
	 * 
	 * @param message
	 */
	private void sendVoiceMsg(Message message) {
		sendImgMsg(message);
	}

	private void sendVideoMsg(Message message) {

	}

	public void notice(Message message) {

	}


	/**
	 * 按质量压缩
	 * 
	 * @param image
	 * @return
	 */
	private Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;

		while (baos.toByteArray().length / 1024 > 256) { // 循环判断如果压缩后图片是否大于512kb,大于继续压缩

			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;// 每次都减少10
			if(options<1){options=1;break;}
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}

//	@Override
//	public boolean onGlobalAppException(AppException appException) {
//		if (appException.getStatu() == ExceptionStatu.ServerException) {
//			if (appException.getErrorCode() == 403 || appException.getErrorCode() == 401) {
//				Intent intent = new Intent(this, LoginActivity.class);
//				startActivity(intent);
//			} else {
//			}
//		} else if (appException.getStatu() == ExceptionStatu.UserOperationFailedException) {
//			if (appException.getErrorCode() == 9203) {
//				Toast.makeText(this, "服务器好像去火星旅游了,您的登陆状态丢失了,请您重新登陆一下",1).show();
//				Intent intent = new Intent(this, LoginActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(intent);
////				ChatActivity.finish();
//			} else {
//			}
//		} else if (appException.getStatu() == ExceptionStatu.ConnectTimeoutException) {
//		}
//		return true;
//	}
}