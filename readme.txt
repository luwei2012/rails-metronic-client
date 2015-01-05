AndroidAsync、ion、pull_library都是lib工程
都需要导入eclipse


aWashCar需要重新配置百度的key，百度key是根据MD5生成的，由于每个人的机器都会生产自己的debug.keystore 所以在开发时需要将AndroidManifest.xml中
	   <!-- 测试版API_KEY -->
 
             <meta-data
            android:name="com.baidu.lbsapi.API_KEY"
            android:value="U5hws2HHtaXIjrUSpGQV1djq" />

这个key替换掉，具体替换成什么需要自己在百度申请开发者账号注册APP生成。

但是正式版本key是根据导出时的证书MD5产生的key，所以到处前需要注释 测试版API_KEY ，取消 正式版API_KEY的注释，并替换正式版API_KEY