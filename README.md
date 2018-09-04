QQ交流群：611902811，有兴趣的可以交流
# IJKPlayer-android
> 优势： 对ijkplayer的抽取值删除了IjkVideoView.java类中几个无关紧要的变量和方法，其他与ijkplayer本身一摸一样。这样ijkplayer升级后，直接拷贝过来，无需任何改动。
  对ijkplayer中的EXOplayer模块一直处于r1.x.x版本升级到r.2.x.x版本。同时ijkplayer-exo2中的IjkExoMediaPlayer.java我增加了RTMP的支持，播放速率，其他的暂时还没有，如果还想增加，请对照EXOplayer进行增加即可。此外还对全局悬浮窗、页面悬浮窗进行了实现。具体实现方式参考
## 效果图
![image](https://raw.githubusercontent.com/DyncKathline/IJKPlayer-android/master/screenshot/GIF.gif)
![image](https://raw.githubusercontent.com/DyncKathline/IJKPlayer-android/master/screenshot/GIF1.gif)
### 2018/09/04
增加了字幕显示，支持ass、srt、stl的格式
> 1.由于没有对应的视频，所以字幕仅仅只是解析完后根据时间显示出来
如果点击下载不了可以从以下找到它
> 2.字幕文件需要是utf-8格式的，不然会解析不出来，可以使用NotePad++进行格式转换一下
## [Demo下载](https://raw.githubusercontent.com/DyncKathline/IJKPlayer-android/master/screenshot/app-debug.apk)
![image](https://raw.githubusercontent.com/DyncKathline/Blog/master/android/%E6%90%9C%E7%8B%97%E6%88%AA%E5%9B%BE20171013113817.png)  

首先打开[/Bilibili/ijkplayer](https://github.com/Bilibili/ijkplayer)，利用git命令
```
git clone https://github.com/Bilibili/ijkplayer.git
```
然后在你现有的项目里新建一个module，如图所示：  
![image](https://raw.githubusercontent.com/DyncKathline/Blog/master/android/%E6%90%9C%E7%8B%97%E6%88%AA%E5%9B%BE20170920160946.png)  
然后从Bilibili/ijkplayer的项目中拷贝出我们需要的文件，如图所示：  
![image](https://raw.githubusercontent.com/DyncKathline/Blog/master/android/%E6%90%9C%E7%8B%97%E6%88%AA%E5%9B%BE20170920160642.png)
![image](https://raw.githubusercontent.com/DyncKathline/Blog/master/android/%E6%90%9C%E7%8B%97%E6%88%AA%E5%9B%BE20170920160745.png)    
不要忘了在module的build.gradle中依赖所需的依赖  
```
compile 'tv.danmaku.ijk.media:ijkplayer-java:0.8.4'
compile 'tv.danmaku.ijk.media:ijkplayer-armv7a:0.8.4'
compile 'tv.danmaku.ijk.media:ijkplayer-exo:0.8.4'
```
如果想支持EXOplayer r2.x.x版本依赖
```
compile 'org.dync.kathline:ijkplayer-exo2:0.8.4'
//有冲突可以使用下面的去除重复
compile ('org.dync.kathline:ijkplayer-exo2:0.8.4'){
   exclude group: 'tv.danmaku.ijk.media',
          module: 'ijkplayer-java'
}
```
![image](https://raw.githubusercontent.com/DyncKathline/Blog/master/android/%E6%90%9C%E7%8B%97%E6%88%AA%E5%9B%BE20170920162026.png)  
做完之后，IjkVideoView.java文件会出错，但是我们只要删除这些报错的变量和方法就好了，做到这步之后，我们开始播放了。  
此外我们可以给IjkVideoView.java增加几个额外的方法。这里我提供我的：  
```
    ///////////////////////////////额外增加的方法//////////////////////////////////

    /**
     * 参考{@link IRenderView#AR_ASPECT_FIT_PARENT}、{@link IRenderView#AR_ASPECT_FILL_PARENT}、{@link IRenderView#AR_ASPECT_WRAP_CONTENT}
     * {@link IRenderView#AR_16_9_FIT_PARENT}、{@link IRenderView#AR_4_3_FIT_PARENT}
     * 设置播放区域拉伸类型
     */
    public void setAspectRatio(int aspectRatio) {
        for (int i = 0; i < s_allAspectRatio.length; i++) {
            if (s_allAspectRatio[i] == aspectRatio) {
                mCurrentAspectRatioIndex = i;
                if (mRenderView != null) {
                    mRenderView.setAspectRatio(mCurrentAspectRatio);
                }
                break;
            }
        }
    }

    /**
     * 设置旋转角度
     */
    public void setPlayerRotation(int rotation) {
        mVideoRotationDegree = rotation;
        if (mRenderView != null) {
            mRenderView.setVideoRotation(mVideoRotationDegree);
        }
    }

    /**
     * 设置播放速率，这里仅对支持IjkMediaPlayer播放器
     *
     * @param rate  0.2~2.0之间
     */
    public void setPlayRate(@FloatRange(from=0.2, to=2.0)float rate) {
        if(mMediaPlayer instanceof IjkMediaPlayer){
            ((IjkMediaPlayer)mMediaPlayer).setSpeed(rate);
        }else {
            Toast.makeText(getContext(), getResources().getString(R.string.TrackType_unknown), Toast.LENGTH_SHORT).show();
        }
    }
```
其中setPlayRate()方法需要在createPlayer()方法中设置如下代码：  
```
ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
```
这几句代码的含义是设置倍速后播放音调不会在android6.0以下变音  
![image](https://raw.githubusercontent.com/DyncKathline/Blog/master/android/%E6%90%9C%E7%8B%97%E6%88%AA%E5%9B%BE20170920162620.png)  
到了这一步差不多完成了，但是android4.4~android6.0之间没有沉浸式。这里用到了我的另一个项目[ChangeStatusColor-Android](https://github.com/DyncKathline/ChangeStatusColor-Android)了。[使用方法](http://blog.csdn.net/dynckathline/article/details/78026789)。  

大功告成了。\\(^o^)/~
如果不想麻烦可以clone我的[IJKPlayer-android](https://github.com/DyncKathline/IJKPlayer-android)项目下来，直接导入我的module。

