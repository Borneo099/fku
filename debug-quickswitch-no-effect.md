# Debug Session: quickswitch-no-effect
- **Status**: [OPEN]
- **Issue**: 秒切启用后仍无物品栏切换迹象，空手攻击无法继承武器魔咒与特性，普通攻击与 Aura 链路均未出现预期效果。
- **Debug Server**: http://127.0.0.1:7777/event
- **Log File**: .dbg/trae-debug-log-quickswitch-no-effect.ndjson

## Reproduction Steps
1. 启用 QuickSwitch，模式设为 `SILENT`。
2. 主手保持空手或非目标武器，热栏放带火焰附加/长矛/锤子的目标武器。
3. 使用原版普攻、TpAura、或其它 Aura 发起攻击。
4. 观察是否出现热栏切换、火焰附加、长矛 5 格手长、锤子满蓄力等效果。

## Hypotheses & Verification
| ID | Hypothesis | Likelihood | Effort | Evidence |
|----|------------|------------|--------|----------|
| A | 攻击包没有被 QuickSwitch 正确识别为 `onAttack()`，所以根本没有进入重写链路 | High | Low | Pending |
| B | 攻击包被识别了，但 Netty 重写序列没有真正发出 `SetCarriedItemPacket`，或顺序不是 `切槽 -> 攻击 -> 恢复` | High | Med | Pending |
| C | 物品栏切换只改了客户端 `selected`，服务端攻击结算读取的主手槽位仍未变化 | High | Med | Pending |
| D | 秒切原理需要依赖更精确的“上一把武器/下一把武器”窗口，本实现只做了普通热栏切槽，未覆盖属性与魔咒继承时机 | Med | Med | Pending |
| E | 某些攻击来源没有走 `ServerboundInteractPacket attack` 这条链，导致 QuickSwitch 对部分普攻/Aura 不生效 | Med | Low | Pending |

## Log Evidence
- Pending

## Verification Conclusion
- Pending
