package com.liulishuo.okdownload;

import android.content.Context;

import com.liulishuo.okdownload.core.file.CustomProcessFileStrategy;


/**
 * 用于快速解决使用okdownload时的一些问题
 * 解决办法来自issues
 * @see <a href="https://github.com/xiandanin/okdownload-ext">xiandanin/okdownload-ext</a>
 */
public final class OkDownloadExt {
    public static void fix415Bug(Context context) {
        OkDownload.setSingletonInstance(
                new OkDownload.Builder(context)
                        .processFileStrategy(new CustomProcessFileStrategy())
                        .build()
        );
    }
}
