package com.projectx.codeecho.execute;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MethodExecutation {
    private final static long TIMEOUT_LONG = 1000; // 15초

    public static Map<String, Object> timeOutCall(Object obj) throws Exception {
        // return Map
        Map<String, Object> returnMap = new HashMap<>();

        // Source를 만들때 지정한 Method
        Method objMethod = obj.getClass().getMethod("main", String[].class);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Future<Map<String, Object>> future = executorService.submit(() -> {
            Map<String, Object> callMap = new HashMap<>();

            callMap.put("return", objMethod.invoke(obj, (Object) new String[] {}));

            callMap.put("result", true);
            return callMap;
        });

        try {
            // 타임아웃 감시할 작업 실행
            returnMap = future.get(TIMEOUT_LONG, TimeUnit.MILLISECONDS); // timeout을 설정
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            // e.printStackTrace();
            returnMap.put("result", false);
        } finally {
            executorService.shutdown();
        }

        return returnMap;
    }
}
