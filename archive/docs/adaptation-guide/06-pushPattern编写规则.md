# 6. 样板输入与 I/O adapter 规则

具体 BlockEntity 不再编写 `pushPattern`。`MeAeMachine`/`MeFactoryAeMachine` 默认方法负责活跃节点、样板归属、busy 状态和 smart-pattern 队列；support/router 负责实际投料。

新增机器只声明物理输入：

1. 普通可交换输入使用 `MeInputLayout.unordered(...)`；
2. 主槽/副槽、催化剂或模具等位置敏感输入使用 `MeInputLayout.lanes(...)`；
3. 工厂主物品槽使用 `MeMachineIoAdapter.autoSortedFactoryItemInput(...)`，保留 Mekanism 自动均分；
4. 输出使用稳定的 `MeOutputPort` 列表；
5. 不在 BlockEntity 或 adapter 中判断 Mekanism recipe 类型；
6. 不在具体机器中解析 `AEItemKey`、`AEFluidKey` 或 `MekanismKey`。

`MePatternInputRouter` 统一完成 key normalization、端口候选分配、容量模拟、执行和完整回滚。`route(...)` 处理普通端口，`routeLanes(...)` 处理位置敏感 lane；smart-pattern 容量计算复用同一套模拟。

非法 key、空 counter、负数、溢出数量或单 counter 含多个 key 必须被 router 拒绝。输入不足时返回 `false`，不得留下半份输入。
