package com.tomsky.hitv

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.tomsky.hitv.data.CheckType
import com.tomsky.hitv.data.TVBean
import com.tomsky.hitv.data.TVMenuAction
import com.tomsky.hitv.data.TVMenuItem
import com.tomsky.hitv.databinding.ActivityMainBinding
import com.tomsky.hitv.ui.TVMenuSelectListener
import com.tomsky.hitv.ui.TVSelectListener
import com.tomsky.hitv.ui.TVSwipeListener
import com.tomsky.hitv.util.AppEnv
import com.tomsky.hitv.util.SP
import com.tomsky.hitv.util.showToast


class MainActivity: FragmentActivity(), TVSelectListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TVViewModel

    private var loadingDialog:AlertDialog? = null

    private val aspectRatio = 16f / 9f

    companion object {
        private const val TAG = "itv-main"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.setAttributes(lp)
        }

        window.decorView.apply {
            systemUiVisibility =
                SYSTEM_UI_FLAG_FULLSCREEN or
                        SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility =
            SYSTEM_UI_FLAG_FULLSCREEN or
                    SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[TVViewModel::class.java]
        setContentView(binding.root)

        initView()
        initData()
    }

    private fun initView() {
        binding.liveMenu.initView(object :TVMenuSelectListener {
            override fun onSelect(tvBean: TVMenuItem) {
                when (tvBean.action) {
                    TVMenuAction.UPDATE_DATA.ordinal -> {
                        hideMenu()
                        showLoading()
                        viewModel.checkVersion(this@MainActivity)
                    }
                    TVMenuAction.CLOSE.ordinal -> {
                        hideMenu()
                        finish()
                    }
                    else -> {

                    }
                }
            }

        })
        binding.chanelControl.setOnClickListener {
            hideControlView()
        }

        binding.liveSetting.setOnClickListener {
            showMenu()
        }
        binding.playerView.run {
            isFocusable = true
            isFocusableInTouchMode = true

            setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when(keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> { // 向左导航
                            showPrevious()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            showNext()
                            true
                        }
                        // ok 按键
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            onClickCenter()
                            true
                        }

                        KeyEvent.KEYCODE_MENU -> {
                            showMenu()
                            true
                        }

                        else -> false
                    }
                }
                false
            }
            setSwipeListener(object : TVSwipeListener {
                override fun onSwipeLeft() {
                    showNext()
                }

                override fun onSwipeRight() {
                    showPrevious()
                }

                override fun onClick() {
                    onClickCenter()
                }

            })

            val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE) // 设置内容类型为音乐
                .setUsage(C.USAGE_MEDIA) // 设置用途为媒体播放
                .build()

            player =
                ExoPlayer.Builder(this@MainActivity)
//                        .setRenderersFactory(renderersFactory!!)
                    .setAudioAttributes(audioAttributes, true)
                    .build()

            player?.playWhenReady = true
            player?.addListener(object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
//                        val ratio = binding.playerView.measuredWidth?.div(binding.playerView.measuredHeight!!)
//                        if (ratio != null) {
//                            val layoutParams = binding.playerView.layoutParams
//                            if (ratio < aspectRatio) {
//                                layoutParams?.height =
//                                    (binding.playerView.measuredWidth?.div(aspectRatio))?.toInt()
//                                binding.playerView.layoutParams = layoutParams
//                            } else if (ratio > aspectRatio) {
//                                layoutParams?.width =
//                                    (binding.playerView.measuredHeight?.times(aspectRatio))?.toInt()
//                                binding.playerView.layoutParams = layoutParams
//                            }
//                        }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (AppEnv.DEBUG) {
                        when (playbackState) {
                            Player.STATE_IDLE -> Log.d(TAG, "Player is idle")
                            Player.STATE_BUFFERING -> Log.d(TAG, "Player is buffering")
                            Player.STATE_READY -> Log.d(TAG, "Player is ready")
                            Player.STATE_ENDED -> Log.d(TAG, "Player has ended playback")
                            else -> {}
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Log.e(TAG, "PlaybackException ${error.errorCode}, ${error.errorCodeName}")
                    val err = "播放错误"
                    showToast(this@MainActivity, "播放失败:${error.errorCodeName}")
//                        tvViewModel?.setErrInfo(err)
//                        tvViewModel?.changed("retry")
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (AppEnv.DEBUG) {
                        Log.e(TAG, "onIsPlayingChanged $isPlaying")
                    }
                    if (isPlaying) {
//                            tvViewModel?.setErrInfo("")
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    if (AppEnv.DEBUG) {
                        Log.e(TAG, "onMediaItemTransition,${mediaItem?.mediaMetadata} reason:$reason")
                    }
                }
            })
        }

//        binding.playerView.viewTreeObserver?.addOnGlobalLayoutListener(object :
//            ViewTreeObserver.OnGlobalLayoutListener {
//            @OptIn(UnstableApi::class)
//            override fun onGlobalLayout() {
//                binding.playerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
//
////                val renderersFactory = context?.let { DefaultRenderersFactory(it) }
////                renderersFactory?.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
//
//                binding.playerView.player =
//                    ExoPlayer.Builder(this@MainActivity)
////                        .setRenderersFactory(renderersFactory!!)
//                        .build()
//
//                binding.playerView.player?.playWhenReady = true
//                binding.playerView.player?.addListener(object : Player.Listener {
//                    override fun onVideoSizeChanged(videoSize: VideoSize) {
////                        val ratio = binding.playerView.measuredWidth?.div(binding.playerView.measuredHeight!!)
////                        if (ratio != null) {
////                            val layoutParams = binding.playerView.layoutParams
////                            if (ratio < aspectRatio) {
////                                layoutParams?.height =
////                                    (binding.playerView.measuredWidth?.div(aspectRatio))?.toInt()
////                                binding.playerView.layoutParams = layoutParams
////                            } else if (ratio > aspectRatio) {
////                                layoutParams?.width =
////                                    (binding.playerView.measuredHeight?.times(aspectRatio))?.toInt()
////                                binding.playerView.layoutParams = layoutParams
////                            }
////                        }
//                    }
//
//                    override fun onPlayerError(error: PlaybackException) {
//                        super.onPlayerError(error)
//                        Log.e(TAG, "PlaybackException $error")
//                        val err = "播放错误"
////                        tvViewModel?.setErrInfo(err)
////                        tvViewModel?.changed("retry")
//                    }
//
//                    override fun onIsPlayingChanged(isPlaying: Boolean) {
//                        super.onIsPlayingChanged(isPlaying)
//                        if (isPlaying) {
////                            tvViewModel?.setErrInfo("")
//                        }
//                    }
//                })
//            }
//        })
    }

    private fun onClickCenter() {
        if (binding.liveMenu.visibility == View.VISIBLE) {
            hideMenu()
        } else {
            showControlView()
        }
    }

    private fun initData() {
        SP.init(this)
        val data = viewModel.parseData(this)
        binding.chanelControl.update(data, this)
        if (data.isNotEmpty()) {
            val index = viewModel.getIndex()
            onSelect(index[0], index[1], data[index[0]].tvList[index[1]])
        }

        viewModel.checkResult.observe(this, Observer{
            hideLoading()
            when (it) {
                CheckType.NONE -> {
                    showToast(this@MainActivity, "已经是最新数据，不需要更新")
                }
                CheckType.SUCCESS -> {
                    showToast(this@MainActivity, "更新数据成功!")
                    val newData = viewModel.getData()
                    binding.chanelControl.update(newData, this)
                    if (data.isNotEmpty()) {
                        val index = viewModel.getIndex()
                        onSelect(index[0], index[1], newData[index[0]].tvList[index[1]])
                    }
                }
                CheckType.FAILED -> {
                    showToast(this@MainActivity, "更新数据失败!")
                }
                else -> {

                }
            }
        })
    }

    private fun showMenu() {
        binding.liveMenu.show(true)
        binding.liveMenu.requestFocus()
        binding.playerView.isFocusable = false
    }
    private fun hideMenu() {
        binding.liveMenu.show(false)
        binding.playerView.isFocusable = true
        binding.playerView.requestFocus()
    }

    private fun hideControlView() {
        binding.chanelControl.clearSelect()
        binding.channelContainer.visibility = View.INVISIBLE
        binding.playerView.isFocusable = true
        binding.playerView.requestFocus()
    }

    private fun showControlView() {
        binding.channelContainer.visibility = View.VISIBLE
        binding.channelContainer.requestFocus()
        binding.playerView.isFocusable = false
        val index = viewModel.getIndex()
        if (index[0] > -1) {
            binding.chanelControl.scrollToSelect(binding.channelContainer,index[0], index[1])
        }
    }

    override fun onSelect(cateIndex: Int, chanelIndex: Int, tvBean: TVBean) {
        viewModel.saveIndex(cateIndex, chanelIndex)
        hideControlView()
        binding.playerView.player?.run {
            setMediaItem(MediaItem.fromUri(tvBean.url!!))
            prepare()
            playWhenReady = true
            volume = viewModel.getVolume()
        }
    }

    private fun showPrevious() {
        val index = viewModel.getPrevious()
        Log.i(TAG, "pre:$index")
        if (index[0] > -1) {
            onSelect(index[0], index[1], viewModel.getTVBean(index[0], index[1]))
        }
    }

    private fun showNext() {
        val index = viewModel.getNext()
        Log.i(TAG, "next:$index")
        if (index[0] > -1) {
            onSelect(index[0], index[1], viewModel.getTVBean(index[0], index[1]))
        }
    }

    override fun onBackPressed() {
        if (binding.channelContainer.visibility == View.VISIBLE) {
            hideControlView()
            return
        }
        if (binding.liveMenu.visibility == View.VISIBLE) {
            hideMenu()
            return
        }
        super.onBackPressed()
    }

    override fun onStart() {
        Log.i(TAG, "onStart")
        super.onStart()
        if (binding.playerView.player?.isPlaying == false) {
            Log.i(TAG, "replay")
            binding.playerView.player?.prepare()
            binding.playerView.player?.play()
        }
    }

    private fun showLoading() {
        if (loadingDialog?.isShowing == true) return

        if (loadingDialog == null) {
            // 使用 LayoutInflater 加载一个自定义的布局
            val progressBar = ProgressBar(this)

            // 构建 AlertDialog
            val builder = AlertDialog.Builder(this)
                .setView(progressBar)  // 将 ProgressBar 作为 View 放入对话框
                .setCancelable(true)  // 禁止用户点击外部关闭对话框
            // 创建对话框实例
            loadingDialog = builder.create()
        }
        loadingDialog?.show()
    }
    private fun hideLoading() {
        if (loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (binding.playerView.player?.isPlaying == true) {
            binding.playerView.player?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.playerView.player?.release()
    }
}