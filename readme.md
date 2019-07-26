#### Camera2 相关参数配置

ISO、快门（曝光时间）、摄像头切换、闪光灯控制、

连拍（左下角输入框输入连拍次数，点击拍照按钮后可进行连拍），因为保存了YUV格式数据所以会有卡顿

![image](https://github.com/xinchen281158/Camera2SampleDemo/blob/master/Screenshot.png)
<img width="150" height="150" src="https://github.com/xinchen281158/Camera2SampleDemo/blob/master/Screenshot.png"/>


#### Camera2Proxy : 为控制 Camera2 相关参数的工具类

##### SENSOR_INFO_SENSITIVITY_RANGE ：获取 Camera2 ISO数值范围，根据手机的不同获得的数值范围也不同，不同的手机也有可能获取不到这个值，所以需根据机型自己适配

##### SENSOR_INFO_EXPOSURE_TIME_RANGE：获取 Camera2 曝光时间范围，根据手机的不同获取的数值范围不同，不同的手机也有可能获取不到这个值，所以需根据机型适配
