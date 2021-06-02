package com.liulishuo.okdownload.core.file;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.breakpoint.DownloadStore;

import java.io.IOException;

/**
 * 来自 @see <a href="https://github.com/lingochamp/okdownload/issues/415">okdownload#415</a>
 * 用于解决 The current offset on block-info isn't update correct, 58953018 != 69212754 on 0
 */
public class CustomMultiPointOutputStream extends MultiPointOutputStream {
    private static final String TAG = "CustomMultiPointOutputStream";
    private final DownloadTask task;

    CustomMultiPointOutputStream(final DownloadTask task, BreakpointInfo info, DownloadStore store, Runnable syncRunnable) {
        super(task, info, store, syncRunnable);
        this.task = task;
    }

    public CustomMultiPointOutputStream(DownloadTask task, BreakpointInfo info, DownloadStore store) {
        this(task, info, store, null);
    }

    @Override
    synchronized void close(int blockIndex) throws IOException {
        final DownloadOutputStream outputStream = outputStreamMap.get(blockIndex);
        if (outputStream != null) {
            outputStream.close();
            synchronized (noSyncLengthMap) {
                // make sure the length of noSyncLengthMap is equal to outputStreamMap
                outputStreamMap.remove(blockIndex);
                noSyncLengthMap.remove(blockIndex);
            }
            Util.d(TAG, "OutputStream close task[" + task.getId() + "] block[" + blockIndex + "]");
        }
    }
}