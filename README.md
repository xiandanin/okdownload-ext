## 快速解决
gradle引入`okdownload`、`sqlite`、`okdownload-ext`，无论是否需要`sqlite`
```
implementation 'com.liulishuo.okdownload:okdownload:1.0.7'
implementation 'com.liulishuo.okdownload:sqlite:1.0.7'
implementation 'in.xiandanin:okdownload-ext:1.0.7'
```

在`Application#onCrate`调用，注意此方法会替换现有配置
```
OkDownloadExt.fix415Bug(this);
```

在`taskEnd`使用`StatusUtil.isCompleted(task)`判断是否下载完成

## 问题详情
##### The current offset on block-info isn't update correct, 58953018 != 69212754 on 0

```
2021-06-01 21:40:31.206 9284-9284/ W/System.err: java.io.IOException: The current offset on block-info isn't update correct, 58953018 != 69212754 on 0
2021-06-01 21:40:31.206 9284-9284/ W/System.err:     at com.liulishuo.okdownload.core.file.MultiPointOutputStream.inspectComplete(MultiPointOutputStream.java:263)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at com.liulishuo.okdownload.core.interceptor.BreakpointInterceptor.interceptFetch(BreakpointInterceptor.java:123)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at com.liulishuo.okdownload.core.download.DownloadChain.processFetch(DownloadChain.java:220)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at com.liulishuo.okdownload.core.interceptor.RetryInterceptor.interceptFetch(RetryInterceptor.java:57)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at com.liulishuo.okdownload.core.download.DownloadChain.processFetch(DownloadChain.java:220)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at com.liulishuo.okdownload.core.download.DownloadChain.start(DownloadChain.java:195)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at com.liulishuo.okdownload.core.download.DownloadChain.run(DownloadChain.java:247)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:462)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
2021-06-01 21:40:31.207 9284-9284/ W/System.err:     at java.lang.Thread.run(Thread.java:919)
```

根据[okdownload#415](https://github.com/lingochamp/okdownload/issues/415#issuecomment-753313471)和[okdownload#425](https://github.com/lingochamp/okdownload/pull/425)，创建`CustomProcessFileStrategy`

```
package com.liulishuo.okdownload.core.file

import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.breakpoint.DownloadStore
import java.io.IOException

class CustomProcessFileStrategy : ProcessFileStrategy() {
    override fun createProcessStream(task: DownloadTask,
                                     info: BreakpointInfo,
                                     store: DownloadStore): MultiPointOutputStream {
        return CustomMultiPointOutputStream(task, info, store)
    }


    class CustomMultiPointOutputStream : MultiPointOutputStream {
        private var task: DownloadTask? = null

        constructor(task: DownloadTask, info: BreakpointInfo, store: DownloadStore, syncRunnable: Runnable?) : super(task, info, store, syncRunnable){
            this.task = task
        }

        constructor(task: DownloadTask, info: BreakpointInfo, store: DownloadStore) : super(task, info, store, null){
            this.task = task
        }

        @Synchronized
        @Throws(IOException::class)
        override fun close(blockIndex: Int) {
            val outputStream = outputStreamMap[blockIndex]
            if (outputStream != null) {
                outputStream.close()
                synchronized(noSyncLengthMap) {
                    // make sure the length of noSyncLengthMap is equal to outputStreamMap
                    outputStreamMap.remove(blockIndex)
                    noSyncLengthMap.remove(blockIndex)
                }
            }
        }
    }
}
```

`Application.onCreate`时设置到OkDownload实例

```
OkDownload.setSingletonInstance(
        new OkDownload.Builder(getApplicationContext())
                .processFileStrategy(new CustomProcessFileStrategy())
                .build());
```

##### 实际未下载完成，但在`taskEnd`回调`COMPLETED`

在`taskEnd`中使用`StatusUtil.isCompleted`判断是否下载完成
```
StatusUtil.isCompleted(task)
```

##### 实际下载成功，但`StatusUtil.isCompletedOrUnknown`返回`UNKNOWN`，导致`StatusUtil.isCompleted`返回false
根据[okdownload#244](https://github.com/lingochamp/okdownload/issues/244)，再引入sqlite库
```
implementation 'com.liulishuo.okdownload:sqlite:1.0.7'
```
