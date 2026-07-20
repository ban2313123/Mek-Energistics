# 6. `pushPattern` 编写规则

`pushPattern` 是 AE 下单把原料推入机器的核心。新增机器时最容易出资源复制或吞资源的问题，必须按下面顺序写：

1. 检查 AE 节点 active。
2. 检查 `patternDetails` 属于 `getAvailablePatterns()`。
3. 检查 `inputHolder` 数量和机器输入数一致。
4. 只声明物理输入端口，并把 `KeyCounter[]` 交给 `MePatternInputRouter` 或 support。
5. 严格区分 item、chemical、fluid；位置敏感输入使用 lane adapter。
6. 所有输入都先模拟，全部成功后再执行。
7. 任何一个输入不能完整插入时整体回滚并返回 `false`。
8. 成功执行后由 support 触发 `saveChanges()`。

已有工具：

- `MeMachineIoAdapter.autoSortedFactoryItemInput(...)`: 将 Mekanism 自动均分的主输入槽视为一个事务端口，汇总容量并整组回滚；实际均分继续由 Mekanism 按配方最小输入量完成。
- `MePatternInputRouter.route(...)`: 普通端口的统一模拟、执行和回滚。
- `MePatternInputRouter.routeLanes(...)`: 多 lane 输入的统一事务分配与回滚。

AE key normalization 只存在于 `MePatternInputRouter` 内部。具体 BlockEntity 和 adapter 不得读取
`AEItemKey`、`AEFluidKey` 或 `MekanismKey` 来选择投料路径。

双输入机器要明确物理 lane。Chemical Infuser 这类左右 tank 可互换的机器，把两个 tank 作为同一候选端口集合交给
`route(...)`，由 router 回溯分配。Combiner 这类主槽/副槽语义不同的机器使用 `routeLanes(...)` 固定位置，不能交换。
