# 29. 当前 guide 维护规则

每次完成新机器或机器族，至少同步：

- 24：新增物理输入/输出组合；
- 27：代表 BlockEntity 和 I/O layout；
- 33：catalog/family/provider 目录规则；
- 23：提交或 PR 的适配记录。

新增 optional compat 时同步 14、20、21、22；修改资源策略时同步 13；修改 factory tier 时同步 12、16。

文档只记录稳定边界和操作规则，不复制大段源码 switch。任何章节出现新的公共逐机注册分支、具体 `pushPattern`、runtime output 登记或手写 tier JSON，都应先确认是否违反当前 catalog/provider/support/datagen 架构。
