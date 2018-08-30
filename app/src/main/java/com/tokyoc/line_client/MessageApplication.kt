package com.tokyoc.line_client

import android.app.Application
import io.realm.Realm

// Applicationクラスはアプリケーション起動時に処理を実行する
// ここではRealmを初期化
class MessageApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}