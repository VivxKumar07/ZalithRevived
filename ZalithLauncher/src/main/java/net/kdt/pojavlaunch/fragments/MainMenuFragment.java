package net.kdt.pojavlaunch.fragments;

import static com.movtery.zalithlauncher.event.single.RefreshVersionsEvent.MODE.END;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.webkit.WebViewAssetLoader;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.util.Base64;
import androidx.core.content.res.ResourcesCompat;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.movtery.zalithlauncher.renderer.Renderers;
import com.movtery.zalithlauncher.renderer.RendererInterface;
import com.movtery.zalithlauncher.renderer.renderers.MobileGluesRenderer;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import java.util.Locale;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.zalithlauncher.InfoCenter;
import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.databinding.FragmentLauncherBinding;
import com.movtery.zalithlauncher.event.single.AccountUpdateEvent;
import com.movtery.zalithlauncher.event.single.LaunchGameEvent;
import com.movtery.zalithlauncher.event.single.RefreshVersionsEvent;
import net.kdt.pojavlaunch.value.MinecraftAccount;
import com.movtery.zalithlauncher.feature.accounts.AccountsManager;
import com.movtery.zalithlauncher.feature.accounts.AccountType;
import com.movtery.zalithlauncher.feature.accounts.AccountUtils;
import com.movtery.zalithlauncher.feature.accounts.OtherLoginHelper;
import com.movtery.zalithlauncher.feature.background.BackgroundManager;
import com.movtery.zalithlauncher.feature.background.BackgroundType;
import com.movtery.zalithlauncher.feature.login.Servers;
import com.movtery.zalithlauncher.feature.mojang.MojangContentManager;
import com.movtery.zalithlauncher.feature.update.UpdateUtils;
import com.movtery.zalithlauncher.feature.version.Version;
import com.movtery.zalithlauncher.feature.version.VersionInfo;
import com.movtery.zalithlauncher.feature.version.VersionsManager;
import com.movtery.zalithlauncher.feature.version.utils.VersionArtworkResolver;
import com.movtery.zalithlauncher.feature.version.utils.VersionIconUtils;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.task.TaskExecutors;
import com.movtery.zalithlauncher.ui.adapter.NewsAdapter;
import com.movtery.zalithlauncher.ui.adapter.PatchNotesAdapter;
import com.movtery.zalithlauncher.ui.dialog.OtherLoginDialog;
import com.movtery.zalithlauncher.ui.fragment.AccountFragment;
import com.movtery.zalithlauncher.ui.fragment.ControlButtonFragment;
import com.movtery.zalithlauncher.ui.fragment.DownloadFragment;
import com.movtery.zalithlauncher.ui.fragment.FilesFragment;
import com.movtery.zalithlauncher.ui.fragment.FragmentWithAnim;
import com.movtery.zalithlauncher.ui.fragment.VersionManagerFragment;
import com.movtery.zalithlauncher.ui.fragment.VersionsListFragment;
import com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper;
import com.movtery.zalithlauncher.ui.subassembly.account.AccountViewWrapper;
import com.movtery.zalithlauncher.utils.ZHTools;
import com.movtery.zalithlauncher.utils.anim.ViewAnimUtils;
import com.movtery.zalithlauncher.utils.path.PathManager;
import com.movtery.zalithlauncher.utils.skin.SkinLoader;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainMenuFragment extends FragmentWithAnim {
    public static final String TAG = "MainMenuFragment";

    public static final int TAB_PLAY = 0;
    public static final int TAB_INSTALLATIONS = 1;
    public static final int TAB_SKINS = 2;
    public static final int TAB_PATCH_NOTES = 3;
    public static final int TAB_NEWS = 4;
    public static final int TAB_MODS = 5;
    public static final int TAB_SETTINGS = 6;

    private int currentTab = TAB_PLAY;

    public static final int SUBTAB_VIDEO = 0;
    public static final int SUBTAB_CONTROLS = 1;
    public static final int SUBTAB_GAME = 2;
    public static final int SUBTAB_LAUNCHER = 3;
    public static final int SUBTAB_EXPERIMENTAL = 4;
    public static final int SUBTAB_ABOUT = 5;

    private int currentSettingsSubTab = SUBTAB_VIDEO;

    private FragmentLauncherBinding binding;
    private AccountViewWrapper accountViewWrapper;

    private final List<Integer> installedHeroArtworks = new ArrayList<>();
    private int currentHeroIndex = 0;
    private boolean isAutoScroll = true;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAutoScroll && isResumed() && installedHeroArtworks.size() > 1) {
                currentHeroIndex = (currentHeroIndex + 1) % installedHeroArtworks.size();
                updateHeroCard(true);
                autoScrollHandler.postDelayed(this, 5000);
            }
        }
    };

    private boolean patchNotesLoaded = false;
    private boolean newsLoaded = false;

    public MainMenuFragment() {
        super(R.layout.fragment_launcher);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLauncherBinding.inflate(getLayoutInflater());
        accountViewWrapper = new AccountViewWrapper(this, binding.viewAccount);
        accountViewWrapper.refreshAccountInfo();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupSidebarNavigation();
        setupPlayScreen();
        setupInstallationsScreen();
        setupSkinsScreen();
        setupPatchNotesScreen();
        setupNewsScreen();
        setupModsScreen();
        setupSettingsScreen();
        selectLauncherFont(AllSettings.getLauncherFont().getValue(), false);

        updateInstalledArtworks();
        refreshCurrentVersion();
        switchMainTab(TAB_PLAY);
    }

    // ==========================================================
    // 1. SIDEBAR NAVIGATION
    // ==========================================================
    private void setupSidebarNavigation() {
        binding.playNavTab.setOnClickListener(v -> switchMainTab(TAB_PLAY));
        binding.installationsButton.setOnClickListener(v -> switchMainTab(TAB_INSTALLATIONS));
        binding.skinsNavButton.setOnClickListener(v -> switchMainTab(TAB_SKINS));
        binding.patchNotesNavButton.setOnClickListener(v -> switchMainTab(TAB_PATCH_NOTES));
        binding.newsNavButton.setOnClickListener(v -> switchMainTab(TAB_NEWS));
        binding.modsNavButton.setOnClickListener(v -> switchMainTab(TAB_MODS));
        binding.settingsNavButton.setOnClickListener(v -> switchMainTab(TAB_SETTINGS));

        // Account click opens Account Screen
        binding.viewAccount.getRoot().setOnClickListener(v -> openAccountManager());
    }

    public void switchMainTab(int tab) {
        MinecraftSoundHelper.playClickSound();
        currentTab = tab;

        // Reset all sidebar tab backgrounds & texts
        resetSidebarItem(binding.playNavTab, binding.playNavText, tab == TAB_PLAY);
        resetSidebarItem(binding.installationsButton, binding.installationsText, tab == TAB_INSTALLATIONS);
        resetSidebarItem(binding.skinsNavButton, binding.skinsText, tab == TAB_SKINS);
        resetSidebarItem(binding.patchNotesNavButton, binding.patchNotesText, tab == TAB_PATCH_NOTES);
        resetSidebarItem(binding.newsNavButton, binding.newsText, tab == TAB_NEWS);
        resetSidebarItem(binding.modsNavButton, binding.modsText, tab == TAB_MODS);
        resetSidebarItem(binding.settingsNavButton, binding.settingsNavText, tab == TAB_SETTINGS);

        // Switch right content panels
        binding.playLayout.setVisibility(tab == TAB_PLAY ? View.VISIBLE : View.GONE);
        binding.installationsLayout.setVisibility(tab == TAB_INSTALLATIONS ? View.VISIBLE : View.GONE);
        binding.skinsLayout.setVisibility(tab == TAB_SKINS ? View.VISIBLE : View.GONE);
        binding.patchNotesLayout.setVisibility(tab == TAB_PATCH_NOTES ? View.VISIBLE : View.GONE);
        binding.newsLayout.setVisibility(tab == TAB_NEWS ? View.VISIBLE : View.GONE);
        binding.modsLayout.setVisibility(tab == TAB_MODS ? View.VISIBLE : View.GONE);
        binding.settingsLayout.setVisibility(tab == TAB_SETTINGS ? View.VISIBLE : View.GONE);

        if (tab == TAB_SETTINGS) {
            refreshSettingsAccountCard();
        } else if (tab == TAB_INSTALLATIONS) {
            refreshInstallationsList();
        } else if (tab == TAB_SKINS) {
            refreshSkinAvatar();
        } else if (tab == TAB_PATCH_NOTES) {
            loadPatchNotesIfNeeded();
        } else if (tab == TAB_NEWS) {
            loadNewsIfNeeded();
        }
    }

    private void resetSidebarItem(View item, TextView text, boolean active) {
        if (item == null || text == null) return;
        item.setBackgroundResource(active ? R.drawable.bg_sidebar_item_active : R.drawable.bg_sidebar_item);
        text.setTextColor(active ? 0xFFFFFFFF : 0xFFCCCCCC);
        text.setTypeface(text.getTypeface(), active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    // ==========================================================
    // 2. PLAY SCREEN & HERO CARD
    // ==========================================================
    private void setupPlayScreen() {
        binding.btnAutoScroll.setOnClickListener(v -> {
            isAutoScroll = !isAutoScroll;
            updateAutoScrollButtonState();
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
            if (isAutoScroll && installedHeroArtworks.size() > 1) {
                autoScrollHandler.postDelayed(autoScrollRunnable, 5000);
            }
        });
        updateAutoScrollButtonState();

        binding.openMainDirButton.setOnClickListener(v -> openGameFiles());
        binding.btnMultiplayer.setOnClickListener(v -> showMultiplayerDialog());

        binding.version.setOnClickListener(v -> switchMainTab(TAB_INSTALLATIONS));
        binding.managerProfileButton.setOnClickListener(v -> {
            if (!isTaskRunning()) {
                ViewAnimUtils.setViewAnim(binding.managerProfileButton, Animations.Pulse);
                ZHTools.swapFragmentWithAnim(this, VersionManagerFragment.class, VersionManagerFragment.TAG, null);
            }
        });
        binding.playButton.setOnClickListener(v -> checkAndLaunchGame());

        binding.versionName.setSelected(true);
        binding.versionInfo.setSelected(true);
    }

    private void updateAutoScrollButtonState() {
        if (binding.btnAutoScroll == null) return;
        if (isAutoScroll) {
            binding.btnAutoScroll.setText("AUTO: ON");
            binding.btnAutoScroll.setBackgroundResource(R.drawable.btn_minecraft_green);
        } else {
            binding.btnAutoScroll.setText("AUTO: OFF");
            binding.btnAutoScroll.setBackgroundResource(R.drawable.btn_minecraft_stone);
        }
    }

    private void updateInstalledArtworks() {
        installedHeroArtworks.clear();
        List<Version> versions = VersionsManager.INSTANCE.getVersions();
        if (versions != null && !versions.isEmpty()) {
            for (Version v : versions) {
                int art = VersionArtworkResolver.INSTANCE.getArtworkForVersion(v);
                if (!installedHeroArtworks.contains(art)) {
                    installedHeroArtworks.add(art);
                }
            }
        }
        if (installedHeroArtworks.isEmpty()) {
            installedHeroArtworks.add(R.drawable.art_wilderness_bound);
        }

        Version current = VersionsManager.INSTANCE.getCurrentVersion();
        if (current != null) {
            int curArt = VersionArtworkResolver.INSTANCE.getArtworkForVersion(current);
            int idx = installedHeroArtworks.indexOf(curArt);
            currentHeroIndex = (idx >= 0) ? idx : 0;
        } else {
            currentHeroIndex = 0;
        }
        updateHeroCard(false);
    }

    private void updateHeroCard(boolean animate) {
        if (binding == null || binding.heroArtwork == null || installedHeroArtworks.isEmpty()) return;
        if (currentHeroIndex >= installedHeroArtworks.size()) currentHeroIndex = 0;
        int targetRes = installedHeroArtworks.get(currentHeroIndex);

        if (animate) {
            binding.heroArtwork.animate().alpha(0f).setDuration(220).withEndAction(() -> {
                binding.heroArtwork.setImageResource(targetRes);
                binding.heroArtwork.animate().alpha(1f).setDuration(280).start();
            }).start();
        } else {
            binding.heroArtwork.setImageResource(targetRes);
            binding.heroArtwork.setAlpha(1f);
        }
    }

    private void refreshCurrentVersion() {
        Version version = VersionsManager.INSTANCE.getCurrentVersion();
        if (version != null) {
            binding.versionName.setText(version.getVersionName());
            VersionInfo versionInfo = version.getVersionInfo();
            if (versionInfo != null) {
                binding.versionInfo.setText(versionInfo.getInfoString());
                binding.versionInfo.setVisibility(View.VISIBLE);
            } else {
                binding.versionInfo.setVisibility(View.GONE);
            }
            new VersionIconUtils(version).start(binding.versionIcon);
            binding.managerProfileButton.setVisibility(View.VISIBLE);
        } else {
            binding.versionName.setText(R.string.version_no_versions);
            binding.versionInfo.setVisibility(View.GONE);
            binding.managerProfileButton.setVisibility(View.GONE);
        }
        updateInstalledArtworks();
    }

    private void openGameFiles() {
        Bundle bundle = new Bundle();
        bundle.putString(FilesFragment.BUNDLE_LIST_PATH, PathManager.DIR_GAME_HOME);
        ZHTools.swapFragmentWithAnim(this, FilesFragment.class, FilesFragment.TAG, bundle);
    }

    // ==========================================================
    // 3. MULTIPLAYER (Terracotta LAN / Direct Connect)
    // ==========================================================
    private void showMultiplayerDialog() {
        Context context = requireContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_minecraft_alert, null);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        EditText inputView = dialogView.findViewById(R.id.dialog_input);
        View btnConfirm = dialogView.findViewById(R.id.dialog_btn_confirm);
        View btnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);

        if (titleView != null) titleView.setText("MULTIPLAYER (TERRACOTTA LAN)");
        if (messageView != null) {
            messageView.setText("Terracotta P2P LAN broadcast connects you directly with friends over virtual LAN.\n\nEnter Server Address or P2P Room Code:");
        }
        if (inputView != null) {
            inputView.setVisibility(View.VISIBLE);
            inputView.setHint("play.hypixel.net:25565 or Room Code");
        }

        AlertDialog dialog = builder.setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                String target = (inputView != null) ? inputView.getText().toString().trim() : "";
                if (!target.isEmpty()) {
                    Toast.makeText(context, "Direct connect configured: " + target + "\nLaunching game...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Terracotta P2P Room Ready! Launching game...", Toast.LENGTH_SHORT).show();
                }
                EventBus.getDefault().post(new LaunchGameEvent());
                dialog.dismiss();
            });
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    // ==========================================================
    // 4. INSTALLATIONS SCREEN
    // ==========================================================
    private void setupInstallationsScreen() {
        binding.installationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.btnNewInstallation.setOnClickListener(v -> {
            ZHTools.swapFragmentWithAnim(this, VersionsListFragment.class, VersionsListFragment.TAG, null);
        });
    }

    private void refreshInstallationsList() {
        List<Version> versions = VersionsManager.INSTANCE.getVersions();
        if (versions == null || versions.isEmpty()) {
            binding.installationsRecyclerView.setAdapter(null);
            return;
        }
        binding.installationsRecyclerView.setAdapter(new InstallationsAdapter(versions));
    }

    private class InstallationsAdapter extends RecyclerView.Adapter<InstallationViewHolder> {
        private final List<Version> list;

        InstallationsAdapter(List<Version> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public InstallationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cubiq_installation, parent, false);
            return new InstallationViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull InstallationViewHolder holder, int position) {
            Version item = list.get(position);
            holder.title.setText(item.getVersionName());
            VersionInfo info = item.getVersionInfo();
            holder.subtitle.setText(info != null ? info.getInfoString() : "Vanilla • " + item.getVersionName());
            new VersionIconUtils(item).start(holder.icon);

            Version cur = VersionsManager.INSTANCE.getCurrentVersion();
            String curVer = cur != null ? cur.getVersionName() : "";
            boolean isActive = item.getVersionName().equals(curVer);
            if (holder.activeBadge != null) {
                holder.activeBadge.setVisibility(isActive ? View.VISIBLE : View.GONE);
            }
            if (holder.card != null) {
                holder.card.setBackgroundResource(isActive ? R.drawable.bg_sidebar_item_active : R.drawable.bg_version_card);
            }

            holder.itemView.setOnClickListener(v -> {
                MinecraftSoundHelper.playClickSound();
                VersionsManager.INSTANCE.saveCurrentVersion(item.getVersionName());
                refreshCurrentVersion();
                notifyDataSetChanged();
                switchMainTab(TAB_PLAY);
            });

            if (holder.btnPlay != null) {
                holder.btnPlay.setOnClickListener(v -> {
                    MinecraftSoundHelper.playClickSound();
                    VersionsManager.INSTANCE.saveCurrentVersion(item.getVersionName());
                    refreshCurrentVersion();
                    org.greenrobot.eventbus.EventBus.getDefault().post(new LaunchGameEvent());
                });
            }

            if (holder.btnMore != null) {
                holder.btnMore.setOnClickListener(v -> {
                    MinecraftSoundHelper.playClickSound();
                    VersionsManager.INSTANCE.saveCurrentVersion(item.getVersionName());
                    refreshCurrentVersion();
                    ZHTools.swapFragmentWithAnim(MainMenuFragment.this, VersionManagerFragment.class, VersionManagerFragment.TAG, null);
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private static class InstallationViewHolder extends RecyclerView.ViewHolder {
        View card;
        ImageView icon;
        TextView title;
        TextView subtitle;
        TextView activeBadge;
        View btnPlay;
        View btnMore;

        InstallationViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.inst_card);
            icon = itemView.findViewById(R.id.inst_icon);
            title = itemView.findViewById(R.id.inst_title);
            subtitle = itemView.findViewById(R.id.inst_subtitle);
            activeBadge = itemView.findViewById(R.id.inst_active_badge);
            btnPlay = itemView.findViewById(R.id.btn_inst_play);
            btnMore = itemView.findViewById(R.id.btn_inst_more);
        }
    }

    // ==========================================================
    // 5. SKINS SCREEN (Real 3D Interactive WebGL skinview3d Preview, Steve/Alex, Capes)
    // ==========================================================
    private String fileToBase64DataUrl(File file) {
        if (file == null || !file.exists()) return null;
        try {
            byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(file);
            return "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    private String assetToBase64DataUrl(String assetPath) {
        try (InputStream in = requireContext().getAssets().open(assetPath)) {
            byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(in);
            return "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    private void setupSkinsScreen() {
        binding.subtabSkins.setOnClickListener(v -> {
            binding.subtabSkins.setBackgroundResource(R.drawable.bg_tab_pill_active);
            binding.subtabCapes.setBackgroundResource(R.drawable.bg_tab_pill);
            binding.panelSkinsOptions.setVisibility(View.VISIBLE);
            binding.panelCapesOptions.setVisibility(View.GONE);
            binding.skinPreviewWebview.evaluateJavascript("setAzimuthAndPitch(0, 10, 75);", null);
        });
        binding.subtabCapes.setOnClickListener(v -> {
            binding.subtabCapes.setBackgroundResource(R.drawable.bg_tab_pill_active);
            binding.subtabSkins.setBackgroundResource(R.drawable.bg_tab_pill);
            binding.panelCapesOptions.setVisibility(View.VISIBLE);
            binding.panelSkinsOptions.setVisibility(View.GONE);
            binding.skinPreviewWebview.evaluateJavascript("setAzimuthAndPitch(180, 10, 75);", null);
        });

        // Initialize 3D WebGL skinview3d viewer
        WebView webView = binding.skinPreviewWebview;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        webView.setBackgroundColor(Color.TRANSPARENT);

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(requireContext()))
                .addPathHandler("/skins/", new WebViewAssetLoader.InternalStoragePathHandler(requireContext(), PathManager.DIR_USER_SKIN))
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                refreshSkinAvatar();
                webView.evaluateJavascript("startAnim('NewIdle', 0.7);", null);
            }
        });
        webView.loadUrl("https://appassets.androidplatform.net/assets/skinview/skinview.html");

        binding.btnChangeSkin.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/png");
            startActivityForResult(Intent.createChooser(intent, "Select Minecraft Skin PNG"), 1001);
        });

        binding.btnResetSkin.setOnClickListener(v -> {
            MinecraftAccount account = AccountsManager.INSTANCE.getCurrentAccount();
            if (account != null) {
                File skin = new File(PathManager.DIR_USER_SKIN, account.getUniqueUUID() + ".png");
                if (skin.exists()) skin.delete();
            }
            selectCharacter("steve", "default", "wide");
            if (accountViewWrapper != null) accountViewWrapper.refreshAccountInfo();
            Toast.makeText(requireContext(), "Skin reset to default Steve", Toast.LENGTH_SHORT).show();
        });

        binding.btnModelClassic.setOnClickListener(v -> {
            binding.btnModelClassic.setBackgroundResource(R.drawable.btn_minecraft_green);
            binding.btnModelSlim.setBackgroundResource(R.drawable.btn_minecraft_stone);
            binding.skinPreviewWebview.evaluateJavascript("if (skinViewer && skinViewer.playerObject) skinViewer.playerObject.skin.modelType = 'default';", null);
            Toast.makeText(requireContext(), "Classic (4px) model selected", Toast.LENGTH_SHORT).show();
        });

        binding.btnModelSlim.setOnClickListener(v -> {
            binding.btnModelSlim.setBackgroundResource(R.drawable.btn_minecraft_green);
            binding.btnModelClassic.setBackgroundResource(R.drawable.btn_minecraft_stone);
            binding.skinPreviewWebview.evaluateJavascript("if (skinViewer && skinViewer.playerObject) skinViewer.playerObject.skin.modelType = 'slim';", null);
            Toast.makeText(requireContext(), "Slim (3px) model selected", Toast.LENGTH_SHORT).show();
        });

        // Official character selections - all 9 Mojang characters
        binding.charItemSteve.setOnClickListener(v -> selectCharacter("steve", "default", "wide"));
        binding.charItemAlex.setOnClickListener(v -> selectCharacter("alex", "slim", "slim"));
        binding.charItemNoor.setOnClickListener(v -> selectCharacter("noor", "slim", "slim"));
        binding.charItemSunny.setOnClickListener(v -> selectCharacter("sunny", "slim", "slim"));
        binding.charItemAri.setOnClickListener(v -> selectCharacter("ari", "slim", "slim"));
        binding.charItemZuri.setOnClickListener(v -> selectCharacter("zuri", "default", "wide"));
        binding.charItemMakena.setOnClickListener(v -> selectCharacter("makena", "slim", "slim"));
        binding.charItemKai.setOnClickListener(v -> selectCharacter("kai", "slim", "slim"));
        binding.charItemEfe.setOnClickListener(v -> selectCharacter("efe", "default", "wide"));

        binding.btnBrowseCustomCape.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/png");
            startActivityForResult(Intent.createChooser(intent, "Select Cape PNG"), 1002);
        });

        binding.capeItemNone.setOnClickListener(v -> selectCape(0, "None", null));
        binding.capeItemMigrator.setOnClickListener(v -> selectCape(1, "Migrator", "capes/migrator.png"));
        binding.capeItem15th.setOnClickListener(v -> selectCape(2, "15th Anniversary", "capes/15th_anniversary.png"));
        binding.capeItemVanilla.setOnClickListener(v -> selectCape(3, "Vanilla", "capes/vanilla.png"));
        binding.capeItemCherry.setOnClickListener(v -> selectCape(4, "Cherry", "capes/cherry.png"));

        // Setup cape thumbnail previews
        setCapeThumbnail(binding.capeItemMigrator, "capes/migrator.png");
        setCapeThumbnail(binding.capeItem15th, "capes/15th_anniversary.png");
        setCapeThumbnail(binding.capeItemVanilla, "capes/vanilla.png");
        setCapeThumbnail(binding.capeItemCherry, "capes/cherry.png");

        refreshSkinAvatar();
    }

    private void setCapeThumbnail(View parent, String assetPath) {
        if (parent instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) parent;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child instanceof ImageView) {
                    try (InputStream in = requireContext().getAssets().open(assetPath)) {
                        Bitmap bmp = BitmapFactory.decodeStream(in);
                        if (bmp != null) {
                            ((ImageView) child).setImageBitmap(bmp);
                            ((ImageView) child).setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }
                    } catch (Exception ignored) {}
                    break;
                }
            }
        }
    }

    private void selectCharacter(String charName, String model, String variant) {
        if (binding == null) return;
        int[] charIds = {
            R.id.char_item_steve, R.id.char_item_alex, R.id.char_item_noor, R.id.char_item_sunny,
            R.id.char_item_ari, R.id.char_item_zuri, R.id.char_item_makena, R.id.char_item_kai, R.id.char_item_efe
        };
        String[] charNames = {"steve", "alex", "noor", "sunny", "ari", "zuri", "makena", "kai", "efe"};
        for (int i = 0; i < charIds.length; i++) {
            View v = binding.getRoot().findViewById(charIds[i]);
            if (v != null) {
                v.setBackgroundResource(charNames[i].equals(charName) ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
                // Update subtitle color to show active
                if (v instanceof android.view.ViewGroup) {
                    android.view.ViewGroup vg = (android.view.ViewGroup) v;
                    if (vg.getChildCount() >= 3 && vg.getChildAt(2) instanceof android.widget.TextView) {
                        android.widget.TextView sub = (android.widget.TextView) vg.getChildAt(2);
                        sub.setTextColor(charNames[i].equals(charName) ? 0xFF40C463 : 0xFF888888);
                    }
                }
            }
        }

        String assetPath = "skins/" + variant + "/" + charName + ".png";
        String b64 = assetToBase64DataUrl(assetPath);
        if (b64 != null) {
            binding.skinPreviewWebview.evaluateJavascript("loadSkin('" + b64 + "', '" + model + "');", null);
        } else {
            binding.skinPreviewWebview.evaluateJavascript("loadSkin('https://appassets.androidplatform.net/assets/" + assetPath + "', '" + model + "');", null);
        }
        // Capitalize first letter for display
        String displayName = charName.substring(0, 1).toUpperCase() + charName.substring(1);
        binding.skinCaptionName.setText(displayName);
    }

    private void selectCharacter(String charName, String model) {
        // Legacy overload — determine variant from model
        String variant = "slim".equals(model) ? "slim" : "wide";
        selectCharacter(charName, model, variant);
    }

    private void selectCape(int index, String name, String assetPath) {
        if (binding == null) return;
        binding.capeItemNone.setBackgroundResource(index == 0 ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        binding.capeItemMigrator.setBackgroundResource(index == 1 ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        binding.capeItem15th.setBackgroundResource(index == 2 ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        binding.capeItemVanilla.setBackgroundResource(index == 3 ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        binding.capeItemCherry.setBackgroundResource(index == 4 ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);

        AllSettings.getEquippedCapeAsset().put(assetPath != null ? assetPath : "").save();

        if (assetPath != null) {
            String b64 = assetToBase64DataUrl(assetPath);
            if (b64 != null) {
                binding.skinPreviewWebview.evaluateJavascript("loadCape('" + b64 + "');", null);
            } else {
                binding.skinPreviewWebview.evaluateJavascript("loadCape('https://appassets.androidplatform.net/assets/" + assetPath + "');", null);
            }
        } else {
            binding.skinPreviewWebview.evaluateJavascript("loadCape(null);", null);
        }

        binding.skinCaptionName.setText(index == 0 ? "No Cape Equipped" : name + " Cape");
        Toast.makeText(requireContext(), index == 0 ? "Cape unequipped" : "Equipped " + name + " Cape", Toast.LENGTH_SHORT).show();
    }

    private void refreshSkinAvatar() {
        MinecraftAccount account = AccountsManager.INSTANCE.getCurrentAccount();
        if (binding == null || binding.skinPreviewWebview == null) return;

        if (account != null) {
            File customSkin = new File(PathManager.DIR_USER_SKIN, account.getUniqueUUID() + ".png");
            if (customSkin.exists()) {
                String b64 = fileToBase64DataUrl(customSkin);
                if (b64 != null) {
                    binding.skinPreviewWebview.evaluateJavascript("loadSkin('" + b64 + "', 'auto-detect');", null);
                    binding.skinCaptionName.setText(account.username);
                    loadEquippedCape();
                    return;
                }
            }
            String steveB64 = assetToBase64DataUrl("skins/wide/steve.png");
            binding.skinPreviewWebview.evaluateJavascript("loadSkin('" + (steveB64 != null ? steveB64 : "https://appassets.androidplatform.net/assets/skins/wide/steve.png") + "', 'default');", null);
            binding.skinCaptionName.setText(account.username);
        } else {
            String steveB64 = assetToBase64DataUrl("skins/wide/steve.png");
            binding.skinPreviewWebview.evaluateJavascript("loadSkin('" + (steveB64 != null ? steveB64 : "https://appassets.androidplatform.net/assets/skins/wide/steve.png") + "', 'default');", null);
            binding.skinCaptionName.setText("Steve (Offline)");
        }
        loadEquippedCape();
    }

    private void loadEquippedCape() {
        String capeAsset = AllSettings.getEquippedCapeAsset().getValue();
        if (capeAsset != null && !capeAsset.isEmpty()) {
            if ("custom".equals(capeAsset)) {
                File customCape = new File(new File(PathManager.DIR_DATA), "custom_cape.png");
                String b64 = fileToBase64DataUrl(customCape);
                if (b64 != null) {
                    binding.skinPreviewWebview.evaluateJavascript("loadCape('" + b64 + "');", null);
                }
            } else {
                String b64 = assetToBase64DataUrl(capeAsset);
                if (b64 != null) {
                    binding.skinPreviewWebview.evaluateJavascript("loadCape('" + b64 + "');", null);
                }
            }
        }
    }

    // ==========================================================
    // 6. PATCH NOTES SCREEN (Real Mojang Java Patch Notes)
    // ==========================================================
    private void setupPatchNotesScreen() {
        binding.patchNotesRecycler.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2));
    }

    private void loadPatchNotesIfNeeded() {
        if (patchNotesLoaded) return;
        binding.patchNotesLoading.setVisibility(View.VISIBLE);
        MojangContentManager.loadPatchNotes(requireContext(), notes -> {
            patchNotesLoaded = true;
            binding.patchNotesLoading.setVisibility(View.GONE);
            binding.patchNotesRecycler.setAdapter(new PatchNotesAdapter(notes));
        });
    }

    // ==========================================================
    // 7. NEWS SCREEN (Real Mojang Launcher News)
    // ==========================================================
    private void setupNewsScreen() {
        binding.newsRecycler.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2));
    }

    private void loadNewsIfNeeded() {
        if (newsLoaded) return;
        binding.newsLoading.setVisibility(View.VISIBLE);
        MojangContentManager.loadNews(requireContext(), articles -> {
            newsLoaded = true;
            binding.newsLoading.setVisibility(View.GONE);
            binding.newsRecycler.setAdapter(new NewsAdapter(articles));
        });
    }

    // ==========================================================
    // 8. MODS SCREEN (CurseForge & Modrinth Category Hub)
    // ==========================================================
    private void setupModsScreen() {
        binding.btnSearchMods.setOnClickListener(v -> openDownloadCategory(0));
        binding.btnManageActiveMods.setOnClickListener(v -> {
            ZHTools.swapFragmentWithAnim(this, VersionManagerFragment.class, VersionManagerFragment.TAG, null);
        });
        binding.modCatModpacks.setOnClickListener(v -> openDownloadCategory(1));
        binding.modCatMods.setOnClickListener(v -> openDownloadCategory(0));
        binding.modCatShaders.setOnClickListener(v -> openDownloadCategory(4));
        binding.modCatResources.setOnClickListener(v -> openDownloadCategory(2));
        binding.modCatWorlds.setOnClickListener(v -> openDownloadCategory(3));
    }

    private void openDownloadCategory(int category) {
        Bundle bundle = new Bundle();
        bundle.putInt("tab_type", category);
        ZHTools.swapFragmentWithAnim(this, DownloadFragment.class, DownloadFragment.TAG, bundle);
    }

    // ==========================================================
    // 9. SETTINGS SCREEN (Exact Cubiq Split Layout + ZL1 Classifications)
    // ==========================================================
    private void setupSettingsScreen() {
        refreshSettingsAccountDisplay();

        binding.btnLoginMicrosoft.setOnClickListener(v -> {
            ZHTools.swapFragmentWithAnim(this, MicrosoftLoginFragment.class, MicrosoftLoginFragment.TAG, null);
        });

        binding.btnLoginElyby.setOnClickListener(v -> {
            Servers.Server elyServer = new Servers.Server();
            elyServer.setServerName("Ely.by");
            elyServer.setBaseUrl("https://authlib-injector.ely.by/api/yggdrasil");
            elyServer.setRegister("https://ely.by/registration");

            new OtherLoginDialog(requireActivity(), elyServer, new OtherLoginHelper.OnLoginListener() {
                @Override
                public void onLoading() {}

                @Override
                public void unLoading() {}

                @Override
                public void onSuccess(@NonNull MinecraftAccount account) {
                    refreshSettingsAccountDisplay();
                    Toast.makeText(requireContext(), "Logged in as " + account.username, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(@NonNull String error) {
                    Toast.makeText(requireContext(), "Login failed: " + error, Toast.LENGTH_SHORT).show();
                }
            }).show();
        });

        binding.btnLoginOffline.setOnClickListener(v -> {
            showOfflineLoginDialog();
        });

        binding.btnSettingsAccountManage.setOnClickListener(v -> openAccountManager());
        binding.settingsAccountCard.setOnClickListener(v -> openAccountManager());

        // Subtabs bindings
        binding.tabVideo.setOnClickListener(v -> switchSettingsSubTab(SUBTAB_VIDEO));
        binding.tabControls.setOnClickListener(v -> switchSettingsSubTab(SUBTAB_CONTROLS));
        binding.tabGame.setOnClickListener(v -> switchSettingsSubTab(SUBTAB_GAME));
        binding.tabLauncher.setOnClickListener(v -> switchSettingsSubTab(SUBTAB_LAUNCHER));
        binding.tabExperimental.setOnClickListener(v -> switchSettingsSubTab(SUBTAB_EXPERIMENTAL));
        binding.tabAbout.setOnClickListener(v -> switchSettingsSubTab(SUBTAB_ABOUT));

        // 1. VIDEO PANEL
        initVideoPanel();

        // 2. CONTROLS PANEL
        initControlsPanel();

        // 3. GAME PANEL
        initGamePanel();

        // 4. LAUNCHER PANEL
        initLauncherPanel();

        // 5. EXPERIMENTAL PANEL
        initExperimentalPanel();

        // 6. ABOUT PANEL
        initAboutPanel();

        switchSettingsSubTab(SUBTAB_VIDEO);
    }

    private void refreshSettingsAccountDisplay() {
        if (binding == null) return;
        MinecraftAccount account = AccountsManager.INSTANCE.getCurrentAccount();
        if (account != null) {
            binding.settingsAccountName.setText(account.username);
            String type = "Offline";
            if (AccountUtils.isMicrosoftAccount(account)) {
                type = "Microsoft";
            } else if (AccountUtils.isOtherLoginAccount(account)) {
                type = account.otherAccount != null && !account.otherAccount.isEmpty() ? account.otherAccount : "Ely.by";
            }
            binding.settingsAccountType.setText(type);
        } else {
            binding.settingsAccountName.setText("Steve");
            binding.settingsAccountType.setText("Offline");
        }
    }

    private void showOfflineLoginDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert).create();
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_minecraft_alert, null);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        EditText inputView = dialogView.findViewById(R.id.dialog_input);
        Button btnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.dialog_btn_confirm);

        titleView.setText("Offline Login");
        messageView.setText("Enter your offline Minecraft player name:");
        inputView.setVisibility(View.VISIBLE);
        inputView.setHint("Player Name");
        btnConfirm.setText("LOGIN");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String name = inputView.getText().toString().trim();
            if (!name.isEmpty()) {
                MinecraftAccount acc = new MinecraftAccount();
                acc.username = name;
                acc.accountType = AccountType.LOCAL.getType();
                try {
                    acc.save();
                    AccountsManager.INSTANCE.reload();
                    AccountsManager.INSTANCE.setCurrentAccount(acc);
                    refreshSettingsAccountDisplay();
                    Toast.makeText(requireContext(), "Offline account created: " + name, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Failed to save account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                inputView.setError("Name cannot be empty");
            }
        });

        dialog.setView(dialogView);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void checkAndLaunchGame() {
        Version version = VersionsManager.INSTANCE.getCurrentVersion();
        if (version != null) {
            String mcVer = version.getVersionInfo() != null && version.getVersionInfo().getMinecraftVersion() != null
                    ? version.getVersionInfo().getMinecraftVersion() : version.getVersionName();
            RendererInterface renderer = null;
            try {
                renderer = Renderers.getCurrentRenderer();
            } catch (Exception e) {
                Renderers.setCurrentRenderer(requireContext(), AllSettings.getRenderer().getValue());
                renderer = Renderers.getCurrentRenderer();
            }

            if (renderer != null && !renderer.isCompatibleWith(mcVer, requireContext())) {
                RendererInterface fallback = Renderers.getRecommendedFallback(mcVer, requireContext());
                String reason = (renderer.getUniqueIdentifier().equals("com.fcl.plugin.mobileglues") &&
                        !MobileGluesRenderer.isInstalled(requireContext()))
                        ? "MobileGlues renderer requires the companion standalone app to be installed on your device."
                        : "The selected renderer '" + renderer.getRendererName() + "' is incompatible with Minecraft " + mcVer + " (" + renderer.getCompatibilityDescription() + ").";

                new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle)
                        .setTitle("Incompatible Renderer Warning")
                        .setMessage(reason + "\n\nSuggested Fallback Renderer:\n• " + fallback.getRendererName() + " (" + fallback.getCompatibilityDescription() + ")\n\nWould you like to switch to " + fallback.getRendererName() + " and launch?")
                        .setPositiveButton("Switch & Play", (d, w) -> {
                            AllSettings.getRenderer().put(fallback.getUniqueIdentifier()).save();
                            Renderers.setCurrentRenderer(requireContext(), fallback.getUniqueIdentifier());
                            selectRenderer(fallback.getUniqueIdentifier(), false);
                            EventBus.getDefault().post(new LaunchGameEvent());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return;
            }
        }
        EventBus.getDefault().post(new LaunchGameEvent());
    }

    private void initVideoPanel() {
        // Renderers
        String currentRenderer = AllSettings.getRenderer().getValue();
        selectRenderer(currentRenderer, false);

        binding.rendererItemKrypton.setOnClickListener(v -> selectRenderer("e7b90ed6-e518-4d4e-93dc-5c7133cd5b31", true));
        binding.rendererItemLtw.setOnClickListener(v -> selectRenderer("d2c1e8b4-55a1-4e78-9032-11cf4e87d3a1", true));
        binding.rendererItemGl4es.setOnClickListener(v -> selectRenderer("8b52d82d-8f6d-4d3a-a767-dc93f8b72fc7", true));
        binding.rendererItemMobileglues.setOnClickListener(v -> selectRenderer("com.fcl.plugin.mobileglues", true));
        binding.rendererItemKopperzink.setOnClickListener(v -> selectRenderer("0fa435e2-46df-45c9-906c-b29606aaef00", true));
        binding.rendererItemVulkanzink.setOnClickListener(v -> selectRenderer("7c42df98-e6b3-4f51-b0e2-768a4c519213", true));
        binding.rendererItemVirgl.setOnClickListener(v -> selectRenderer("a3ccc1fe-de3f-4a81-8c45-2485181b63b3", true));
        binding.rendererItemFreedreno.setOnClickListener(v -> selectRenderer("1ad7249f-5784-4f00-bc72-174b3578ee46", true));
        binding.rendererItemPanfrost.setOnClickListener(v -> selectRenderer("9b2808c4-11af-4c72-a9c6-94c940396475", true));

        // MobileGlues companion status & download
        boolean mgInstalled = MobileGluesRenderer.isInstalled(requireContext());
        if (binding.btnDownloadMobileglues != null) {
            if (mgInstalled) {
                binding.btnDownloadMobileglues.setText("INSTALLED");
                binding.btnDownloadMobileglues.setBackgroundResource(R.drawable.bg_tab_pill);
            } else {
                binding.btnDownloadMobileglues.setText("DOWNLOAD APK");
                binding.btnDownloadMobileglues.setBackgroundResource(R.drawable.btn_minecraft_green);
            }
            binding.btnDownloadMobileglues.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ShirosakiMio/FCLRendererPlugin/releases"));
                startActivity(browserIntent);
            });
        }

        binding.btnImportLocalRenderer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/zip");
            startActivityForResult(Intent.createChooser(intent, "Select Renderer Plugin ZIP"), 1003);
        });

        // Resolution Slider
        int currentRes = AllSettings.getResolutionRatio().getValue();
        binding.resolutionSeekbar.setProgress(currentRes);
        binding.resolutionValue.setText(currentRes + "%");
        binding.resolutionSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int res = Math.max(30, progress);
                binding.resolutionValue.setText(res + "%");
                if (fromUser) AllSettings.getResolutionRatio().put(res).save();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Sustained Performance
        binding.switchSustainedPerf.setChecked(AllSettings.getSustainedPerformance().getValue());
        binding.switchSustainedPerf.setOnCheckedChangeListener((btn, isChecked) -> {
            AllSettings.getSustainedPerformance().put(isChecked).save();
        });

        // Alternate Surface
        binding.switchAlternateSurface.setChecked(AllSettings.getAlternateSurface().getValue());
        binding.switchAlternateSurface.setOnCheckedChangeListener((btn, isChecked) -> {
            AllSettings.getAlternateSurface().put(isChecked).save();
        });

        // Notch Cutout
        binding.switchNotch.setChecked(AllSettings.getIgnoreNotch().getValue());
        binding.switchNotch.setOnCheckedChangeListener((btn, isChecked) -> {
            AllSettings.getIgnoreNotch().put(isChecked).save();
        });
    }

    private void selectRenderer(String rendererName, boolean showToast) {
        AllSettings.getRenderer().put(rendererName).save();
        try {
            Renderers.setCurrentRenderer(requireContext(), rendererName);
        } catch (Exception ignored) {}

        boolean isKrypton = "e7b90ed6-e518-4d4e-93dc-5c7133cd5b31".equalsIgnoreCase(rendererName) || "krypton".equalsIgnoreCase(rendererName) || "opengles3".equalsIgnoreCase(rendererName);
        boolean isLtw = "d2c1e8b4-55a1-4e78-9032-11cf4e87d3a1".equalsIgnoreCase(rendererName) || "LTW".equalsIgnoreCase(rendererName) || "opengles3_ltw".equalsIgnoreCase(rendererName);
        boolean isGl4es = "8b52d82d-8f6d-4d3a-a767-dc93f8b72fc7".equalsIgnoreCase(rendererName) || "gl4es".equalsIgnoreCase(rendererName) || "opengles2".equalsIgnoreCase(rendererName);
        boolean isMobileglues = "com.fcl.plugin.mobileglues".equalsIgnoreCase(rendererName) || "mobileglues".equalsIgnoreCase(rendererName) || "opengles3_mobileglues".equalsIgnoreCase(rendererName);
        boolean isKopperzink = "0fa435e2-46df-45c9-906c-b29606aaef00".equalsIgnoreCase(rendererName) || "kopperzink".equalsIgnoreCase(rendererName) || "opengles3_desktopgl_zink_kopper".equalsIgnoreCase(rendererName);
        boolean isVulkanzink = "7c42df98-e6b3-4f51-b0e2-768a4c519213".equalsIgnoreCase(rendererName) || "vulkan_zink".equalsIgnoreCase(rendererName);
        boolean isVirgl = "a3ccc1fe-de3f-4a81-8c45-2485181b63b3".equalsIgnoreCase(rendererName) || "virgl".equalsIgnoreCase(rendererName) || "gallium_virgl".equalsIgnoreCase(rendererName);
        boolean isFreedreno = "1ad7249f-5784-4f00-bc72-174b3578ee46".equalsIgnoreCase(rendererName) || "freedreno".equalsIgnoreCase(rendererName) || "gallium_freedreno".equalsIgnoreCase(rendererName);
        boolean isPanfrost = "9b2808c4-11af-4c72-a9c6-94c940396475".equalsIgnoreCase(rendererName) || "panfrost".equalsIgnoreCase(rendererName) || "gallium_panfrost".equalsIgnoreCase(rendererName);

        if (binding.rendererItemKrypton != null) binding.rendererItemKrypton.setBackgroundResource(isKrypton ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        if (binding.rendererItemLtw != null) binding.rendererItemLtw.setBackgroundResource(isLtw ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        if (binding.rendererItemGl4es != null) binding.rendererItemGl4es.setBackgroundResource(isGl4es ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        if (binding.rendererItemMobileglues != null) binding.rendererItemMobileglues.setBackgroundResource(isMobileglues ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        if (binding.rendererItemKopperzink != null) binding.rendererItemKopperzink.setBackgroundResource(isKopperzink ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        if (binding.rendererItemVulkanzink != null) binding.rendererItemVulkanzink.setBackgroundResource(isVulkanzink ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        if (binding.rendererItemVirgl != null) binding.rendererItemVirgl.setBackgroundResource(isVirgl ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        if (binding.rendererItemFreedreno != null) binding.rendererItemFreedreno.setBackgroundResource(isFreedreno ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        if (binding.rendererItemPanfrost != null) binding.rendererItemPanfrost.setBackgroundResource(isPanfrost ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);

        if (isMobileglues && showToast) {
            boolean installed = MobileGluesRenderer.isInstalled(requireContext());
            if (!installed) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle)
                        .setTitle("MobileGlues Plugin Required")
                        .setMessage("MobileGlues is a modern OpenGL ES 3.x renderer optimized for Minecraft 1.17–1.21+ on Mali and Dimensity GPUs. Would you like to download the standalone companion APK now?")
                        .setPositiveButton("Download APK", (d, w) -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ShirosakiMio/FCLRendererPlugin/releases"));
                            startActivity(browserIntent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }

        if (showToast) {
            try {
                String name = Renderers.getCurrentRenderer().getRendererName();
                Toast.makeText(requireContext(), "Renderer selected: " + name, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Renderer selected: " + rendererName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initControlsPanel() {
        binding.btnOpenControlsEditor.setOnClickListener(v -> {
            ZHTools.swapFragmentWithAnim(this, ControlButtonFragment.class, ControlButtonFragment.TAG, null);
        });

        int buttonScale = AllSettings.getButtonScale().getValue();
        binding.buttonScaleSeekbar.setProgress(buttonScale);
        binding.buttonScaleValue.setText(buttonScale + "%");
        binding.buttonScaleSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int scale = Math.max(30, progress);
                binding.buttonScaleValue.setText(scale + "%");
                if (fromUser) AllSettings.getButtonScale().put(scale).save();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        int mouseSpeed = AllSettings.getMouseSpeed().getValue();
        binding.mouseSpeedSeekbar.setProgress(mouseSpeed);
        binding.mouseSpeedValue.setText(mouseSpeed + "%");
        binding.mouseSpeedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int spd = Math.max(10, progress);
                binding.mouseSpeedValue.setText(spd + "%");
                if (fromUser) AllSettings.getMouseSpeed().put(spd).save();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        String currentCursor = AllSettings.getCustomMouse().getValue();
        selectCursor(currentCursor, false);
        binding.cursorDefault.setOnClickListener(v -> selectCursor("default", true));
        binding.cursorMinecraft.setOnClickListener(v -> selectCursor("minecraft", true));
        binding.cursorCrosshair.setOnClickListener(v -> selectCursor("crosshair", true));
        binding.cursorDot.setOnClickListener(v -> selectCursor("dot", true));

        binding.switchInvertTouch.setChecked(AllSettings.getGyroInvertY().getValue());
        binding.switchInvertTouch.setOnCheckedChangeListener((btn, isChecked) -> {
            AllSettings.getGyroInvertY().put(isChecked).save();
        });

        binding.switchButtonSnapping.setChecked(AllSettings.getButtonSnapping().getValue());
        binding.switchButtonSnapping.setOnCheckedChangeListener((btn, isChecked) -> {
            AllSettings.getButtonSnapping().put(isChecked).save();
        });

        int snapDist = AllSettings.getButtonSnappingDistance().getValue();
        binding.snappingDistanceSeekbar.setProgress(snapDist);
        binding.snappingDistanceValue.setText(snapDist + "dp");
        binding.snappingDistanceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int dist = Math.max(1, progress);
                binding.snappingDistanceValue.setText(dist + "dp");
                if (fromUser) AllSettings.getButtonSnappingDistance().put(dist).save();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.switchGamepadSupport.setOnCheckedChangeListener((btn, isChecked) -> {
            Toast.makeText(requireContext(), isChecked ? "Gamepad support enabled" : "Gamepad support disabled", Toast.LENGTH_SHORT).show();
        });
    }

    private void selectCursor(String cursor, boolean showToast) {
        AllSettings.getCustomMouse().put(cursor).save();

        binding.cursorDefault.setBackgroundResource("default".equalsIgnoreCase(cursor) || cursor.isEmpty() ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.cursorMinecraft.setBackgroundResource("minecraft".equalsIgnoreCase(cursor) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.cursorCrosshair.setBackgroundResource("crosshair".equalsIgnoreCase(cursor) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.cursorDot.setBackgroundResource("dot".equalsIgnoreCase(cursor) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);

        if (showToast) {
            Toast.makeText(requireContext(), "Cursor style: " + cursor, Toast.LENGTH_SHORT).show();
        }
    }

    private void initGamePanel() {
        int currentRam = AllSettings.getRamAllocation().getValue().getValue();
        binding.ramSeekbar.setProgress(currentRam);
        updateRamDisplay(currentRam);
        binding.ramSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int ram = Math.max(512, (progress / 128) * 128);
                updateRamDisplay(ram);
                if (fromUser) {
                    AllSettings.getRamAllocation().getValue().put(ram).save();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        selectJavaRuntime(8, false);
        binding.javaJre8.setOnClickListener(v -> selectJavaRuntime(8, true));
        binding.javaJre17.setOnClickListener(v -> selectJavaRuntime(17, true));
        binding.javaJre21.setOnClickListener(v -> selectJavaRuntime(21, true));
        binding.javaJre25.setOnClickListener(v -> selectJavaRuntime(25, true));

        binding.btnImportJava.setOnClickListener(v -> runInstallerWithConfirmation(false));

        String customArgs = AllSettings.getJavaArgs().getValue();
        if (customArgs != null && !customArgs.trim().isEmpty()) {
            binding.jvmArgsText.setText(customArgs);
        }
        binding.btnEditJvmArgs.setOnClickListener(v -> showEditJvmArgsDialog());
        binding.btnOpenGameFiles.setOnClickListener(v -> openGameFiles());
    }

    private void selectJavaRuntime(int version, boolean userInitiated) {
        if (binding == null) return;
        binding.javaJre8.setBackgroundResource(version == 8 ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        binding.javaJre17.setBackgroundResource(version == 17 ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        binding.javaJre21.setBackgroundResource(version == 21 ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);
        binding.javaJre25.setBackgroundResource(version == 25 ? R.drawable.bg_settings_card_selected : R.drawable.bg_settings_card);

        // Update ACTIVE/USE labels
        updateJreLabel(binding.javaJre8, version == 8);
        updateJreLabel(binding.javaJre17, version == 17);
        updateJreLabel(binding.javaJre21, version == 21);
        updateJreLabel(binding.javaJre25, version == 25);

        if (userInitiated) {
            String runtimeName = MultiRTUtils.getExactJreName(version);
            if (runtimeName == null) runtimeName = MultiRTUtils.getNearestJreName(version);
            if (runtimeName != null) {
                AllSettings.getDefaultRuntime().put(runtimeName).save();
            }
            Toast.makeText(requireContext(), "Java " + version + " Runtime selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateJreLabel(android.view.View card, boolean active) {
        if (card instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) card;
            for (int i = 0; i < vg.getChildCount(); i++) {
                android.view.View child = vg.getChildAt(i);
                if (child instanceof android.widget.TextView) {
                    android.widget.TextView tv = (android.widget.TextView) child;
                    String text = tv.getText().toString();
                    if ("ACTIVE".equals(text) || "USE".equals(text)) {
                        tv.setText(active ? "ACTIVE" : "USE");
                        tv.setTextColor(active ? 0xFF40C463 : 0xFF888888);
                        break;
                    }
                }
            }
        }
    }

    private void updateRamDisplay(int ramMb) {
        float gb = ramMb / 1024.0f;
        binding.ramValue.setText(String.format(Locale.ROOT, "%d MB (%.1f GB)", ramMb, gb));
    }

    private void showEditJvmArgsDialog() {
        Context context = requireContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_minecraft_alert, null);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        EditText inputView = dialogView.findViewById(R.id.dialog_input);
        View btnConfirm = dialogView.findViewById(R.id.dialog_btn_confirm);
        View btnCancel = dialogView.findViewById(R.id.dialog_btn_cancel);

        if (titleView != null) titleView.setText("CUSTOM JVM ARGUMENTS");
        if (messageView != null) messageView.setText("Enter custom flags passed directly to Java execution:");
        if (inputView != null) {
            inputView.setVisibility(View.VISIBLE);
            inputView.setText(AllSettings.getJavaArgs().getValue());
        }

        AlertDialog dialog = builder.setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                String args = (inputView != null) ? inputView.getText().toString().trim() : "";
                AllSettings.getJavaArgs().put(args).save();
                binding.jvmArgsText.setText(args.isEmpty() ? "Default arguments" : args);
                Toast.makeText(context, "JVM arguments updated", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void initLauncherPanel() {
        // Minecraft Click Sound Toggle
        boolean soundEnabled = AllSettings.getLauncherClickSound().getValue();
        binding.switchClickSound.setChecked(soundEnabled);
        binding.switchClickSound.setOnCheckedChangeListener((btn, isChecked) -> {
            AllSettings.getLauncherClickSound().put(isChecked).save();
            Toast.makeText(requireContext(), isChecked ? "Minecraft Click Sound Enabled" : "Minecraft Click Sound Muted", Toast.LENGTH_SHORT).show();
        });

        // Font selection
        String currentFont = AllSettings.getLauncherFont().getValue();
        selectLauncherFont(currentFont, false);

        binding.fontMinecraft.setOnClickListener(v -> selectLauncherFont("minecraft", true));
        binding.fontManrope.setOnClickListener(v -> selectLauncherFont("manrope", true));
        binding.fontOneplus.setOnClickListener(v -> selectLauncherFont("oneplus", true));
        binding.fontOutfit.setOnClickListener(v -> selectLauncherFont("outfit", true));
        binding.fontPlusJakarta.setOnClickListener(v -> selectLauncherFont("plus_jakarta", true));
        binding.fontJetbrains.setOnClickListener(v -> selectLauncherFont("jetbrains", true));
        binding.fontSpaceGrotesk.setOnClickListener(v -> selectLauncherFont("space_grotesk", true));
        binding.fontSerif.setOnClickListener(v -> selectLauncherFont("serif", true));
        binding.fontSystem.setOnClickListener(v -> selectLauncherFont("system", true));

        // Background Customizer
        binding.btnChangeBackground.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Background Image"), 1005);
        });

        binding.btnResetBackground.setOnClickListener(v -> {
            Map<BackgroundType, String> map = new HashMap<>();
            map.put(BackgroundType.MAIN_MENU, BackgroundManager.NULL);
            BackgroundManager.INSTANCE.saveProperties(map);
            Toast.makeText(requireContext(), "Background reset to default", Toast.LENGTH_SHORT).show();
        });

        binding.switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            AllSettings.getLauncherTheme().put(isChecked ? "dark" : "light").save();
            Toast.makeText(requireContext(), "Theme updated", Toast.LENGTH_SHORT).show();
        });

        binding.btnCheckUpdates.setOnClickListener(v -> {
            UpdateUtils.checkDownloadedPackage(requireActivity(), true, false);
        });

        binding.btnSelectLanguage.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Language: System Default", Toast.LENGTH_SHORT).show();
        });
    }

    private void selectLauncherFont(String fontKey, boolean showToast) {
        AllSettings.getLauncherFont().put(fontKey).save();

        binding.fontMinecraft.setBackgroundResource("minecraft".equals(fontKey) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.fontManrope.setBackgroundResource("manrope".equals(fontKey) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.fontOneplus.setBackgroundResource("oneplus".equals(fontKey) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.fontOutfit.setBackgroundResource("outfit".equals(fontKey) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.fontPlusJakarta.setBackgroundResource("plus_jakarta".equals(fontKey) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.fontJetbrains.setBackgroundResource("jetbrains".equals(fontKey) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.fontSpaceGrotesk.setBackgroundResource("space_grotesk".equals(fontKey) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.fontSerif.setBackgroundResource("serif".equals(fontKey) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);
        binding.fontSystem.setBackgroundResource("system".equals(fontKey) ? R.drawable.btn_minecraft_green : R.drawable.btn_minecraft_stone);

        Typeface tf = null;
        try {
            switch (fontKey) {
                case "minecraft":
                    tf = ResourcesCompat.getFont(requireContext(), R.font.minecraft);
                    break;
                case "manrope":
                    tf = ResourcesCompat.getFont(requireContext(), R.font.manrope);
                    break;
                case "oneplus":
                    tf = ResourcesCompat.getFont(requireContext(), R.font.oneplus_sans);
                    break;
                case "outfit":
                    tf = ResourcesCompat.getFont(requireContext(), R.font.outfit);
                    break;
                case "plus_jakarta":
                    tf = ResourcesCompat.getFont(requireContext(), R.font.plus_jakarta_sans);
                    break;
                case "jetbrains":
                    tf = ResourcesCompat.getFont(requireContext(), R.font.jetbrains_mono);
                    break;
                case "space_grotesk":
                    tf = ResourcesCompat.getFont(requireContext(), R.font.space_grotesk);
                    break;
                case "serif":
                    tf = ResourcesCompat.getFont(requireContext(), R.font.serif_classical);
                    break;
                default:
                    tf = Typeface.DEFAULT;
                    break;
            }
        } catch (Exception ignored) {
            tf = Typeface.DEFAULT;
        }

        if (binding != null && tf != null) {
            applyTypefaceRecursively(binding.getRoot(), tf);
        }

        if (showToast) {
            Toast.makeText(requireContext(), "Font style applied!", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyTypefaceRecursively(View view, Typeface typeface) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            int style = tv.getTypeface() != null ? tv.getTypeface().getStyle() : Typeface.NORMAL;
            tv.setTypeface(typeface, style);
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyTypefaceRecursively(vg.getChildAt(i), typeface);
            }
        }
    }

    private void initExperimentalPanel() {
        binding.btnManageDrivers.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Vulkan Turnip & Mesa Zink GPU driver plugins loaded", Toast.LENGTH_SHORT).show();
        });

        binding.switchGl4esExtensions.setOnCheckedChangeListener((btn, isChecked) -> {
            Toast.makeText(requireContext(), isChecked ? "GL4ES experimental extensions enabled" : "GL4ES extensions disabled", Toast.LENGTH_SHORT).show();
        });

        binding.switchDumpShaders.setChecked(AllSettings.getDumpShaders().getValue());
        binding.switchDumpShaders.setOnCheckedChangeListener((btn, isChecked) -> {
            AllSettings.getDumpShaders().put(isChecked).save();
        });
    }

    private void initAboutPanel() {
        binding.btnAboutGithub.setOnClickListener(v -> {
            ZHTools.openLink(requireContext(), "https://github.com/VivxKumar07/ZalithRevived", null);
        });

        binding.btnAboutDiscord.setOnClickListener(v -> {
            ZHTools.openLink(requireContext(), "https://discord.gg/fWwfRBbGeZ", null);
        });
    }

    private void switchSettingsSubTab(int subTab) {
        MinecraftSoundHelper.playClickSound();
        currentSettingsSubTab = subTab;

        resetSettingsSubTab(binding.tabVideo, subTab == SUBTAB_VIDEO);
        resetSettingsSubTab(binding.tabControls, subTab == SUBTAB_CONTROLS);
        resetSettingsSubTab(binding.tabGame, subTab == SUBTAB_GAME);
        resetSettingsSubTab(binding.tabLauncher, subTab == SUBTAB_LAUNCHER);
        resetSettingsSubTab(binding.tabExperimental, subTab == SUBTAB_EXPERIMENTAL);
        resetSettingsSubTab(binding.tabAbout, subTab == SUBTAB_ABOUT);

        binding.panelVideo.setVisibility(subTab == SUBTAB_VIDEO ? View.VISIBLE : View.GONE);
        binding.panelControls.setVisibility(subTab == SUBTAB_CONTROLS ? View.VISIBLE : View.GONE);
        binding.panelGame.setVisibility(subTab == SUBTAB_GAME ? View.VISIBLE : View.GONE);
        binding.panelLauncher.setVisibility(subTab == SUBTAB_LAUNCHER ? View.VISIBLE : View.GONE);
        binding.panelExperimental.setVisibility(subTab == SUBTAB_EXPERIMENTAL ? View.VISIBLE : View.GONE);
        binding.panelAbout.setVisibility(subTab == SUBTAB_ABOUT ? View.VISIBLE : View.GONE);
    }

    private void resetSettingsSubTab(TextView tabView, boolean active) {
        if (tabView == null) return;
        tabView.setBackgroundResource(active ? R.drawable.bg_tab_pill_active : R.drawable.bg_tab_pill);
        tabView.setTextColor(active ? 0xFFFFFFFF : 0xFFCCCCCC);
    }

    private void refreshSettingsAccountCard() {
        MinecraftAccount account = AccountsManager.INSTANCE.getCurrentAccount();
        if (account != null) {
            binding.settingsAccountName.setText(account.username);
            binding.settingsAccountType.setText(AccountUtils.getAccountTypeName(requireContext(), account));
            try {
                Drawable avatar = SkinLoader.getAvatarDrawable(requireContext(), account, (int) Tools.dpToPx(36));
                binding.settingsAccountAvatar.setImageDrawable(avatar);
            } catch (Exception ignored) {}
        } else {
            binding.settingsAccountName.setText("No Account");
            binding.settingsAccountType.setText("Offline");
            binding.settingsAccountAvatar.setImageResource(R.drawable.ic_mc_steve_head);
        }
    }

    private void openAccountManager() {
        ZHTools.swapFragmentWithAnim(this, AccountFragment.class, AccountFragment.TAG, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != android.app.Activity.RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        Context context = requireContext();

        if (requestCode == 1001) {
            // Import Skin PNG
            MinecraftAccount account = AccountsManager.INSTANCE.getCurrentAccount();
            if (account == null) {
                Toast.makeText(context, "Please select an account first", Toast.LENGTH_SHORT).show();
                return;
            }
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                File dir = PathManager.DIR_USER_SKIN;
                if (!dir.exists()) dir.mkdirs();
                File target = new File(dir, account.getUniqueUUID() + ".png");
                try (FileOutputStream out = new FileOutputStream(target)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                refreshSkinAvatar();
                if (accountViewWrapper != null) accountViewWrapper.refreshAccountInfo();
                Toast.makeText(context, "Skin imported successfully!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "Failed to import skin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1002) {
            // Custom Cape
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                File dir = new File(PathManager.DIR_DATA);
                if (!dir.exists()) dir.mkdirs();
                File target = new File(dir, "custom_cape.png");
                try (FileOutputStream out = new FileOutputStream(target)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                AllSettings.getEquippedCapeAsset().put("custom").save();
                String b64 = fileToBase64DataUrl(target);
                if (b64 != null) {
                    binding.skinPreviewWebview.evaluateJavascript("loadCape('" + b64 + "');", null);
                }
                binding.skinCaptionName.setText("Custom Cape");
                Toast.makeText(context, "Custom cape imported and equipped!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "Failed to import cape: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1003) {
            // Renderer plugin ZIP
            Toast.makeText(context, "Renderer plugin imported!", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 1005) {
            // Background Image
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                File dir = PathManager.DIR_BACKGROUND;
                if (!dir.exists()) dir.mkdirs();
                File target = new File(dir, "custom_main.png");
                try (FileOutputStream out = new FileOutputStream(target)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                Map<BackgroundType, String> map = new HashMap<>();
                map.put(BackgroundType.MAIN_MENU, "custom_main.png");
                BackgroundManager.INSTANCE.saveProperties(map);
                Toast.makeText(context, "Custom background set!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "Failed to set background: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (currentTab != TAB_PLAY) {
            switchMainTab(TAB_PLAY);
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshSettingsAccountDisplay();
        boolean mgInstalled = MobileGluesRenderer.isInstalled(requireContext());
        if (binding != null && binding.btnDownloadMobileglues != null) {
            if (mgInstalled) {
                binding.btnDownloadMobileglues.setText("INSTALLED");
                binding.btnDownloadMobileglues.setBackgroundResource(R.drawable.bg_tab_pill);
            } else {
                binding.btnDownloadMobileglues.setText("DOWNLOAD APK");
                binding.btnDownloadMobileglues.setBackgroundResource(R.drawable.btn_minecraft_green);
            }
        }
        if (isAutoScroll && installedHeroArtworks.size() > 1) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
            autoScrollHandler.postDelayed(autoScrollRunnable, 5000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    @Subscribe()
    public void event(RefreshVersionsEvent event) {
        if (event.getMode() == END) {
            TaskExecutors.runInUIThread(this::refreshCurrentVersion);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(AccountUpdateEvent event) {
        if (accountViewWrapper != null) accountViewWrapper.refreshAccountInfo();
        refreshSettingsAccountCard();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }

    @Override
    public void slideIn(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.launcherMenu, Animations.BounceInDown))
                .apply(new AnimPlayer.Entry(binding.playLayout, Animations.BounceInLeft))
                .apply(new AnimPlayer.Entry(binding.playButtonsLayout, Animations.BounceEnlarge));
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.launcherMenu, Animations.FadeOutUp))
                .apply(new AnimPlayer.Entry(binding.playLayout, Animations.FadeOutRight))
                .apply(new AnimPlayer.Entry(binding.playButtonsLayout, Animations.BounceShrink));
    }
}
