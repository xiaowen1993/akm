package cn.zsmy.akm.http;



import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by qinwei on 2015/10/6.
 * email:qinwei_it@163.com
 */
public abstract class JsonCallback<T> extends AbstractCallback<T> {
    private Type type;

    public JsonCallback() {
        type = getClass().getGenericSuperclass();
        type = ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    @Override
    public T bindData(String content) throws AppException {
        try {
            return JsonParser.deserializeFromJson(content, type);
        } catch (JsonSyntaxException e) {
            throw new AppException(AppException.ErrorType.PARSE_JSON, e.getMessage());
        }
    }
}
