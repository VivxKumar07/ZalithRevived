package com.movtery.feature.mod.modloader;

import android.content.Context;
import android.widget.Toast;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

import java.util.List;

public class NeoForgeVersionListAdapter extends ModVersionListAdapter implements TaskCountListener {
    private boolean mTasksRunning;

    public NeoForgeVersionListAdapter(Context context, ModloaderListenerProxy modloaderListenerProxy, ModloaderDownloadListener listener, List<String> mData) {
        super(mData);
        ProgressKeeper.addTaskCountListener(this);
        setIconDrawable(R.drawable.ic_neoforge);
        setOnItemClickListener(version -> {
            if (mTasksRunning) {
                Toast.makeText(context, context.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                return;
            }

            modloaderListenerProxy.attachListener(listener);
            PojavApplication.sExecutorService.execute(() -> new NeoForgeDownloadTask(modloaderListenerProxy, (String) version).run());
        });
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        mTasksRunning = !(taskCount == 0);
    }
}
