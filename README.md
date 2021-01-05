# pull-lyric

## 简介
下载歌词的小工具，基于KuGou的API

## 快速开始
执行命令 java -jar pull-lyric.jar [音乐文件夹] [歌词文件夹]  
_例：java -jar pull-lyric.jar D:\Music D:\lyric_  
_或 java -jar pull-lyric.jar D:\Music_  

## 注意
- 若不填写[歌词文件夹]，则歌词默认存放在[音乐文件夹]
- 文件夹内的文件命名需要遵循 [歌手名] - [歌曲名].[文件类型后缀] 的格式  
  _例：Adele-Someone Like You.mp3 或 Adele - Someone Like You.mp3_
- 歌词保存位置为运行jar时输入的文件夹，歌词文件命名规则为 [音乐文件名].lrc
