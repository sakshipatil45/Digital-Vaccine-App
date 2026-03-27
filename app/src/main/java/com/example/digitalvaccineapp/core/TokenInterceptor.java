package com.example.digitalvaccineapp.core;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return chain.proceed(originalRequest);
        }

        try {
            // Wait for the token task to complete synchronously in the background thread
            Task<GetTokenResult> tokenTask = user.getIdToken(false);
            GetTokenResult result = Tasks.await(tokenTask);
            String token = result.getToken();

            if (token != null) {
                Request newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .method(originalRequest.method(), originalRequest.body())
                        .build();
                return chain.proceed(newRequest);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return chain.proceed(originalRequest);
    }
}
