### English

#### Fix

- Fixed highly parallel ME machines and factories reporting insufficient energy after an autocrafting order even when the AE network contained enough FE. Network-backed recipe checks now see the full available local and network energy instead of being capped by the machine's local energy buffer.

### 中文

#### 修复

- 修复高并行 ME 机器与工厂在自动合成下单后，即使 AE 网络中有足够 FE 仍显示能量不足的问题。网络供能配方检查现在会读取本地与网络中的完整可用能量，不再受机器本地能量缓存上限限制。
