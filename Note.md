#IDEA插件开发
 <https://blog.csdn.net/csdn_xpw/article/details/78946781>  
	例子：<https://blog.csdn.net/chengtt_love/article/details/53745025>    
		  <https://blog.csdn.net/O4dC8OjO7ZL6/article/details/79722289>  
注：破解版的IDEA，必须是使用注册码或者Liscense Server激活的，否则无法对插件进行运行和调试  
本次使用的激活码：https://blog.csdn.net/jilky123/article/details/80522125


**问题：**  
* a. Intellij导入插件工程，不能运行。可能是需要更改*.iml，\<module type="PLUGIN_MODULE" version="4"\>
https://blog.csdn.net/wjskeepmaking/article/details/78815896
* b. lombok编译时，报找不到符号。  
https://blog.csdn.net/sunrainamazing/article/details/80763743

* c. 运行和调试  
运行：Run -> Run xxx  （xxx为插件名称）  
调试：Run -> Debug xxx

* d. 插件打包  
Build -> Prepare Plugin Module xxx For Deployment