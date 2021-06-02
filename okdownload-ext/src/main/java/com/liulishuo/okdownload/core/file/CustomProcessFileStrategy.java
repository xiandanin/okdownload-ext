package com.liulishuo.okdownload.core.file;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.breakpoint.DownloadStore;

/**
 * 来自 @see <a href="https://github.com/lingochamp/okdownload/issues/415">okdownload#415</a>
 * 用于解决 The current offset on block-info isn't update correct, 58953018 != 69212754 on 0
 */
public class CustomProcessFileStrategy extends ProcessFileStrategy {

    @Override
    public MultiPointOutputStream createProcessStream(DownloadTask task, BreakpointInfo info, DownloadStore store) {
        return new CustomMultiPointOutputStream(task, info, store);
    }
}