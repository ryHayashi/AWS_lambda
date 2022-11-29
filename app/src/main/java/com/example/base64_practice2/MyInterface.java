package com.example.base64_practice2;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;
import java.util.List;

public interface MyInterface {

    /**
     * Invoke the Lambda function "AndroidBackendLambdaFunction".
     * The function name is the method name.
     */
    @LambdaFunction
    String ctx19team8_toEC2s3(RequestClass request);



}