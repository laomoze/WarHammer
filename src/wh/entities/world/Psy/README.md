# 灵能系统结构

- `PsychicModule`：灵能数值的储存、容量限制与读写。
- `PsychicNetworkNode`：网络连接、压力与能量传输协议。
- `PsychicStorage`：带灵能储存的建筑共用接口。
- `PsychicConsumer`：带过载状态的灵能消费建筑共用逻辑。
- `PsychicBlock`：大多数灵能建筑的基础类，包含配方消费和过载状态。
- `PsychicNode`：节点连接状态与能量调度；`PsychicRouterBlock` 和 `PsychicSplitGateBlock` 定义各自的方向和分流策略。
- `PsychicLinkScanner`：放置预览和已建节点共用的方向扫描。
- `PsychicBeamEffects`：节点光束上的流动粒子绘制。
- 其余方块类负责各自的生产、存储、修复或特殊效果；`sandbox` 为沙盒方块。
- `unused` 包含旧灵能场实现，其中的 `PsychicFieldState` 仍被实体注册引用，不能直接删除。

各建筑的 `version()`、`read()` 和 `write()` 还负责兼容旧存档，调整序列化格式时需要保留历史版本读取逻辑。
