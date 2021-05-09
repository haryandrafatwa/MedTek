package com.example.medtek.model.state;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * State Type pada saat login
 */

@IntDef({
        ApplyStateType.State_After_login,
        ApplyStateType.State_Finish_Login
})

@Retention(RetentionPolicy.SOURCE)
public @interface ApplyStateType {
    int State_None = 0;

    // state setelah login
    int State_After_login = 1000;

    // state ketika sudah pernah login
    int State_Finish_Login = 1001;
}
