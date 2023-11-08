package com.projectx.codeecho.builder;

import com.projectx.codeecho.CodeEchoApplication;
import com.projectx.codeecho.execute.MethodExecutation;
import com.projectx.codeecho.model.result.ApiResponseResult;
import com.projectx.codeecho.util.common.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CompileBuilder {
    private final String path = CodeEchoApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();

    public Object compileCode(String body) throws Exception {
        String uuid = UUIDUtil.createUUID();
        String uuidPath = path + uuid + "/";

        // Source를 이용한 java file 생성
        File newFolder = new File(uuidPath);
        File sourceFile = new File(uuidPath + "DynamicClass.java");
        File classFile = new File(uuidPath + "DynamicClass.class");

        Class<?> cls = null;

        // compile System err console 조회용 변수
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream origErr = System.err;

        try {
            newFolder.mkdir();
            new FileWriter(sourceFile).append(body).close();

            // 만들어진 Java 파일을 컴파일
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            // System의 error outputStream을 ByteArrayOutputStream으로 받아오도록 설정
            System.setErr(new PrintStream(err));

            // compile 진행
            int compileResult = compiler.run(null, null, null, sourceFile.getPath());
            // compile 실패인 경우 에러 로그 반환
            if(compileResult == 1) {
                return err.toString();
            }

            // 컴파일된 Class를 Load
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] {new File(uuidPath).toURI().toURL()});
            cls = Class.forName("DynamicClass", true, classLoader);

            // Load한 Class의 Instance를 생성
            return cls.newInstance();

        } catch (Exception e) {
            log.error("[CompileBuilder] 소스 컴파일 중 에러 발생 :: {}", e.getMessage());
            e.printStackTrace();
            return null;

        } finally {
            // Syetem error stream 원상태로 전환
            System.setErr(origErr);

            if(sourceFile.exists())
                sourceFile.delete();
            if(classFile.exists())
                classFile.delete();
            if(newFolder.exists())
                newFolder.delete();
        }
    }

    // run method : parameter byte array, return byte array
    @SuppressWarnings("rawtypes")
    public Map<String, Object> runObject(Object obj) throws Exception {
        // 실행 중에 출력되는 스트림을 변수로 저장하고 싶을 때
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;

        Map<String, Object> returnMap = new HashMap<>();

        try {
            // System의 out, error outputStream을 ByteArrayOutputStream으로 받아오도록 설정
            System.setOut(new PrintStream(out));
            System.setErr(new PrintStream(err));

            // invoke() 함수가 실행 중에 System.out 출력물을 변수로 'out' 변수에 저장함.
            // 메소드 timeout을 체크하며 실행(15초 초과 시 강제종료)
            Map<String, Object> result = MethodExecutation.timeOutCall(obj);

            // stream 정보 저장
            boolean b = err.toString() != null && !err.toString().equals("");
            if((Boolean) result.get("result")) {
                returnMap.put("result", ApiResponseResult.SUCEESS.getText());
                returnMap.put("return", result.get("return"));
                if(b) {
                    returnMap.put("SystemOut", err.toString().replace("\n", ""));
                }else {
                    returnMap.put("SystemOut", out.toString().replace("\n", ""));
                }
            }else {
                returnMap.put("result", ApiResponseResult.FAIL.getText());
                if(b) {
                    returnMap.put("SystemOut", err.toString().replace("\n", ""));
                }else {
                    returnMap.put("SystemOut", "제한 시간 초과");
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            // Syetem out, error stream 원상태로 전환
            System.setOut(origOut);
            System.setErr(origErr);
        }
        return returnMap;
    }
}
